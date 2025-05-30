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

class base {

    public function __construct() {
    }

    public function __call(string $name, array $arguments): mixed {
    }

    public static function __callStatic(string $name, array $arguments): mixed {
    }

    public function __clone(): void {
    }

    public function __debugInfo(): array {
    }

    public function __destruct() {
    }

    public function __get(string $name): mixed {
    }

    public function __invoke(): mixed {
    }

    public function __isset(string $name): bool {
    }

    public function __serialize(): array {
    }

    public function __set(string $name, mixed $value): void {
    }

    public static function __set_state(array $properties): object {
    }

    public function __sleep(): array {
    }

    public function __toString(): string {
        return "Test";
    }

    public function __unserialize(array $data): void {
    }

    public function __unset(string $name): void {
    }

    public function __wakeup(): void {
    }
}

class TestOverride extends base {
    __
}
