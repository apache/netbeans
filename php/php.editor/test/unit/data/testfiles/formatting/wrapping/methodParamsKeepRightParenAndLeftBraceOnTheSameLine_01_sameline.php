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

//function testNoReturnType1() {
//}
function testNoReturnType1() {
}

//function testNoReturnType2(
//){
//}
function testNoReturnType2(
){
}

//function testNoReturnType3($param1, &$param2){
//}
function testNoReturnType3($param1, &$param2){
}

//function testNoReturnType4(
//        $param1,
//        $param2
//){
//}
function testNoReturnType4(
        $param1,
        $param2
){
}

//function testHasReturnType($param1,
//        $param2
//): void{
//}
function testHasReturnType1(): void{
}

//function testHasReturnType2(
//        ): void{
//}
function testHasReturnType2(
        ): void{
}

//function testHasReturnType3($param1, int $param2): void{
//}
function testHasReturnType3($param1, int $param2): void{
}

//function testHasReturnType(int $param1,
//        Test &$param2,
//): void{
//}
function testHasReturnType4(int $param1,
        Test &$param2,
): void{
}

//$labmda = function () {
//};
$labmda = function () {
};

//$labmda = function (
//        ) {
//};
$labmda = function (
        ) {
};

//$labmda = function (int $param1, ...$pram2) {
//};
$labmda = function (int $param1, ...$pram2) {
};

//$labmda = function (
//        int $param1,
//        ...$pram2) {
//};
$labmda = function (
        int $param1,
        ...$pram2) {
};

// has return type
//$labmda = function (): int {
//};
$labmda = function (): int {
};

//$labmda = function (
//        ): string {
//};
$labmda = function (
        ): string {
};

//$labmda = function (int $param1, ...$pram2): int {
//};
$labmda = function (int $param1, ...$pram2): int {
};

//$labmda = function (
//        int $param1,
//        ...$pram2): string {
//};
$labmda = function (
        int $param1,
        ...$pram2): string {
};

// has lexical variables
//$labmda = function () use ($lexical1, $lexical2) {
//    
//};
$labmda = function () use ($lexical1, $lexical2) {
    
};

//$labmda = function (&$param1) use ($lexical1, $lexical2) {
//    
//};
$labmda = function (&$param1) use ($lexical1, $lexical2) {
    
};

//$labmda = function () use (
//        $lexical1, $lexical2) {
//};
$labmda = function () use (
        $lexical1, $lexical2) {
};

//$labmda = function ($param1, string $param2) use (
//        $lexical1, $lexical2,) {
//};
$labmda = function ($param1, string $param2) use (
        $lexical1, $lexical2,) {
};

//$labmda = function (
//        $param1, string $param2) use (
//        $lexical1, $lexical2,) {
//};
$labmda = function (
        $param1, string $param2) use (
        $lexical1, $lexical2,) {
};

// has return type & lexical variables
//$labmda = function ($param1, $pram2, ) use ($lexical1, $lexical2,): int {
//};
$labmda = function ($param1, $pram2, ) use ($lexical1, $lexical2,): int {
};

//$labmda = function (
//        $param1, $pram2, ) use ($lexical1, $lexical2,): int {
//};
$labmda = function (
        $param1, $pram2, ) use ($lexical1, $lexical2,): int {
};

//$labmda = function (
//        $param1, $pram2, ) use (
//        $lexical1, $lexical2,): int {
//};
$labmda = function (
        $param1, $pram2, ) use (
        $lexical1, $lexical2,): int {
};

class Test {
//    public function __construct(private int $test1, private string $test2) {
//        
//}
    public function __construct(private int $test1, private string $test2) {
        
}

//public function __construct(
//            private int $test1, private string $test2) {
//        
//}
public function __construct(
            private int $test1, private string $test2) {
        
}

//public function testNoReturnType1() {
//}
public function testNoReturnType1() {
}

//public function testNoReturnType2(
//){
//}
public function testNoReturnType2(
){
}

//public function testNoReturnType3($param1, &$param2){
//}
public function testNoReturnType3($param1, &$param2){
}

//public function testNoReturnType4(
//        $param1,
//        $param2
//){
//}
public function testNoReturnType4(
        $param1,
        $param2
){
}

//public function testHasReturnType1(): void{
//}
public function testHasReturnType1(): void{
}

//public function testHasReturnType2(
//        ): void{
//}
public function testHasReturnType2(
        ): void{
}

//public function testHasReturnType3($param1, int $param2): void{
//}
public function testHasReturnType3($param1, int $param2): void{
}

//public function testHasReturnType4(int $param1,
//        Test &$param2,
//): void{
//}
public function testHasReturnType4(int $param1,
        Test &$param2,
): void{
}

}

interface TestInterface {

//public function testNoReturnType1();
public function testNoReturnType1();

//public function testNoReturnType2(
//);
public function testNoReturnType2(
);

//public function testNoReturnType3($param1, &$param2);
public function testNoReturnType3($param1, &$param2);

//public function testNoReturnType4(
//        $param1,
//        $param2
//);
public function testNoReturnType4(
        $param1,
        $param2
);

//public function testHasReturnType1(): void;
public function testHasReturnType1(): void;

//public function testHasReturnType2(
//        ): void;
public function testHasReturnType2(
        ): void;

//public function testHasReturnType3($param1, int $param2): void;
public function testHasReturnType3($param1, int $param2): void;

//public function testHasReturnType4(int $param1,
//        Test &$param2,
//): void;
//}
public function testHasReturnType4(int $param1,
        Test &$param2,
): void;
}
