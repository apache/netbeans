<?php
class StaticAccessTest
{
    const CONSTANT_SA = 'CONSTANT_SA';
    public static $publicStaticSAField = "\$publicStaticSAField";
    protected static $protectedStaticSAField = "\$protectedStaticSAField";
    private static $privateStaticSAField = "\$privateStaticSAField";

    public $publicSAField = "\$publicSAField";
    protected $protectedSAField = "\$protectedSAField";
    private $privateSAField = "\$privateSAField";

    public static function publicStaticSAMethod() {
        $enclosedAccess = new StaticAccessTest();
        $enclosedAccess::privateStaticSAMethod(); // test

        echo PHP_EOL;
        $pre = "[\$enclosedAccess::] ";
        echo $pre . $enclosedAccess::privateStaticSAMethod() . PHP_EOL;
        echo $pre . $enclosedAccess::protectedStaticSAMethod() . PHP_EOL;
        echo $pre . $enclosedAccess::$publicStaticSAField . PHP_EOL;
        echo $pre . $enclosedAccess::$privateStaticSAField . PHP_EOL;
        echo $pre . $enclosedAccess::$protectedStaticSAField . PHP_EOL;
        echo $pre . $enclosedAccess::CONSTANT_SA . PHP_EOL;

        return "publicStaticSAMethod()";
    }

    protected static function protectedStaticSAMethod() {
        return "protectedStaticSAMethod()";
    }

    private static function privateStaticSAMethod() {
        return "privateStaticSAMethod()";
    }

    public function publicSAMethod() {
        return "publicSAMethod()";
    }

    protected function protectedSAMethod() {
        return "protectedSAMethod()";
    }

    private function privateSAMethod() {
        return "privateSAMethod()";
    }

}

trait BaseTrait
{
    public static $publicStaticBaseTraitField = "\$publicStaticBaseTraitField";
    protected static $protectedStaticBaseTraitField = "\$protectedStaticBaseTraitField";
    private static $privateStaticBaseTraitField = "\$privateStaticBaseTraitField";
    public $publicBaseTraitField = "\$publicBaseTraitField";
    protected $protectedBaseTraitField = "\$protectedBaseTraitField";
    private $privateBaseTraitField = "\$privateBaseTraitField";

    public static function publicStaticBaseTraitMethod() {
        return "publicStaticBaseTraitMethod()";
    }

    protected static function protectedStaticBaseTraitMethod() {
        return "protectedStaticBaseTraitMethod()";
    }

    private static function privateStaticBaseTraitMethod() {
        return "privateStaticBaseTraitMethod()";
    }

    public function publicBaseTraitMethod() {
        return "publicBaseTraitMethod()";
    }

    protected function protectedBaseTraitMethod() {
        return "protectedBaseTraitMethod()";
    }

    private function privateBaseTraitMethod() {
        return "privateBaseTraitMethod()";
    }
}

class ExClass extends StaticAccessTest
{
    use BaseTrait;

    const CONSTANT_EX = 'CONSTANT_EX';
    public static $publicStaticExField = "\$publicStaticExField";
    protected static $protectedStaticExField = "\$protectedStaticExField";
    private static $privateStaticExField = "\$privateStaticExField";

    public $publicExField = "\$publicExField";
    protected $protectedExField = "\$protectedExField";
    private $privateExField = "\$privateExField";

    public static function newInstance() {
        return new ExClass();
    }

    public static function publicStaticExMethod() {
        $enclosed = new ExClass();
        $enclosed::privateStaticExMethod(); // test

        echo PHP_EOL;
        $pre = "[\$enclosed::] ";
        echo $pre . $enclosed::privateStaticExMethod() . PHP_EOL;
        echo $pre . $enclosed::protectedStaticExMethod() . PHP_EOL;
        echo $pre . $enclosed::$publicStaticExField . PHP_EOL;
        echo $pre . $enclosed::$privateStaticExField . PHP_EOL;
        echo $pre . $enclosed::$protectedStaticExField . PHP_EOL;
        echo $pre . $enclosed::CONSTANT_EX . PHP_EOL;
//        echo $enclosed::class . PHP_EOL; // invalid!
        echo $pre . $enclosed::protectedStaticSAMethod() . PHP_EOL;
        echo $pre . $enclosed::$publicStaticSAField . PHP_EOL;
        echo $pre . $enclosed::$protectedStaticSAField . PHP_EOL;
        echo $pre . $enclosed::CONSTANT_SA . PHP_EOL;
        echo $pre . $enclosed::publicStaticBaseTraitMethod() . PHP_EOL;
        echo $pre . $enclosed::privateStaticBaseTraitMethod() . PHP_EOL;
        echo $pre . $enclosed::protectedStaticBaseTraitMethod() . PHP_EOL;
        echo $pre . $enclosed::$publicStaticBaseTraitField . PHP_EOL;
        echo $pre . $enclosed::$privateStaticBaseTraitField . PHP_EOL;
        echo $pre . $enclosed::$protectedStaticBaseTraitField . PHP_EOL;

        return "publicStaticExMethod()";
    }

    protected static function protectedStaticExMethod() {
        return "protectedStaticExMethod()";
    }

    private static function privateStaticExMethod() {
        return "privateStaticExMethod()";
    }

    public function publicExMethod() {
        $this::privateStaticExMethod(); // test

        $pre = "[\$this::] ";
        echo $pre. $this::privateStaticExMethod() . PHP_EOL;
        echo $pre. $this::protectedStaticExMethod() . PHP_EOL;
        echo $pre. $this::$privateStaticExField . PHP_EOL;
        echo $pre. $this::CONSTANT_EX . PHP_EOL;
//        echo $this::class . "[\$this::class]" . PHP_EOL; // invalid!
        return "publicExMethod()";
    }

    protected function protectedExMethod() {
        return "protectedExMethod()";
    }

    private function privateExMethod() {
        return "privateExMethod()";
    }

}

echo PHP_EOL . "[StaticAccessTest]" . PHP_EOL;
$staticAccessTest = new StaticAccessTest();
$staticAccessTest::publicStaticSAMethod(); // test

$staticAccessTestArray[0] = new StaticAccessTest();
$staticAccessTestArray[0]::publicStaticSAMethod(); // test

StaticAccessTest::publicStaticSAMethod(); // test

echo $staticAccessTest::$publicStaticSAField . PHP_EOL;
echo $staticAccessTest::CONSTANT_SA. PHP_EOL;
//echo $staticAccessClass::class. PHP_EOL; // invalid!

echo PHP_EOL . "[ExClass]" . PHP_EOL;
$extendedClass = new ExClass();
$extendedClass::publicStaticExMethod(); // test
ExClass::publicStaticExMethod(); // test

echo $extendedClass::publicStaticSAMethod() . PHP_EOL;
echo $extendedClass::publicStaticBaseTraitMethod() . PHP_EOL;
echo $extendedClass::$publicStaticExField . PHP_EOL;
echo $extendedClass::$publicStaticSAField . PHP_EOL;
echo $extendedClass::$publicStaticBaseTraitField . PHP_EOL;
echo $extendedClass::CONSTANT_EX . PHP_EOL;
echo $extendedClass::CONSTANT_SA . PHP_EOL;
//echo $extendedClass::class . PHP_EOL; // invalid!

$extendedClass::newInstance()->publicExMethod(); // test

$pre = "[\$extendedClass::newInstance()->] ";
echo $pre . $extendedClass::newInstance()->publicStaticExMethod(). PHP_EOL;
echo $pre . $extendedClass::newInstance()->publicStaticBaseTraitMethod(). PHP_EOL;
echo $pre . $extendedClass::newInstance()->publicStaticSAMethod(). PHP_EOL;

echo $pre . $extendedClass::newInstance()->publicExMethod(). PHP_EOL;
echo $pre . $extendedClass::newInstance()->publicBaseTraitMethod(). PHP_EOL;
echo $pre . $extendedClass::newInstance()->publicSAMethod(). PHP_EOL;

echo $pre . $extendedClass::newInstance()->publicExField. PHP_EOL;
echo $pre . $extendedClass::newInstance()->publicBaseTraitField. PHP_EOL;
echo $pre . $extendedClass::newInstance()->publicSAField. PHP_EOL;
