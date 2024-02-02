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

class B extends A {}
class C extends A {}

class ClassTest {
    public const WITHOUT_TYPE = 1;
    public const ?A NULLABLE = null;
    public const ?int NULLABLE2 = 1;
    private const A|B UNION = A;
    protected const A&B INTERSECTION = B;
    public const (A&B)|C DNF = C;
    public const string STRING = 'a';
    public const int INT = 1;
    public const float FLOAT = 1.5;
    public const bool BOOL = true;
    public const array ARRAY = ['t', 'e', 's', 't'];
    public const iterable ITERABLE = ['a', 'b', 'c'];
    public const mixed MIXED = 1 + self::WITHOUT_TYPE;
    public const object OBJECT = A;
    public const string|array UNION2 = 'a' . InterfaceTest::STRING;
    public const int|null UNION3 = null;
}

interface InterfaceTest {
    const string STRING = "string"; // interface
    public const ?int NULLABLE = 1; // interface
    public const A|B UNION = A; // interface
    public const A&B INTERSECTION = B; // interface
    public const (A&B)|C DNF = C; // interface
}

trait TraitTest {
    const string STRING = "string"; // trait
    public const ?int NULLABLE = 1; // trait
    private const A|B UNION = A; // trait
    protected const A&B INTERSECTION = B; // trait
    public const (A&B)|C DNF = C; // trait
}

enum EnumTest {
    const string STRING = "string"; // enum
    public const ?int NULLABLE = 1; // enum
    private const A|B UNION = A; // enum
    protected const A&B INTERSECTION = B; // enum
    public const (A&B)|(A&C) DNF = C; // enum
    public const static A = EnumTest::Test; // enum

    case Test;
}

define("A", new A());
define("B", new B());
define("C", new C());

var_dump(ClassTest::WITHOUT_TYPE);
var_dump(ClassTest::NULLABLE);
var_dump(ClassTest::UNION);
var_dump(ClassTest::INTERSECTION);
var_dump(ClassTest::DNF);
var_dump(ClassTest::STRING);
var_dump(ClassTest::INT);
var_dump(ClassTest::FLOAT);
var_dump(ClassTest::BOOL);
var_dump(ClassTest::ARRAY);
var_dump(ClassTest::ITERABLE);
var_dump(ClassTest::MIXED);
var_dump(ClassTest::OBJECT);
var_dump(ClassTest::UNION2);
var_dump(ClassTest::UNION3);
