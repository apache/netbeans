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
namespace EnumTestNamespace;
function returnType(): Enum1 {
}

enum EnumTest {
    public function publicMethod1(): Enum1 {
    }
    public function publicMethod2(): Enum1|Enum2 {
    }
    public static function publicMethod3(): Enum1&Enum2 {
    }
    public function publicMethod4(): ?Enum1 {
    }

}
enum Enum1 {
    case CASE1;
}

enum Enum2 {
    case CASE1;
}

