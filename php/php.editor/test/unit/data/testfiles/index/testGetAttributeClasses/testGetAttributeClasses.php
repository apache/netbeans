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
namespace {
    #[\Attribute]
    class AttrGlobal1 {
        public function __construct(int $int, string $string) {
        }
    }
    #[Attribute]
    class AttrGlobal2 {
        public function __construct(int $int, string $string) {
        }
    }
}

namespace Attributes\A {
    #[\Attribute]
    class AttrA1 {
        public function __construct(int $int, string $string) {
        }
    }

    #[Attribute]
    class NotAttr1 {
        public function __construct(string $string, bool $bool = true) {
        }
    }

    #[\Attribute]
    class AttrA2 {
        public function __construct(string $string, bool $bool = true) {
        }
    }
}

namespace Attributes\B {
    use Attribute;

    #[Attribute]
    class AttrB1 {
        public function __construct(int $int, string $string) {
        }
    }
    #[\Attribute]
    class AttrB2 {}
    #[Attribute]
    class AttrB3 {}
}
