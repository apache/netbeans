<?php
trait BaseTrait
{
    private $usedField;
    private $unusedField;
    private static $usedStaticField;
    private static $unusedStaticField;

    private function usedMethod1() {
    }

    private function unusedMethod() {
    }

    private static function usedStaticMethod() {
    }

    private static function unusedStaticMethod() {
    }

    protected function protectedMethod() {
    }

    public function testMethod() {
        $this->usedMethod1();
        $this->usedMethod2();
        BaseTrait::usedStaticMethod();
        $this->usedField = "used";
        self::$usedStaticField = "used static";
    }

    private function usedMethod2() {
    }

}
