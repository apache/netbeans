<?php
namespace Test204925_06_P\a\b\c {

    class Test204925_06_A { function foo(){} }

}

namespace Test204925_06_C\d\e\f {

    use \Test204925_06_P\a\b;

    class Test204925_06_A { function bar(){} }

    class Test204925_06_B extends \Test204925_06_C\d\e\f\Test204925_06_A {
        /**
         * @return b\c\Test204925_06_A
         */
        function bar(){

        }
    }
}

namespace {
    $test = new \Test204925_06_C\d\e\f\Test204925_06_B();
    $test->bar()->
}
?>