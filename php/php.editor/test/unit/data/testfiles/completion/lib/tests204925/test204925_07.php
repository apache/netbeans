<?php
namespace Test204925_07_P\a\b\c {

    class Test204925_07_A { function foo(){} }

}

namespace Test204925_07_C\d\e\f {

    use \Test204925_07_P\a\b as aliasX;

    class Test204925_07_A { function bar(){} }

    class Test204925_07_B extends \Test204925_07_C\d\e\f\Test204925_07_A {
        /**
         * @return aliasX\c\Test204925_07_A
         */
        function bar(){

        }
    }
}

namespace {
    $test = new \Test204925_07_C\d\e\f\Test204925_07_B();
    $test->bar()->
}
?>