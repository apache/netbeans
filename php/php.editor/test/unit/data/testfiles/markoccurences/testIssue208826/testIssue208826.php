<?php
namespace {

    class Model {}

    class ClassName {

        /**
         *
         * @var \Model
         */
        private $m;
        /**
         * @return \Model
         */
        function foo() {
        }

    }

}
/////////////////////////////

namespace A\B {

class Bag {}

}

namespace Foo {

    use \A\B;

    class Bar {

        /**
         *
         * @param B\Bag $param
         */
        function functionName1(B\Bag $param) {

        }
    }

}
?>