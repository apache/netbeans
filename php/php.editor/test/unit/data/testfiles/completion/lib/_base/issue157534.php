<?php
function issue157534(&$var) {
    $var++;
}
class SuperClass157534 {
    function add157534(&$a, &$b) {}
}

class SubClass157534 extends SuperClass157534 {
    /**/add
}

$v157534 = new SubClass157534();
$v157534->add157534($a, $b);
issue157534;
?>