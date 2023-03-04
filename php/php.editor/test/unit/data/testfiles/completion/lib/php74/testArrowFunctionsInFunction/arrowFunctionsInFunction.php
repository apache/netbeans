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

$globalVariable = 910;

function test(ArrowFunctions $af, int $number) {
    $fn = fn() => $af->getNumber() + $number;
    echo $fn() . PHP_EOL;
}

function test2(ArrowFunctions $af, int $number) {
    global $globalVariable;
    $fn = fn() => $af->getNumber() + $number + $globalVariable;
    echo $fn() . PHP_EOL;
}

class ArrowFunctions
{

    public function test() {
    }

    public function getNumber() {
        return 100;
    }

}
