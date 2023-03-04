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

class ClassExample1 {
    public function test1(private $incorrect1): void {
    }

    public static function test2(int $param1, public int $incorrect) {
    }
}

abstract class AbstractClassExample1 {

    abstract public function test1(private ?int $incorrect = 1);
    abstract public function test2(
            public $incorrect1,
            private ?int $incorrect2 = 1
    );

}

interface InterfaceExample1 {
    public function test(public int|string $incorrect1): void;
    public function test(
            int $param,
            public int|string $incorrect1,
            protected $incorrect2 = "",
    );
}

$lambda1 = function (private $incorrect1) {};
$lambda2 = function (
        string $test,
        private $incorrect1,
        protected int $incorrect2 = 1,
) {};
$lambda3 = static function (public ?string $incorrect1 = "test"){};

$arrow1 = fn(int $test, private ?int $incorrect1,): int => 1;
$arrow2 = fn(
        int $test,
        private ?int $incorrect1,
        private string|int $incorrect2
): int => 1;
