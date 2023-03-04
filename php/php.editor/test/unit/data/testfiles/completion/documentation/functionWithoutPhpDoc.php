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

class TestClass
{

    public function testTyped(int $param1, Foo $param2, \Test\Bar $param3): void {
    }

    public static function testNullableType(?int $param1, ?Foo $param2, ?\Test\Bar $param3): ?Foo {
    }

    public function testUnionType(int|float $param1, Foo|Bar $param2, \Test\Foo|\Test\Bar $param3): int|Foo|\Test\Bar {
    }

    public function testIntersectionType(Foo&Bar $param1, \Test\Foo&\Test\Bar $param2): Foo&\Test\Bar {
    }
}

function testTyped(int $param1, Baz $param2, \Test\Bar $param3): Foo {
}

function testNullableType(?int $param1, ?Foo $param2, ?\Test\Bar $param3): ?\Test\Foo {
}

function testUnionType(int|float $param1, Foo|Bar $param2, \Test\Foo|\Test\Bar $param3): int|Foo|\Test\Bar {
}

function testIntersectionType(Foo&Bar $param1, \Test\Foo&\Test\Bar $param2): Foo&\Test\Bar {
}

$instance = new TestClass();
$instance->testTyped(1, null, null);
TestClass::testNullableType(null, null, null);
$instance->testUnionType(1, null, null);
$instance->testIntersectionType(null, null);

testTyped(2, null, null);
testNullableType(2, null, null);
testUnionType(2, null, null);
testIntersectionType(null, null); // function
