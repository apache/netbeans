<?php
namespace Test204925_02_P {
    class Test204925_02_A { function foo(){} }
}

namespace Test204925_02_C {
    class Test204925_02_A { function bar(){} }
    class Test204925_02_B extends \Test204925_02_C\Test204925_02_A {
        /**
         * @return Test204925_02_A
         */
        function bar(){}
    }
}

namespace {
    $test = new \Test204925_02_C\Test204925_02_B();
    $test->bar()->
}
?>