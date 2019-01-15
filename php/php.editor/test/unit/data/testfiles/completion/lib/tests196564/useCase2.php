<?php

class Parent7 {
    /**
     * @return this
     */
    public function foo() {
        return $this;
    }
}

class Child extends Parent7 {
    /**
     * @return Child
     */
    public function bar() {
        return $this;
    }
}

$c = new Child();
$c->; // CC shows foo() and bar()
$c->foo()->; // CC shows foo() and bar()

?>