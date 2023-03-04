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

// Fatal error: Cannot use "static" when no class scope is active
function test(): static {
    return new static;
}

class Redundant {

    public object|int|string|array|null|bool|float $test;

    // Fatal error: Type static|callable|object|array|string|int|float|bool|null contains both object and a class type, which is redundant
    public function testStatic(): object|int|string|array|callable|null|bool|float|static {
    }

    public function testSelf(): object|int|string|array|callable|null|bool|float|self {
    }

    public function testParent(): object|int|string|array|callable|null|bool|float|parent {
    }

    public function testNamespaceName(): object|int|string|array|callable|null|bool|float|\Foo\Bar {
    }

    public function test(): void {
        $af = fn(int|string|callable|foo|\test\bar|iterable $test): static => new static;
    }
}
