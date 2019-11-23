<?php
//  namespace MyProject\DB;
require 'db.php';

use GroupUse\MyProjectDB; // fine; same as DB\

use GroupUse\{MyProjectDBConnection as DBC}; // fine
use MyProjectDB as HM; // fine
use GroupUse\{HMConnection as DBC2, DBC3}; // class call ends with FATAL!!!

$x = new DBC(); // fine
$y = new HMConnection(); // fine
$z = new DBC2(); // Fatal error: Class 'HM\Connection' not found
