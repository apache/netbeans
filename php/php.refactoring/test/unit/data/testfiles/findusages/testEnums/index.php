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

namespace A;

enum EnumA {

    public const CONSTANT_A = 1;

    case Case1;
    case Case2;

    public function test(): void {
        $this->publicMethod();
        self::publicStaticMethod();
        static::publicStaticMethod();
    }

    public function publicMethod(): void { // EnumA
    }

    public static function publicStaticMethod(): string { // EnumA
        return "publicStaticMethod()";
    }
}

namespace B;

use A\EnumA;

interface InterfaceB {
}

enum EnumB: string implements InterfaceB {

    public const CONSTANT_B = EnumA::CONSTANT_A;

    case A = 'A';
    case B = 'B';
    case C = EnumA::Case1;

    public function publicMethod(): void { // EnumB
    }

    public static function publicStaticMethod(): string { // EnumB
        return "publicStaticMethod()";
    }
}

EnumB::A->publicMethod();
EnumB::A::publicStaticMethod();
