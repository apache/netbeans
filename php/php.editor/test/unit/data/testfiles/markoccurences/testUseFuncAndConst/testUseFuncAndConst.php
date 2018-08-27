<?php

namespace Name\Space {
    const FOO = 42;
    function fnc() {}
}

namespace {
use const Name\Space\FOO;
use const Name\Space\FOO as FOO2;
use function Name\Space\fnc;
use function Name\Space\fnc as fnc2;

echo FOO;
echo FOO2;
fnc();
fnc2();
}
?>