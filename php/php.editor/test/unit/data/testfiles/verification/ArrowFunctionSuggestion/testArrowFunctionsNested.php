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

$variable1 = 100;

fn($param1): ?int => fn($param2) => $param1 + $param2 + $variable1;
function($param1) use ($variable1): ?int {
    return fn($param2) => $param1 + $param2 + $variable1;
};
function($param1) use ($variable1): ?int { // 02
    return function ($param2) use ($param1, $variable1) {
        return $param1 + $param2 + $variable1;
    };
}; // 02
