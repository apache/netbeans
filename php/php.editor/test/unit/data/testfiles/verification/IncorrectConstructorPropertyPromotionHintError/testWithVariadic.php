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

class ClassExample1 {
    public function __construct(
            private ...$incorrect1, // error1
    ) {
    }
}

class ClassExample2 {
    public function __construct(
            int $test1,
            private string ...$incorrect1, // error2
    ) {
    }
}

$anon1 = new class(1) {
    public function __construct(
            protected(set) string ...$incorrect1, // error3
    ) {
    }
};

class ClassSetVisibilityExample1 {
    public function __construct(
            private(set) ...$incorrect1, // error4
    ) {
    }
}

class ClassSetVisibilityExample2 {
    public function __construct(
            int $test1,
            private(set) string ...$incorrect1, // error5
    ) {
    }
}

$anon1 = new class(1) {
    public function __construct(
            protected(set) string ...$incorrect1, // error6
    ) {
    }
};
