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
namespace Attributes;

use Attribute;

const CONST_1 = 1;

#[Attribute]
class AttributeClass1 {
    public function __construct(int $int = 0, string $string = "default") {
    }
}
#[Attribute]
class AttributeClass2 {
    public function __construct(int $int = 0, string $string = "default", object $object = new \stdClass()) {
    }
}
#[Attribute]
class AttributeClass3 {
    public function __construct(int $int = 0, string $string = "default") {
    }
}

class Example {
    public const string CONST_EXAMPLE = "example";
}

namespace AttributeTest1;

use Attributes\AttributeClass1;
use Attributes\AttributeClass2;
use Attributes\AttributeClass3;
use Attributes\Example;

use const Attributes\CONST_1;

#[AttributeClass1(1, self::CONSTANT_CLASS)]
#[AttributeClass2(1, "class")]
class AttributedClass
{
    #[AttributeClass1(2, "class const")]
    #[AttributeClass2(2, "class const", new Example())]
    public const CONSTANT_CLASS = 'constant';

    #[AttributeClass1(3, "class field")]
    public int|string $field;

    #[AttributeClass1(4, "class static field"), AttributeClass2(4, "class static field", new \Attributes\Example)] // group
    public $staticField;

    #[AttributeClass1(5, "class method")]
    public function method(#[AttributeClass1(5, "class method param")] $param1, #[AttributeClass1(5, 'class method param')] int $pram2) {}

    #[\Attributes\AttributeClass1(6, "class static method")]
    public static function staticMethod(#[\Attributes\AttributeClass1(6, "class static method param")] int|string $param1): bool|int {
        return false;
    }
}

#[AttributeClass1(1, "class child")]
class AttributedClassChild extends AttributedClass {
}

#[AttributeClass1(1, "trait")]
#[AttributeClass2(1, "trait")]
trait AttributedTrait
{
    #[AttributeClass2(2, "trait const")]
    public const CONSTANT_TRAIT = 'constant';

    #[AttributeClass1(3, "trait field")]
    public int $field;

    #[AttributeClass1(4, "trait static field")]
    public $staticField;

    #[AttributeClass1(5, "trait method")]
    public function traitMethod(#[AttributeClass1(5, "trait method param")] $param1) {}

    #[AttributeClass1(6, "trait static method")]
    #[AttributeClass2(6, "trait static method")]
    #[AttributeClass3(6, "trait static method")]
    public static function staticTraitMethod(#[AttributeClass3] int|string $param1): bool|int {
        return false;
    }
}

$anon = new #[AttributeClass1(1, "anonymous class")] class () {};

$test = 1;
$anon2 = new #[AttributeClass2(1, "anonymous class")] class ($test) {
    #[AttributeClass2(2, "anonymous class const")]
    public const CONSTANT_ANON = 'constant';

    #[AttributeClass2(3, "anonymous class field")]
    #[AttributeClass3(3, "anonymous class field")]
    public string $field = 'test';

    #[AttributeClass2(4, "anonymous class static field")]
    public static int $staticField = 1;

    #[AttributeClass2(5, "anonymous class constructor")]
    public function __construct(#[AttributeClass2(5, "anonymous class")] $param1) {
    }

    #[AttributeClass2(6, "anonymous class method")] #[AttributeClass3()]
    public function method(#[AttributeClass2(6, "anonymous class method param")] $param1): int|string {
        return 1;
    }

    #[AttributeClass1(int: 7, string: "anonymous class static method")]
    private static function staticMethod(#[AttributeClass2(7, "anonymous class static method param")] int|bool $pram1): int|string {
        return 1;
    }
};

#[AttributeClass1(1, "interface")]
interface AttributedInterface
{
    #[AttributeClass2(2, "interface const")]
    public const CONSTANT_INTERFACE = 3;

    #[AttributeClass2(self::CONSTANT_INTERFACE, "interface method")] #[AttributeClass3()]
    public function interfaceMethod(#[AttributeClass2(AttributedInterface::CONSTANT_INTERFACE, "interface method param")] $param1): int|string;

    #[AttributeClass2(4, "interface static method")] #[AttributeClass3()]
    public static function staticInterfaceMethod(#[AttributeClass2(4, "interface static method param" . Example::CONST_EXAMPLE)] $param1): int|string;
}

#[\Attributes\AttributeClass2(1, "enum", new Example)]
#[\Attributes\AttributeClass3(1, "enum"), AttributeClass1]
enum AttributedEnum {
    #[AttributeClass1(2, "enum const")]
    public const CONSTANT_ENUM = 'constant';

    #[AttributeClass1(3, "enum case")]
    case Case1;

    #[AttributeClass1(4, "enum method")] #[AttributeClass3()]
    public function method(#[AttributeClass2(4, "enum method param")] $param1): int|string {
        return 1;
    }

    #[AttributeClass1(int: 5, string: "enum static method")]
    public static function staticMethod(#[AttributeClass3(5, "enum static method param")] int|bool $pram1): int|string {
        return 1;
    }
}

#[
    AttributeClass1(1, "function"),
    AttributeClass3(int: CONST_1, string: "function" . Example::class)
] //group
function func1() {}

#[AttributeClass2(1, "function")]
function func2(#[AttributeClass2(1 + \Attributes\CONST_1 + 1, Example::CONST_EXAMPLE . "function param")] int|string $param): void {}

$labmda1 = #[AttributeClass1(1, "closure")] function() {};
$labmda2 = #[AttributeClass2(2, "closure")] #[AttributeClass3(2, "closurr")] function(#[AttributeClass2(int: 2 * \Attributes\CONST_1, string: "closure param" . Example::CONST_EXAMPLE)] $test): void {};
$labmda3 = #[AttributeClass3(3, "closure")] static function(): void {};

$arrow1 = #[AttributeClass1(1, "arrow")] fn() => 100;
$arrow2 = #[AttributeClass1(2, "arrow"), AttributeClass2(2, "arrow")] fn(#[AttributeClass1(\Attributes\CONST_1 / 5, "arrow param" . Example::class)] $test): int|string => 100;
$arrow3 = #[AttributeClass1(3, string: Example::CONST_EXAMPLE . "arrow")] static fn(): int|string => 100;
