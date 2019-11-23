<?php

namespace Foo\Bar {
    class ClassName {
        const BAR = 2;
        public static $bar;
        static function bar() {}
    }

    class AliasedClassName {
        const FOO = 1;
        public static $foo;
        static function foo() {}
    }
}


namespace {

    use \Foo\Bar as Omg;
    use \Foo\Bar\AliasedClassName as Cls;
    use \Foo\Bar\ClassName;

    class MyCls {
        function __construct() {
            (new Omg\AliasedClassName())->bar();
            (new Cls())->bar();
            (new ClassName())->bar();
            new Omg\AliasedClassName();
            new Cls();
            new ClassName();
            Omg\AliasedClassName::foo();
            Cls::foo();
            ClassName::bar();
            Omg\AliasedClassName::FOO;
            Cls::FOO;
            ClassName::BAR;
            Omg\AliasedClassName::$foo;
            Cls::$foo;
            ClassName::$bar;
            if ($x instanceof Omg\AliasedClassName) {}
            if ($x instanceof Cls) {}
            if ($x instanceof ClassName) {}
        }
    }
}
?>