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

class ConstructorPropertyPromotion {

    public function __construct(
        public $param1,
        protected int $param2,
        private int|string $param3,
        private float &$param4 = 1,
        private ?string $param5 = null,
        public string $param6 = "default value",
        public string|int $param7 = "default value",
    ) {
        echo $this->param2 = $param3;
    }

}

class LegacySyntax {
    public $param1;
    protected int $param2;
    private int|string $param3;
    private float $param4;
    private ?string $param5;
    public string $param6;
    public string|int $param7;

    public function __construct(
        $param1,
        int $param2,
        int|string $param3,
        float &$param4 = 1,
        ?string $param5 = null,
        string $param6 = "default value",
        string|int $param7 = "default value",
    ) {
        $this->param1 = $param1;
        $this->param2 = $param2;
        $this->param3 = $param3;
        $this->param4 = $param4;
        $this->param5 = $param5;
        $this->param6 = $param6;
        $this->param7 = $param7;
    }

}

class ConstructorPropertyPromotionClass2 {

    public function __construct(
        $param1,
        public int $param2,
        string $param3 = "default value",
        public string $param4 = "default value",
    ) {
    }

}

$anon = new class (1) {
    public function __construct(
        public int $x,
        public int $y = 0,
    ) {
    }
};

trait ConstructorPropertyPromotionTrait {

    public function __construct(
        public $param1,
        protected int $param2,
        private int|string $param3,
        private float &$param4 = 1,
        private ?string $param5 = null,
        public string $param6 = "default value",
        public string|int $param7 = "default value",
    ) {
        echo $this->param2 = $param3;
    }

}

new ConstructorPropertyPromotion(1, 2, 3);