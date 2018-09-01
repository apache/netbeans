<?php
var_dump(new class {
    public function testA() {
        $this->testB();
    }

    private function testB() {
        echo 'testB' . PHP_EOL;
    }
});
