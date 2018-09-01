<?php
$index = 0;
const CONSTANT1 = [0, 1];
const CONSTANT2 = CONSTANT1[0];
const CONSTANT3 = "String"[0] + "String"[1];
const CONSTANT4 = [1, 2][0];
const CONSTANT5 = CONSTANT1[CONSTANT1[0] + CONSTANT1[0]];
const CONSTANT6 = ["a" => [0, 1], "b" => ["c", "d"]];
const CONSTANT7 = CONSTANT6["b"][CONSTANT1[1]];

CONSTANT1[0];
CONSTANT1[$index];
echo CONSTANT6["a"][CONSTANT1[1]];
echo CONSTANT7;

class ConstantClass {

    const CLASS_CONSTANT1 = ["a", "b"];
    const CLASS_CONSTANT2 = self::CLASS_CONSTANT1[0];
    const CLASS_CONSTANT3 = CONSTANT1[0] + CONSTANT1[1];
    const CLASS_CONSTANT4 = array("foo" => array("one", "two"), "bar" => array("three", "four"));
    const CLASS_CONSTANT5 = ConstantClass::CLASS_CONSTANT4["foo"][0];

    public function test() {
        $index = 0;
        self::CLASS_CONSTANT1[CONSTANT1[1]];
        self::CLASS_CONSTANT1[$index];
    }
}

ConstantClass::CLASS_CONSTANT1[0];
ConstantClass::CLASS_CONSTANT1[$index];
echo ConstantClass::CLASS_CONSTANT5;

interface ConstantInterface {

    const INTERFACE_CONSTANT1 = ["a", "b"];
    const INTERFACE_CONSTANT2 = self::INTERFACE_CONSTANT1[0];
    const INTERFACE_CONSTANT3 = CONSTANT1[0] + CONSTANT1[1];

}
