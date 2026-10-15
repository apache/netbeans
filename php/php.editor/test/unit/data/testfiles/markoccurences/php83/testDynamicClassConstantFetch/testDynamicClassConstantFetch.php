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
class Test {
    public const TEST = 'test';
    public const TES = 'TES';
    public const T = 'T';
    public const TEST1 = self::{'TEST'};
    public const TEST2 = self::{'TES' . 'T'};
    public const TEST3 = self::{self::TES . self::T};

    public static function method(): void {}
}

enum EnumTest: string {
    case TEST = 'test';
    case TES = 'TES';
    case T = 'T';
    case TEST1 = self::{'TEST'};
    case TEST2 = self::{'TES' . 'T'};
    case TEST3 = self::{self::TES . self::T};
}

function test(string $param = "test"): void {
}

$variable = 'TEST';
$e = 'e';
$test = "Test";

Test::{"TEST"};
$test::{"TEST"};
Test::{$variable};
$test::{$variable};
Test::{$variable . $e};
$test::{$variable . $e};
Test::{strtoupper("test")};
$test::{strtoupper("test")};
Test::{'$variablee'};
$test::{'$variablee'};
Test::{strtolower("CLASS")};
$test::{strtolower("CLASS")};
Test::{1000};
$test::{1000};
EnumTest::{[]};
$test::{[]};

Test::{Test::method()}::{test($variable)};
Test::{test('test1')}::{test('test2')};

$test = new Test();
