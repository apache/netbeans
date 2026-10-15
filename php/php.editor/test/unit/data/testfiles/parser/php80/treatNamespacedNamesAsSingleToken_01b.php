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

namespace test\require;

const CONSTANT = "test";
function test() {}

namespace test\function;
function test() {}

namespace readOnly;

function test() {}

namespace match;

function test() {}

namespace Test;

use test\require;
use \test\function;
use function readOnly\test as aliasTest1;
use function \test\function\test as aliasTest2;
test\require\test();
namespace\test\require\test();
\test\require\test();
test\require\CONSTANT;
\test\require\CONSTANT;
namespace\test\require\CONSTANT;
readOnly\test();
\readOnly\test();
namespace\readOnly\test();
