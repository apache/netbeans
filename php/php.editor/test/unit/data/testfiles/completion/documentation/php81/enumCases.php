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

enum EnumCase {
    case CASE_A;
    case CASE_B = 1; // fatal error: non-backed enum must not have a value
}

enum BackeEnumCaseString: string {
    case CASE_A = "string";
    case CASE_B = self::CASE_A->value. "string";
    case CASE_C; // fatal error: backed enum must have error
}

enum BackeEnumCaseInt: int {
    case CASE_A = 1;
    case CASE_B = self::CASE_A->value + 2;
    case CASE_C; // fatal error: backed enum must have error
}

EnumCase::CASE_A;
EnumCase::CASE_B;
BackeEnumCaseString::CASE_A;
BackeEnumCaseString::CASE_B;
BackeEnumCaseString::CASE_C;
BackeEnumCaseInt::CASE_A;
BackeEnumCaseInt::CASE_B;
BackeEnumCaseInt::CASE_C;
