<?php
interface NamespaceScopeTestInterface {
    
}

class NamespaceScopeTestClass {
    
}

trait NamespaceScopeTestTrait {
    
}

return new class($a, $b) extends NamespaceScopeTestClass implements NamespaceScopeTestInterface {
    use NamespaceScopeTestTrait;
    const CONSTANT = "CONSTANT";
    public $publicField;
    private $privateField;
    protected $protectedField;
    public static $publicStaticField;
    private static $privateStaticField;
    protected static $protectedStaticField;

    public function __construct($a, $b){
    }

    public function publicMethod() {
    }

    private function privateMethod() {
    }

    protected function protectedMethod() {
    }

    public static function publicStaticMethod() {
    }

    private static function privateStaticMethod() {
    }

    protected static function protectedStaticMethod() {
    }
};

$instance = new class {
    public function test() {
        return new class {
            public function nested() {
            }
        };
    }
};
