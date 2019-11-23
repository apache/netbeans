<?php
abstract class base {
    function show() {
        echo "base\n";
    }

    abstract function test();
}

abstract class animal extends base {};

class derived extends base {
    function test() {
        return 0;
    }
}

class fail {
	abstract function show();
}
?>