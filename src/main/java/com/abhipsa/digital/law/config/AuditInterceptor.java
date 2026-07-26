package com.abhipsa.digital.law.config;

import com.abhipsa.digital.law.entity.AuditLog;
import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.Client;
import com.abhipsa.digital.law.entity.Court;
import com.abhipsa.digital.law.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Hibernate-level interceptor: every entity save/update/delete that flows
 * through the session factory gets recorded here, regardless of which
 * service/controller triggered it. Hibernate instantiates this class itself
 * via the "hibernate.session_factory.interceptor" property (see
 * application.properties), so it can't take Spring constructor injection —
 * {@link AuditInterceptorWiring} pushes the DataSource into the static field
 * once the Spring context is up.
 *
 * Entries are buffered per-thread during the flush, then written (via a
 * plain JDBC connection, bypassing the ORM session entirely to avoid
 * re-entrant flush issues) from a Spring transaction synchronization's
 * afterCommit() — Hibernate's own Interceptor.afterTransactionCompletion(tx)
 * turned out unreliable here: under Spring-managed (JpaTransactionManager)
 * transactions, tx.wasSuccessful()/getStatus() do not reflect the real
 * outcome by the time that callback fires.
 */
@Slf4j
public class AuditInterceptor implements Interceptor {

    private static volatile DataSource dataSource;

    static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ThreadLocal<List<Entry>> pending = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        record(entity, id, "CREATE", null, state, propertyNames);
        return false;
    }

    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
                                 String[] propertyNames, Type[] types) {
        record(entity, id, "UPDATE", previousState, currentState, propertyNames);
        return false;
    }

    @Override
    public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        record(entity, id, "DELETE", state, null, propertyNames);
    }

    private void record(Object entity, Object id, String action, Object[] before, Object[] after, String[] propertyNames) {
        if (entity instanceof AuditLog) {
            return; // never audit the audit trail itself
        }
        try {
            List<Entry> entries = pending.get();
            boolean firstInTransaction = entries.isEmpty();
            String diff = buildDiffJson(propertyNames, before, after);
            entries.add(new Entry(currentActor(), entity.getClass().getSimpleName(),
                    String.valueOf(id), action, diff));

            if (firstInTransaction && TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        AuditInterceptor.this.flush(pending.get());
                    }

                    @Override
                    public void afterCompletion(int status) {
                        pending.remove();
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Failed to capture audit entry for {} (action={})", entity.getClass().getSimpleName(), action, e);
        }
    }

    private String currentActor() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User user) {
                return user.getEmail();
            }
            return "system";
        } catch (Exception e) {
            return "system";
        }
    }

    private String buildDiffJson(String[] propertyNames, Object[] before, Object[] after) {
        Map<String, Object> diff = new LinkedHashMap<>();
        for (int i = 0; i < propertyNames.length; i++) {
            String beforeValue = stringify(propertyNames[i], before == null ? null : before[i]);
            String afterValue = stringify(propertyNames[i], after == null ? null : after[i]);

            // For UPDATE (both sides present) skip fields that didn't actually change.
            if (before != null && after != null && Objects.equals(beforeValue, afterValue)) {
                continue;
            }

            Map<String, String> pair = new LinkedHashMap<>();
            pair.put("before", beforeValue);
            pair.put("after", afterValue);
            diff.put(propertyNames[i], pair);
        }
        try {
            return objectMapper.writeValueAsString(diff);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String stringify(String propertyName, Object value) {
        if (value == null) {
            return null;
        }
        if (propertyName.toLowerCase().contains("password")) {
            return "[REDACTED]";
        }
        try {
            String display = displayName(value);
            return display != null ? display : String.valueOf(value);
        } catch (Exception e) {
            return "[unavailable]";
        }
    }

    // Entity relations (assignedTo, caseDetails, etc.) otherwise stringify as
    // Object#toString()'s default "ClassName@hashcode" - render the known
    // ones by a human-readable field instead so audit diffs are legible.
    private String displayName(Object value) {
        if (value instanceof User u) {
            String name = ((u.getName() != null ? u.getName() : "") + " " + (u.getSurname() != null ? u.getSurname() : "")).trim();
            return !name.isEmpty() ? name : u.getEmail();
        }
        if (value instanceof CaseDetails c) {
            return c.getCaseNumber() != null ? c.getCaseNumber() : c.getOfficeFileNumber();
        }
        if (value instanceof Court c) {
            return c.getName();
        }
        if (value instanceof Client c) {
            return c.getName();
        }
        return null;
    }

    private void flush(List<Entry> entries) {
        if (entries.isEmpty() || dataSource == null) {
            return;
        }
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO audit_log (id, actor, entity_type, entity_id, action, diff_json, created_at) VALUES (?,?,?,?,?,?,?)")) {
                for (Entry e : entries) {
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, e.actor());
                    ps.setString(3, e.entityType());
                    ps.setString(4, e.entityId());
                    ps.setString(5, e.action());
                    ps.setString(6, e.diffJson());
                    ps.setTimestamp(7, Timestamp.from(Instant.now()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            log.error("Failed to persist {} audit log entr{}", entries.size(), entries.size() == 1 ? "y" : "ies", e);
        }
    }

    private record Entry(String actor, String entityType, String entityId, String action, String diffJson) {}
}
