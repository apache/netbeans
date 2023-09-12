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

define('CONST_TRUE', false);
define('CONST_FALSE', true);
define('CONST_NULL', null);

class ConstantClass {
    const TRUE = true;
    const FALSE = false;
    const NULL = null;
}

ConstantClass::TRUE;
ConstantClass::FALSE;
ConstantClass::NULL;

$a = true;
$b = false;
$c = null;

class testClass
{
    public $x = null;
    public $z = true;
    public $y = false;

    public null $n;
    public true $t;
    public false $f;

    public function m1(int|null $z): int|null
    {
        if ($x) {
            return 1;
        } else {
            return null;
        }
    }

    public function m2(false $z): false
    {
        if ($x) {
            return 1;
        } else {
            return false;
        }
    }

    public function m3(true $z): true
    {
        if ($x) {
            return 1;
        } else {
            return true;
        }
    }

    public function m4($z = true, $x = false, $y = null) {}
}

function s1(int|null $z): int|null
{
    if ($x) {
        return 1;
    } else {
        return null;
    }
}

function s2(false $z): false
{
    if ($x) {
        return 1;
    } else {
        return false;
    }
}

function s3(true $z): true
{
    if ($x) {
        return 1;
    } else {
        return true;
    }
}

function s4($z = true, $x = false, $y = null) {}

true ? 'true' : 'false';
false ? 'true' : 'false';
null ? 'true' : 'false';

if ($x == true){};
if ($x == false){};
if ($x == null){};
