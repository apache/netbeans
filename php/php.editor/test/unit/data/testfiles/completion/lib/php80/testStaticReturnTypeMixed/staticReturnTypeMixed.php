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

class ParentClass {

    public function testSelfReturnTypeParent(): self {
    }

    public function testSelfReturnNullableTypeParent(): ?self {
    }

    public function testSelfReturnUnionTypeParent(): string|self|null {
    }

    public function testStaticReturnTypeParent(): static {
    }

    public function testStaticReturnNullableTypeParent(): ?static {
    }

    public function testStaticReturnUnionTypeParent(): string|static|null {
    }

    public function testStaticSelfReturnUnionTypeParent(): static|self|null {
    }

    // static methods
    public static function testSelfReturnTypeParentStatic(): self {
        self::testStaticReturnTypeParentStatic()
                ->testSelfReturnNullableTypeParent() // parent test1
                ::testSelfReturnUnionTypeParentStatic(); // parent test2
    }

    public static function testSelfReturnNullableTypeParentStatic(): ?self {
    }

    public static function testSelfReturnUnionTypeParentStatic(): string|self|null {
    }

    public static function testStaticReturnTypeParentStatic(): static {
    }

    public static function testStaticReturnNullableTypeParentStatic(): ?static {
    }

    public static function testStaticReturnUnionTypeParentStatic(): string|static|null {
    }

    public static function testStaticSelfReturnUnionTypeParentStatic(): static|self|null {
    }

}

class ChildClass extends ParentClass {
    use TestTrait;

    public function testStaticReturnTypeChild(): static {
        $this::testStaticReturnNullableTypeTraitStatic()
                ->testSelfReturnTypeParent() // all test3
                ::testStaticReturnNullableTypeParentStatic() // parent test4
                ->testStaticReturnTypeParent(); // parent test5
    }

    public function testStaticReturnNullableTypeChild(): ?static {
    }

    public function testStaticReturnUnionTypeChild(): static|null {
    }

    // static methods
    public static function testStaticReturnTypeChildStatic(): static {
    }

    public static function testStaticReturnNullableTypeChildStatic(): ?static {
    }

    public static function testStaticReturnUnionTypeChildStatic(): static|null {
    }

}

trait TestTrait {

    public function testStaticReturnTypeTrait(): static {
    }

    public function testStaticReturnNullableTypeTrait(): ?static {
    }

    public function testStaticReturnUnionTypeTrait(): array|static|null {
    }

    // static methods
    public static function testStaticReturnTypeTraitStatic(): static {
        self::testStaticReturnNullableTypeTraitStatic()
                ->testStaticReturnUnionTypeTrait() // trait test6
                ::testStaticReturnTypeTraitStatic(); // trait test7
    }

    public static function testStaticReturnNullableTypeTraitStatic(): ?static {
    }

    public static function testStaticReturnUnionTypeTraitStatic(): array|static|null {
    }
}
