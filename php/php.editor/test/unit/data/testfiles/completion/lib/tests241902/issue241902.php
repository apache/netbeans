<?php

namespace MyNamespace {
    use ; //ns
    class MyClass extends Object {}
}

namespace {
    class Foo {
        use ; //cls
    }
    trait FooT {
        use ; //trt
    }
}

?>