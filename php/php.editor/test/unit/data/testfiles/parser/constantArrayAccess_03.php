<?php

class ConstantClass {

    private static $index = 0;
    const CLASS_CONSTANT1 = ["a", "b"];
    const CLASS_CONSTANT2 = self::CLASS_CONSTANT1[self::$index];

}

