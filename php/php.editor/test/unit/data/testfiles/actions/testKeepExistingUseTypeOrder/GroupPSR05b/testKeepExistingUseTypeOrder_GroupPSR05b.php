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

namespace NS\Test;

use function NS2\{
    ns2Function1,
    ns2Function2
};

ns1Function();
ns2Function1();
ns2Function2();
$const1 = NS1_CONSTANT;
$const2 = NS2_CONSTANT1;
$const2 = NS2_CONSTANT2;

class Test implements NS1Interface, NS2Interface {
    public const CONSTANT1 = NS1Enum::Case1;
    public const CONSTANT2 = NS2Enum::Case1;
    use NS1Trait;
    use NS2Trait;
    public function method(): void {
        $ns1 = new NS1Class();
        $ns2 = new NS2Class();
    }
}

namespace NS1;

function ns1Function() {}

const NS1_CONSTANT = "NS1_CONSTANT";

class NS1Class {}
interface NS1Interface {}
trait NS1Trait {}
enum NS1Enum {
    case CASE1;
}

namespace NS2;

function ns2Function1() {}
function ns2Function2() {}

const NS2_CONSTANT1 = "NS2_CONSTANT1";
const NS2_CONSTANT2 = "NS2_CONSTANT2";

class NS2Class {}
interface NS2Interface {}
trait NS2Trait {}
enum NS2Enum {
    case CASE1;
}

