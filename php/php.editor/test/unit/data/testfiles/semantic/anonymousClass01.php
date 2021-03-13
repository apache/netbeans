<?php

new class {
    private const USED_PRIVATE_CONST = 1;
    private const UNUSED_PRIVATE_CONST = 2;
    private $usedField;
    private $unusedField;
    private static $usedStaticField;
    private static $unusedStaticField;


    public function publicMethod() {
        self::USED_PRIVATE_CONST;
        $this->usedField = 10;
        self::$usedStaticField = 20;
        $this->usedPrivateMethod();
        self::usedStaticPrivateMethod();
    }
    private function usedPrivateMethod() {
    }
    private function unusedPrivateMethod() {
    }
    private static function usedStaticPrivateMethod() {
    }
    private static function unusedStaticPrivateMethod() {
    }
};
