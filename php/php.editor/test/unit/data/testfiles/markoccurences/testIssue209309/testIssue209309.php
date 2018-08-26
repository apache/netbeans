<?php

namespace Foo\Bar {
    class AliasedClassName {}
}

namespace {

    use \Foo\Bar as Omg;
    use \Foo\Bar\AliasedClassName as Cls;

    class ClassName {
        function bar(Omg\AliasedClassName $p, Cls $a) {}
    }
}
?>