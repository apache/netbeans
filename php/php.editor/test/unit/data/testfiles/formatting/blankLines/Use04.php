<?php
//  namespace MyProject\DB;
require 'db.php';

use MyProjectDB; // fine; same as DB\

use MyProjectDBConnection as DBC; // fine
use MyProjectDB as HM; // fine
use HMConnection as DBC2; // class call ends with FATAL!!!

$x = new DBC(); // fine
$y = new HMConnection(); // fine
$z = new DBC2(); // Fatal error: Class 'HM\Connection' not found
?>