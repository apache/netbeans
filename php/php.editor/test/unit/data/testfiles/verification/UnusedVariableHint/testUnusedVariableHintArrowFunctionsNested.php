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

// nested arrow/lambda functions
$usedLexialVariable = 100;

fn(array $usedArray) => fn(int $usedNumber) => $usedNumber + count($usedArray);
$fn = fn(array $usedArray) => fn(int $usedNumber) => $usedNumber + count($usedArray);

fn(array $unusedArray) => fn(int $unusedNumber) => $usedNumber + count($usedArray); // unused 1a $unusedArray $unusedNumber
$fn = fn(array $unusedArray) => fn(int $unusedNumber) => $usedNumber + count($usedArray); // unused 1b $unusedArray $unusedNumber

function ($usedLambdaParam) use ($usedLexialVariable) {
    return fn($used) => $used + $usedLambdaParam + $usedLexialVariable;
};

function ($unusedLabmdaParam) use ($unusedLexialVariable) { // unused 2a $unusedLabmdaParam $unusedLexialVariable
    return fn($unused) => $used + $usedLabmdaParam + $usedLexialVariable; // unused 2b $unused
};

$fn = function ($unused) use ($usedLexialVariable) { // unused 3 $unused
    return fn($used) => $used + $usedLexialVariable;
};


fn($used) => function ($usedLambda) use ($used) {
    return $usedLambda + $used;
};

fn($unused) => function ($usedLambda) use ($usedLexialVariable) {// unused 4 $unused
    return $usedLambda + $usedLexialVariable;
};

// $unused1 is not used
$fn = fn($unused1) => function ($unusedLambda) use ($unusedLexialVariable) { // unused 5a $unused1 $unusedLambda $unusedLexialVariable
    return fn(int $unused): ?int => $unused1 + $usedLambda + $usedLexialVariable; // unused 5b $unused
};

// $unused1 is not used
$fn = fn($unused1) => function ($usedLambda) use ($unusedLexialVariable) { // unused 6a $unused1 $unusedLexialVariable
    test($usedLambda);
    return fn(int $unused): ?int => $unused1 + $usedLambda + $usedLexialVariable; // unused 6b $unused
};
