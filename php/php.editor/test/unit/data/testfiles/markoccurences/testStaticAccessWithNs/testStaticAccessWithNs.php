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
            Omg\AliasedClassName::foo();
            Cls::foo();
            ClassName::bar();
            Omg\AliasedClassName::FOO;
            Cls::FOO;
            ClassName::BAR;
            Omg\AliasedClassName::$foo;
            Cls::$foo;
            ClassName::$bar;
            \Foo\Bar\ClassName::$bar;
            \Foo\Bar\ClassName::bar();
            \Foo\Bar\ClassName::BAR;
        }
    }
}
?>