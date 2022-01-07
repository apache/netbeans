<?php

namespace Test;

trait BarTrait {

    public int $publicBarTraitField;
    private int $privateBarTraitField;
    protected int $protectedBarTraitField;

    public static int $publicStaticBarTraitField;
    private static int $privateStaticBarTraitField;
    protected static int $protectedStaticBarTraitField;

    public function publicBarTraitMethod(int $param): void {
    }

    private function privateBarTraitMethod(int $param1, string $param2): void {
    }

    protected function protectedBarTraitMethod(int $param1, string $param2): void {
    }

    public static function publicStaticBarTraitMethod(int $param): void {
    }

    private static function privateStaticBarTraitMethod(int $param1, string $param2): void {
    }

    protected static function protectedStaticBarTraitMethod(int $param1, string $param2): void {
    }
}
