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

// no parameters
$array = [
"test", function() {
echo "test";
},
];

$array = [
"test", function() use (
    $var1,
    $var2
        ){
echo "test";
},
];

$array = [
"test", function()
    use (
    $var1,
    $var2
        ){
echo "test";
},
];

$array = [
"test", function() use (
    $var1,
    $var2
        ): void {
echo "test";
},
];

$array = [
"test", function()
    use (
    $var1,
    $var2
        ): void {
echo "test";
},
];

$array = [
    "test",
function() {
 echo "test";
    },
];

$array = [
"test",
function() use (
    $var1,
    $var2
        ){
 echo "test";
    },
];

    $array = [
"test",
function()
        use (
    $var1,
    $var2
        ){
 echo "test";
    },
];

    $array = [
"test",
function() use (
    $var1,
    $var2
        ): void{
 echo "test";
    },
];

    $array = [
"test",
function() 
        use (
    $var1,
    $var2
        ): void{
 echo "test";
    },
];

// has parameters
    $array = [
"test", function(
    $param1,
    $param2,
    ) {
echo "test";
}, $test,
];

$array = [
"test", function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ) {
echo "test";
},
];

$array = [
"test", function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ): void{
echo "test";
},
        $test
];

$array = [
    "test",
    function(
    $param1,
    $param2,
    ) {
        echo "test";
    },
];

    $array = [
    "test",
    function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ){
        echo "test";
    },
];

$array = [
    "test",
    function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ) : void{
        echo "test";
    },
];

// key => value
// no parameters
$array = [
"test", "key" => function() {
echo "test";
}, $test
];

$array = [
"test", "key" => function() use (
    $var1,
    $var2
        ){
echo "test";
},
];

$array = [
"test", "key" => function()
    use (
    $var1,
    $var2
        ){
echo "test";
},
];

$array = [
"test", "key" => function() use (
    $var1,
    $var2
        ): void {
echo "test";
},
];

$array = [
"test", "key" => function()
    use (
    $var1,
    $var2
        ): void {
echo "test";
},
];

$array = [
    "test",
"key" => function() {
 echo "test";
    },
];

$array = [
"test",
"key" => function() use (
    $var1,
    $var2
        ){
 echo "test";
    },
];

    $array = [
"test",
"key" => function()
        use (
    $var1,
    $var2
        ){
 echo "test";
    },
];

    $array = [
"test",
"key" => function() use (
    $var1,
    $var2
        ): void{
 echo "test";
    },
];

    $array = [
"test",
"key" => function() 
        use (
    $var1,
    $var2
        ): void{
 echo "test";
    },
];

// has parameters
    $array = [
"test", "key" => function(
    $param1,
    $param2,
    ) {
echo "test";
}, $test,
];

$array = [
"test", "key" => function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ) {
echo "test";
},
];

$array = [
"test", "key" => function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ): void{
echo "test";
},
        $test
];

$array = [
 "test",
 "key" => function(
    $param1,
    $param2,
    ) {
        echo "test";
    },
];

    $array = [
    "test",
    "key"  =>  function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ){
        echo "test";
    },
            "key2" => "value2"
];

$array = [
  "test",
 "key" => function(
    $param1,
    $param2,
    ) use (
        $var1,
        $var2
            ) : void{
        echo "test";
    }, $test => "value",
];
