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
    case CASE1;
    case CASE2;
    const CONSTANT1 = "CONSTANT1";
    const CONSTANT2 = self::CASE2;

    public function publicMethod(): void {
        echo "publicMethod()" . PHP_EOL;
    }

    public static function publicStaticMethod(): void {
        echo "publicStaticMethod()" . PHP_EOL;
    }

    public function test(): string {
        return match ($this) {
            static::CASE1 => 'Case1',
            static::CASE2 => 'Case2',
        };
    }

    public static function staticTest(): void {
        Simple::CASE2;
        Simple::CONSTANT1;
        Simple::publicStaticMethod();
        Simple::CASE1->publicMethod();
        Simple::CASE1::publicStaticMethod();
        self::CASE1;
        self::CONSTANT2;
        self::publicStaticMethod();
        self::CASE1->publicMethod();
        static::CASE1;
        static::CONSTANT2;
        static::publicStaticMethod();
        static::CASE1->publicMethod();
    }
}

class ExampleClass {
}

namespace Enum2;

use Enum1\Simple;
use Enum1\ExampleClass;

Simple::CASE1::CONSTANT1;
Simple::CASE1::CASE2;
Simple::CASE2->publicMethod();
Simple::CASE2::publicStaticMethod();
Simple::publicStaticMethod(); // 2
Simple::staticTest();
$i = Simple::CASE2;
$i::CASE1;
$i->publicMethod();
$i::publicStaticMethod();
