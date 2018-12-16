<?php

trait T {

    /**
     * @var B
     */
    public static $staticFied;

    public static function test() {
        echo "test()" . PHP_EOL;

        $a = self::getA();
        $a->publicAMethod(); // test

        self::$staticFied = new B();
        $b = self::$staticFied;
        $b->publicBMethod(); // test
    }

    public static function getA() {
        return new A();
    }

}

class A {

    public function publicAMethod() {
        echo "publicAMethod()" . PHP_EOL;
    }

}

class B {

    public function publicBMethod() {
        echo "publicBMethod()" . PHP_EOL;
    }

}

class C {

    use T;
}

$c = new C();
$c->test();
