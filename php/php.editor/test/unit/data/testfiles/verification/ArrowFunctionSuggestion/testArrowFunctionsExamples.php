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

$y = 5;

function arrray_map_example($array, $keys) {
    return array_map(function ($x) use ($array) { return $array[$x]; }, $keys);
}

$test = function ($param) use ($callable1, $callable2) {
    return $callable1($callable2($param), $param);
};

$instance->filed = array_filter($tests, function ($needle) use ($array) {
    return in_array($needle, $array);
});

function test(callable $callable) {
    return function (...$args) use ($callable) {
        return !$callable(...$args);
    };
}

$result = TestClass::test([10, 20])
    ->test1(function ($param) {
        return $param * 100;
    })
    ->test2(function ($param1, $param2) {
        return $param1 + $param2;
    }, 0);
