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

class Test {
    public const BAR = 'bar';
    public const BA = 'BA';
    public const R = 'R';
    public const A = self::{'BAR'};
    public const B = self::{'BA' . 'R'};
    public const C = self::{self::BA . self::R};
}

$bar = 'BAR';

Test::{"BAR"};
$test::{"BAR"};
Test::{$bar};
$test::{$bar};
Test::{$bar . $r};
$test::{$bar . $r};
Test::{strtoupper("bar")};
$test::{strtoupper("bar")};
Test::{'$barr'};
$test::{'$barr'};
Test::{strtolower("CLASS")};
$test::{strtolower("CLASS")};
Test::{100};
$test::{100};
Test::{[]};
$test::{[]};

Test::{foo()}::{bar()};
Test::{test('foo')}::{test('bar')};
