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

$a = $a === NULL ? TRUE : FALSE;
$a = $a === TRUE ? FALSE : NULL;
$a = $a === FALSE ? NULL : TRUE;

$a = $a === NULL
        ? TRUE
        : FALSE;
$a = $a === TRUE
        ? FALSE
        : NULL;
$a = $a === FALSE
        ? NULL
        : TRUE;

test(TRUE: $a === NULL ? TRUE : FALSE, FALSE: $a === NULL ? FALSE : TRUE, NULL: $a === TRUE ? NULL : FALSE);
test(
        TRUE: $a === NULL ? TRUE : FALSE,
        FALSE: $a === NULL ? FALSE : TRUE,
        NULL: $a === TRUE ? NULL : FALSE
);

#[A(TRUE: 1, FALSE: 2, NULL: 3)]
#[A(FALSE: 1, NULL: 2, TRUE: 3)]
#[A(NULL: 1, TRUE: 2, FALSE: 3)]
#[A(
    TRUE: 1,
    FALSE: 2,
    NULL: 3
)]
#[
    A(
        FALSE: 1,
        NULL: 2,
        TRUE: 3
    )
]
#[
    A(
        NULL: 1,
        TRUE: 2,
        FALSE: 3
    )
]
class TrueFalseNull {}
