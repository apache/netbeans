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

// anonymous class
$anon = new class() {
    // OK
    function implicitPublicAnonClassMethod01(): void {} // OK anon class
    public function publicAnonClassMethod01(): void {} // OK anon class
    protected function protectedAnonClassMethod01(): void {} // OK anon class
    private function privateAnonClassMethod01(): void {} // OK anon class
    final function finalImplicitPublicAnonClassMethod(): void {} // OK anon class
    final public function finalPublicAnonClassMethod(): void {} // OK anon class
    final protected function finalProtectedAnonClassMethod(): void {} // OK anon class
    static function implicitPublicStaticAnonClassMethod(): void {} // OK anon class
    public static function publicStaticAnonClassMethod(): void {} // OK anon class
    protected static function protectedStaticAnonClassMethod(): void {} // OK anon class
    private static function privateStaticAnonClassMethod(): void {} // OK anon class
    final static function finalImplicitPublicStaticAnonClassMethod(): void {} // OK anon class
    final public static function finalPublicStaticAnonClassMethod(): void {} // OK anon class
    final protected static function finalProtectedStaticAnonClassMethod(): void {} // OK anon class
};
