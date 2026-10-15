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

interface TestInterface {
    public function interfaceMethod(): void;
}

abstract class AbstractClass {
    public function classMethod(): void {}
    public abstract function abstractPublicClassMethod(): void;
    protected abstract function abstractProtectedClassMethod(): string;
    public abstract static function abstractPublicStaticClassMethod(): int;
    protected abstract static function abstractProtectedStaticClassMethod(): int;
}

trait AbstractTrait {
    public function traitMethod(): void {}
    public abstract function abstractPublicTraitMethod(): void;
    protected abstract function abstractProtectedTraitMethod(): string;
    private abstract function abstractPrivateTraitMethod(): string;
    public abstract static function abstractPublicStaticTraitMethod(): int;
    protected abstract static function abstractProtectedStaticTraitMethod(): int;
    private abstract static function abstractPrivateStaticTraitMethod(): int;
}

class Implement extends AbstractClass implements TestInterface {
    use AbstractTrait;
}
