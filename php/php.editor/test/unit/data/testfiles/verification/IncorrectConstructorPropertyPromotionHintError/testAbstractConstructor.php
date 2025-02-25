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

abstract class AbstractClassExample1 {

    abstract public function __construct(private ?int $incorrect1 = 1); // error1

}

abstract class AbstractClassExample2 {

    abstract public function __construct(
            $test,
            private $incorrect1, // error2
            private ?int $incorrect2 = 1 // error3
    );

}

interface InterfaceExample1 {
    public function __construct(public int|string $incorrect1); // error4
}

interface InterfaceExample2 {
    public function __construct(int $test, protected $incorrect1); // error5
}

abstract class AbstractClassSetVisibilityExample1 {

    abstract public function __construct(private(set) ?int $incorrect1 = 1); // error6

}

abstract class AbstractClassSetVisibilityExample2 {

    abstract public function __construct(
            $test,
            private(set) $incorrect1, // error7
            private(set) ?int $incorrect2 = 1 // error8
    );

}

interface InterfaceSetVisibilityExample1 {
    public function __construct(public(set) int|string $incorrect1); // error9
}

interface InterfaceSetVisibilityExample2 {
    public function __construct(int $test, protected(set) $incorrect1); // error10
}
