<?php

namespace Foo\Bar {
    class AliasedClassName {
        public function objFoo();
    }
}


namespace {

    use \Foo\Bar as Omg;
    use \Foo\Bar\AliasedClassName as Cls;

    class MyCls {
        function __construct() {
            (new Omg\AliasedClassName())->objFoo();
            (new \Foo\Bar\AliasedClassName())->objFoo();
            (new Cls())->objFoo();
        }
    }
}
?>