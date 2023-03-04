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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

class ClassExample1 {
    public function __construct() {}
}

class ClassExample2 {
    public function __construct(
            int $correct1,
            private $correct2,
            private &$correct3,
            public int $correct4,
            public ?string $correct5,
            public int|string $correct6,
            public int|string &$correct7,
            public ?string $correct8 = "default value",
    ) {}

    public function correct(
            $correct1,
            int $correct2,
            ?array $correct3,
            ?array &$correct3,
            string|array $correct5,
            $correct6 = "default value",
    ): void {}
}

class ClassExample3 {
    public function __construct(
            string ...$correct1,
    ) {}
}

class ClassExample4 {
    public function __construct(
            string &...$correct1,
    ) {}
}

abstract class AbstractClassExample1 {
    abstract public function __construct(
    );
}

abstract class AbstractClassExample2 {
    abstract public function __construct(
            $correct1,
            int $correct2,
            int &$correct3,
            ?array $correct4,
            string|array $correct5,
            $correct6 = "default value",
    );

    abstract public function correct(
            $correct1,
            int $correct2,
            ?array &$correct3,
            ?array $correct4,
            string|array $correct5,
            $correct6 = "default value",
    ): void;
}

abstract class AbstractClassExample3 {
    abstract public function __construct(
            string ...$correct1,
    );
}

abstract class AbstractClassExample4 {
    abstract public function __construct(
            string &...$correct1,
    );
}

interface InterfaceExample1 {
    public function __construct(
    );
}

interface InterfaceExample2 {
    public function __construct(
            $correct1,
            int $correct2,
            int &$correct3,
            ?array $correct4,
            string|array $correct5,
            $correct6 = "default value",
    );

    public function correct(
            $correct1,
            int $correct2,
            ?array &$correct3,
            ?array $correct4,
            string|array $correct5,
            $correct6 = "default value",
    ): void;
}

interface InterfaceExample3 {
    public function __construct(
            string ...$correct1,
    );
}

interface InterfaceExample4 {
    public function __construct(
            string &...$correct1,
    );
}

$anon1 = new class() {
    public function __construct() {}
};

$anon2 = new class() {
    public function __construct(
            int $correct1,
            private $correct2,
            private &$correct3,
            public int $correct4,
            public ?string $correct5,
            public int|string $correct6,
            public int|string &$correct7,
            public ?string $correct8 = "default value",
    ) {}

    public function correct(
            $correct1,
            int $correct2,
            ?array $correct3,
            ?array &$correct3,
            string|array $correct5,
            $correct6 = "default value",
    ): void {}
};

function functionExample1(
): void {
}

function functionExample2(
        $correct1,
        int $correct2,
        ?array &$correct3,
        ?array $correct4,
        string|array $correct5,
        $correct6 = "default value",
): void {
}

function functionExample3(
        string ...$correct1,
): void {
}

function functionExample4(
        string &...$correct1,
): void {
}

$labmda1 = function (
): void {};

$labmda2 = function (
        $correct1,
        int $correct2,
        ?array &$correct3,
        ?array $correct4,
        string|array $correct5,
        $correct6 = "default value",
): void {};

$labmda3 = function (
        string ...$correct1,
): void {};

$labmda4 = function (
        string &...$correct1,
): void {};

$arrow1 = fn() => "";

$arrow2 = fn(
        $correct1,
        int $correct2,
        ?array &$correct3,
        ?array $correct4,
        string|array $correct5,
        $correct6 = "default value",
) => "";

$arrow3 = fn(...$correct1) => "";

$arrow4 = fn(&...$correct1) => "";
