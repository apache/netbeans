<?php
namespace test\ns\constants\ctx;
const CONSTANT_XYZ = 'Hello World';
define("CONSTANT_ABC", "Hello world.");
echo CONSTANT_XYZ;
echo CONSTANT_ABC;
?>