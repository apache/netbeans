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
class Example {}
$test = "Example";
$a = new Example();
$string = $a instanceof ((string) $test);
$double = $a instanceof ((double) $test);
$int = $a instanceof ((int) $test);
$object = $a instanceof ((object) $test);
$array = $a instanceof ((array) $test);
$bool = $a instanceof ((bool) $test);
$unset = $a instanceof ((unset) $test);
var_dump($boolean);

$b = 2;

$boolean2 = $b instanceof ((string) $test);

fn($test) => $a instanceof ($test);

fn($test) => $a instanceof ((int) $test);

var_dump($boolean);
