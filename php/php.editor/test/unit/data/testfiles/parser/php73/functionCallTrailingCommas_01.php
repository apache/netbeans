<?php
class Foo {
    public function bar($param1, $param2) {
        echo "bar method : class Foo" . PHP_EOL;
    }
}

// Function Calls
compact(
    $a1,
    $a2,
);

$array1 = ['baz'];
$array2 = ['qux'];
$merged = array_merge(
    $array1,
    $array2,
    ['foo', 'bar'],
);
var_dump($merged);
$a1 = [
    1, 2, 3,
];

$a2 = [
    "a", "b", "c",
];

var_dump(
    $a1,
    $a2,
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
