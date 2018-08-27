<?php

namespace Foo\Bar {
    class AliasedClassName {}
}

namespace {

    use \Foo\Bar as Omg;
    use \Foo\Bar\AliasedClassName as Cls;

    class ClassName {

        /** @var Cls */
        public $cls;

        /** @var Omg\AliasedClassName */
        public $omg;

        /**
         * @return Omg\AliasedClassName
         * @throws Omg\AliasedClassName
         * @throws Cls
         */
        function foo() {}

        /**
         * @param Omg\AliasedClassName $p
         * @param Cls $a
         * @param \Foo\Bar\AliasedClassName $name Description
         * @return Cls
         */
        function bar(Omg\AliasedClassName $p, Cls $a, \Foo\Bar\AliasedClassName $name) {}
    }
}
?>