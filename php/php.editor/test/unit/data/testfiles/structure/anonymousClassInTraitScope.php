<?php
interface TraitScopeTestInterface {
    
}

trait TraitScopeTestTrait {
    
}

class TraitScopeClass {
    
}

trait TraitScopeTest {

    public function test1() {
        return new class($a, $b) extends TraitScopeTest implements TraitScopeTestInterface {
            use TraitScopeTestTrait;
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
    }

    public function test2() {
        return new class implements TraitScopeTestInterface2 { // interface doesn't exist
            public function publicMethod() {
            }
        };
    }

    public function test3() {
        $instance = new class {
            public function test() {
                return new class {
                    public function nested() {
                    }
                };
            }
        };
    }
}
