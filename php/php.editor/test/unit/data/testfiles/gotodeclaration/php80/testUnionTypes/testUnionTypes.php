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

namespace Union\Types1;

use Union\Types2\MyTrait;
use Union\Types2\TestClass1;
use Union\Types2\TestClass2;

class ParentClass
{
    use MyTrait;
}

class ChildClass extends ParentClass
{
    private ParentClass|ChildClass $field;
    private static \Union\Types2\TestClass1|TestClass2 $staticField;

    public function testMethod(ParentClass|ChildClass|null $param): ChildClass|\Union\Types1\ParentClass {
        return new ChildClass;
    }

    public static function testStaticMethod(TestClass1|\Union\Types2\TestClass2|null $param1, ChildClass|null $param2): TestClass2|ParentClass|null {
        return new ParentClass();
    }
}

namespace Union\Types2;

class TestClass1
{
}

class TestClass2
{
}

trait MyTrait
{
    public function traitMethod(TestClass1|TestClass2 $param): TestClass1|TestClass2|null {
        return null;
    }
}
