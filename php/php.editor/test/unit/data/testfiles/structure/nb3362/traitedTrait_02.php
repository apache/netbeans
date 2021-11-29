<?php

namespace Test;

trait FooBarTrait {

    use FooTrait, BarTrait;

    public int $publicFooBarTraitField;
    private int $privateFooBarTraitField;
    protected int $protectedFooBarTraitField;

    public static int $publicStaticFooBarTraitField;
    private static int $privateStaticFooBarTraitField;
    protected static int $protectedStaticFooBarTraitField;

    public function publicFooBarTraitMethod(int $param): void {
    }

    private function privateFooBarTraitMethod(int $param1, string $param2): void {
    }

    protected function protectedFooBarTraitMethod(int $param1, string $param2): void {
    }

    public static function publicStaticFooBarTraitMethod(int $param): void {
    }

    private static function privateStaticFooBarTraitMethod(int $param1, string $param2): void {
    }

    protected static function protectedStaticFooBarTraitMethod(int $param1, string $param2): void {
    }

    // override
    public function publicFooTraitMethod(int $param): void {
    }

    protected static function privateStaticBarTraitMethod(int $param1, string $param2): void {
    }

}
