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

namespace TestParent1;

interface InterfaceTest {

}

class ParentClass implements InterfaceTest {
}

class Child extends ParentClass {
    public function testParent(parent $parent): parent {
        return new ParentClass();
    }
    public function testUniontypes((Foo&Bar)|self $self, parent|(Foo&Bar) $parent): (Foo&Bar)|parent|self|null {
        return new ParentClass();
    }
}

class Foo{}
class Bar{}

namespace TestParent2;

use TestParent1\Child;
use TestParent1\ParentClass;

class Grandchild extends Child {
    test
}

$instance = new Grandchild();
$instance->test(new ParentClass());
var_dump($instance);
