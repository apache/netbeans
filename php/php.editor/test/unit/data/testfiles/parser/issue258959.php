<?php

const A = ["test"];
class MyClass {
    const CONSTANT = [0 => "test0", 1 => "test1", 2 => [0 => "test2"]];
    public static $staticField = [0 => "test0", 1 => "test1"];
    public function myMethod(){
        return ["test1", "test2", "test3"];
    }

    public static function myStaticMethod() {
        return ["test1", "test2", "test3"];
    }
}

function testFunction() {
    return ["a", "b", "c"];
}

function testFunction2() {
    return function() {
        return ["a", "b", "c"];
    };
}

$a = A[0];
$c = 0;
$d = [0 => 1];
$myClass = new MyClass();

// PHP7: OK, PHP5.6: OK
var_dump(isset($b, $a));
var_dump(isset($myClass::$staticField[0]));
var_dump(isset(MyClass::$staticField[0]));
var_dump(isset(testFunction()[0]));
var_dump(isset($myClass->myMethod()[0]));
var_dump(isset(MyClass::myStaticMethod()[0]));

// PHP7: OK, PHP5.6: Error
var_dump(isset("test"[0]));
var_dump(isset("test"[0], $a));
var_dump(isset("test"[0], $b));
var_dump(isset([1][0]));
var_dump(isset(A[0]));
var_dump(isset($myClass::CONSTANT[0]));
var_dump(isset($myClass::CONSTANT[$c]));
var_dump(isset($myClass::CONSTANT[$d[0]]));
var_dump(isset($myClass::CONSTANT[2][$c]));
var_dump(isset(MyClass::CONSTANT[0]));
var_dump(isset(MyClass::CONSTANT[$c]));
var_dump(isset(MyClass::CONSTANT[$d[0]]));
var_dump(isset(MyClass::CONSTANT[2][$c]));
var_dump(isset(testFunction2()()[0]));
