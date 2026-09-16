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
namespace Test;

use PropertyHooks\Test\AbstractClass;
use PropertyHooks\Test\Trait1;
use PropertyHooks\Test\Trait3;
use PropertyHooks\Test\Interface1;

abstract class AbstractTest extends AbstractClass {
    public int $prop = 100;
}

class Child extends AbstractTest implements Interface1{
    use Trait1, Trait3;
    public int $prop {
        get => parent::$prop::get();
        set {
            parent::$prop::set($value);
        }
    }
}
