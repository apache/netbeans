<?php
namespace Test204925_04_P {
    class Test204925_04_A { function foo(){} }
}

namespace Test204925_04_C {
    class Test204925_04_A { function bar(){} }
    class Test204925_04_B extends \Test204925_04_C\Test204925_04_A {
        /**
         * @return Test204925_04_A
         */
        function bar(){
            $a = new Test204925_04_A();
            return $a;
        }
    }
}

namespace {
    $test = new \Test204925_04_C\Test204925_04_B();
    $test->bar()->
}
?>