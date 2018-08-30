<?php
namespace My;

$anon = new class {
    public function testAnon() {
        $this->testBnon();
    }
// magic methods
    private function testBnon() {
        echo 'testBnon' . PHP_EOL;
    }
};
$anon->testAnon();
