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

$new1 = []; // don't fold
$new2 = [
    "not empty" => [1, 2],
    "empty" => [], // don't fold
];
$new3 = [
    1,
    2,
    [], // don't fold
    [1, 2, 3],
];
$new4 = [1, 2];
$new5 = array_merge([], [1, 2]);
$new6 = ["a" => [1, 2], "b", "c" => []];

$old1 = array(); // don't fold
$old2 = array(
    "not empty" => array(1, 2),
    "empty" => array(), // don't fold
);
$old3 = array(
    1,
    2,
    array(), // don't fold
    array(1, 2, 3),
);
$old4 = array(1, 2);
$old5 = array_merge(array(), array(1, 2));
$old6 = ["a" => array(1, 2), "b", "c" => array()];
