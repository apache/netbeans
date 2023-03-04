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

class C1
{
    public function __construct(
        public int $param, // C1
        public string $param = "default value", // C1
        private string|null $param = "default value", // C1
    ) {
    }
}

class C2
{
    private $param;
    public function __construct(
        public int $param, // C2
        public string $param = "default value", // C2
        private string|null $param = "default value", // C2
    ) {
    }
}

trait T1
{
    public function __construct(
        public int $param, // T1
        public string $param = "default value", // T1
        private string|null $param = "default value", // T1
    ) {
    }
}

trait T2
{
    private $param;
    public function __construct(
        public int $param, // T2
        public string $param = "default value", // T2
        private string|null $param = "default value", // T2
    ) {
    }
}

$anon1 = new class(1) {
    public function __construct(
        public int $param, // anon1
        public string $param = "default value", // anon1
        private string|null $param = "default value", // anon1
    ) {
    }
};

$anon2 = new class(1) {
    private $param;
    public function __construct(
        protected int $param, // anon2
        public string $param = "default value", // anon2
        private string|null $param = "default value", // anon2
    ) {
    }
};
