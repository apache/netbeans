<?php
namespace My\Firm;

function foo() {
}

$int = 10;
$anon = new class($int, foo()) {
    private $prop1;
    private $prop2;

    public function __construct(int $number, $mixed) {
        $this->prop1 = $number;
        $this->prop2 = $mixed;
    }
};
