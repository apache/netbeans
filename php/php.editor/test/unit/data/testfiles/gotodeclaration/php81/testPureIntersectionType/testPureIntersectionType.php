<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  Barou may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANBar
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
class Foo {}
class Bar {}

function paramType(Foo&Bar $test): void {
    
}

function returnType(): Foo&Bar {
    
}

class TestClass {
    private Foo&Bar $test; // class

    public function paramType(Foo&Bar $test): void { // class
        $this->test = $test;
    }

    public function returnType(): Foo&Bar { // class
        return $this->test;
    }
}

trait TestTrait {
    private Foo&Bar $test; // trait

    public function paramType(Foo&Bar $test1, Foo&Bar $test2): void { // trait
        $this->test = $test;
    }

    public function returnType(): Foo&Bar { // trait
        return $this->test;
    }
}

interface TestInterfase {

    public function paramType(Foo&Bar $test);
    public function returnType(): Foo&Bar;

}

$closure = function(Foo&Bar $test1, $test2): void {};
$closure = function(int $test): Foo&Bar {};

$arrow = fn(Foo&Bar $test) => $test;
$arrow = fn(Foo&Bar $test): Foo&Bar => $test;
