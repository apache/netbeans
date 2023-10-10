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

function test($a, $b, $c, $name) {
}

test(1, ...[1, 2, 3],); // OK
test(1, 2, 3, 4, ...[],); // OK
test(1, ...[2, 3, 4], ...[]); // OK
test(...[],); // OK
test(name: 'arg'); // OK
test($test, name: 'arg'); // OK
test(
    ...[], // OK
    name: 'arg'
);
test(
    1,
    2,
    3,
    name: 'arg',
    ...[], // NG
);
$uppacking = [1, 2, 3];
test(...$unpacking, name: "arg"); // OK
test(name: "arg", ...$unpacking); // NG
test(
        1,
        b:2,
        ...[2], // NG
        ...[1] // NG
);
test(
        a:1,
        b:2,
        ...[2], // NG
        ...[1] // NG
);

class TestExample {
    public function test($a, $b, $c, $name): void {}
    public static function staticTest($a, $b, $c, $name): void {}

    public function testUnpackingAfterNamed(): void {
        $this->test(
            ...[1, 2, 3], // OK
            name: 'arg'
        );
        $this->test(
            name: 'arg',
            ...[1, 2, 3], // NG
        );
        self::staticTest(
            ...[1, 2, 3], // OK
            name: 'arg'
        );
        self::staticTest(
            name: 'arg',
            ...[1, 2, 3], // NG
        );
    }
}

$test = new Test(
    name: "test",
    ...['a', 'b'] // NG
);
$test = new Test(
    ...['a', 'b'], // OK
    name: "test",
);

$anon = new class(
    name: "test",
    ...['a', 'b'], // NG
) {
    public function __construct($name, $a, $b) {}
};

$anon = new class(
    ...['a', 'b'], // OK
    name: "test",
) {
    public function __construct($name, $a, $b) {}
};
