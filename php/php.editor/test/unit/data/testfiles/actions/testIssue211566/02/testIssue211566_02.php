<?php

namespace Foo\Bar;

class Baz {

    function __construct() {

    }

}

namespace Omg;

class ClassName {

    function __construct() {
        new \Foo\Bar\Baz(); //HERE
    }

}
?>