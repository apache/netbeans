<?php

namespace {

    class Exception {
        function testMethod() {
        }
    }

}

namespace Yaaaaaa {
    class ExcText {
        function functionName() {
            try {
            } catch (\Exception $e) {
                $e->testMethod();
            }
        }
    }
}

namespace Ywwwwww {
    use Exception;
    class ExcText {
        function functionName() {
            try {
            } catch (Exception $ee) {
                $ee->testMethod();
            }
        }
    }
}

?>