<?php
namespace Test204925_05_P {
    class Test204925_05_A { function foo(){} }
}

namespace Test204925_05_C {
    class Test204925_05_A { function bar(){} }
    class Test204925_05_B extends \Test204925_05_C\Test204925_05_A {
        /**
         * @return null|string|Test204925_05_A
         */
        function bar(){

        }
    }
}

namespace {
    $test = new \Test204925_05_C\Test204925_05_B();
    $test->bar()->
}
?>