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

enum InvalidMethodEnum {
    // OK
    function implicitPublicEnumMethod01(): void {} // OK enum
    public function publicEnumMethod01(): void {} // OK enum
    protected function protectedEnumMethod01(): void {} // OK enum
    private function privateEnumMethod01(): void {} // OK enum
    final function finalImplicitPublicEnumMethod(): void {} // OK enum
    final public function finalPublicEnumMethod(): void {} // OK enum
    final protected function finalProtectedEnumMethod(): void {} // OK enum
    static function implicitPublicStaticEnumMethod(): void {} // OK enum
    public static function publicStaticEnumMethod(): void {} // OK enum
    protected static function protectedStaticEnumMethod(): void {} // OK enum
    private static function privateStaticEnumMethod(): void {} // OK enum
    final static function finalImplicitPublicStaticEnumMethod(): void {} // OK enum
    final public static function finalPublicStaticEnumMethod(): void {} // OK enum
    final protected static function finalProtectedStaticEnumMethod(): void {} // OK enum
}
