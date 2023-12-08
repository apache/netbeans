<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
class ClassX {
    const IMPLICIT_X_CONSTANT = "implicit constant";
    public const PUBLIC_X_CONSTANT = "public constant";
    private const PRIVATE_X_CONSTANT = "private constant";
    protected const PROTECTED_X_CONSTANT = "protected constant";
    public (ClassY&ClassZ)|ClassX $publicXField;
    private (ClassX&ClassZ)|(ClassY&ClassZ)|ClassX $privateXField;
    protected ClassX|(ClassY&ClassZ)|ClassY $protectedXField;
    public static (ClassY&ClassZ)|ClassX $publicStaticXField;
    private static (ClassY&ClassZ)|ClassY $privateStaticXField;
    protected static (ClassY&ClassZ)|ClassX|ClassY $protectedStaticXField;

    public function publicXMethod(): ClassX|(ClassY&ClassZ) {}
    private function privateXMethod(): (ClassY&ClassZ)|ClassX {}
    protected function protectedXMethod(): (ClassY&ClassZ)|(ClassX&ClassZ) {}
    public static function publicStaticXMethod(): (ClassY&ClassZ)|ClassX {}
    private static function privateStaticXMethod(): ClassX|(ClassY&ClassZ){}
    protected static function protectedStaticXMethod(): (ClassY&ClassZ)|ClassX {}
}

class ClassY {
    const IMPLICIT_Y_CONSTANT = "implicit constant";
    public const PUBLIC_Y_CONSTANT = "public constant";
    private const PRIVATE_Y_CONSTANT = "private constant";
    protected const PROTECTED_Y_CONSTANT = "protected constant";
    public (ClassY&ClassZ)|ClassX $publicYField;
    private (ClassX&ClassZ)|(ClassY&ClassZ)|ClassX $privateYField;
    protected ClassX|(ClassY&ClassZ)|ClassY $protectedYField;
    public static (ClassY&ClassZ)|ClassX $publicStaticYField;
    private static (ClassY&ClassZ)|ClassY $privateStaticYField;
    protected static (ClassY&ClassZ)|ClassX|ClassY $protectedStaticYField;

    public function publicYMethod(): ClassX|(ClassY&ClassX) {}
    private function privateYMethod(): (ClassY&ClassZ)|ClassX {}
    protected function protectedYMethod(): (ClassY&ClassZ)|(ClassX&ClassZ) {}
    public static function publicStaticYMethod(): (ClassY&ClassZ)|ClassX {}
    private static function privateStaticYMethod(): ClassX|(ClassY&ClassZ){}
    protected static function protectedStaticYMethod(): (ClassY&ClassZ)|ClassX {}
}

class ClassZ {
    const IMPLICIT_Z_CONSTANT = "implicit constant";
    public const PUBLIC_Z_CONSTANT = "public constant";
    private const PRIVATE_Z_CONSTANT = "private constant";
    protected const PROTECTED_Z_CONSTANT = "protected constant";
    public (ClassY&ClassZ)|ClassX $publicZField;
    private (ClassX&ClassZ)|(ClassY&ClassZ)|ClassX $privateZField;
    protected ClassX|(ClassY&ClassZ)|ClassY $protectedZField;
    public static (ClassY&ClassZ)|ClassX $publicStaticZField;
    private static (ClassY&ClassZ)|ClassY $privateStaticZField;
    protected static (ClassY&ClassZ)|ClassX|ClassY $protectedStaticZField;

    public function publicZMethod(): ClassX|(ClassY&ClassZ)|ClassY {}
    private function privateZMethod(): (ClassY&ClassZ)|ClassX {}
    protected function protectedZMethod(): (ClassY&ClassZ)|(ClassX&ClassZ) {}
    public static function publicStaticZMethod(): (ClassX&ClassZ)|(ClassY&ClassZ)|ClassX {}
    private static function privateStaticZMethod(): ClassX|(ClassY&ClassZ){}
    protected static function protectedStaticZMethod(): (ClassY&ClassZ)|ClassX {}
}

trait TestTrait {
    const IMPLICIT_TRAIT_CONSTANT = "implicit constant";
    public const PUBLIC_TRAIT_CONSTANT = "public constant";
    private const PRIVATE_TRAIT_CONSTANT = "private constant";
    protected const PROTECTED_TRAIT_CONSTANT = "protected constant";
    public (ClassX&ClassY)|ClassY $publicTraitField;
    private (ClassX&ClassY)|(ClassY&ClassZ) $privateTraitField;
    protected ClassY|(ClassX&ClassY) $protectedTraitField;
    public static ClassY|(ClassX&ClassY)|ClassX $publicStaticTraitField;
    private static (ClassX&ClassZ)|ClassY $privateStaticTraitField;
    protected static (ClassX&ClassY&ClassZ)|ClassX $protectedStaticTraitField;

    public function publicTraitMethod(): (ClassX&ClassY)|ClassZ {}
    private function privateTraitMethod(): ClassZ|(ClassX&ClassY)|null {}
    protected function protectedTraitMethod(): (ClassX&ClassY)|ClassZ {}
    public static function publicStaticTraitMethod(): (ClassX&ClassY)|(ClassX&ClassY&ClassZ) {}
    private static function privateStaticTraitMethod():  ClassX|(ClassX&ClassY)|ClassZ {}
    protected static function protectedStaticTraitMethod(): ClassZ|(ClassX&ClassY) {}
}

function testFunctionReturnType(): (ClassX&ClassY)|ClassZ {
}

interface TestInterface {

    public function paramType(ClassX|(ClassY&ClassZ) $test): void;
    public function returnType(): ClassX|(ClassY&ClassZ);

}

/**
 * @method ClassX|(ClassX&ClassZ) methodTag() Description
 * @property (ClassX&ClassY)|ClassY $propertyTag Description
 */
class TestClass implements TestInterface {
    use TestTrait;
    /**
     * @var ClassX|(ClassX&ClassY)
     */
    public $publicPhpdocField;
    public (ClassX&ClassZ)|(ClassY&ClassZ) $publicFiled;
    private ClassX|(ClassY&ClassZ) $privateFiled;
    protected ClassZ|ClassX|(ClassY&ClassZ) $protectedFiled;
    /**
     * @var (ClassX&ClassY)|ClassZ
     */
    public static $publicPhpdocStaticField;
    public static (ClassX&ClassZ)|ClassZ $publicStaticField;
    private static ClassZ|(ClassZ&ClassY) $privateStaticField;
    protected static (ClassX&ClassZ)|ClassZ $protectedStaticField;

    public function __construct(
            public ClassZ|(ClassZ&ClassY) $publicPromotedFiled,
            private null|(ClassZ&ClassX) $privatePromotedFiled,
            protected ClassZ|(ClassZ&ClassY)|ClassX $protectedPromotedFiled,
    ) {
        $publicPromotedFiled->publicYMethod(); // ClassZ|(ClassZ&ClassY)
        $publicPromotedFiled->publicYMethod()->publicXMethod(); // ClassX|(ClassY&ClassX)
        $publicPromotedFiled->publicYMethod()::$publicStaticXField; // ClassX|(ClassY&ClassX)
        $publicPromotedFiled::publicStaticYMethod(); // ClassZ|(ClassZ&ClassY)
        $publicPromotedFiled::$publicStaticYField->publicYField; // (ClassY&ClassZ)|ClassX
        $publicPromotedFiled::$publicStaticYField::PUBLIC_Z_CONSTANT; // (ClassY&ClassZ)|ClassX
        $privatePromotedFiled->publicXField; // null|(ClassZ&ClassX)
        $privatePromotedFiled->publicXField->publicZMethod(); // (ClassY&ClassZ)|ClassX
        $privatePromotedFiled->publicXField::$publicStaticYField; // (ClassY&ClassZ)|ClassX
        $protectedPromotedFiled->publicXMethod(); // ClassZ|(ClassZ&ClassY)|ClassX
        $protectedPromotedFiled::$publicStaticZField(); // ClassZ|(ClassZ&ClassY)|ClassX
    }

    public function paramType(ClassX|(ClassY&ClassZ) $param1, (ClassY&ClassZ)|ClassZ $param2, (ClassX&ClassY)|null|(ClassX&ClassZ) $param3): void {
        $param1->publicXMethod(); // ClassX|(ClassY&ClassZ)
        $param1->publicYMethod()->publicZMethod(); // ClassX|(ClassY&ClassX)
        $param1->publicXMethod()::PUBLIC_Y_CONSTANT; // ClassX|(ClassY&ClassZ)
        $param1::publicStaticYMethod(); // ClassX|(ClassY&ClassZ)
        $param1::publicStaticYMethod()::$publicStaticYField; // (ClassY&ClassZ)|ClassX
        $param2->publicYField; // (ClassY&ClassZ)|ClassZ
        $param2::IMPLICIT_Y_CONSTANT; // (ClassY&ClassZ)|ClassZ
        $param3?->publicZField; // (ClassX&ClassY)|null|(ClassX&ClassZ)
        $param3::$publicStaticZField; // (ClassX&ClassY)|null|(ClassX&ClassZ)
    }

    public function param(X|(X&Y)|(Y&Z) $param): void {}

    /**
     * PHPDoc test.
     *
     * @param null|ClassX|(ClassX&ClassZ) $phpdoc1
     * @param (ClassX&ClassY&ClassZ)|null|(ClassX&ClassZ) $phpdoc2
     */
    public function phpdocParamType($phpdoc1, $phpdoc2) {
        $phpdoc1->publicXMethod(); // null|ClassX|(ClassX&ClassZ)
        $phpdoc1::publicStaticZMethod(); // null|ClassX|(ClassX&ClassZ)
        $phpdoc2->publicYField; // (ClassX&ClassY&ClassZ)|null|(ClassX&ClassZ)
        $phpdoc2::PUBLIC_Y_CONSTANT; // (ClassX&ClassY&ClassZ)|null|(ClassX&ClassZ)
    }

    /**
     * @return (ClassX&ClassZ)|(ClassX&ClassY) Description
     */
    public function phpdocReturnType() {
    }

    public function returnType(): ClassX|(ClassY&ClassZ) {
    }

    public static function publicStaticMethod(): (ClassX&ClassZ)|(ClassX&ClassY) {
    }

    public function test(): void {
        // method return types
        $this->returnType()->publicXField; // ClassX|(ClassY&ClassZ)
        $this->returnType()->publicXMethod()->publicYField; // ClassX|(ClassY&ClassZ)
        $this->returnType()?->publicYMethod()?->publicXField; // ClassX|(ClassY&ClassX)
        $this->privateTraitMethod()->publicXField; // ClassZ|(ClassX&ClassY)|null
        $this->protectedTraitMethod()->publicYMethod()->publicXField; // ClassX|(ClassY&ClassX)
        $this->phpdocReturnType()->publicXField; // (ClassX&ClassZ)|(ClassX&ClassY)
        $this->returnType()::IMPLICIT_X_CONSTANT; // ClassX|(ClassY&ClassZ)
        $this->returnType()::publicStaticXMethod()::$publicStaticXField; // (ClassY&ClassZ)|ClassX
        $this->returnType()::publicStaticXMethod()->publicXField; // (ClassY&ClassZ)|ClassX
        $this->privateTraitMethod()::publicStaticXMethod(); // ClassZ|(ClassX&ClassY)|null
        $this->methodTag()->publicXMethod(); // ClassX|(ClassX&ClassZ)
        self::publicStaticMethod()::IMPLICIT_X_CONSTANT; // (ClassX&ClassZ)|(ClassX&ClassY)
        self::publicStaticMethod()::publicStaticXMethod()::publicStaticYMethod(); // (ClassY&ClassZ)|ClassX
        self::publicStaticMethod()::publicStaticXMethod()->publicXMethod(); // (ClassY&ClassZ)|ClassX
        static::publicStaticMethod()->publicZMethod(); // (ClassX&ClassZ)|(ClassX&ClassY)
        static::publicStaticMethod()->publicZMethod()::$publicStaticXField; // ClassX|(ClassY&ClassZ)|ClassY
        static::publicStaticMethod()->publicZMethod()->publicYMethod(); // ClassX|(ClassY&ClassZ)|ClassY
        static::publicStaticTraitMethod()->publicXMethod()->publicXField; // ClassX|(ClassY&ClassZ)
        // field types
        $this->privateFiled->publicXMethod(); // ClassX|(ClassY&ClassZ)
        $this->publicFiled->publicXMethod()->publicYField; // ClassX|(ClassY&ClassZ)
        $this->protectedFiled::PUBLIC_Z_CONSTANT; // ClassZ|ClassX|(ClassY&ClassZ)
        $this->publicPhpdocField->publicXMethod(); // ClassX|(ClassX&ClassY)
        $this->publicPhpdocField::$publicStaticXField; // ClassX|(ClassX&ClassY)
        $this->protectedTraitField->publicXMethod(); // ClassY|(ClassX&ClassY)
        $this->privatePromotedFiled->publicYMethod(); // null|(ClassZ&ClassX)
        $this->propertyTag->publicYMethod(); // (ClassX&ClassY)|ClassY
        static::$privateStaticField::IMPLICIT_Y_CONSTANT; // ClassZ|(ClassZ&ClassY)
        static::$privateStaticField?->publicStaticZMethod(); // ClassZ|(ClassZ&ClassY)
        self::$publicPhpdocStaticField::$publicStaticXField; // (ClassX&ClassY)|ClassZ
        self::$publicPhpdocStaticField->publicZField; // (ClassX&ClassY)|ClassZ
        self::$publicStaticTraitField->publicXMethod(); // ClassY|(ClassX&ClassY)|ClassX
        // display in CC list
        $this->param(null);
    }
}

testFunctionReturnType()->publicXField; // (ClassX&ClassY)|ClassZ
testFunctionReturnType()::PUBLIC_X_CONSTANT; // (ClassX&ClassY)|ClassZ

$testFunctionReturnType = testFunctionReturnType();
$testFunctionReturnType->publicXMethod(); // (ClassX&ClassY)|ClassZ
$testFunctionReturnType::IMPLICIT_Z_CONSTANT(); // (ClassX&ClassY)|ClassZ

/** @var (ClassX&ClassY)|null $vardoc1 */
$vardoc1->publicXField; // (ClassX&ClassY)|null
$vardoc1::$publicStaticXField; // (ClassX&ClassY)|null
/** @var ClassZ|(ClassX&ClassY)|null $vardoc2 */
$vardoc2->publicXField; // ClassZ|(ClassX&ClassY)|null
$vardoc2::$publicStaticXField; // ClassZ|(ClassX&ClassY)|null
/** @var ClassZ|(ClassX&ClassY)|(ClassX&ClassY&ClassZ) $vardoc3 */
$vardoc3->publicXField; // ClassZ|(ClassX&ClassY)|(ClassX&ClassY&ClassZ)
$vardoc3::$publicStaticXField; // ClassZ|(ClassX&ClassY)|(ClassX&ClassY&ClassZ)

/* @var $vardoc4 (ClassX&ClassY)|null */
$vardoc4->publicXField; // (ClassX&ClassY)|null
$vardoc4::$publicStaticXField; // (ClassX&ClassY)|null
/* @var $vardoc5 ClassZ|(ClassX&ClassY)|null */
$vardoc5->publicXField; // ClassZ|(ClassX&ClassY)|null
$vardoc5::$publicStaticXField; // ClassZ|(ClassX&ClassY)|null
/* @var $vardoc6 ClassZ|(ClassX&ClassY)|(ClassX&ClassY&ClassZ) */
$vardoc6->publicXField; // ClassZ|(ClassX&ClassY)|(ClassX&ClassY&ClassZ)
$vardoc6::$publicStaticXField; // ClassZ|(ClassX&ClassY)|(ClassX&ClassY&ClassZ)

$closure = function((ClassX&ClassY&ClassZ)|ClassY $closure1, ClassX|(ClassX&ClassZ)|(ClassY&ClassZ) $closure2): void {
    $closure1->publicXField; // (ClassX&ClassY&ClassZ)|ClassY
    $closure1::PUBLIC_Y_CONSTANT; // (ClassX&ClassY&ClassZ)|ClassY
    $closure2->publicYMethod()->publicXField; // ClassX|(ClassY&ClassX)
    $closure2::$publicStaticYField->publicZField; // (ClassY&ClassZ)|ClassX
};

$arrow1 = fn((ClassX&ClassY)|ClassX $test) => $test->publicXField; // (ClassX&ClassY)|ClassX
$arrow2 = fn((ClassX&ClassY)|ClassX $test) => $test::publicStaticXMethod()->publicZField; // (ClassY&ClassZ)|ClassX
