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

enum IncorrectProperties {
    public int $property;
    public static string $staticProperty = "error";
    case CASE_NAME;
}

enum IncorrectPropertiesWithTrait {
    use TestTrait1;
    case CASE_NAME;
}

enum IncorrectBackingType: Foo {
    public int $property = 1;
    public static string $staticProperty = "error";
    case CASE_NAME;
}

enum IncorrectConstructor {
    public function __construct() {
    }
}

enum IncorrectMethods {
    public function cases():array {}
    public function __get($name) {}
    public function __set($name,$value) {}
    public function __isset($name) {}
    public function __unset($name) {}
    public function __sleep() {}
    public function __wakeup() {}
    public function __serialize() {}
    public function __unserialize($array) {}
    public function __toString() {}
    public static function __set_state($state) {}
    public function __clone() {}
    public function __debugInfo() {}
}

enum IncorrectMethodsBacked: string {
    public function cases():array {}
    public function from(string|int $value):self {}
    public function tryFrom(string|int $value):self {}
}

enum IncorrectMethodsCorrectCaseInMessage {
    public function Cases():array {}
    public function __GET($name) {}
    public function __seT($name,$value) {}
    public function __ISset($name) {}
    public function __unSET($name) {}
    public function __sleeP() {}
    public function __wAkeup() {}
    public function __Serialize() {}
    public function __Unserialize($array) {}
    public function __ToString() {}
    public static function __Set_State($state) {}
    public function __clONe() {}
    public function __DebugInfo() {}
}

enum CorrectMethods {
    public function from(string|int $value):self {}
    public function tryFrom(string|int $value):self {}
    public function __call($name,$args) {}
    public static function __callStatic($name,$args) {}
    public function __invoke() {}
}

enum CorrectBackingTypeString: string {
    case CASE_NAME; // string
}

enum CorrectBackingTypeInt: int {
    case CASE_NAME; // int
}

class IncorrectEnumCase {
    case C;
    case D = 1;
    public int $property = 0;
    public static string $property = "correct";
}

trait IncorrectTraitEnumCase {
    case X;
    case Y = 1;
    public int $property = 0;
    public static string $property = "correct";
}

trait TestTrait1 {
    use TestTrait2;
    public int $property = 1;
    public static string $property = "correct";
}

trait TestTrait2 {
    public int $property2 = 1;
    public static string $property2 = "correct";
}

interface TestInterface {
    public const CONSTANT = 1;
}

// interface and trait are handled as errors by the parser
//interface TestInterface {
//    case C;
//}
//

//trait TestTrait {
//    case C;
//}
