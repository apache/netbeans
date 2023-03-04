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
enum Simple {
case   A;
case      B;
case C;
const CONSTANT1 = 1;

public function test(int $test): void {
}

public static function staticTest(Foo&Bar $param): void {
}
}
#[Attr]
enum WithAttributes
{
#[AAA]
case Foo1;
#[AAA]
case Foo2;
#[AAA]
const Bar = 2;
}

enum WithDoc {
/**
 * test
 */
case Foo1;
/**
 * test
 */
case Foo2;
/**
 * test
 */
const Bar = 2;
}

enum BackingTypeInt      : int {
const CONSTANT = 1;
case A =   self::CONSTANT;
  case   B = 2;
}

enum BackingTypeString:string{
const CONSTANT = "test";
/**
 * test
 */
case    A    =    self::CONSTANT;
/**
 * test
 */
#[AAA]
case    B=    "testB";
}
