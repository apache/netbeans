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

function testFunction((X&Y)|(U&V) $test): (X&Y)|(U&V) {
    return $test;
}

class TestClass {
    public (X&Y)|Z $field;
    public static (X&Y)|null $staticField;

    public function testMethod((X&Y)|(U&V) $test): (X&Y)|(U&V) {
        return $test;
    }
}

interface TestInterface {
    public function testMethod(int|(A&B&C) $test): (A&B&C)|int;
}

trait TestTrait {
    public (X&Y)|Z|null $field;
    public static (X&Y)|(A&B&C)|(E&D) $staicField;

    public function testMethod((X&Y)|(U&V) $test): (X&Y)|(U&V) {
        return $test;
    }
}
