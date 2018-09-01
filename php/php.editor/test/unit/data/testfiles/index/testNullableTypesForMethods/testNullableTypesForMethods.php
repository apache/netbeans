<?php
class NullableTypes {

    public function parameterType(?string $param) {
    }

    public static function parameterTypeStatic(?string $param) {
    }

    public function returnType(int $num) : ?\Foo {
    }

    public static function returnTypeStatic (int $num) : ?Foo {
    }

}
