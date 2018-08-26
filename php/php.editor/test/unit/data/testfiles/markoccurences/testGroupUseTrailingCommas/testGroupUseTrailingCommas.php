<?php

namespace A;

class Foo {
    
}

class Bar {
    
}

namespace A\B;

class Baz {
    
}

namespace C;

use A\{
    Foo,
    Bar,
    B\Baz,
};

$foo = new Foo();
$bar = new Bar();
$baz = new Baz();
