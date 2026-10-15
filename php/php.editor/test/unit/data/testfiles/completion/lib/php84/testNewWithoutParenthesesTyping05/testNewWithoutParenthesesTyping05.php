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
class Example {
    const IMPLICIT_PUBLIC_CONST = "implicit public const";
    public const string PUBLIC_CONST = "public const";
    protected const string PROTECTED_CONST = "protected const";
    private const string PRIVATE_CONST = "private const";

    public int $publicField = 1;
    protected int $protectedField = 2;
    private int $privateField = 3;
    public Test $test;

    public static string $publicStaticField = "public static field";
    protected static string $protectedStaticField = "protected static field";
    private static string $privateStaticField = "private static field";

    public function __construct() {
        $this->test = new Test();
    }

    public function test(): string|int {
        $example = new Example()->
        return $example;
    }

    public function returnThis(): self {
        return $this;
    }

    public function publicMethod(): string {
        return "public method";
    }

    protected function protectedMethod(): string {
        return "protected method";
    }

    private function privateMethod(): string {
        return "private method";
    }

    public static function publicStaticMethod(): string {
        return "public static method";
    }

    protected static function protectedStaticMethod(): string {
        return "protected static method";
    }

    private static function privateStaticMethod(): string {
        return "private static method";
    }
}
