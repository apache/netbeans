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

function returnType(): (X&Y&DeprecatedType)|(X&Z)|DeprecatedType {
}

class TestClass {
    public function returnType(): (DeprecatedType&Y)|Z|X {
        return $this->test;
    }
}

trait TestTrait {
    public function returnType(): (X&Y)|(Y&Z&DeprecatedType) {
        return $this->test;
    }
}

interface TestInterface {
    public function returnType(): X|(X&Y&Z)|DeprecatedType;
}

enum TestEnum {
    case Case1;
    public function returnType(): (X&Y)|DeprecatedType {}
}

$closure = function(int $test): (X&Y&Z)|(DeprecatedType&Z) {};

$arrow = fn($test): (X&DeprecatedType)|(Y&Z)|(X&Z) => $test;
