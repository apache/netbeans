<?php

namespace My\Sub;

class Foo {

    public $publicFooField;
    private $privateFooField;
    protected $protectedFooField;
    public static $publicStaticFooField;
    private static $privateStaticFooField;
    protected static $protectedStaticFooField;

    public function publicFooMethod() {
    }

    private function privateFooMethod() {
    }

    protected function protectedFooMethod() {
    }

    public static function publicStaticFooMethod() {
    }

    private static function privateStaticFooMethod() {
    }

    protected static function protectedStaticFooMethod() {
    }

}

class NullableType {

    public function classReturnType(): ?Foo { // CC
    }

    public static function classReturnTypeStatic(): ?Foo { // CC
    }

    public function classParameterType(?Foo $foo, ?string $stirng) { // CC
        $foo->publicFooMethod(); // CC class
    }

    public static function classParameterTypeStatic(?Foo $foo, ?string $stirng) { // CC
        $foo->publicFooMethod(); // CC class static
    }

}

trait NullableTypeTrait {

    public function traitReturnType(): ?Foo { // CC
    }

    public static function traitReturnTypeStatic(): ?\My\Sub\Foo { // CC
    }

    public function traitParameterType(?\My\Sub\Foo $foo, ?string $stirng): Foo { // CC
        $foo->publicFooMethod(); // CC trait
    }

    public static function traitParameterTypeStatic(?Foo $foo, ?string $stirng) { // CC
        $foo->publicFooMethod(); // CC trait static
    }

}

interface NullableTypeInterface {

    public function interfaceReturnType(?string $string): ?Foo; // CC

    public static function interfaceReturnTypeStatic(): ?Foo; // CC

    public function interfaceParameterType(?Foo $foo, ?string $stirng); // CC

    public static function interfaceParameterTypeStatic(?Foo $foo, ?string $stirng): ?string; // CC

}

$nullableType = new NullableType();
$nullableType->classReturnType()->publicFooMethod(); // CC

NullableType::classReturnTypeStatic()->publicFooMethod(); // CC
NullableTypeTrait::traitReturnTypeStatic()->publicFooMethod(); // CC
NullableTypeInterface::interfaceReturnTypeStatic()->publicFooMethod(); // CC

function returnType(?Foo $foo): ?Foo { // CC
}

function returnType2(?Foo $foo): ?iterable { // CC
}

function parameterType(?Foo $foo): Foo { // CC
    $foo->publicFooMethod(); // CC function
}

returnType()->publicFooMethod(); //CC
