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

function test($name) {}

test(name: 1);
test(name: 1, name: 2);
test(
    name: 1,
    name: 2,
    name: 3
);

class Example {
    public function test($name): void {}

    public static function staticTest($name): void {}

    public function duplicateNames(): void {
        $this->test(name: 1,);
        $this->test(name: 1, name: 2);
        $this->test(
            name: 1,
            name: 2,
            name: 3
        );

        self::staticTest(name: 1,);
        self::staticTest(name: 1, name: 2);
        self::staticTest(
            name: 1,
            name: 2,
            name: 3
        );
    }
}

$date = new \DateTime(datetime: "now", datetime: "now");

$anon = new class (test: 1, test: 1) {
    public function __construct(public $test) {
        echo $test;
    }
};

