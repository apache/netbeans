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

#[Class1(1, self::CONSTANT)]
class AttributeSyntax
{
    #[Class1(2)]
    #[Class2(2)]
    public const CONSTANT = 'constant';

    #[Class1(3)]
    public int|string $field;

    #[Class1(4), Class2(4)] // group
    public $staticField;

    #[Class1(5)]
    public function method(#[Class1(6)] $param1, #[Class1('foo', 'bar', 7)] int $pram2) {}

    #[Class1(8)]
    public static function staticMethod(#[Class1\Test(9)] int|string $param1): bool|int {
        return false;
    }
}

#[ChildClass1(1)]
class AttributeSyntaxChild extends AttributeSyntax {
}

#[Trait1(1)]
#[Trait2(1)]
trait TestTrait
{
    #[Trait1(2)]
    public $field;

    #[Trait1(3)]
    public $staticField;

    #[Trait1(4)]
    public function method(#[Trait1(5)] $param1) {}

    #[Trait1(6)]
    #[Trait2(6)]
    public static function staticMethod(#[Trait1] int|string $param1): bool|int {
        return false;
    }
}

$anon = new #[Anon1(1)] class () {};

$anon2 = new #[Anon2(1)] class ($test) {
    #[Anon2(2)]
    public const CONSTANT = 'constant';

    #[Anon2(3)]
    public string $field = 'test';

    #[Anon2(4)]
    public static int $staticField = 1;

    #[Anon2(5)]
    public function __construct(#[Anon2(6)] $param1) {
    }

    #[Anon2(7)] #[Anon2()]
    public function method(#[Anon2(8)] $param1): int|string {
        return 1;
    }

    #[Anon2]
    private static function staticMethod(#[Anon2(9)] int|bool $pram1): int|string {
        return 1;
    }
};

#[
    Func1(1),
    Func1(2),
] //group
function func1() {}

#[Func2(1)]
function func2(#[Func2(2)] int|string $param): void {}

$labmda1 = #[Lambda1(1)] function() {};
$labmda2 = #[Lambda2(1)] #[Lambda2()] function(): void {};
$labmda3 = #[Lambda3(1)] static function(): void {};

$arrow1 = #[Arrow1(1)] fn() => 100;
$arrow2 = #[Arrow2(1), Arrow2(2, "test")] fn(): int|string => 100;
$arrow3 = #[Arrow3(1)] static fn(): int|string => 100;
