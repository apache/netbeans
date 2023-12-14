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

class ChildClass1 extends ParentClass {

    // override
    const IMPLICIT_PUBLIC_PARENT_CONST = "child";
    public const PUBLIC_PARENT_CONST = "child";
    protected const PROTECTED_PARENT_CONST = "child";

    // private
    private const PRIVATE_PARENT_CONST = "child";

    // child constants & fields
    const IMPLICIT_PUBLIC_CHILD_CONST = "child";
    public const PUBLIC_CHILD_CONST = "child";
    private const PRIVATE_CHILD_CONST = "child";
    protected const PROTECTED_CHILD_CONST = "child";

    public int $publicChildClassField;
    private int $privateChildClassField;
    protected int $protectedChildClassField;

    public static int $publicStaticChildClassField;
    private static int $privateStaticChildClassField;
    protected static int $protectedStaticChildClassField;

    public function publicChildClassMethod(int $param): void {
    }

    private function privateChildClassMethod(int $param1, string $param2): void {
    }

    protected function protectedChildClassMethod(int $param1, string $param2): void {
    }

    public static function publicStaticChildClassMethod(int $param): void {
    }

    private static function privateStaticChildClassMethod(int $param1, string $param2): void {
    }

    protected static function protectedStaticChildClassMethod(int $param1, string $param2): void {
    }
}
