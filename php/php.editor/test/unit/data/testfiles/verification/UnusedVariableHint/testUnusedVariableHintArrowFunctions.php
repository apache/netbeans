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

$usedVariable;

fn($used, $unused) => $used; // 1a $unused
$fn = fn($used, $unused) => $used; // 1b $unused

fn($unused, $used) => $used; // 2a $unused
$fn = fn($unused, $used) => $used; // 2b $unused

fn($used1, $used2) => $used1 + $used2;
$fn = fn($used1, $used2) => $used1 + $used2;

fn($unused1, $unused2) => $used1 + $used2; // 3a $unused1 $unused2
$fn = fn($unused1, $unused2) => $used1 + $used2; // 3b $unused1 $unused2

fn($used) => $used + $usedVariable;
$fn = fn($used) => $used + $usedVariable;

fn(array $used) => $used;
fn(array $unused) => $used; // 4 $unused

fn($used = 100) => $used;
fn($unused = 100) => $used; // 5 $unused

fn(&$used) => $used;
fn(&$unused) => $used; // 6 $unused

fn&($used) => $used;
fn&($unused) => $used; // 7 $unused

fn($used1, ...$used2) => $used1 + count($used2);
fn($unused, ...$used) => $used; // 8 $unused
fn($unused1, ...$unused2) => $used; // 9 $unused1 $unused2

$af = fn($used) => $callable1($callable2($used), $used);
$af = fn($unused) => $callable1($callable2($used), $used); // 10 $unused

$af = Example::test([1, 2])
        ->test1(fn($used) => $used * 2)
        ->test2(fn($used1, $used2) => $used1 + $used2, 0);

$af = Example::test([1, 2])
        ->test1(fn($unused) => $used * 2)  // 11 $unused
        ->test2(fn($unused1, $unused2) => $used1 + $used2, 0); // 12 $unused1 $unused2

function array_map_example1(array $usedArray, array $usedKeys) {
    return array_map(fn($used) => $usedArray[$used], $usedKeys);
}

function array_map_example2(array $unusedArray, array $unusedKeys) { // 13 $unusedArray $unusedKeys
    $used = "test";
    $usedArray = "test";
    $usedKeys = "test";
    return array_map(fn($unused) => $usedArray[$used], $usedKeys); // 14 $unused
}

function test1(callable $usedCallable) {
    return fn(...$used) => !$usedCallable(...$used);
}

function test2(callable $unusedCallable) { // 15 $unusedCallable
    return fn(...$unused) => !$usedCallable(...$used); // 16 $unused
}
