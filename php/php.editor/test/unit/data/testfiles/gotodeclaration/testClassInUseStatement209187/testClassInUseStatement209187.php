<?php

namespace Foo\Bar {

    class ClassName {
    }

}


namespace {

use \Foo\Bar\ClassName;

    class MyCls {
        function __construct() {
            new ClassName();
        }
    }

}
?>