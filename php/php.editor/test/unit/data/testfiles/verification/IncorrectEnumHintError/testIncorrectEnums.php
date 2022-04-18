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

enum CorrectBackingTypeString: string {
    case CASE_NAME;
}

enum CorrectBackingTypeInt: int {
    case CASE_NAME;
}

class IncorrectEnumCase {
    case C;
    case D = 1;
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
