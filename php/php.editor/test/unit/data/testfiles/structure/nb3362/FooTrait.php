<?php

namespace Test;

trait FooTrait {

    public int $publicFooTraitField;
    private int $privateFooTraitField;
    protected int $protectedFooTraitField;

    public static int $publicStaticFooTraitField;
    private static int $privateStaticFooTraitField;
    protected static int $protectedStaticFooTraitField;

    public function publicFooTraitMethod(int $param): void {
    }

    private function privateFooTraitMethod(int $param1, string $param2): void {
    }

    protected function protectedFooTraitMethod(int $param1, string $param2): void {
    }

    public static function publicStaticFooTraitMethod(int $param): void {
    }

    private static function privateStaticFooTraitMethod(int $param1, string $param2): void {
    }

    protected static function protectedStaticFooTraitMethod(int $param1, string $param2): void {
    }
}
