<?php

class ParentA {
    const PARENT_CONST = 'PARENT_CONST';
    public static $parentFieldStatic = 'parentFieldStatic';
    // unused, cannot be called via 'parent'
    //public $parentFieldInstance = 'parentFieldInstance';
    public function parentTestInstance() {
        return 'parentTestInstance';
    }
    public static function parentTestStatic() {
        return 'parentTestStatic';
    }
}

class A extends ParentA {
    const MY_CONST = 'MY_CONST';
    public static $myFieldStatic = 'myFieldStatic';
    public $myFieldInstance = 'myFieldInstance';
    public function myTestInstance() {
        return 'myTestInstance';
    }
    public static function myTestStatic() {
        return 'myTestStatic';
    }

    public function test() {
        $this->filter(['a']);
    }

    public function filter(array $values) {
        return array_filter($values, function ($val) {
            // parent
            echo parent::PARENT_CONST . PHP_EOL;
            // invalid, not a constant
            // echo parent::parentFieldInstance . PHP_EOL;
            echo parent::$parentFieldStatic . PHP_EOL;
            echo parent::parentTestInstance() . PHP_EOL;
            echo parent::parentTestStatic() . PHP_EOL;
            // me
            echo $this->myFieldInstance . PHP_EOL;
            echo $this->myTestInstance() . PHP_EOL;
            echo self::MY_CONST . PHP_EOL;
            echo self::$myFieldStatic . PHP_EOL;
            echo self::myTestStatic() . PHP_EOL;
            return true;
        });
    }
}
(new A())->test();
