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
$y = 2;

// in global scope
fn(int $x) => $x + $y;
fn(int &$x) => $x + $y;
fn(int ...$x) => $x + $y;
fn(int &...$x) => $x + $y;
fn(int $x) => $x + $y;

fn(int &$x) => fn($z) => $x + $y + $z + $a;
fn(int ...$x) => fn($z) => $x + $y + $z + $a;
fn(int &...$x) => fn($z) => $x + $y + $z + $a;

fn(int $x) => fn(&$z) => $x + $y + $z + $a;
fn(int $x) => fn(...$z) => $x + $y + count($z) + $a;
fn(int $x) => fn(&...$z) => $x + $y + count($z) + $a;

// in function scopes
function test1a($x) {
    $af = fn($u) => $x + $u + $a; // 1a $a
}
function test1b($x) {
    $af = fn(&$u) => $x + $u + $a; // 1b $a
}
function test1c($x) {
    $af = fn(...$u) => $x + count($u) + $a; // 1c $a
}
function test1d($x) {
    $af = fn(&...$u) => $x + count($u) + $a; // 1d $a
}

function test2($x) {
    global $a;
    $af = fn($u) => $u + $a; // $a is not uninitialized
}

// nested
function($x) use ($y) {
    return fn($u) => $x + $y + $u;
};

function($x) use ($y) {
    return fn(&$u) => $x + $y + $u;
};

function($x) use ($y) {
    return fn(...$u) => $x + $y + $u;
};

function($x) use ($y) {
    return fn(&...$u) => $x + $y + $u;
};

function($x) use ($y) {
    return fn($u) => $x + $y + $u + $v; // nested1 $v
};
function($x) use ($y) {
    return fn(&$u) => $x + $y + $u + $v; // nested2 $v
};
function($x) use ($y) {
    return fn(...$u) => $x + $y + $u + $v; // nested3 $v
};
function($x) use ($y) {
    return fn(&...$u) => $x + $y + $u + $v; // nested4 $v
};

fn($u) => function($x) use ($y) {
    return fn($v) => $u + $x + $y + $v; // nested5 $u
};
fn($u) => function($x) use ($y) {
    return fn(&$v) => $u + $x + $y + $v; // nested6 $u
};
fn($u) => function($x) use ($y) {
    return fn(...$v) => $u + $x + $y + $v; // nested7 $u
};
fn($u) => function($x) use ($y) {
    return fn(&...$v) => $u + $x + $y + $v; // nested8 $u
};

function test3a($u) {
    fn(int $x) => fn($z) => $x + $y + $z + $a; // 3a $y $a
}
function test3b($u) {
    fn(int &$x) => fn($z) => $x + $y + $z + $a; // 3b $y $a
}
function test3c($u) {
    fn(int ...$x) => fn($z) => $x + $y + $z + $a; // 3c $y $a
}
function test3d($u) {
    fn(int &...$x) => fn($z) => $x + $y + $z + $a; // 3d $y $a
}
function test3e($u) {
    fn(int $x) => fn(&$z) => $x + $y + $z + $a; // 3e $y $a
}
function test3f($u) {
    fn(int $x) => fn(...$z) => $x + $y + count($z) + $a; // 3f $y $a
}
function test3g($u) {
    fn(int $x) => fn(&...$z) => $x + $y + count($z) + $a; // 3g $y $a
}
