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
        $this->testStaticReturnTypeParent()
                ->testSelfReturnNullableTypeParent() // parent
                ->testSelfReturnUnionTypeParent(); // parent
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

}

class ChildClass extends ParentClass {
    use TestTrait;

    public function testStaticReturnTypeChild(): static {
        $this->testStaticReturnNullableTypeTrait()
                ->testSelfReturnTypeParent() // all test16
                ->testStaticReturnNullableTypeParent() // parent test17
                ->testStaticReturnTypeParent(); // parent test 18
    }

    public function testStaticReturnNullableTypeChild(): ?static {
    }

    public function testStaticReturnUnionTypeChild(): static|null {
    }

}

trait TestTrait {

    public function testStaticReturnTypeTrait(): static {
        $this->testStaticReturnNullableTypeTrait()
                ->testStaticReturnUnionTypeTrait(); // trait test19
    }

    public function testStaticReturnNullableTypeTrait(): ?static {
    }

    public function testStaticReturnUnionTypeTrait(): array|static|null {
    }
}

$child = new ChildClass();
$child->testStaticReturnTypeParent()->testStaticReturnTypeChild(); // all
$child->testSelfReturnTypeParent()->testStaticReturnTypeParent(); // parent items

$child->testStaticReturnNullableTypeParent()->testStaticReturnTypeChild(); // all
$child->testSelfReturnNullableTypeParent()->testStaticReturnTypeParent(); // parent items

$child->testStaticReturnUnionTypeParent()->testStaticReturnTypeChild(); // all
$child->testSelfReturnUnionTypeParent()->testStaticReturnTypeParent(); // parent items

$child->testStaticSelfReturnUnionTypeParent()->testStaticReturnTypeChild(); // all

$child->testStaticReturnTypeTrait()->testStaticReturnTypeChild(); // all
$child->testStaticReturnNullableTypeTrait()->testStaticReturnTypeChild(); // all
$child->testStaticReturnUnionTypeTrait()->testStaticReturnTypeChild(); // all

$child->testStaticReturnTypeChild()->testSelfReturnNullableTypeParent(); // all
$child->testStaticReturnNullableTypeChild()->testSelfReturnNullableTypeParent(); // all
$child->testStaticReturnUnionTypeChild()->testStaticReturnTypeTrait(); // all
