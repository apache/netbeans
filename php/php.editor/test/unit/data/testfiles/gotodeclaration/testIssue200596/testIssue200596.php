<?php

namespace Foo\Bar {

    class AliasedClassName {
        const FOO = 1;
        public static $foo;
        static function foo() {}
    }
}


namespace {

    use \Foo\Bar as Omg;
    use \Foo\Bar\AliasedClassName as Cls;

    class MyCls {
        function __construct() {
            (new Omg\AliasedClassName())->bar();
            (new Cls())->bar();
            new Omg\AliasedClassName();
            new Cls();
            Omg\AliasedClassName::foo();
            Cls::foo();
            Omg\AliasedClassName::FOO;
            Cls::FOO;
            Omg\AliasedClassName::$foo;
            Cls::$foo;
            if ($x instanceof Omg\AliasedClassName) {}
            if ($x instanceof Cls) {}
        }
    }
}
?>