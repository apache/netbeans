<?php
namespace A\B;

class Bag {}

namespace Foo;

use \A\B;

class Bar {

    /**
     * @param B\Bag $param
     */
    function functionName1(B\Bag $param) {

    }

    /**
     * @return B\Bag
     */
    function functionName() {
        return new B\Bag();
    }
}
?>