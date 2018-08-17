<?php
namespace Test204925_08_P\a\b\c {

    class Test204925_08_A { function foo(){} }

}

namespace Test204925_08_C\d\e\f {

    class Test204925_08_A { function bar(){} }

    class Test204925_08_B extends \Test204925_08_C\d\e\f\Test204925_08_A {
        /**
         * @return \Test204925_08_P\a\b\c\Test204925_08_A
         */
        function bar(){

        }
    }
}

namespace {
    $test = new \Test204925_08_C\d\e\f\Test204925_08_B();
    $test->bar()->
}
?>