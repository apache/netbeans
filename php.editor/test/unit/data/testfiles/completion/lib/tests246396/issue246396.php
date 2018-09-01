<?php
trait BaseTrait
{
    public static $publicStaticField = "public static field";
    private static $privateStaticField = "private static field";
    protected static $protectedStaticField = "protected static field";

    public  $publicField = "public field";
    private  $privateField = "private field";
    protected  $protectedField = "protected field";

    public static function publicStaticMethod() {}

    private static function privateStaticMethod() {}

    protected static function protectedStaticMethod() {}

    public function publicMethod()
    {
        self::publicStaticMethod();
        static::privateStaticMethod();
    }

    public function privateMethod(){}

    public function protectedMethod(){}

}

trait TraitedTrait
{
    use BaseTrait;

    public static $publicStaticTraitedField = "public static field";
    private static $privateStaticTraitedField = "private static field";
    protected static $protectedStaticTraitedField = "protected static field";

    public  $publicTraitedField = "public field";
    private  $privateTraitedField = "private field";
    protected  $protectedTraitedField = "protected field";

    // override
    public static function publicStaticMethod() {}

    public static function publicStaticTraitedMethod() {}

    private static function privateStaticTraitedMethod() {}

    protected static function protectedStaticTraitedMethod() {}

    public function publicTraitedMethod()
    {
        self::publicStaticTraitedMethod();
        static::privateStaticTraitedMethod();
    }

    public function privateTraitedMethod(){}

    public function protectedTraitedMethod(){}

}
