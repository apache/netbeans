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
 */
class Test {
    const IMPLICIT_PUBLIC_TEST_CONST = "implicit public test const";
    public const string PUBLIC_TEST_CONST = "public test const";
    protected const string PROTECTED_TEST_CONST = "protected test const";
    private const string PRIVATE_TEST_CONST = "private test const";

    public int $publicTestField = 1;
    protected int $protectedTestField = 2;
    private int $privateTestField = 3;

    public static string $publicStaticTestField = "public static test field";
    protected static string $protectedStaticTestField = "protected static test field";
    private static string $privateStaticTestField = "private static test field";

    public function publicTestMethod(): string {
        return "public test method";
    }

    protected function protectedTestMethod(): string {
        return "protected test method";
    }

    private function privateTestMethod(): string {
        return "private test method";
    }

    public static function publicStaticTestMethod(): string {
        return "public static test method";
    }

    protected static function protectedStaticTestMethod(): string {
        return "protected static test method";
    }

    private static function privateStaticTestMethod(): string {
        return "private static test method";
    }
}

