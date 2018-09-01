<?php
function sum(...$array) {
    return array_sum($array);
}

function sum2(&...$array2) {
    return array_sum($array2);
}
