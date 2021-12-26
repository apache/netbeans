<?php

namespace Test;

class ChildClass extends ParentClass {

    use FooTrait;

    const IMPLICIT_PUBLIC_CHILD_CONST = "child";
    public const PUBLIC_CHILD_CONST = "child";
    private const PRIVATE_CHILD_CONST = "child";
    protected const PROTECTED_CHILD_CONST = "child";
    // override
    public const PUBLIC_PARENT_CONST = "child";

    public int $publicChildClassField;
    private int $privateChildClassField;
    protected int $protectedChildClassField;
    // override
    protected int $protectedParentClassField;

    public static int $publicStaticChildClassField;
    private static int $privateStaticChildClassField;
    protected static int $protectedStaticChildClassField;
    // override
    public static int $publicStaticParentClassField;

    public function publicChildClassMethod(int $param): void {
    }

    private function privateChildClassMethod(int $param1, string $param2): void {
    }

    protected function protectedChildClassMethod(int $param1, string $param2): void {
    }

    public static function publicStaticChildClassMethod(int $param): void {
    }

    private static function privateStaticChildClassMethod(int $param1, string $param2): void {
    }

    protected static function protectedStaticChildClassMethod(int $param1, string $param2): void {
    }

    // override
    private function privateFooTraitMethod(int $param1, string $param2): void {
    }

    protected function protectedParentClassMethod(int $param1, string $param2): void {
    }

    public static function publicStaticFooTraitMethod(int $param): void {
    }

    public static function publicStaticParentClassMethod(int $param): void {
    }
}
