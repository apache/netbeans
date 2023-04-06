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
namespace Vendor\Package\Classes;

class MyClass implements Iface {

}

namespace Vendor\Package\Traits;

trait MyTrait {
    public function test(): void {
    }
}

namespace Vendor\Package\Interfaces;

interface MyInterface {
    public function test(): void;
}

namespace Vendor\Package\Enums;

enum MyEnum {
    case Case1;
}

namespace Vendor\Package\Constants;

const CONSTANT_A = "CONSTANT_A";
const CONSTANT_B = "CONSTANT_B";

namespace Vendor\Package\Functions;

function functionA(): void {
}

function functionB(): void {
}

namespace Test;

class TestClass {
    public function test(): void {
    }
}
functionA();
