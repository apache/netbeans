<?php

function foo() {
    foreach ([] as list($a, $b, $c)) {
        echo $a;
        echo $b;
        echo $c;
    }

    foreach ([] as ["x" => $x, "y" => $y, "z" => $z]) { // PHP 7.1
        echo $x;
        echo $y;
        echo $z;
    }
}
