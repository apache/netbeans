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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

require __DIR__ . '/../vendor/autoload.php';

use Tester\Assert;
use Tester\Environment;

Environment::setup();

$calculator = new Calculator();

Assert::same(0, $calculator->plus(0, 0));
Assert::same(1, $calculator->plus(0, 1));
Assert::same(1, $calculator->plus(1, 0));
Assert::same(2, $calculator->plus(1, 1));
