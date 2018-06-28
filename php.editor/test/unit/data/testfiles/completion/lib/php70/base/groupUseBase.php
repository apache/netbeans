<?php
namespace A;

const C_A = 'C-A';

function fa() {
}

class ClsA {
    public function test() {
        echo 'ClsA' . PHP_EOL;
    }
}

namespace A\B;

const C_B = 'C-B';

function fb() {
}

class ClsAB {
    public function test() {
        echo 'ClsAB' . PHP_EOL;
    }
}

namespace A\B\C;

const C_C = 'C-C';

function fc() {
}

class ClsABC {
    public function test() {
        echo 'ClsABC' . PHP_EOL;
    }
}
