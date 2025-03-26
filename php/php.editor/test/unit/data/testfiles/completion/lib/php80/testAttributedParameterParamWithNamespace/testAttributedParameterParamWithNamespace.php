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
    const CONST_GLOBAL = "const";
    define("CONST_DEFINE", "define");
}

namespace Attributes\Test1 {
    const CONST_ATTRIBUTES_TEST1  = "const";
    #[Attribute]
    class Attr1 {
        public const CONST_ATTR1 = "test";
        public function __construct(int $int, string $string) {
        }
    }

    #[\Attribute]
    class Attr2 {
        public const CONST_ATTR2 = true;
        public function __construct(string $string, bool $bool = true) {
        }
    }
}

namespace Attributes\Test2 {
    use Attributes\Test1\Attr1;
    use Attributes\Test1\Attr2;
    use const Attributes\Test1\CONST_ATTRIBUTES_TEST1;

    class ExampleClass {
        public function method(#[Attr2(string: CONST_ATTRIBUTES_TEST1, bool: Attr2::CONST_ATTR2)] int $int): void {
        }

        public static function staticMethod(
                int $int,
                #[Attr2(\CONST_GLOBAL, bool: Attr2::CONST_ATTR2)] string $string,
        ): int {
            return 0;
        }
    }
}
