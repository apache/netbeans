<?php

trait DeprecatedFooTrait {

    /** @deprecated */
    public int $publicFooTraitField;
    /** @deprecated */
    private int $privateFooTraitField;
    /** @deprecated */
    protected int $protectedFooTraitField;

    /** @deprecated */
    public static int $publicStaticFooTraitField;
    /** @deprecated */
    private static int $privateStaticFooTraitField;
    /** @deprecated */
    protected static int $protectedStaticFooTraitField;

    /** @deprecated */
    public function publicFooTraitMethod(int $param): void {
    }

    /** @deprecated */
    private function privateFooTraitMethod(int $param1, string $param2): void {
    }

    /** @deprecated */
    protected function protectedFooTraitMethod(int $param1, string $param2): void {
    }

    /** @deprecated */
    public static function publicStaticFooTraitMethod(int $param): void {
    }

    /** @deprecated */
    private static function privateStaticFooTraitMethod(int $param1, string $param2): void {
    }

    /** @deprecated */
    protected static function protectedStaticFooTraitMethod(int $param1, string $param2): void {
    }
}

class DeprecatedParentClass {
    /** @deprecated */
    const IMPLICIT_PUBLIC_PARENT_CONST = "parent";
    /** @deprecated */
    public const PUBLIC_PARENT_CONST = "parent";
    /** @deprecated */
    private const PRIVATE_PARENT_CONST = "parent";
    /** @deprecated */
    protected const PROTECTED_PARENT_CONST = "parent";

    /** @deprecated */
    public int $publicParentClassField;
    /** @deprecated */
    private int $privateParentClassField;
    /** @deprecated */
    protected int $protectedParentClassField;

    /** @deprecated */
    public static int $publicStaticParentClassField;
    /** @deprecated */
    private static int $privateStaticParentClassField;
    /** @deprecated */
    protected static int $protectedStaticParentClassField;

    /** @deprecated */
    public function publicParentClassMethod(int $param): void {
    }

    /** @deprecated */
    private function privateParentClassMethod(int $param1, string $param2): void {
    }

    /** @deprecated */
    protected function protectedParentClassMethod(int $param1, string $param2): void {
    }

    /** @deprecated */
    public static function publicStaticParentClassMethod(int $param): void {
    }

    /** @deprecated */
    private static function privateStaticParentClassMethod(int $param1, string $param2): void {
    }

    /** @deprecated */
    protected static function protectedStaticParentClassMethod(int $param1, string $param2): void {
    }
}


class ChildClass extends DeprecatedParentClass
{
    use DeprecatedFooTrait;
}
