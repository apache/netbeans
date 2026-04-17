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
namespace Test;

use Attribute;

#[Attribute(Attribute::TARGET_CLASS)]
class SomeAttribute
{
    public function __construct(string $name) {}
}

#[SomeAttribute(name: self::TEST_CLASS)]
class TestClass
{
    private const string TEST_CLASS = 'test';
}

#[SomeAttribute(name: self::TEST_ENUM)]
enum TestEnum
{
    private const string TEST_ENUM = 'test';
}

#[SomeAttribute(name: self::TEST_TRAIT)]
trait TestTrait
{
    private const string TEST_TRAIT = 'test';
}

$anon = new #[SomeAttribute(name: self::TEST_ANON)] class() {
    private const string TEST_ANON = 'test';
};
