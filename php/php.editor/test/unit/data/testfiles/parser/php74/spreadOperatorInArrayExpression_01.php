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

$array0 = [...[]];
$array1 = [1, 2, 3];
$array2 = [...$array1];
$array2_a = [...[1, 2, 3]];
$array3 = [0, ...$array1];
$array4 = array(...$array1, ...$array2, 111);
$array5 = [...$array1, ...$array1];


function getArray() {
    return ['a', 'b', 'c'];
}

$array6 = [...getArray()];

$array7 = [...new ArrayIterator(['a', 'b', 'c'])];

function arrayGenerator() {
    for ($i = 1; $i < 10; $i++) {
        yield $i;
    }
}

$array8 = [...arrayGenerator()];

const CONSTANT1 = [...CONSTANT];
const CONSTANT1a = [...[0, 1, 2, 3]];
const CONSTANT2 = [100, ...CONSTANT, ...CONSTANT1,];
const CONSTANT3 = [...CONSTANT2, 100 => 0, ...CONSTANT];

// Errors: these are not handled in the parser
[$test1, ...$test2] = [1, 2, 3]; // Error!
[...3]; // Error!
[..."string"]; // Error!
[...['a' => 'b']]; // Error!
