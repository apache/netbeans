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

#[A(static: "test", default: self::CONSTANT)]
class NamedArgumentsWithKeyword
{
    public const CONSTANT = "CONSTANT";

    public function __construct($array, $default) {
    }

    public function test() {
        parent::staticReservedKeyword(array: [1, 2], default: "default");
        self::staticReservedKeyword(array: [1, 2], default: "default");
        static::staticReservedKeyword(array: [1, 2], default: "default");
        $this->reservedKeyword(string: "test", int: 1);

        // default keyword in switch statement
        switch ($test) {
            case self::CONSTANT:
                break;
            default: // default keyword
                break;
        }

        $result = match ($a) {
            1 => test(),
            default => 0,
        };
    }

    public function reservedKeyword() {}
    public static function staticReservedKeyword() {}
}

reservedKeyword(array: [1, 2], default: "default");
reservedKeyword(array: [1, reservedKeyword(default: "default")], default: "default");
reservedKeyword(default: reservedKeyWord(array: [0, 2]));

// default keyword in switch statement
switch ($test) {
    case $value:
        break;
    default: // default keyword
        break;
}

$result = match ($a) {
    1 => test(),
    default => 0,
};
