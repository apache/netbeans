<?php

class Foo {}

// variable
$tests = [];
$anon = function() use ($tests) {
    $tests[0] = [1, 2, 3];
};

$anon();
var_dump($tests);

// reference
$references = [];
$anon2 = function() use (&$references) {
    $tests;
    $references[0] = [1, 2, 3];
};

$anon2();
var_dump($references);

// reference and instanceof
$foo = new Foo();
$anon3 = function() use (&$foo) {
    $a = new stdClass();
    if($a instanceof $foo) {
        echo get_class($foo) . PHP_EOL;
    } else {
        echo get_class($a) .PHP_EOL;
    }
};

$anon3();
