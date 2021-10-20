<?php
// Function Calls
compact(
$a1,
$a2,
);

$merged = array_merge(
$array1,
$array2,
['foo', 'bar'], // comment
);

$format = "sprintf %s %s";
echo sprintf(
$format,
'NetBeans',
'IDE',
) . PHP_EOL;

// Method & Closure Calls
$foo = new Foo(
'constructor',
'foo',
);

$foo->bar(
'method',
'bar',
);

$bar = function(...$args) {
    echo "closure" . PHP_EOL;
};
$bar(
'closure',
'bar',
);

// Language Constructs
unset(
$param1,
$param2,
);

$isset1 = "isset1";
$isset2 = "isset2";
var_dump(isset($isset1, $isset2,));
