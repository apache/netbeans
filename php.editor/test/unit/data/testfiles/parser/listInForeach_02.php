<?php
$array = [
    "key1" => [1, 2],
    "key2" => [3, 4],
];

foreach ($array as $key => list($v1, $v2)) {
    echo "$key => A: $v1, B: $v2\n";
}
