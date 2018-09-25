<?php

namespace A;

class Foo {
    public static function stMethod() {}

    public function inMethod() {}
}

namespace C;

use A\Foo;

class Baz {

    public function __construct() {
        Foo::stMethod();
        $f = new Foo();
        $f->inMethod();
        $retFoo = $this->retFoo();
        $retFoo->inMethod();
        $retFoo2 = $this->retFoo2();
        $retFoo2->inMethod();
    }

    private function retFoo() {
        return new Foo();
    }

    /**
     *
     * @return Foo
     */
    private function retFoo2() {
    }

}

?>