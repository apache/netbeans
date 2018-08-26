<?php

class Bar {
    /**
     * PHPdoc for getBar()
     */
    function bar() {}
    /**
     * Returns Foo object
     * @return Foo
     */
    function getFoo() {}
}

class Foo {
    function foo() {
        $a = $this->getBar();
        $t = $this->getBar();
        $th = $this->getBar();
        $thi = $this->getBar();
        $thisA = $this->getBar();

        $a->bar();// gives autocompletion
        $thisA->bar();
        // does not give autocompletion
        $t->bar();
        $th->bar();
        $thi->bar();
    }
    /**
     * @return Bar
     */
    function getBar() {}
}