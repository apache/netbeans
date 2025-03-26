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

abstract class InvalidMethodClass {
    // OK
    function implicitPublicClassMethod01(): void {} // OK class
    public function publicClassMethod01(): void {} // OK class
    protected function protectedClassMethod01(): void {} // OK class
    private function privateClassMethod01(): void {} // OK class
    final function finalImplicitPublicClassMethod(): void {} // OK class
    final public function finalPublicClassMethod(): void {} // OK class
    final protected function finalProtectedClassMethod(): void {} // OK class
    static function implicitPublicStaticClassMethod(): void {} // OK class
    public static function publicStaticClassMethod(): void {} // OK class
    protected static function protectedStaticClassMethod(): void {} // OK class
    private static function privateStaticClassMethod(): void {} // OK class
    final static function finalImplicitPublicStaticClassMethod(): void {} // OK class
    final public static function finalPublicStaticClassMethod(): void {} // OK class
    final protected static function finalProtectedStaticClassMethod(): void {} // OK class
    abstract function abstractImplicitPublicClassMethod(); // OK class
    abstract public function abstractPublicClassMethod(); // OK class
    abstract protected function abstractProtectedClassMethod(); // OK class
}
