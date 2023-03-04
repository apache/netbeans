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

enum Simple {}

enum Simple1 {
    case A;
    case B;
    case C;
    case D;
}

enum BackedCaseInt: int {}

enum BackedCaseInt1: int {
    case A = 1;
    case B = 2;
    case C = 3;
    case D = 4;
    case E = 1 << 3;
    case F = -1;
}

enum BackedCaseString: string {}

enum BackedCaseString1: string {
    case A = "A";
    case B = "B";
    case C = "C";
    case D = "D";
    case E = "E" . "E";
    case F = <<<F
    Test
    Test
    Test
    F;
    case G = <<<'G'
    Test
    Test
    Test
    G;
}

enum Impl implements Iface1, Iface2 {
    case A;
    case B;
    case C;

    public function implMethod(Test $test): void {
    }
}

#[A1]
enum Attributes: int implements Iface {
    #[A1]
    case A = 1;
    #[A1]
    case B = 2;

    #[A1]
    const CONSTANT1 = "constant";
    const CONSTANT2 = self::A;

    #[A1]
    public function implMethod(#[A1] Test $test): void {
        self::CONSTANT1;
        self::A;
    }
}

trait TestTrait {
    public static function test(): void {
    }
}

enum WithTrait {
    use TestTrait;

    case A;
    case B;

    public static function staticMethod(): void {
    }
}

interface Iface {}
interface Iface1 {}
interface Iface2 {}
