<?php

class ParentA {
    const PARENT_CONST = 'PARENT_CONST';
    public static $parentFieldStatic = 'parentFieldStatic';
    protected static $parentProtectedFieldStatic = 'parentProtectedFieldStatic';
    private static $parentPrivateFieldStatic = 'parentPrivateFieldStatic';
    // unused, cannot be called via 'parent'
    //public $parentFieldInstance = 'parentFieldInstance';
    public function parentTestInstance() {
        return 'parentTestInstance';
    }
    private function parentPrivateTestInstance() {
        return 'parentPrivateTestInstance';
    }
    protected function parentProtectedTestInstance() {
        return 'parentProtectedTestInstance';
    }
    public static function parentTestStatic() {
        return 'parentTestStatic';
    }
    private static function parentPrivateTestStatic() {
        return 'parentPrivateTestStatic';
    }
    protected static function parentProtectedTestStatic() {
        return 'parentProtectedTestStatic';
    }
}

class A extends ParentA {
    const MY_CONST = 'MY_CONST';
    public static $myFieldStatic = 'myFieldStatic';
    private static $myPrivateFieldStatic = 'myPrivateFieldStatic';
    protected static $myProtectedFieldStatic = 'myProtectedFieldStatic';
    public $myFieldInstance = 'myFieldInstance';
    private $myPrivateFieldInstance = 'myPrivateFieldInstance';
    protected $myProtectedFieldInstance = 'myProtectedFieldInstance';
    public function myTestInstance() {
        return 'myTestInstance';
    }
    private function myPrivateTestInstance() {
        return 'myPrivateTestInstance';
    }
    protected function myProtectedTestInstance() {
        return 'myProtectedTestInstance';
    }
    public static function myTestStatic() {
        return 'myTestStatic';
    }
    private static function myPrivateTestStatic() {
        return 'myPrivateTestStatic';
    }
    protected static function myProtectedTestStatic() {
        return 'myProtectedTestStatic';
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
            echo parent::$parentProtectedFieldStatic . PHP_EOL;
            // not allowed
            // echo parent::$parentPrivateFieldStatic . PHP_EOL;
            echo parent::parentTestInstance() . PHP_EOL;
            echo parent::parentProtectedTestInstance() . PHP_EOL;
            // not allowed
            //echo parent::parentPrivateTestInstance() . PHP_EOL;
            echo parent::parentTestStatic() . PHP_EOL;
            echo parent::parentProtectedTestStatic() . PHP_EOL;
            // not allowed
            // echo parent::parentPrivateTestStatic() . PHP_EOL;
            // me
            echo $this->myFieldInstance . PHP_EOL;
            echo $this->myPrivateFieldInstance . PHP_EOL;
            echo $this->myProtectedFieldInstance . PHP_EOL;
            echo $this->myTestInstance() . PHP_EOL;
            echo $this->myPrivateTestInstance() . PHP_EOL;
            echo $this->myProtectedTestInstance() . PHP_EOL;
            echo self::MY_CONST . PHP_EOL;
            echo self::$myFieldStatic . PHP_EOL;
            echo self::$myPrivateFieldStatic . PHP_EOL;
            echo self::$myProtectedFieldStatic . PHP_EOL;
            echo self::myTestStatic() . PHP_EOL;
            echo self::myPrivateTestStatic() . PHP_EOL;
            echo self::myProtectedTestStatic() . PHP_EOL;
            return true;
        });
    }
}
(new A())->test();
