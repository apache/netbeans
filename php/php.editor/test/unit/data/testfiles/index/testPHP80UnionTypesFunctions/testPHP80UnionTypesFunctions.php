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

function union_types(int|float $number, Foo|Bar|null $param): int|float|Foo {
    return 1;
}

function union_types(int|float $number, \Test1|Foo|Bar|null $param): int|float|\Test1\Foo {
    return 1;
}

$closure = function(int|float|null $number): Foo|Bar {
    return new Foo();
};

$closure2 = function(int|float|null $number): \Test1\Foo|\Test2\Bar {
    return new Foo();
};

$arrow = fn(int|float $param): string|false => "test";

$arrow2 = fn(int|float $param): \Test1\Foo|\Test2\Bar|string => "test";
