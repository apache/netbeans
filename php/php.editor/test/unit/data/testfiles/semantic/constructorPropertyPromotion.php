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
class ConstructorPropertyPromotionClass1 {

    public function __construct(
        $mandatory,
        private ?int $optional1 = 1,
        public int $optional2 = 1,
        protected int|string $optional3 = 1,
    ) {
    }

}

class ConstructorPropertyPromotionClass2 {

    private int $mandatory;
    public function __construct(
        $mandatory,
        private ?int $optional1 = 1,
        public int $optional2 = 1,
        protected int|string $optional3 = 1,
    ) {
    }

    public function test() {
        $this->optional1;
    }

}

trait ConstructorPropertyPromotionTrait1 {

    private int $mandatory;
    public function __construct(
        $mandatory,
        private ?int $optional1 = 1,
        public int $optional2 = 1,
        protected int|string $optional3 = 1,
    ) {
    }

}

trait ConstructorPropertyPromotionTrait2 {

    private int $mandatory;
    public function __construct(
        $mandatory,
        private ?int $optional1 = 1,
        public int $optional2 = 1,
        protected int|string $optional3 = 1,
    ) {
    }

    public function test() {
        $this->optional1;
    }

}

$anon1 = new class(1) {

    private int $mandatory;
    public function __construct(
        $mandatory,
        private ?int $optional1 = 1,
        public int $optional2 = 1,
        protected int|string $optional3 = 1,
    ) {
    }

};

$anon2 = new class(1) {

    private int $mandatory;
    public function __construct(
        $mandatory,
        private ?int $optional1 = 1,
        public int $optional2 = 1,
        protected int|string $optional3 = 1,
    ) {
    }

    public function test() {
        $this->optional1;
    }

};

