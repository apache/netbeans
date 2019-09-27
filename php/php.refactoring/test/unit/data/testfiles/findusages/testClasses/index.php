<?php

namespace A;

class Foo {
    const CON = 1;
    public static $stField;
    public static function stMethod() {}
}

namespace B;

use A\Foo;

class Bar extends Foo {

}

namespace C;

use A\Foo;

class Baz {

    public function __construct() {
        Foo::stMethod();
        Foo::$stField;
        Foo::CON;
    }

}

?>