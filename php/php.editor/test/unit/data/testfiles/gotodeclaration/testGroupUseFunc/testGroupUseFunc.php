<?php

namespace A;

function fa() {
    echo __FUNCTION__ . PHP_EOL;
}

namespace A\B;

function fab() {
    echo __FUNCTION__ . PHP_EOL;
}

namespace A\B\C;

function fabc() {
    echo __FUNCTION__ . PHP_EOL;
}

namespace Run;

use function A\{
    fa,
    B\fab,
    B\C\fabc,
    B\C\fabc AS MyFabc
};

echo fa();
echo fab();
echo fabc();
echo MyFabc();
