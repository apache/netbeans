<?php
    class Test {
        public $variable;

        public function foo() {

        }
    }

    function bar(Test &$t) {
        $t->;
    }

?>