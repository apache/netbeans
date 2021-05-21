<?php

namespace A;

class Foo {
    const CON = 1;
    public static $stField;
    public $pubField;
}

namespace C;

use A\Foo;

class Baz {

    public function __construct() {
        Foo::$stField;
        Foo::CON;
        $f = new Foo();
        $f->pubField;
    }

}

?>