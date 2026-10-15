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
    public (ClassY   &   ClassZ)   |   ClassX $publicXField;
private (ClassX&ClassZ)|(ClassY&ClassZ)|ClassX $privateXField;
protected ClassX|(ClassY&ClassZ)|    ClassY     $protectedXField;
public static      (ClassY&ClassZ)|ClassX $publicStaticXField;
private static (ClassY&ClassZ)|ClassY $privateStaticXField;
protected static (ClassY&ClassZ)   |      ClassX|ClassY $protectedStaticXField;

public function publicXMethod((ClassY&ClassZ)|ClassX $param1): ClassX|(ClassY&ClassZ) {}
 private function privateMXethod(ClassZ|(ClassY&ClassZ)|ClassX $param1 = null): (ClassY&ClassZ)|ClassX {}
       protected function protectedXMethod(  (ClassY & ClassZ)   |(ClassX   &ClassZ) $param1): (ClassY&ClassZ)|(ClassX&ClassZ) {}
public static function publicStaticXMethod(ClassX | (ClassY & ClassZ) $param1, int $param2): (ClassY&ClassZ)|ClassX {}
     private static function privateStaticXMethod(int $param1, (ClassX&ClassY)|ClassZ $param2): ClassX|(ClassY&ClassZ){}
protected static function   protectedStaticXMethod(ClassX|null $param1, (ClassX&ClassZ)|null $param2 = null): (ClassY&ClassZ)|ClassX {}
}

class ClassY {
const IMPLICIT_Y_CONSTANT = "implicit constant";
public const PUBLIC_Y_CONSTANT = "public constant";
private const PRIVATE_Y_CONSTANT = "private constant";
            protected const PROTECTED_Y_CONSTANT = "protected constant";
public (ClassY&ClassZ)|    ClassX $publicYField;
          private (ClassX&ClassZ)|(ClassY&ClassZ)|ClassX $privateYField;
protected ClassX|(ClassY&ClassZ)|ClassY $protectedYField;
public static (ClassY&     ClassZ)|ClassX $publicStaticYField;
private static (    ClassY&ClassZ)|ClassY $privateStaticYField;
protected static (ClassY&ClassZ)|ClassX|ClassY $protectedStaticYField;

public function publicYMethod((ClassY&ClassZ)|   ClassX $param1, (ClassY&ClassZ)|ClassX|(ClassX&ClassZ) $param2): ClassX|(ClassY&ClassZ) {}
private function privateYMethod(ClassX $param1, (ClassY&ClassZ)   |null $param2): (ClassY&ClassZ)|ClassX {}
protected function protectedYMethod((ClassY&ClassZ)|   ClassX $param1, ClassY|(ClassX&ClassZ&ClassY) $param2): (ClassY&ClassZ)|(ClassX&ClassZ) {}
public static function publicStaticYMethod(null|(ClassY&ClassZ)|   ClassX $param1): (ClassY&ClassZ)|ClassX {}
private static function privateStaticYMethod(    (ClassY&ClassZ)|ClassX|ClassY $param): ClassX|(ClassY&ClassZ){}
protected static function protectedStaticYMethod(      (ClassY&ClassZ)|(ClassX&ClassZ) $param1): (ClassY&ClassZ)|ClassX {}
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

public function publicZMethod(
        ClassX|(ClassY&ClassZ)|ClassY $param1
        ): ClassX|(ClassY&ClassZ)|ClassY {}
private function privateZMethod(): (ClassY&ClassZ)|ClassX {}
protected function protectedZMethod(ClassX $param = null): (ClassY&ClassZ)|(ClassX&ClassZ) {}
public static function publicStaticZMethod(ClassX&ClassY $param): (ClassX&ClassZ)|(ClassY&ClassZ)|ClassX {}
private static function privateStaticZMethod(ClassX|null $param): ClassX|(ClassY&ClassZ){}
protected static function protectedStaticZMethod(
        int $param,
        ClassX|ClassY $param2
        ): (ClassY&ClassZ)|ClassX {}
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

  public function publicTraitMethod(?ClassX $param): (ClassX&ClassY)|ClassZ {}
  private function privateTraitMethod((ClassX&ClassY)| ClassZ $param):  (ClassX&ClassY)|ClassZ {}
  protected function protectedTraitMethod(string $param1, (ClassX&ClassY)|(ClassX&ClassZ) $param2): (ClassX&ClassY)|ClassZ {}
  public static function publicStaticTraitMethod(): (ClassX&ClassY)|(ClassX&ClassY&ClassZ) {}
  private static function privateStaticTraitMethod(ClassX|(ClassX&ClassY)|ClassZ $param1, ClassX|(ClassX&ClassY)|ClassZ $param2):  ClassX|(ClassX&ClassY)|ClassZ {}
  protected static function protectedStaticTraitMethod(string|int $param = 1): ClassZ|(ClassX&ClassY) {}
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
}

public function paramType(ClassX|(ClassY&ClassZ) $param1): void {
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

}

/** @var (ClassX&ClassY)|null $vardoc1 */
$vardoc1->publicXField;
/** @var ClassZ|(ClassX&ClassY)|null $vardoc2 */
$vardoc2->publicXField;
/** @var ClassZ|(ClassX&ClassY)|(ClassX&ClassZ) $vardoc3 */
$vardoc3->publicXField;

/* @var $vardoc4 (ClassX&ClassY)|null */
$vardoc4->publicXField;
/* @var $vardoc5 ClassZ|(ClassX&ClassY)|null */
$vardoc5->publicXField;
/* @var $vardoc6 ClassZ|(ClassX&ClassY)|(ClassX&ClassZ) */
$vardoc6->publicXField;

$closure = function((X&Y&Z)|Y $test1, X|(X&Z)|(Y&Z) $test2): void {};
$closure = function(int $test): (X&Y&Z)|(X&Z) {};

$arrow = fn((X&Y)|(Y&Z) $test) => $test;
$arrow = fn((X&Y)|(Y&Z) $test): (X&Y)|(Y&Z)|(X&Z) => $test;

$anon = new class($test) {
 private (ClassX&ClassY)|ClassZ $property;

 public function __construct((ClassX&ClassY)|ClassZ $property)
        {
            $this->property = $property;
    }
    
    public function test((ClassX&ClassY)|null $param): (ClassX&ClassY)|(ClassY&ClassZ) {
    }
};
