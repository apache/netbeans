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

namespace Test1 {
    class ConstructorPropertyPromotion
    {
        public function __construct(
            private int $field1,
            protected int|string|\Test2\Foo $field2,
            public ?string $field3 = "default value",
        ) {
            echo $field1;
            echo $field2;
            echo $field3;
        }

        public test(): void {
            $this->field1;
            $this->field2;
            $this->field3;
            var_dump($this->field1);
            var_dump($this->field2);
            var_dump($this->field3);
        }
    }
    $instance = new ConstructorPropertyPromotion(1, 2);
    $instance->field3;
}

namespace Test2 {
    class Foo {}
}