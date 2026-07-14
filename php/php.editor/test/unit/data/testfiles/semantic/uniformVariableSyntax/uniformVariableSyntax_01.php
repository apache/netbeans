<?php

$test->instance->field;
test()->instance->field["test"];
$test[1]->instance->field["test"][0];
$test->instance->method();
$test->instance->method()[0];

($test)->instance->field;
(test())->instance->field["test"];
($test[1])->instance->field["test"][0];
($test->instance)->method();
($test)->instance->method()[0];

$test->instance::CONSTANT;
test()->instance::CONSTANT["test"];
$test[1]->instance::$staticField;
$test->instance::$staticField[0];
$test->instance::$staticField[0][1];
$test->instance::$staticField();
$test->instance::staticMethod();
$test->instance::staticMethod()[0];

($test)->instance::CONSTANT;
(test())->instance::CONSTANT["test"];
($test[1])->instance::$staticField;
($test->instance)::$staticField[0];
($test)->instance::$staticField[0][1];
($test)->instance::$staticField();
($test)->instance::staticMethod();
($test)->instance::staticMethod()[0];
