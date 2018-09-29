<?php
namespace Test204925_03_P {
    class Test204925_03_A { function foo(){} }
}

namespace Test204925_03_C {
    class Test204925_03_A { function bar(){} }
    class Test204925_03_B extends \Test204925_03_C\Test204925_03_A {
        /**
         * @return Test204925_03_A
         */
        function bar(){
            return new Test204925_03_A();
        }
    }
}

namespace {
    $test = new \Test204925_03_C\Test204925_03_B();
    $test->bar()->
}
?>