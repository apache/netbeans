<?php
    class test {

        private function f1() {
            return 0;
        }

        private function f2() {
            return 1;
        }

        public function f3() {
            return $this->f1();
        }
    }

?>