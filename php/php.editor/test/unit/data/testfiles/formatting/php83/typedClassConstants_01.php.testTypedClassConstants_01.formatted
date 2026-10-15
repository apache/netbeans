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

class A implements Stringable {

    public function __toString() {
        return static::class;
    }
}

class B extends A {
    
}

class C extends A {
    
}

class ClassTest {

    public const WITHOUT_TYPE = 1;
    public const ?int NULLABLE = 1;
    private const A|B UNION = D_A;
    protected const A&B INTERSECTION = D_B;
    public const (A&B)|C DNF = D_C;
    public const string STRING = 'a';
    public const int INT = 1;
    public const float FLOAT = 1.5;
    public const bool BOOL = true;
    public const array ARRAY = ['t', 'e', 's', 't'];
    public const iterable ITERABLE = ['a', 'b', 'c'];
    public const mixed MIXED = 1;
    public const object OBJECT = D_A;
    public const string|array UNION2 = 'a', UNION3 = ['a'];

    #[Attr]
    public const int|null UNION4 = null;
}

interface InterfaceTest {

    const string STRING = "string";
    public const ?int NULLABLE = 1;
    public const A|B UNION = D_A;
    public const A&B INTERSECTION = D_B;
    public const (A&B)|C DNF = D_C;
}

trait TraitTest {

    const string STRING = "string";
    public const ?int NULLABLE = 1;
    private const A|B UNION = D_A;
    protected const A&B INTERSECTION = D_B;
    public const (A&B)|C DNF = D_C;
}

enum EnumTest {

    const string STRING = "string";
    public const ?int NULLABLE = 1;
    private const A|B UNION = D_A;
    protected const A&B INTERSECTION = D_B;
    public const (A&B)|(A&C) DNF = D_C;
    public const static A = EnumTest::Test;

    case Test;
}

define("D_A", new A());
define("D_B", new B());
define("D_C", new C());

echo ClassTest::DNF . PHP_EOL;
var_dump(ClassTest::DNF);
