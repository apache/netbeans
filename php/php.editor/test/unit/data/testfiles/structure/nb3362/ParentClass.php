<?php

namespace Test;

class ParentClass {

    const IMPLICIT_PUBLIC_PARENT_CONST = "parent";
    public const PUBLIC_PARENT_CONST = "parent";
    private const PRIVATE_PARENT_CONST = "parent";
    protected const PROTECTED_PARENT_CONST = "parent";

    public int $publicParentClassField;
    private int $privateParentClassField;
    protected int $protectedParentClassField;

    public static int $publicStaticParentClassField;
    private static int $privateStaticParentClassField;
    protected static int $protectedStaticParentClassField;

    public function publicParentClassMethod(int $param): void {
    }

    private function privateParentClassMethod(int $param1, string $param2): void {
    }

    protected function protectedParentClassMethod(int $param1, string $param2): void {
    }

    public static function publicStaticParentClassMethod(int $param): void {
    }

    private static function privateStaticParentClassMethod(int $param1, string $param2): void {
    }

    protected static function protectedStaticParentClassMethod(int $param1, string $param2): void {
    }
}
