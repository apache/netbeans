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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
namespace Enum1;
enum Simple {

    use TestTrait;

    case CASE1;
    case CASE2;
    public const PUBLIC_CONST = "public";
    private const PRIVATE_CONST = "private";
    protected const PROTECTED_CONST = "protected";
    const CONSTANT1 = "CONSTANT1";
    const CONSTANT2 = self::CASE2;

    public function publicEnumMethod(): void {
        echo "publicEnumMethod()" . PHP_EOL;
    }

    private function privateEnumMethod(): void {
        echo "privateEnumMethod()" . PHP_EOL;
    }

    protected function protectedEnumMethod(): void {
        echo "protectedEnumMethod()" . PHP_EOL;
    }

    public static function publicStaticEnumMethod(): void {
        echo "publicStaticEnumMethod()" . PHP_EOL;
    }

    private static function privateStaticEnumMethod(): void {
        echo "privateStaticEnumMethod()" . PHP_EOL;
    }

    protected static function protectedStaticEnumMethod(): void {
        echo "protectedStaticEnumMethod()" . PHP_EOL;
    }

    public function testEnum(): string {
        return match ($this) {
            static::CASE1 => 'Case1',
            static::CASE2 => 'Case2',
        };
    }

    public static function testStaticEnum(): void {
        Simple::CASE2;
        Simple::PRIVATE_CONST;
        Simple::protectedStaticEnumMethod();
        Simple::CASE1->publicEnumMethod();
        Simple::CASE1::publicStaticEnumMethod();
        self::CASE1;
        self::privateStaticEnumMethod();
        self::CASE1->privateEnumMethod();
        self::CASE1::protectedStaticEnumMethod();
        static::privateStaticEnumMethod();
        static::CASE1->publicEnumMethod();
        static::CASE1::publicEnumMethod();
    }
}

trait TestTrait {
    public function publicTraitMethod(): void {
    }
    private function privateTraitMethod(): void {
    }
    protected function protectedTraitMethod(): void {
    }
    public static function publicStaticTraitMethod(): void {
    }
    private static function privateStaticTraitMethod(): void {
    }
    protected static function protectedStaticTraitMethod(): void {
    }
}

namespace Enum2;

use Enum1\Simple;

Simple::CASE1::CONSTANT1;
Simple::CASE2->publicEnumMethod();
Simple::publicStaticEnumMethod();
$i = Simple::CASE2;
$i::CASE1;
$i->publicEnumMethod();
