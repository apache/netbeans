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
enum E: string {
    case Case = 'E::Case';
    const C1 = [self::Case->value => self::Case];
    const C2 = [self::Case?->value => self::Case];
}

// global const
const NAME = E::Case->name;
const VALUE = E::Case->value;
const NAME_NULLSAFE = E::Case?->name;
const VALUE_NULLSAFE = E::Case?->value;

// class
#[Attr(E::Case->name)]
#[Attr(E::Case?->name)]
class ExampleClass {
    const NAME = E::Case->name;
    const VALUE = E::Case->value;
    const NAME_NULLSAFE = E::Case?->name;
    const VALUE_NULLSAFE = E::Case?->value;

    public string $name = E::Case->name;
    public string $value = E::Case->value;
    public string $nameNullsafe = E::Case?->name;
    public string $valueNullsafe = E::Case?->value;

    public static string $staticName = E::Case->name;
    public static string $staticValue = E::Case->value;
    public static string $staticNameNullsafe = E::Case?->name;
    public static string $staticValueNullsafe = E::Case?->value;
}

function test(
    // default value of parameter
    $name = E::Case->name,
    $value = E::Case->value,
    $nameNullsafe = E::Case?->name,
    $valueNullsafe = E::Case?->value,
) {
    // static variable
    static $staticName = E::Case->name;
    static $staticValue = E::Case->value;
    static $staticNameNullsafe = E::Case?->name;
    static $staticValueNullsafe = E::Case?->value;
}

enum ExampleEnum: string {
    case NAME = E::Case->name;
    case VALUE = E::Case->value;
    case NULLSAFE_NAME = E::Case?->name;
    case NULLSAFE_VALUE = E::Case?->value;
    case TEST1 = E::Case;
    case TEST2 = TEST1->name;
}

const VALUE = 'value';
class ReflectionVariableTest {
     const C = E::Case->{VALUE};
}
