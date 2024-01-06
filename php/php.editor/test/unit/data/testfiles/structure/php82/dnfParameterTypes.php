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
class Test {}

function parameterType((X&Y&Test)|(X&Z) $param): void {
}

class TestClass {
    public function parameterType((Test&Y)|Z|X $param1, bool $param2): (Test&Y)|Z|X {
        return $this->test;
    }
}

trait TestTrait {
    public function parameterType((X&Y)|(Y&Z&Test) $param): self {
        return $this->test;
    }
}

interface TestInterface {
    public function parameterType(X|(X&Y&Z)|Z $param);
}

enum TestEnum {
    case Case1;
    public function parameterType(X&Y $param1, (X&Y)|Z $param2): (X&Y)|Z {}
}

$closure = function((X&Y&Z)|(X&Z) $test): (X&Y&Z)|(X&Z) {};

$arrow = fn((X&Y)|(Y&Z)|(X&Z) $test): (X&Y)|(Y&Z)|(X&Z) => $test;

/**
 * Magick methods.
 *
 * @method testVoid((X&Y&Z)|(X&Z) $test)  test comment
 * @method int testType(X|(X&Y&Z)|Z $test)  test comment
 * @method (X&Y)|(Y&Z)|(X&Z) dnfType1($param1, (X&Y)|(Y&Z)|Z $param2)  test comment
 * @method Y|(Y&Z)|X dnfType2($param1, X|(Y&Z)|Z $param2)  test comment
 * @method static staticTestVoid((Test&Y)|Z|X $test) test comment
 * @method static int staticTestType((X&Y)|(Y&Z)|(X&Z) $param1, int $param2)  test comment
 * @method static (X&Y)|(Y&Z)|(X&Z) staticDnfType1((X&Y)|(Y&Z)|(X&Z) $param1, int $param2)  test comment
 * @method static X|(Y&Z)|Z staticDnfType2((X&Y)|(Y&Z)|(X&Z) $param1, int $param2)  test comment
 * @method static X|(Y&Z) staticDnfType3((X&Y)|(Y&Z)|(X&Z) $param1, int $param2)  test comment
 * @method static ?int staticTestNullable(?string $param, (X&Y)|Y|(X&Z) $param2) test comment
 * @method static ?Example getDefault() Description
 */
class MagickMethods {
}
