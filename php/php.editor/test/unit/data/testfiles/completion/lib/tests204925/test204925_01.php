<?php
namespace Test204925_01_P {
    class Test204925_01_A { function foo(){} }
}

namespace Test204925_01_C {
    class Test204925_01_A { function bar(){} }
    class Test204925_01_B extends Test204925_01_A { function bar(){} }
}

namespace {
    $test = new \Test204925_01_C\Test204925_01_B();
    $test->
}
?>