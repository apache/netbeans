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

trait InvalidMethodTrait {
    // OK
    function implicitPublicTraitMethod01(): void {} // OK trait
    public function publicTraitMethod01(): void {} // OK trait
    protected function protectedTraitMethod01(): void {} // OK trait
    private function privateTraitMethod01(): void {} // OK trait
    final function finalImplicitPublicTraitMethod(): void {} // OK trait
    final public function finalPublicTraitMethod(): void {} // OK trait
    final protected function finalProtectedTraitMethod(): void {} // OK trait
    static function implicitPublicStaticTraitMethod(): void {} // OK trait
    public static function publicStaticTraitMethod(): void {} // OK trait
    protected static function protectedStaticTraitMethod(): void {} // OK trait
    private static function privateStaticTraitMethod(): void {} // OK trait
    final static function finalImplicitPublicStaticTraitMethod(): void {} // OK trait
    final public static function finalPublicStaticTraitMethod(): void {} // OK trait
    final protected static function finalProtectedStaticTraitMethod(): void {} // OK trait
    abstract function abstractImplicitPublicTraitMethod(); // OK trait
    abstract public function abstractPublicTraitMethod(); // OK trait
    abstract protected function abstractProtectedTraitMethod(); // OK trait
    abstract private function abstractPrivateTraitMethod(); // OK trait
}
