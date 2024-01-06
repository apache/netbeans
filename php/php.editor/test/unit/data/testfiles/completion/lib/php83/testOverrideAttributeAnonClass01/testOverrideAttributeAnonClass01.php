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

namespace Test\Override\Attribute2;

interface TestInterface {
    public function testInterfaceMethod(): void;
}

abstract class AbstractClass {
    public function testClassMethod(): void {}
    public abstract function testAbstractPublicClassMethod(): void;
    protected abstract function testAbstractProtectedClassMethod(): string;
    public abstract static function testAbstractPublicStaticClassMethod(): int;
    protected abstract static function testAbstractProtectedStaticClassMethod(): int;
}

trait AbstractTrait {
    public function testTraitMethod(): void {}
    public abstract function testAbstractPublicTraitMethod(): void;
    protected abstract function testAbstractProtectedTraitMethod(): string;
    private abstract function testAbstractPrivateTraitMethod(): string;
    public abstract static function testAbstractPublicStaticTraitMethod(): int;
    protected abstract static function testAbstractProtectedStaticTraitMethod(): int;
    private abstract static function testAbstractPrivateStaticTraitMethod(): int;
}

$anon = new class () extends AbstractClass implements TestInterface {
    use AbstractTrait;

    test
};
