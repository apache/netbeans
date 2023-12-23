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

class ParentClass {

    const IMPLICIT_PUBLIC_PARENT_CONST = "parent";
    public const PUBLIC_PARENT_CONST = "parent";
    private const PRIVATE_PARENT_CONST = "parent";
    protected const PROTECTED_PARENT_CONST = "parent";

    public int $publicParentClassField;
    private int $privateParentClassField;
    protected int $protectedParentClassField;

    public static int $publicStaticParentClassField;
    private static int $privateStaticParentClassField;
    protected static int $protectedStaticParentClassField;

    public function publicParentClassMethod(int $param): void {
    }

    private function privateParentClassMethod(int $param1, string $param2): void {
    }

    protected function protectedParentClassMethod(int $param1, string $param2): void {
    }

    public static function publicStaticParentClassMethod(int $param): void {
    }

    private static function privateStaticParentClassMethod(int $param1, string $param2): void {
    }

    protected static function protectedStaticParentClassMethod(int $param1, string $param2): void {
    }
}
