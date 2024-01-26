<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  Barou may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANBar
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
class Foo {
    public const CONSTANT_FOO = "test";
    public (Foo&Bar)|Foo $fieldFoo;
    public static (Foo&Bar)|Bar $staticFieldFoo;

    public function methodFoo(): (Foo&Bar)|(Bar&Baz) {}
    public static function staticMethodFoo(Foo|Bar $param): void {}
}
class Bar {
    public const CONSTANT_BAR = "test";
    public (Foo&Bar)|(Baz&Foo) $fieldBar;
    public static (Foo&Bar)|Bar $staticFieldBar;

    public function methodBar(): (Foo&Bar)|Baz {}
    public static function staticMethodBar(Foo|Bar $param): void {}
}
class Baz {
    public const CONSTANT_BAZ = "test";
    public (Foo&Bar)|(Baz&Foo) $fieldBaz;
    public static (Foo&Bar)|Bar $staticFieldBaz;

    public function methodBaz(): Foo|(Bar&Baz) {}
    public static function staticMethodBaz(Foo&Bar $param): void {}
}

function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {
}

function returnType1(): (Foo&Bar)|Baz {}
function returnType2(): Baz|(Foo&Bar) {}
function returnType3(): Baz|(Foo&Bar)|Foo {}
function returnType4(): (Foo&Bar)|(Foo&Baz) {}

/**
 * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description
 * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description
 * @property Foo|(Bar&Baz) $propertyTag Description
 */
class TestClass {
    private (Foo&Bar)|Baz $fieldClass; // class
    private static Bar|(Bar&Baz) $staticFieldClass; // class

    public function paramType((Foo&Baz)|(Foo&Bar)|Baz $test): void { // class
        $this->fieldClass = $test;
        $this->fieldClass::CONSTANT_FOO;
        $this->fieldClass::$staticFieldBaz;
        $this->fieldClass->fieldBar;
        $test::CONSTANT_BAZ;
        $test::$staticFieldBar;
        self::$staticFieldClass->methodBar();
        self::$staticFieldClass::staticmethodBaz(null);
    }

    public function returnType(): (Foo&Bar)|Baz { // class
        return $this->fieldClass;
    }
}

trait TestTrait {
    private (Foo&Bar)|Baz $test; // trait

    public function paramType((Foo&Bar)|(Bar&Baz) $test1, Foo|(Foo&Bar) $test2): void { // trait
    }

    public function returnType(): Foo|(Foo&Bar) { // trait
    }
}

interface TestInterfase {

    public function paramType(Foo|(Foo&Bar)|null $test);
    public function returnType(): (Foo&Bar)|(Bar&Baz);

}

$closure = function(Foo|(Foo&Bar)|(Bar&Baz) $test1, $test2): void {};
$closure = function(int $test): (Foo&Bar)|null {};

$arrow = fn(Foo|Bar|(Foo&Bar) $test) => $test;
$arrow = fn((Foo&Bar)|null $test): Foo|(Foo&Bar) => $test;

/** @var (Foo&Bar)|Foo|(Bar&Baz&Foo) $vardoc1 */
$vardoc1->methodFoo();

/* @var $vardoc2 (Foo&Bar)|Baz */
$vardoc2::staticMethodBaz(null);

/** @var Bar|Baz|Foo $unionType */
$unionType->methodFoo();

/** @var Bar&Baz&Foo $intersectionType */
$intersectionType->methodFoo();

/** @var ?Foo $nullableType */
$nullableType->methodFoo();
