<?php

class SomeClass {
    /**
     * @return self
     */
    public static function create() {
        return new self();
    }

    function someFnc($param) {}
}

$c = SomeClass::create();
$c->; // CC shows methods from SomeClass

?>