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

function test($name1, $name2) {}

test(1, name2: 2);
test(name1: 1, name2: 2);
test(
    name1: 1,
    "function"
);

class Example {
    public function test($name1, $name2): void {}
    public static function staticTest($name1, $name2): void {}

    public function positionalAfterNamed(): void {
        $this->test(1, name2: 2);
        $this->test(name1: 1, name2: 2);
        $this->test(
            name1: 1,
            "method",
        );

        self::staticTest(1, name2: 2);
        self::staticTest(name1: 1, name2: 2);
        self::staticTest(
            name1: 1,
            "static method",
        );
    }
}

$test = new Test(1, name2: 2);
$test = new Test(name1: 1, name2: 2);
$test = new Test(
    name1: 1,
    "constructor",
);

$anon = new class(
    name1: 1,
    "anonymous class",
) {
    public function __construct($name1, $name2) {}
};
