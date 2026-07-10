-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 10, 2026 at 05:28 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `law_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `approved`
--

CREATE TABLE `approved` (
  `id` varchar(255) NOT NULL,
  `approved` bit(1) NOT NULL,
  `hearing_date` date DEFAULT NULL,
  `next_date` date DEFAULT NULL,
  `summary` varchar(5000) DEFAULT NULL,
  `case_details_id` varchar(255) DEFAULT NULL,
  `court_id` varchar(255) DEFAULT NULL,
  `case_id` varchar(255) DEFAULT NULL,
  `entered_by` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `approved`
--

INSERT INTO `approved` (`id`, `approved`, `hearing_date`, `next_date`, `summary`, `case_details_id`, `court_id`, `case_id`, `entered_by`) VALUES
('APP-2026-001', b'1', '2026-05-10', '2026-08-12', 'Interim injunction granted to plaintiff.', NULL, 'CRT-MUMBI-HIG', 'CASE-2026-A01', 'Anjali Mehta'),
('APP-2026-002', b'0', '2026-06-01', '2026-07-20', 'Defendant granted 3 weeks to file statement.', NULL, 'CRT-PUNE-DIST', 'CASE-2026-A02', 'Amit Verma'),
('APP-2026-003', b'1', '2026-06-15', '2026-09-05', 'Notice issued to Ministry of Environment.', NULL, 'CRT-DELHI-SUP', 'CASE-2026-A03', 'Rajesh Sharma'),
('APP-2026-004', b'1', '2026-05-28', '2026-07-18', 'Plaintiff completed cross-examination.', NULL, 'CRT-DELHI-HIG', 'CASE-2026-A04', 'Karan Malhotra'),
('APP-2026-005', b'1', '2026-06-12', '2026-08-01', 'Adjourned due to non-availability of bench.', NULL, 'CRT-BANG-DIST', 'CASE-2026-A05', 'Anjali Mehta'),
('APP-2026-006', b'0', '2026-06-20', '2026-07-29', 'Interim protection extended by two weeks.', NULL, 'CRT-DELHI-SUP', 'CASE-2026-A06', 'Amit Verma'),
('APP-2026-007', b'1', '2026-06-05', '2026-08-22', 'Arguments heard from respondents side.', NULL, 'CRT-CHAI-HIG', 'CASE-2026-A07', 'Karan Malhotra'),
('APP-2026-008', b'1', '2026-06-18', '2026-10-15', 'Fresh application for condonation filed.', NULL, 'CRT-KOLK-HIG', 'CASE-2026-A08', 'Rajesh Sharma');

-- --------------------------------------------------------

--
-- Table structure for table `associate`
--

CREATE TABLE `associate` (
  `id` varchar(255) NOT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `open_tasks` int(11) NOT NULL,
  `pending_cases` int(11) NOT NULL,
  `case_details_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `associate`
--

INSERT INTO `associate` (`id`, `designation`, `email`, `mobile`, `name`, `open_tasks`, `pending_cases`, `case_details_id`) VALUES
('35a1df58-6c37-4078-85f9-df2948a4c5c5', 'Associate', 'mohit.marwal@gmail.com', '8983000588', 'mohit marwal', 0, 0, NULL),
('ASC-2026-001', 'Junior Associate', 'rohan.das@abhipsa.law', '9123456780', 'Rohan Das', 3, 5, 'CASE-2026-A01'),
('ASC-2026-002', 'Senior Associate', 'priya.rai@abhipsa.law', '9123456781', 'Priya Rai', 1, 2, 'CASE-2026-A02'),
('ASC-2026-003', 'Junior Legal Officer', 'rahul@abhipsa.law', '9123456782', 'Rahul Saxena', 4, 4, 'CASE-2026-A03'),
('ASC-2026-004', 'Legal Researcher', 'megha@abhipsa.law', '9123456783', 'Megha Gupta', 2, 1, 'CASE-2026-A04'),
('ASC-2026-005', 'Senior Associate', 'vikram.s@abhipsa.law', '9123456784', 'Vikram Singh', 2, 3, 'CASE-2026-A05'),
('ASC-2026-006', 'Junior Associate', 'divya@abhipsa.law', '9123456785', 'Divya Teja', 5, 6, 'CASE-2026-A06'),
('ASC-2026-007', 'Intern', 'sid@abhipsa.law', '9123456786', 'Siddharth Roy', 1, 0, 'CASE-2026-A07'),
('ASC-2026-008', 'Junior Associate', 'neha@abhipsa.law', '9123456787', 'Neha Kakkar', 3, 3, 'CASE-2026-A08');

-- --------------------------------------------------------

--
-- Table structure for table `bill`
--

CREATE TABLE `bill` (
  `id` varchar(255) NOT NULL,
  `balance` double NOT NULL,
  `bill_date` date DEFAULT NULL,
  `bill_no` varchar(255) DEFAULT NULL,
  `received` double NOT NULL,
  `total` double NOT NULL,
  `case_details_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bill`
--

INSERT INTO `bill` (`id`, `balance`, `bill_date`, `bill_no`, `received`, `total`, `case_details_id`) VALUES
('BIL-2026-001', 30000, '2026-03-01', 'BILL-A01-X', 20000, 50000, 'CASE-2026-A01'),
('BIL-2026-002', 0, '2026-04-15', 'BILL-A02-Y', 75000, 75000, 'CASE-2026-A02'),
('BIL-2026-003', 70000, '2026-05-10', 'BILL-A03-Z', 50000, 120000, 'CASE-2026-A03'),
('BIL-2026-004', 0, '2026-05-20', 'BILL-A04-W', 45000, 45000, 'CASE-2026-A04'),
('BIL-2026-005', 60000, '2026-06-01', 'BILL-A05-V', 30000, 90000, 'CASE-2026-A05'),
('BIL-2026-006', 35000, '2026-06-11', 'BILL-A06-U', 0, 35000, 'CASE-2026-A06'),
('BIL-2026-007', 0, '2026-06-18', 'BILL-A07-T', 60000, 60000, 'CASE-2026-A07'),
('BIL-2026-008', 50000, '2026-07-01', 'BILL-A08-S', 100000, 150000, 'CASE-2026-A08');

-- --------------------------------------------------------

--
-- Table structure for table `case_details`
--

CREATE TABLE `case_details` (
  `id` varchar(255) NOT NULL,
  `case_number` varchar(255) DEFAULT NULL,
  `defendant` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `filing_date` date DEFAULT NULL,
  `next_date` date DEFAULT NULL,
  `office_file_number` varchar(255) DEFAULT NULL,
  `plaintiff` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `assigned_user_id` varchar(255) DEFAULT NULL,
  `court_id` varchar(255) DEFAULT NULL,
  `case_type` varchar(255) DEFAULT NULL,
  `limitation_date` date DEFAULT NULL,
  `matter_type` varchar(255) DEFAULT NULL,
  `approved` bit(1) DEFAULT NULL,
  `approved_on` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `case_details`
--

INSERT INTO `case_details` (`id`, `case_number`, `defendant`, `description`, `filing_date`, `next_date`, `office_file_number`, `plaintiff`, `status`, `assigned_user_id`, `court_id`, `case_type`, `limitation_date`, `matter_type`, `approved`, `approved_on`) VALUES
('11d8b9c0-0249-48ba-8de0-5d4283a776b6', 'demo', 'sdf', 'hi ', '2026-07-05', '6767-07-06', 'demo', 'sdf', 'Pending', 'USR-2026-007', 'CRT-HYD-DIST', 'demo', '4545-05-04', NULL, b'1', '2026-07-01'),
('3d4eac0e-7847-11f1-aa03-a44cc81356c5', 'TEST/001/2026', 'Jane Smith', 'hi this is demo case descriptionsdfsdf', NULL, '2026-07-05', NULL, 'John Doe', 'Pending', 'USR-2026-001', 'CRT-BANG-DIST', 'asf', NULL, 'asf', b'1', '2026-07-03'),
('81b79ae9-b7d1-4675-a147-60e2d2203dec', 'dfg', 'sdfsdf', 'hi this is demo desc', '2026-07-05', '1988-02-05', 'dfg', 'sdfsdf', 'Pending', 'USR-2026-008', 'CRT-KOLK-HIG', 'sdf', '1988-02-05', 'sdf', b'1', '2026-07-06'),
('case-001', 'RCS/214/2024', 'Sunita Deshmukh', 'Issues framed. Plaintiffs evidence to begin on next date.', '2024-05-10', '2026-07-05', '1024', 'Ramesh Patil', 'Pending', NULL, 'court-001', 'SUIT', '2026-07-20', NULL, b'1', '2026-07-05'),
('case-002', 'Cri/MA/87/2025', 'Vijay Joshi', 'Reply filed by APP. Heard in part.', '2025-02-15', '2026-07-04', '1025', 'State of Maharashtra', 'Pending', NULL, 'court-002', 'COMPLAINT', '2026-07-15', NULL, b'1', '2026-05-15'),
('case-003', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Disposed', NULL, NULL, 'SUIT', NULL, NULL, b'1', '2026-01-10'),
('CASE-2026-A01', 'WP-1024-2025', 'State of Maharashtra', 'Land acquisition dispute', '2025-01-15', '2026-08-12', 'OFF-2025-001', 'Alpha Commercials Inc.', 'HEARING', 'USR-2026-002', 'CRT-MUMBI-HIG', NULL, NULL, NULL, b'1', '2026-02-20'),
('CASE-2026-A02', 'CS-4050-2024', 'Jane Smith Logistics', 'Breach of contract suit', '2024-06-10', '2026-07-20', 'OFF-2024-089', 'John Doe LLC', 'PENDING', 'USR-2026-003', 'CRT-PUNE-DIST', NULL, NULL, NULL, b'1', '2026-03-12'),
('CASE-2026-A03', 'PIL-0012-2026', 'Union of India', 'Environmental pollution standard appeal', '2026-02-22', '2026-09-05', 'OFF-2026-003', 'Green Earth NGO', 'ADMITTED', 'USR-2026-001', 'CRT-DELHI-SUP', NULL, NULL, NULL, b'1', '2026-04-05'),
('CASE-2026-A04', 'CA-5520-2025', 'Global Ventures Ltd', 'Intellectual Property infringement', '2025-08-04', '2026-07-18', 'OFF-2025-144', 'TechCorp India', 'ARGUMENTS', 'USR-2026-006', 'CRT-DELHI-HIG', NULL, NULL, NULL, b'1', '2026-06-10'),
('CASE-2026-A05', 'ST-3011-2023', 'Delta Realities', 'Property possession restoration', '2023-11-02', '2026-08-01', 'OFF-2023-412', 'Robert Frost', 'EVIDENCE', 'USR-2026-002', 'CRT-BANG-DIST', NULL, NULL, NULL, b'1', '2026-06-25'),
('CASE-2026-A06', 'COMP-44-2026', 'Zylog Enterprises', 'Anti-competitive practices investigation', '2026-04-10', '2026-07-29', 'OFF-2026-015', 'Fair Trade Commission', 'NOTICE_STAGE', 'USR-2026-003', 'CRT-DELHI-SUP', NULL, NULL, NULL, b'1', '2026-06-28'),
('CASE-2026-A07', 'WP-2219-2025', 'University of Chennai', 'Service matter and wrongful termination', '2025-05-19', '2026-08-22', 'OFF-2025-302', 'Anita Desai', 'HEARING', 'USR-2026-006', 'CRT-CHAI-HIG', NULL, NULL, NULL, b'1', '2026-07-05'),
('CASE-2026-A08', 'CS-9012-2026', 'Metro Rail Corp', 'Arbitration award enforcement challenge', '2026-05-01', '2026-10-15', 'OFF-2026-045', 'Skyline Infra', 'Disposed', 'USR-2026-001', 'CRT-KOLK-HIG', NULL, NULL, NULL, b'1', '2026-06-15');

-- --------------------------------------------------------

--
-- Table structure for table `court`
--

CREATE TABLE `court` (
  `id` varchar(255) NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `court`
--

INSERT INTO `court` (`id`, `location`, `name`, `type`) VALUES
('a0b6501c-a8bc-4663-a85e-bcddb4c2df0e', 'Pune', 'Pune Distric Courts', 'District Court'),
('court-001', 'Akola', 'CJSD/CJM Akola', 'District'),
('court-002', 'Akot', 'JMFC Akot', 'Magistrate'),
('CRT-BANG-DIST', 'Bengaluru', 'District Court Bengaluru', 'District Court'),
('CRT-CHAI-HIG', 'Chennai', 'Madras High Court', 'High Court'),
('CRT-DELHI-HIG', 'New Delhi', 'Delhi High Court', 'High Court'),
('CRT-DELHI-SUP', 'New Delhi', 'Supreme Court of India', 'Supreme Court'),
('CRT-HYD-DIST', 'Hyderabad', 'City Civil Court Hyderabad', 'District Court'),
('CRT-KOLK-HIG', 'Kolkata', 'Calcutta High Court', 'High Court'),
('CRT-MUMBI-HIG', 'Mumbai', 'Bombay High Court', 'High Court'),
('CRT-PUNE-DIST', 'Pune', 'District & Sessions Court Pune', 'District Court');

-- --------------------------------------------------------

--
-- Table structure for table `master_data`
--

CREATE TABLE `master_data` (
  `id` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `file_no` varchar(255) DEFAULT NULL,
  `mobile_numbers` varchar(255) DEFAULT NULL,
  `parties_name` varchar(255) DEFAULT NULL,
  `reg_case_no` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `master_data`
--

INSERT INTO `master_data` (`id`, `email`, `file_no`, `mobile_numbers`, `parties_name`, `reg_case_no`) VALUES
('MST-2026-001', 'alpha@commercials.com', 'OFF-2025-001', '9876543210, 9988776655', 'Alpha Commercials vs State of Maha', 'WP-1024-2025'),
('MST-2026-002', 'contact@johndoellc.com', 'OFF-2024-089', '9123456780', 'John Doe LLC vs Jane Smith Log', 'CS-4050-2024'),
('MST-2026-003', 'earth@greengo.org', 'OFF-2026-003', '9123456782', 'Green Earth NGO vs Union of India', 'PIL-0012-2026'),
('MST-2026-004', 'legal@techcorp.in', 'OFF-2025-144', '9988776622', 'TechCorp India vs Global Ventures', 'CA-5520-2025'),
('MST-2026-005', 'robert@frost.com', 'OFF-2023-412', '9988776611', 'Robert Frost vs Delta Realities', 'ST-3011-2023'),
('MST-2026-006', 'compliance@zylog.com', 'OFF-2026-015', '9988776600', 'Fair Trade Comm vs Zylog Ent', 'COMP-44-2026'),
('MST-2026-007', 'anita.desai@email.com', 'OFF-2025-302', '9988776699', 'Anita Desai vs Univ of Chennai', 'WP-2219-2025'),
('MST-2026-008', 'arbitration@skylineinfra.com', 'OFF-2026-045', '9988776688', 'Skyline Infra vs Metro Rail Corp', 'CS-9012-2026');

-- --------------------------------------------------------

--
-- Table structure for table `mobile_contact`
--

CREATE TABLE `mobile_contact` (
  `id` varchar(255) NOT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `case_details_id` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `mobile_contact`
--

INSERT INTO `mobile_contact` (`id`, `mobile`, `name`, `role`, `case_details_id`, `email`) VALUES
('7f76eb8b-ffa9-49fe-9392-1770ce701bda', '8983000588', 'mohit', 'client', 'CASE-2026-A04', NULL),
('CON-2026-001', '9988776655', 'Suresh Kumar', 'Primary Client', 'CASE-2026-A01', NULL),
('CON-2026-002', '9988776644', 'Manish G', 'Opposing Counsel', 'CASE-2026-A02', NULL),
('CON-2026-003', '9988776633', 'Dr. Ramesh Shinde', 'Expert Witness', 'CASE-2026-A03', NULL),
('CON-2026-004', '9988776622', 'John Executive', 'Corporate SPOC', 'CASE-2026-A04', NULL),
('CON-2026-005', '9988776611', 'Gaurav Patil', 'Local Agent', 'CASE-2026-A05', NULL),
('CON-2026-006', '9988776600', 'Kiran Shah', 'Co-defendant representative', 'CASE-2026-A06', NULL),
('CON-2026-007', '9988776699', 'Nisha Desai', 'Client Sister', 'CASE-2026-A07', NULL),
('CON-2026-008', '9988776688', 'Tarun Khurana', 'Arbitrator Assistant', 'CASE-2026-A08', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `notice`
--

CREATE TABLE `notice` (
  `id` varchar(255) NOT NULL,
  `dispatch_date` date DEFAULT NULL,
  `notice_date` date DEFAULT NULL,
  `office_file_no` varchar(255) DEFAULT NULL,
  `reference_no` varchar(255) DEFAULT NULL,
  `case_details_id` varchar(255) DEFAULT NULL,
  `courier_name` varchar(255) DEFAULT NULL,
  `delivery_status` varchar(255) DEFAULT NULL,
  `suit_status` varchar(255) DEFAULT NULL,
  `tracking_number` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notice`
--

INSERT INTO `notice` (`id`, `dispatch_date`, `notice_date`, `office_file_no`, `reference_no`, `case_details_id`, `courier_name`, `delivery_status`, `suit_status`, `tracking_number`) VALUES
('NTC-2026-001', '2025-02-03', '2025-02-01', 'OFF-2025-001', 'REF-NOT-101', 'CASE-2026-A01', 'Speed Post', 'Delivered', 'Active', 'TRK10001'),
('NTC-2026-002', '2024-07-15', '2024-07-12', 'OFF-2024-089', 'REF-NOT-102', 'CASE-2026-A02', 'Blue Dart', 'In Transit', 'Closed', 'TRK10002'),
('NTC-2026-003', '2026-03-05', '2026-03-01', 'OFF-2026-003', 'REF-NOT-103', 'CASE-2026-A03', 'DTDC', 'Delivered', 'Active', 'TRK10003'),
('NTC-2026-004', '2025-08-18', '2025-08-15', 'OFF-2025-144', 'REF-NOT-104', 'CASE-2026-A04', 'Professional Couriers', 'Pending', 'Active', 'TRK10004'),
('NTC-2026-005', '2023-11-12', '2023-11-10', 'OFF-2023-412', 'REF-NOT-105', 'CASE-2026-A05', 'Speed Post', 'Delivered', 'Closed', 'TRK10005'),
('NTC-2026-006', '2026-04-22', '2026-04-20', 'OFF-2026-015', 'REF-NOT-106', 'CASE-2026-A06', 'Blue Dart', 'In Transit', 'Active', 'TRK10006'),
('NTC-2026-007', '2025-06-03', '2025-06-01', 'OFF-2025-302', 'REF-NOT-107', 'CASE-2026-A07', 'DTDC', 'Delivered', 'Active', 'TRK10007'),
('NTC-2026-008', '2026-05-17', '2026-05-15', 'OFF-2026-045', 'REF-NOT-108', 'CASE-2026-A08', 'Speed Post', 'Failed', 'Active', 'TRK10008');

-- --------------------------------------------------------

--
-- Table structure for table `notification`
--

CREATE TABLE `notification` (
  `id` varchar(255) NOT NULL,
  `channel` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `reference_no` varchar(255) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `case_details_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notification`
--

INSERT INTO `notification` (`id`, `channel`, `message`, `reference_no`, `sent_at`, `status`, `case_details_id`) VALUES
('', 'email', 'Your case notification was processed successfully.', 'REF12345', '2026-07-05 16:15:20.000000', 'successful', 'case-001'),
('notif-001', 'email', 'Your case has been approved.', 'REF12345', '2026-07-05 16:20:18.000000', 'approved', 'case-001'),
('NTF-2026-001', 'EMAIL', 'Reminder: Next hearing is scheduled on 2026-08-12.', 'NT-5541', '2026-07-01 10:00:00.000000', 'SENT', 'CASE-2026-A01'),
('NTF-2026-002', 'SMS', 'Alert: Payment balance of 30,000 INR is due.', 'NT-5542', '2026-07-02 14:30:00.000000', 'DELIVERED', 'CASE-2026-A01'),
('NTF-2026-003', 'EMAIL', 'Hearing updated for case CS-4050-2024.', 'NT-5543', '2026-07-03 09:15:00.000000', 'SENT', 'CASE-2026-A02'),
('NTF-2026-004', 'SMS', 'Notice copy dispatched regarding PIL.', 'NT-5544', '2026-07-03 11:00:00.000000', 'FAILED', 'CASE-2026-A03'),
('NTF-2026-005', 'EMAIL', 'IP Case arguments list finalized.', 'NT-5545', '2026-07-04 10:30:00.000000', 'SENT', 'CASE-2026-A04'),
('NTF-2026-006', 'SMS', 'Adjourned notice sent to Client Robert.', 'NT-5546', '2026-07-04 12:00:00.000000', 'DELIVERED', 'CASE-2026-A05'),
('NTF-2026-007', 'EMAIL', 'Urgent document submission required.', 'NT-5547', '2026-07-04 14:15:00.000000', 'PENDING', 'CASE-2026-A06'),
('NTF-2026-008', 'EMAIL', 'Enforcement brief ready for evaluation.', 'NT-5548', '2026-07-04 15:45:00.000000', 'SENT', 'CASE-2026-A08');

-- --------------------------------------------------------

--
-- Table structure for table `task`
--

CREATE TABLE `task` (
  `id` varchar(255) NOT NULL,
  `due_date` date DEFAULT NULL,
  `priority` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `task` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `assigned_to` varchar(255) DEFAULT NULL,
  `case_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `task`
--

INSERT INTO `task` (`id`, `due_date`, `priority`, `status`, `task`, `type`, `assigned_to`, `case_id`) VALUES
('TSK-2026-001', '2026-07-25', 'HIGH', 'IN_PROGRESS', 'Draft Rejoinder Application', 'Legal Drafting', 'USR-2026-002', 'CASE-2026-A01'),
('TSK-2026-002', '2026-07-15', 'MEDIUM', 'PENDING', 'Collect certified order copies', 'Documentation', 'USR-2026-003', 'CASE-2026-A02'),
('TSK-2026-003', '2026-08-20', 'HIGH', 'IN_PROGRESS', 'Prepare synopsis of environmental damage', 'Research', 'USR-2026-004', 'CASE-2026-A03'),
('TSK-2026-004', '2026-06-30', 'LOW', 'COMPLETED', 'File Vakalatnama in High Court', 'Filing', 'USR-2026-006', 'CASE-2026-A04'),
('TSK-2026-005', '2026-07-22', 'MEDIUM', 'PENDING', 'Client meeting for evidence tracking', 'Meeting', 'USR-2026-005', 'CASE-2026-A05'),
('TSK-2026-006', '2026-07-27', 'HIGH', 'PENDING', 'Review reply from Zylog Enterprises', 'Review', 'USR-2026-003', 'CASE-2026-A06'),
('TSK-2026-007', '2026-08-10', 'MEDIUM', 'IN_PROGRESS', 'Collate past 5 years salary slips', 'Evidence Prep', 'USR-2026-007', 'CASE-2026-A07'),
('TSK-2026-008', '2026-07-02', 'LOW', 'COMPLETED', 'Pay nominal arbitration stamp fees', 'Financial', 'USR-2026-008', 'CASE-2026-A08');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `surname` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `enabled`, `name`, `password`, `phone`, `role`, `surname`) VALUES
('USR-2026-001', 'rajesh@abhipsa.law', b'1', 'Rajesh', 'pass_secure_1', '9876543210', 'ROLE_ADMIN', 'Sharma'),
('USR-2026-002', 'anjali@abhipsa.law', b'1', 'Anjali', 'pass_secure_2', '9876543211', 'ROLE_LAWYER', 'Mehta'),
('USR-2026-003', 'amit@abhipsa.law', b'1', 'Amit', 'pass_secure_3', '9876543212', 'ROLE_LAWYER', 'Verma'),
('USR-2026-004', 'sneha@abhipsa.law', b'1', 'Sneha', 'pass_secure_4', '9876543213', 'ROLE_ASSOCIATE', 'Patil'),
('USR-2026-005', 'vikram@abhipsa.law', b'1', 'Vikram', 'pass_secure_5', '9876543214', 'ROLE_ASSOCIATE', 'Joshi'),
('USR-2026-006', 'karan@abhipsa.law', b'1', 'Karan', 'pass_secure_6', '9876543215', 'ROLE_LAWYER', 'Malhotra'),
('USR-2026-007', 'riya@abhipsa.law', b'1', 'Riya', 'pass_secure_7', '9876543216', 'ROLE_ASSOCIATE', 'Sen'),
('USR-2026-008', 'admin@law.com', b'1', 'Deepak', 'admin123', '9876543217', 'ROLE_ADMIN', 'Nair'),
('USR-2026-CLI01', 'ramesh.patil@gmail.com', b'1', 'Ramesh', 'client_pass_1', '9811111111', 'ROLE_CLIENT', 'Patil'),
('USR-2026-CLI02', 'sunita.d@yahoo.com', b'1', 'Sunita', 'client_pass_2', '9822222222', 'ROLE_CLIENT', 'Deshmukh'),
('USR-2026-CLI03', 'contact@mahalakshmitraders.com', b'1', 'Vijay', 'client_pass_3', '9833333333', 'ROLE_CLIENT', 'Joshi');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `approved`
--
ALTER TABLE `approved`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKne89hkbxokfo2jndr03lemd7g` (`case_details_id`),
  ADD KEY `FKisorf695ym83vj69xkk1y61na` (`court_id`),
  ADD KEY `FK8maog501sca8tp3ogwh902cby` (`case_id`);

--
-- Indexes for table `associate`
--
ALTER TABLE `associate`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKm66yb53abwpngn1sfx8t2tu6j` (`case_details_id`);

--
-- Indexes for table `bill`
--
ALTER TABLE `bill`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK12fn07oi3fpj9hl96aq940sy2` (`case_details_id`);

--
-- Indexes for table `case_details`
--
ALTER TABLE `case_details`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKexdeoxu6qqjou34nq5ot6hbgu` (`assigned_user_id`),
  ADD KEY `FKjmafqwac3c2klqyj058o36ayb` (`court_id`);

--
-- Indexes for table `court`
--
ALTER TABLE `court`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `master_data`
--
ALTER TABLE `master_data`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mobile_contact`
--
ALTER TABLE `mobile_contact`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKh2bxuu5jb6cyxe8pa1ymqr9se` (`case_details_id`);

--
-- Indexes for table `notice`
--
ALTER TABLE `notice`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKks445ckdtfxqmqab6of911ujb` (`case_details_id`);

--
-- Indexes for table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK2i8drhvpieewmjn9mg08yxsg9` (`case_details_id`);

--
-- Indexes for table `task`
--
ALTER TABLE `task`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK4amgamieqvrq9ej2wy2oe6878` (`assigned_to`),
  ADD KEY `FK79prfolqe1blpkvaefyyx2b71` (`case_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `approved`
--
ALTER TABLE `approved`
  ADD CONSTRAINT `FK8maog501sca8tp3ogwh902cby` FOREIGN KEY (`case_id`) REFERENCES `case_details` (`id`),
  ADD CONSTRAINT `FKisorf695ym83vj69xkk1y61na` FOREIGN KEY (`court_id`) REFERENCES `court` (`id`),
  ADD CONSTRAINT `FKne89hkbxokfo2jndr03lemd7g` FOREIGN KEY (`case_details_id`) REFERENCES `case_details` (`id`);

--
-- Constraints for table `associate`
--
ALTER TABLE `associate`
  ADD CONSTRAINT `FKm66yb53abwpngn1sfx8t2tu6j` FOREIGN KEY (`case_details_id`) REFERENCES `case_details` (`id`);

--
-- Constraints for table `bill`
--
ALTER TABLE `bill`
  ADD CONSTRAINT `FK12fn07oi3fpj9hl96aq940sy2` FOREIGN KEY (`case_details_id`) REFERENCES `case_details` (`id`);

--
-- Constraints for table `case_details`
--
ALTER TABLE `case_details`
  ADD CONSTRAINT `FKexdeoxu6qqjou34nq5ot6hbgu` FOREIGN KEY (`assigned_user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKjmafqwac3c2klqyj058o36ayb` FOREIGN KEY (`court_id`) REFERENCES `court` (`id`);

--
-- Constraints for table `mobile_contact`
--
ALTER TABLE `mobile_contact`
  ADD CONSTRAINT `FKh2bxuu5jb6cyxe8pa1ymqr9se` FOREIGN KEY (`case_details_id`) REFERENCES `case_details` (`id`);

--
-- Constraints for table `notice`
--
ALTER TABLE `notice`
  ADD CONSTRAINT `FKks445ckdtfxqmqab6of911ujb` FOREIGN KEY (`case_details_id`) REFERENCES `case_details` (`id`);

--
-- Constraints for table `notification`
--
ALTER TABLE `notification`
  ADD CONSTRAINT `FK2i8drhvpieewmjn9mg08yxsg9` FOREIGN KEY (`case_details_id`) REFERENCES `case_details` (`id`);

--
-- Constraints for table `task`
--
ALTER TABLE `task`
  ADD CONSTRAINT `FK4amgamieqvrq9ej2wy2oe6878` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FK79prfolqe1blpkvaefyyx2b71` FOREIGN KEY (`case_id`) REFERENCES `case_details` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
