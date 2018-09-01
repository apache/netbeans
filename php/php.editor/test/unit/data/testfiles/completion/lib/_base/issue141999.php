<?php
class Test141999 {
    public function foo() {}
}

$test141999 = new Test141999();


function bar() {
    global $test141999;
    echo $test141999->foo();
}
?>