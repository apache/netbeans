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

$a = $a === null ? true : false;
$a = $a === true ? false : null;
$a = $a === false ? null : true;

$a = $a === null
        ? true
        : false;
$a = $a === true
        ? false
        : null;
$a = $a === false
        ? null
        : true;

test(true: $a === null ? true : false, false: $a === null ? false : true, null: $a === true ? null : false);
test(
        true: $a === null ? true : false,
        false: $a === null ? false : true,
        null: $a === true ? null : false
);

#[A(true: 1, false: 2, null: 3)]
#[A(false: 1, null: 2, true: 3)]
#[A(null: 1, true: 2, false: 3)]
#[A(
    true: 1,
    false: 2,
    null: 3
)]
#[
    A(
        false: 1,
        null: 2,
        true: 3
    )
]
#[
    A(
        null: 1,
        true: 2,
        false: 3
    )
]
class TrueFalseNull {}
