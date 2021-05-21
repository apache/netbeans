<?php
trait BaseTrait1
{
    public static $publicStaticBaseTrait1Field;
    private static $privateStaticBaseTrait1Field;
    protected static $protectedStaticBaseTrait1Field;

    public $publicBaseTrait1Field;
    private $privateBaseTrait1Field;
    protected $protectedBaseTrait1Field;

    public static function publicStaticBaseTrait1Method() {}

    private static function privateStaticBaseTrait1Method() {}

    protected static function protectedStaticBaseTrait1Method() {}

    public function publicBaseTrait1Method() {}

    private function privateBaseTrait1Method() {}

    protected function protectedBaseTrait1Method() {}
}

trait BaseTrait2
{
    public static $publicStaticBaseTrait2Field;
    private static $privateStaticBaseTrait2Field;
    protected static $protectedStaticBaseTrait2Field;

    public $publicBaseTrait2Field;
    private $privateBaseTrait2Field;
    protected $protectedBaseTrait2Field;

    public static function publicStaticBaseTrait2Method() {}

    private static function privateStaticBaseTrait2Method() {}

    protected static function protectedStaticBaseTrait2Method() {}

    public function publicBaseTrait2Method() {}

    private function privateBaseTrait2Method() {}

    protected function protectedBaseTrait2Method() {}
}

trait TraitedTrait
{
    use BaseTrait1;

    public static $publicStaticTraitedTraitField;
    private static $privateStaticTraitedTraitField;
    protected static $protectedStaticTraitedTraitField;

    public $publicTraitedTraitField;
    private $privateTraitedTraitField;
    protected $protectedTraitedTraitField;

    public function publicTraitedTraitMethod()
    {
        $this->privateTraitedTraitField;
    }

    private function privateTraitedTraitMethod() {}

    protected function protectedTraitedTraitMethod() {}

    public static function publicStaticTraitedTraitMethod()
    {
        TraitedTrait::$privateStaticBaseTrait1Field;
    }

    private static function privateStaticTraitedTraitMethod() {}

    protected static function protectedStaticTraitedTraitMethod() {}
}

trait MultipleUsedTrait
{
    use TraitedTrait, BaseTrait2;

    public static $publicStaticMultipleUsedTraitField;
    private static $privateStaticMultipleUsedTraitField;
    protected static $protectedStaticMultipleUsedTraitField;

    public $publicMultipleUsedTraitField;
    private $privateMultipleUsedTraitField;
    protected $protectedMultipleUsedTraitField;

    public function publicMultipleUsedTraitMethod()
    {
        $this->privateMultipleUsedTraitField;
    }

    private function privateMultipleUsedTraitMethod() {}

    protected function protectedMultipleUsedTraitMethod() {}

    public static function publicStaticMultipleUsedTraitMethod()
    {
        MultipleUsedTrait::$privateStaticMultipleUsedTraitField;
    }

    private static function privateStaticMultipleUsedTraitMethod() {}

    protected static function protectedStaticMultipleUsedTraitMethod() {}
}

class BaseClass
{
    use BaseTrait1;

    private $privateBaseClassField;

    public function publicBaseClassMethod()
    {
        $this->privateBaseClassField;
        BaseClass::$privateStaticBaseTrait1Field;
    }
}

class ExtendedClass extends BaseClass
{
    private $privateExtendedClassField;

    public function publicExtendedClassMethod()
    {
        $this->privateExtendedClassField;
    }

    public static function publicStaticExtendedClassMethod()
    {
        ExtendedClass::privateStaticExtendedClassMethod();
    }

    private static function privateStaticExtendedClassMethod() {}

    protected static function protectedStaticExtendedClassMethod() {}
}

class TraitedExtendedClass extends BaseClass
{
    use BaseTrait2;

    private $privateTraitedExtendedClassField;

    public function publicTraitedExtendedClassMethod()
    {
        $this->privateTraitedExtendedClassField;
        TraitedExtendedClass::$privateStaticBaseTrait2Field;
    }
}

TraitedTrait::$publicStaticTraitedTraitField;

MultipleUsedTrait::$publicStaticMultipleUsedTraitField;

$baseClass = new BaseClass();
$baseClass->publicBaseClassMethod();
BaseClass::$publicStaticBaseTrait1Field;

$extendedClass = new ExtendedClass();
$extendedClass->publicExtendedClassMethod();
ExtendedClass::$publicStaticBaseTrait1Field;

$traitedExtendedClass = new TraitedExtendedClass();
$traitedExtendedClass->publicTraitedExtendedClassMethod();
TraitedExtendedClass::$publicStaticBaseTrait1Field;
