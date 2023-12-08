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

function paramType(X&Y $test): void {
    
}

function returnType(): X&Y {
    
}

class TestClass {
    private X&Y $test;

    public function paramType(X&Y $test): void {
        $this->test = $test;
    }

    public function returnType(): X&Y {
        return $this->test;
    }
}

trait TestTrait {
    private X&Y $test;

    public function paramType(X&Y $test1, X&Y&Z $test2): void {
        $this->test = $test;
    }

    public function returnType(): X&Y {
        return $this->test;
    }
}

interface TestInterface {

    public function paramType(X&Y&Z $test);
    public function returnType(): X&Y&Z;

}

$closure = function(X&Y&Z $test1, Y&Z $test2): void {};
$closure = function(int $test): X&Y&Z {};

$arrow = fn(X&Y $test) => $test;
$arrow = fn(X&Y $test): X&Y => $test;
