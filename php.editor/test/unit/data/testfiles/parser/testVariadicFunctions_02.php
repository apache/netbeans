<?php
function f($req, $opt = null, &...$params) {
    // $params is an array containing the remaining arguments.
}

f(1);
f(1, 2);
f(1, 2, 3);
f(1, 2, 3, 4);
?>