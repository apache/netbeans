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
class X {}
class Y {}
class Z {}
/**
 * @deprecated
 */
class DeprecatedType {}

function parameterType((X&Y&DeprecatedType)|(X&Z)|DeprecatedType $param1, (X&Y&DeprecatedType)|DeprecatedType $param2): void {
}

class TestClass {
    public function __construct(
            (DeprecatedType&Y)|Z|X $test,
    ) {}

    public function parameterType((DeprecatedType&Y)|Z|X $param): int {
        return $this->test;
    }
}

trait TestTrait {
    public function parameterType((X&Y)|(Y&Z&DeprecatedType) $param): ?string {
        return $this->test;
    }
}

interface TestInterface {
    public function parameterType(X|(X&Y&Z)|DeprecatedType $param): void;
}

enum TestEnum {
    case Case1;
    public function parameterType(int $param1, (X&Y)|DeprecatedType $param2): void {}
}

$closure = function((X&Y&Z)|(DeprecatedType&Z) $test): void {};

$arrow = fn((X&DeprecatedType)|(Y&Z)|(X&Z) $test): (X&DeprecatedType)|(Y&Z)|(X&Z) => $test;
