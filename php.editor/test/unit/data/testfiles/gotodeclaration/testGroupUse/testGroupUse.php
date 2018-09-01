<?php

namespace A;

interface Iface {
}

class ClsA {
    public function test() { // ClsA
        echo 'ClsA' . PHP_EOL;
    }
}

class ClsAB implements Iface {
}

class MyCls implements Iface {
}

namespace A\B;

class ClsAB {
    public function test() { // ClsAB
        echo 'ClsAB' . PHP_EOL;
    }
}

namespace A\B\C;

class ClsABC {
    public function test() { // ClsABC
        echo 'ClsABC' . PHP_EOL;
    }
}

class ClsABC2 {
    public function test() { // ClsABC2
        echo 'ClsABC2' . PHP_EOL;
    }
}

namespace Run;

use A\{
    ClsA,
    B\ClsAB,
    B\C\ClsABC,
    B\C\ClsABC2 AS MyCls
};

$a = new ClsA();
$a->test();
$ab = new ClsAB();
$ab->test();
$abc = new ClsABC();
$abc->test();
$mycls = new MyCls();
$mycls->test();
