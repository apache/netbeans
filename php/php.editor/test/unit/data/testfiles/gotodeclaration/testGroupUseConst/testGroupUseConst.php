<?php

namespace A;

const CA = 'CA';

namespace A\B;

const CAB = 'CAB';

namespace A\B\C;

const CABC = 'CABC';

namespace Run;

use const A\{
    CA,
    B\CAB,
    B\C\CABC,
    B\C\CABC AS MyCABC
};

echo CA . PHP_EOL;
echo CAB . PHP_EOL;
echo CABC . PHP_EOL;
echo MyCABC . PHP_EOL;
