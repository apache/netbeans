-- phpMyAdmin SQL Dump
-- version 2.11.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 12, 2008 at 11:42 PM
-- Server version: 5.0.45
-- PHP Version: 5.2.4

--First you need to create a DB by name AirAlliance.

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `AirAlliance`
--

-- --------------------------------------------------------

--
-- Table structure for table `Flights`
--

CREATE TABLE IF NOT EXISTS `Flights` (
  `FID` int(11) NOT NULL,
  `FName` varchar(10) NOT NULL,
  `SourceSID` int(11) NOT NULL,
  `DestSID` int(11) NOT NULL,
  PRIMARY KEY  (`FID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Flights`
--

INSERT INTO `Flights` (`FID`, `FName`, `SourceSID`, `DestSID`) VALUES
('1', 'AA056', 1, '3'),
('2', 'AA032', 5, '6'),
('3', 'AA087', 20, '4'),
('4', 'AA003', 19, '17'),
('5', 'AA004', 10, '13'),
('6', 'AA045', 2, '5'),
('7', 'AA033', 8, '11'),
('8', 'AA089', 12, '9'),
('9', 'AA099', 7, '16'),
('10', 'AA098', 15, '14');

-- --------------------------------------------------------

--
-- Table structure for table `Guest`
--

CREATE TABLE IF NOT EXISTS `Guest` (
  `GID` int(10) NOT NULL auto_increment,
  `FirstName` varchar(20) NOT NULL,
  `LastName` varchar(20) NOT NULL,
  PRIMARY KEY  (`GID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Dumping data for table `Guest`
--

INSERT INTO `Guest` (`GID`, `FirstName`, `LastName`) VALUES
('1', 'Frank', 'Jennings'),
('2', 'Florence', 'Justina'),
('3', 'Randy', 'Pauch'),
('4', 'Dorris', 'Lessing'),
('5', 'Orhan', 'Pamuk'),
('6', 'Harold', 'Pinter'),
('7', 'Toni', 'Morrison'),
('8', 'Dario', 'Fo'),
('9', 'Ivan', 'Bunin'),
('10', 'Henri', 'Bergson');

-- --------------------------------------------------------

--
-- Table structure for table `Itinerary`
--

CREATE TABLE IF NOT EXISTS `Itinerary` (
  `IID` int(11) NOT NULL auto_increment,
  `GID` int(11) NOT NULL,
  `FID` int(11) NOT NULL,
  `SID` int(11) NOT NULL,
  PRIMARY KEY  (`IID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Dumping data for table `Itinerary`
--

INSERT INTO `Itinerary` (`IID`, `GID`, `FID`, `SID`) VALUES
(1, 4, 6, '5'),
(2, 1, 10, '9'),
(3, 6, 1, '1'),
(4, 9, 8, '7'),
(5, 3, 3, '3'),
(6, 2, 4, '8'),
(7, 7, 7, '4'),
(8, 5, 9, '6'),
(9, 10, 5, '2'),
(10, 8, 2, '10');

-- --------------------------------------------------------

--
-- Table structure for table `Schedule`
--

CREATE TABLE IF NOT EXISTS `Schedule` (
  `SID` int(11) NOT NULL auto_increment,
  `GID` int(11) NOT NULL,
  `FID` int(11) NOT NULL,
  `Date` date NOT NULL,
  PRIMARY KEY  (`SID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Dumping data for table `Schedule`
--

INSERT INTO `Schedule` (`SID`, `GID`, `FID`, `Date`) VALUES
(1, 4, '4', '2008-11-01'),
(2, 6, '1', '2008-11-04'),
(3, 2, '10', '2008-10-04'),
(4, 3, '9', '2008-10-21'),
(5, 5, '8', '2008-10-20'),
(6, 1, '7', '2008-10-03'),
(7, 8, '6', '2008-10-04'),
(8, 9, '3', '2008-10-07'),
(9, 7, '2', '2008-10-15'),
(10, 10, '5', '2008-10-17');

-- --------------------------------------------------------

--
-- Table structure for table `Sectors`
--

CREATE TABLE IF NOT EXISTS `Sectors` (
  `SID` int(11) NOT NULL auto_increment,
  `Sector` varchar(10) NOT NULL,
  PRIMARY KEY  (`SID`),
  KEY `Sector` (`Sector`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=21 ;

--
-- Dumping data for table `Sectors`
--

INSERT INTO `Sectors` (`SID`, `Sector`) VALUES
('1', 'BLR'),
('2', 'MAS'),
('3', 'BOM'),
('4', 'FRA'),
('5', 'PEK'),
('6', 'SIN'),
('7', 'SCA'),
('8', 'SFO'),
('9', 'CAL'),
('10', 'CHI'),
('11', 'MAL'),
('12', 'SPB'),
('13', 'SYD'),
('14', 'LON'),
('15', 'ANG'),
('16', 'TOK'),
('17', 'JAV'),
('18', 'VIS'),
('19', 'DEL'),
('20', 'CAL');

