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

$billion1 = 1000000000;
$billion2 = 1_000_000_000;

const CONSTANT1 = 123456789.88;
const CONSTANT2 = 123_456_789.88;
const CONSTANT1 = 1_234_5678_9.88_888_8_88;

$value1 = 13500;
$value2 = 135_00;

.88;
.88_00;
.88_000_0;
33.;
33_44.;
33_44_5_6666.;

1.234567e-11; // float
1.234_567e-11; // float
1.2_34_567e-1_1; // float
1.2_3456_7E1_11; // float

123456789; // decimal
123_456_789; // decimal
12_3456_7_89; // decimal

0x1234ABCD; // hexadecimal
0x1234_ABCD; // hexadecimal
0x1_23_4ABC_D; // hexadecimal

0b01011111; // binary
0b0101_1111; // binary
0b010_11_1_11; // binary

0123456; // octal
0123_456; // octal
01_2_345_6; // octal

_100; // constant name
