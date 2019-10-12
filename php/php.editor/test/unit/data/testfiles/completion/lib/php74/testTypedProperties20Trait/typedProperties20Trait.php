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
namespace Foo;
use Bar\MyClass;

trait TypedPropertiesTrait {

    public ?string $string;
    public MyClass $myClass;
    public ?MyClass $myClass2;
    private \Bar\MyClass $myClass3;
    protected ?\Bar\MyClass $myClass4;
    public static MyClass $staticMyClass;
    public $test;
    var string $string;
    public const CONSTANT = "constant";

    public function test(): void {
        $this->myClass->publicTestMethod();
        $this->myClass2->publicTestMethod();
        $this->myClass3->publicTestMethod();
        $this->myClass4->publicTestMethod();
        $this::$staticMyClass->publicTestMethod();
        $this->myClass::publicStaticTestMethod();
        $this->myClass2::publicStaticTestMethod();
        $this->myClass3::publicStaticTestMethod();
        $this->myClass4::publicStaticTestMethod();
    }

}

namespace Bar;
class MyClass {

    public function publicTestMethod(): void {
    }

    public static function publicStaticTestMethod(): void {
    }
}
