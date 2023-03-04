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
namespace Test1;

class UnionTypes1{}
class UnionTypes2{}

namespace Test2;
class TestClass
{
    public function classMethod(
            \Test1\UnionTypes1|TestClass $object,
            string|int|float|null $param,
    ): \Test1\UnionTypes2|\Test1\UnionTypes1|null {
        
    }

    public static function classStaticMethod(
            \Test1\UnionTypes1|TestClass $object, // static
            string|int|float|null $param, // static
    ): \Test1\UnionTypes2|self|null {
        
    }
}

class TestChildClass extends TestClass
{
    public function childClassMethod(parent|self|null $object, self|parent|null $object2): self|parent|null {
        
    }
}

interface TestInterface
{
    public function interfaceMethod(\Test1\UnionTypes1|TestClass $object, string|int|float|null $param, ): int|float|null;
}

abstract class TestAbstractClass
{
    abstract protected function abstractClassMethod(array|object $param, TestInerface|TestClass|null $object,): iterable|false;
}

trait TestTrait
{
    private function traitMethod(TestInterface|\Test1\TestUnionTypes2 $object, bool|callable $param): TestInterface|false|null {
        
    }
}
