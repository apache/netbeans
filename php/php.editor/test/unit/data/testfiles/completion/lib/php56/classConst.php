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

class Constants {

    const INDEX = 123;
    const ALICE = 'Alice';
    const PLANET = "Earth";
    const NO_KEYS = [ 44, 55, 66, 77 ];
    const WITH_KEYS = [ 1 => 'A', 2 => 'B' ];
    const LONG_ARRAY = [ 1 => 'Alice', 2 => 'Bob',  3 => 'Charlie', 4 => 'Dave', 5 => 'Eve', 6 => 'Frank' ];
    const CONST_REF = [ 1 => self::ALICE, self::INDEX => 'Bob' ];

    public function printConsts() {
        echo self::INDEX;
        echo self::ALICE;
        echo self::PLANET;
        echo self::NO_KEYS;
        echo self::WITH_KEYS;
        echo self::LONG_ARRAY;
        echo self::CONST_REF;
    }

}
