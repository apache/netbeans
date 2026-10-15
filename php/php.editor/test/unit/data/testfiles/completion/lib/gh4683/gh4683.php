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

/**
 * @property-read TestData $data
 */
class MixinClass {
    public const PUBLIC_CONSTANT = "public const";
    private const PRIVATE_CONSTANT = "private const";
    protected const PROTECTED_CONSTANT = "protected const";

    public static $publicStaticField;
    private static $privateStaticField;
    protected static $protectedStaticField;

    public function __construct(
        public readonly array $publicField,
        private array $privatecField,
        protected int $protectedField,
    ) {

    }

    public function publicMethod(): void {

    }

    private function privateMethod(): void {

    }

    protected function protectedMethod(): void {

    }

    public static function publicStaticMethod(): void {

    }

    private static function privateStaticMethod(): void {

    }

    protected static function protectedStaticMethod(): void {

    }
}

/**
 * @mixin MixinClass
 */
class TestClass {

    public function test(): void {
        $this->data;
        $this->data->publicField;
        $this->data::PUBLIC_CONSTANT;
    }

}

class TestData {

    public const PUBLIC_CONSTANT = "public const";
    private const PRIVATE_CONSTANT = "private const";
    protected const PROTECTED_CONSTANT = "protected const";

    public static $publicStaticField;
    private static $privateStaticField;
    protected static $protectedStaticField;

    public function __construct(
        public readonly array $publicField,
        private array $privatecField,
        protected int $protectedField,
    ) {

    }

    public function publicMethod(): void {

    }

    private function privateMethod(): void {

    }

    protected function protectedMethod(): void {

    }

    public static function publicStaticMethod(): void {

    }

    private static function privateStaticMethod(): void {

    }

    protected static function protectedStaticMethod(): void {

    }

}

$test = new TestClass();
$data = new TestData([], [], 0);

$test->data;
$test->data->publicField;
$test->data::PUBLIC_CONSTANT;
