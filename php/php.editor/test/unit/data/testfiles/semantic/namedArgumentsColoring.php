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

#[A("test", name: 1, name2: 2)]
class NamedArguments {
    #[A(name: 1)]
    private int $test = 1;
    #[A(name: 1)]
    const CONSTANT = "";

    #[A(name: 1)]
    public function test(int $param1, string $param2, $param3): void {
    }

    #[A(name: 1, test: 2)]
    public static function staticTest(int $param1, string $param2, $param3): void {
    }

}

$instance = new NamedArguments();
$instance->test(param1: 1, pram2: "test", param3: null);

#[A(test: 1)]
function test(int $param1, string $param2, $param3): void {
}

test(param1: 1, pram2: "test", param3:null);

$anon = new #[A(test: 1)] class() {

};

$lambda = #[A(test: 1)] function (){};

$allow = #[A(0, test: 1)] fn() => 1;
