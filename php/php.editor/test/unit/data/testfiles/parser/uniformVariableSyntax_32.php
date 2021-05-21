<?php

class ArrayCall { public static function test() { echo "array call" . PHP_EOL;} }
$array = new ArrayCall();
['ArrayCall', 'test']();
(['ArrayCall', 'test'])();
[new ArrayCall, 'test']();
[new ArrayCall(), 'test']();
array('ArrayCall', 'test')();
array($array, 'test')();
$test = "test";
[new class {public static function test($param) {echo "anonymous class array call" . PHP_EOL;}}, $test]("test");
