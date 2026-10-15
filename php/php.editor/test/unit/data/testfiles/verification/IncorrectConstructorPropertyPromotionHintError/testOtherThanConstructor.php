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

class ClassExample1 {
    public function test1(private $incorrect1): void { // error1
    }

    public static function test2(int $param1, public int $incorrect) { // error2
    }
}

abstract class AbstractClassExample1 {

    abstract public function test1(private ?int $incorrect = 1); // error3
    abstract public function test2(
            public $incorrect1, // error4
            private ?int $incorrect2 = 1 // error5
    );

}

interface InterfaceExample1 {
    public function test(public int|string $incorrect1): void; // error6
    public function test2(
            int $param,
            public int|string $incorrect1, // error7
            protected $incorrect2 = "", // error8
    );
}

$lambda1 = function (private $incorrect1) {}; // error9
$lambda2 = function (
        string $test,
        private $incorrect1, // error10
        protected int $incorrect2 = 1, // error11
) {};
$lambda3 = static function (public ?string $incorrect1 = "test"){}; // error12

$arrow1 = fn(int $test, private ?int $incorrect1,): int => 1; // error13
$arrow2 = fn(
        int $test,
        private ?int $incorrect1, // error14
        private string|int $incorrect2 // error15
): int => 1;

class ClassSetVisibilityExample1 {
    public function test1(private(set) $incorrect1): void { // error16
    }

    public static function test2(int $param1, public(set) int $incorrect) { // error17
    }
}

abstract class AbstractClassSetVisibilityExample1 {

    abstract public function test1(private(set) ?int $incorrect = 1); // error18
    abstract public function test2(
            public(set) $incorrect1, // error19
            private(set) ?int $incorrect2 = 1 // error20
    );

}

interface InterfaceSetVisibilityExample1 {
    public function test(public(set) int|string $incorrect1): void; // error21
    public function test2(
            int $param,
            public(set) int|string $incorrect1, // error22
            protected(set) $incorrect2 = "", // error23
    );
}

$lambda1 = function (private(set) $incorrect1) {}; // error24
$lambda2 = function (
        string $test,
        private(set) $incorrect1, // error25
        protected(set) int $incorrect2 = 1, // error26
) {};
$lambda3 = static function (public(set) ?string $incorrect1 = "test"){}; // error27

$arrow1 = fn(int $test, private(set) ?int $incorrect1,): int => 1; // error28
$arrow2 = fn(
        int $test,
        private(set) ?int $incorrect1, // error29
        private(set) string|int $incorrect2 // error30
): int => 1;
