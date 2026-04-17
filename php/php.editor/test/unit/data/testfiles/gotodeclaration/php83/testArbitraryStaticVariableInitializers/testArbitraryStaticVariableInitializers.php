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

class Example {
    private int $field = 1;

    public function method(): int {
        return 1;
    }

    public function run(int $param1) : void {
        static $example1 = rand();
        static $example2 = $param1;
        static $example3 = $this->field;
        static $example4 = $this->method();
        static $example5 = new class() {};
        static $example6 = new stdClass(...[0]);
        static $example6 = new stdClass($param1);
        static $example7 = new (Test);
        static $example8 = new static;
        static $example9 = $param1 <= 100 ? run($param1 + 1) : "Test $param1";
        static $example10 = rand(), $example11 = rand();
    }
}

$variable = 1;
static $example1 = rand();
static $example2 = $variable;
