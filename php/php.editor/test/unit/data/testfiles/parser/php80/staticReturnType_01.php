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

class Foo {
}

class StaticReturnType
{
    public function staticReturnType(int $param): static {
        return new static;
    }

    public function staticReturnNullableType(int $param): ?static {
        return new static;
    }

    public static function staticReturnUnionType(): self|static {
        return new static;
    }
}

trait TestTrait {

    public function traitStaticReturnType(): static {
        return new static;
    }

    public function traitStaticReturnNullableType(): ?static {
        return new static;
    }

    public static function traitStaticReturnUnionType(): static|null|Foo {
        return new static;
    }

}

$closure = function(): static {
    return new static;
};

$af = fn(): static => new static;

// the parser is not recoginze as an error
// handle this as a hint error
function staticReturnType(): static {
}
