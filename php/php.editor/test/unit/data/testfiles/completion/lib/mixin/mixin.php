<?php

/**
 * @mixin C2
 */
class C1
{
    public const PUBLIC_CONST_C1 = "PUBLIC_CONST_C1";
    private const PRIVATE_CONST_C1 = "PRIVATE_CONST_C1";
    protected const PROTECTED_CONST_C1 = "PROTECTED_CONST_C1";

    public $publicFieldC1;
    private $privateFieldC1;
    protected $protectedFieldC1;

    public static $publicStaticFieldC1;
    private static $privateStaticFieldC1;
    protected static $protectedStaticFieldC1;

    public function publicMethodC1()
    {
    }

    private function privateMethodC1()
    {
    }

    protected function protectedMethodC1()
    {
    }

    public static function publicStaticMethodC1()
    {
    }

    private static function privateStaticMethodC1()
    {
    }

    protected static function protectedStaticMethodC1()
    {
    }
}

class C2
{
    public const PUBLIC_CONST_C2 = "PUBLIC_CONST_C2";
    private const PRIVATE_CONST_C2 = "PRIVATE_CONST_C2";
    protected const PROTECTED_CONST_C2 = "PROTECTED_CONST_C2";

    public $publicFieldC2;
    private $privateFieldC2;
    protected $protectedFieldC2;

    public static $publicStaticFieldC2;
    private static $privateStaticFieldC2;
    protected static $protectedStaticFieldC2;

    public function publicMethodC2()
    {
    }

    private function privateMethodC2()
    {
    }

    protected function protectedMethodC2()
    {
    }

    public static function publicStaticMethodC2()
    {
    }

    private static function privateStaticMethodC2()
    {
    }

    protected static function protectedStaticMethodC2()
    {
    }
}

class C3
{
    public const PUBLIC_CONST_C3 = "PUBLIC_CONST_C3";
    private const PRIVATE_CONST_C3 = "PRIVATE_CONST_C3";
    protected const PROTECTED_CONST_C3 = "PROTECTED_CONST_C3";

    public $publicFieldC3;
    private $privateFieldC3;
    protected $protectedFieldC3;

    public static $publicStaticFieldC3;
    private static $privateStaticFieldC3;
    protected static $protectedStaticFieldC3;

    public function publicMethodC3()
    {
    }

    private function privateMethodC3()
    {
    }

    protected function protectedMethodC3()
    {
    }

    public static function publicStaticMethodC3()
    {
    }

    private static function privateStaticMethodC3()
    {
    }

    protected static function protectedStaticMethodC3()
    {
    }
}

class C4
{
    public const PUBLIC_CONST_C4 = "PUBLIC_CONST_C4";
    private const PRIVATE_CONST_C4 = "PRIVATE_CONST_C4";
    protected const PROTECTED_CONST_C4 = "PROTECTED_CONST_C4";

    public $publicFieldC4;
    private $privateFieldC4;
    protected $protectedFieldC4;

    public static $publicStaticFieldC4;
    private static $privateStaticFieldC4;
    protected static $protectedStaticFieldC4;

    public function publicMethodC4()
    {
    }

    private function privateMethodC4()
    {
    }

    protected function protectedMethodC4()
    {
    }

    public static function publicStaticMethodC4()
    {
    }

    private static function privateStaticMethodC4()
    {
    }

    protected static function protectedStaticMethodC4()
    {
    }
}

class C5
{
    public const PUBLIC_CONST_C5 = "PUBLIC_CONST_C5";
    private const PRIVATE_CONST_C5 = "PRIVATE_CONST_C5";
    protected const PROTECTED_CONST_C5 = "PROTECTED_CONST_C5";

    public $publicFieldC5;
    private $privateFieldC5;
    protected $protectedFieldC5;

    public static $publicStaticFieldC5;
    private static $privateStaticFieldC5;
    protected static $protectedStaticFieldC5;

    public function publicMethodC5()
    {
    }

    private function privateMethodC5()
    {
    }

    protected function protectedMethodC5()
    {
    }

    public static function publicStaticMethodC5()
    {
    }

    private static function privateStaticMethodC5()
    {
    }

    protected static function protectedStaticMethodC5()
    {
    }
}

/**
 * @mixin C3
 */
class MixinParent
{
    public function testParent()
    {
    }
}

/**
 * @mixin C1
 * @mixin C4|C5
 */
class Mixin extends MixinParent
{
    public function test()
    {
        $this->protectedMethodC1(); // CC
        Mixin::protectedStaticMethodC1(); // CC
    }
}

$mixin = new Mixin();
$mixin->publicMethodC1(); // CC
Mixin::publicStaticMethodC1(); // CC
