<?php

new class {
    private $usedField;
    private $unusedField;
    private static $usedStaticField;
    private static $unusedStaticField;


    public function publicMethod() {
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
