<?php

class AA {
    const LETTER = 'a';

    public function testNonStatic() {

    }

    public static function testStatic() {
        
    }
    
    public function display() {
        
        echo static::LETTER."\n";
        echo self::LETTER . "\n";
        echo AA::LETTER."\n";
    }

}

$a = new AA();
$a->display();

class A {
    const LETTER = 'a';

    public function getB() {
        
    }

    public static function who() {
        echo __CLASS__;
    }

    public static function test() {

        static::who(); // Here comes Late Static Bindings
    }

}

class B extends A {

    public static function who() {
        echo __CLASS__;
    }

}

B::test();
?>
