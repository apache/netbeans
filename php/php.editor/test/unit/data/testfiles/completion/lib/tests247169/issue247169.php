<?php

trait T {

    public static function getTraitSelf() {
        return new self();
    }

    public static function getTraitStatic() {
        return new static();
    }

    public function publicTMethod() {
        echo "publicTMethod()" . PHP_EOL;
    }

}

class A {

    public static function getClassSelf() {
        return new self();
    }

    public static function getClassStatic() {
        return new static();
    }

    public function publicAMethod() {
        echo "publicAMethod()" . PHP_EOL;
    }

}

class B extends A {

    use T;

    public function publicBMethod() {
        echo "publicBMethod()" . PHP_EOL;
    }

}

$traitSelf = B::getTraitSelf();
$traitSelf->publicBMethod(); // test

$traitStatic = B::getTraitStatic();
$traitStatic->publicTMethod(); // test

echo get_class(A::getClassSelf()) . PHP_EOL;   // A
echo get_class(A::getClassStatic()) . PHP_EOL; // A
echo get_class(B::getClassSelf()) . PHP_EOL;   // A
echo get_class(B::getClassStatic()) . PHP_EOL; // B
echo get_class(B::getTraitSelf()) . PHP_EOL;   // B
echo get_class(B::getTraitStatic()) . PHP_EOL; // B
