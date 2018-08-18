<?php
namespace Name\Space {
    const FOO = 42;
    function fnc() { echo __FUNCTION__."\n"; }
}

namespace {
    use const Name\Space\FOO;
    use function Name\Space\fnc;

    echo FOO."\n";
    fnc();
}
?>