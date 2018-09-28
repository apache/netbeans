<?php
$array1 = [1, 2];
list($a1, &$b1) = $array1;
[$a2, &$b2] = $array1;

$array2 = [1, 2, 3, [4, 5]];
list(&$a3, $b3, list(&$c3, $d3)) = $array2;
var_dump($array2);

$array3 = [[1, 2], [3, 4]];
foreach ($array3 as list(&$a, $b)) {
    $a = 5;
}
var_dump($array3);
