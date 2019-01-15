<?php

trait Foo {
    function fnc(); //Foo
}

trait Bar {
    function fnc(); //Bar
}

trait Baz {
    function fnc(); //Baz
}

class Cls {
    use Foo, Bar, Baz {
        Foo::fnc insteadof Bar, Baz;
        Bar::fnc as aliased;
    }

}

?>