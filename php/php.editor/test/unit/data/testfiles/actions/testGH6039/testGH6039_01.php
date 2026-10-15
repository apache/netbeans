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
namespace NS\GH6039;

class TestClass1{};
class TestClass2{};
class TestClass3{};
class TestClass4{};

namespace Test;

class Example {
    /**
     * @return array<int> Description
     */
    public function gh6039_01(): array {
        return [];
    }

    /**
     * @return array<int, string> Description
     */
    public function gh6039_02(): array {
        return [];
    }

    /**
     * @return Example<int> Description
     */
    public function gh6039_03(): Example {
        return $this;
    }

    /**
     * @return int<0, 100> Description
     */
    public function gh6039_04(): Example {
        return 1;
    }

    /**
     * @return array{int, int} Description
     */
    public function gh6039_05(): array {
        return [];
    }
}
