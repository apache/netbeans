<?php



// comment 1
// comment 2

// comment 3
// comment 4

namespace GroupUse;





use My\Full\{Classname as Another};





// this is the same as use My\Full\NSname1 as NSname1
use My\Full\{NSname1, NSname2};
// comment



// importing a global class




use \ArrayObject;
$obj = new namespaces\Another; // instantiates object of class foo\Another
$obj = new Another; // instantiates object of class My\Full\Classname
NSname\subns\func(); // calls function My\Full\NSname\subns\func
$a = new ArrayObject(array(1)); // instantiates object of class ArrayObject
// without the "use \ArrayObject" we would instantiate an object of class foo\ArrayObject
