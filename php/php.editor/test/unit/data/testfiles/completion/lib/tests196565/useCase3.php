<?php

class ParentP {
    /**
     * @return static
     */
    public static function create() {
        return new static();
    }
}

class ChildP extends ParentP {
    function childFnc($param) {}
}

$test = ChildP::create();
$test->; // CC shows methods from both Child and Parent
?>