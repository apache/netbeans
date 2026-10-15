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

class Test1
{
    public $test2 = 'Test2';
}

class Test2 {
    const CONSTANT = ['Test2' => 'Test'];
}

$test1 = new Test1;
$test = "Test2";

var_dump($test1->test2::CONSTANT);
//array(1) {
//  'Test2' =>
//  string(4) "Test"
//}

var_dump($test1->test2::CONSTANT[$test]);
// string(4) "Test"

var_dump($test1->test2::CONSTANT[$test1->test2]);
// string(4) "Test"
