<?php
class ParentClass {
}

class Foo extends ParentClass {

    function functionName($param) {
        parent
        ::class;
        self:: class;
        static::
            class;
    }
}

$foo = new Foo;
$foo->functionName($param);

echo Foo::
    class;
