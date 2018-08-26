<?php
namespace {

    $index = 0;
    const GLOBAL_CONSTANT1 = [0, 1];
    const GLOBAL_CONSTANT2 = GLOBAL_CONSTANT1[0];
    const GLOBAL_CONSTANT3 = GLOBAL_CONSTANT1[GLOBAL_CONSTANT1[0] + GLOBAL_CONSTANT1[0]];
    const GLOBAL_CONSTANT4 = ["a" => [0, 1], "b" => ["c", "d"]];
    const GLOBAL_CONSTANT5 = GLOBAL_CONSTANT4["b"][GLOBAL_CONSTANT1[1]];
    GLOBAL_CONSTANT1[$index];
    [1][GLOBAL_CONSTANT1[0]];
    echo GLOBAL_CONSTANT4["a"][GLOBAL_CONSTANT1[$index]];

}

namespace Foo {
    use Bar\ConstantInterface;
    class ConstantClass implements ConstantInterface {

        const CLASS_CONSTANT1 = ["a", "b"];
        const CLASS_CONSTANT2 = self::CLASS_CONSTANT1[0];
        const CLASS_CONSTANT3 = GLOBAL_CONSTANT1[0] + GLOBAL_CONSTANT1[1];
        const CLASS_CONSTANT4 = [0, 1];

        public function test() {
            $index = 0;
            self::CLASS_CONSTANT1[GLOBAL_CONSTANT1[1]];
            self::CLASS_CONSTANT1[ConstantClass::CLASS_CONSTANT4[self::CLASS_CONSTANT4[0]]];
        }
    }

    ConstantClass::CLASS_CONSTANT1[$index];
    "String"[ConstantClass::CLASS_CONSTANT4[0]];

    ConstantInterface::INTERFACE_CONSTANT1[GLOBAL_CONSTANT1[1]];

}

namespace Bar {

    interface ConstantInterface {

        const INTERFACE_CONSTANT1 = ["a", "b"];
        const INTERFACE_CONSTANT2 = self::INTERFACE_CONSTANT1[0];
        const INTERFACE_CONSTANT3 = ConstantInterface::INTERFACE_CONSTANT1[0] . GLOBAL_CONSTANT1[1];

    }

}
