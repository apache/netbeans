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

class ClassTest {
    const callable TEST1 = T::TEST;
    const void TEST2 = T::TEST;
    const never TEST3 = T::TEST;
    const int|callable callable = T::TEST;
    const false false = T::TEST;
    const null null = T::TEST;
    const true true = T::TEST;
    const bool|false boolFalse = T::TEST;
    const true|bool trueBool = T::TEST;
    const bool|bool duplicatedBool = T::TEST;
    const true|false bothTrueAndFalse = T::TEST;
    const int|false|true bothTrueAndFalse2 = T::TEST;
    const int|INT duplicatedInt = T::TEST;
    const iterable|array iterable1 = T::TEST;
    const iterable|Traversable iterable2 = T::TEST;
    const iterable|array|Traversable iterable3 = T::TEST;
    const null|false nullFalse = T::TEST; // PHP 8.2: OK
}

interface InterfaceTest {
    public const callable TEST1 = T::TEST;
    public const void TEST2 = T::TEST;
    public const never TEST3 = T::TEST;
    public const int|callable callable = T::TEST;
    public const false false = T::TEST;
    public const null null = T::TEST;
    public const true true = T::TEST;
    public const bool|false boolFalse = T::TEST;
    public const true|bool trueBool = T::TEST;
    public const bool|bool duplicatedBool = T::TEST;
    public const true|false bothTrueAndFalse = T::TEST;
    public const int|false|true bothTrueAndFalse2 = T::TEST;
    public const int|INT duplicatedInt = T::TEST;
    public const iterable|array iterable1 = T::TEST;
    public const iterable|Traversable iterable2 = T::TEST;
    public const iterable|array|Traversable iterable3 = T::TEST;
    public const null|false nullFalse = T::TEST; // PHP 8.2: OK
}

trait TraitTest {
    private const callable TEST1 = T::TEST;
    private const void TEST2 = T::TEST;
    private const never TEST3 = T::TEST;
    private const int|callable callable = T::TEST;
    private const false false = T::TEST;
    private const null null = T::TEST;
    private const true true = T::TEST;
    private const bool|false boolFalse = T::TEST;
    private const true|bool trueBool = T::TEST;
    private const bool|bool duplicatedBool = T::TEST;
    private const true|false bothTrueAndFalse = T::TEST;
    private const int|false|true bothTrueAndFalse2 = T::TEST;
    private const int|INT duplicatedInt = T::TEST;
    private const iterable|array iterable1 = T::TEST;
    private const iterable|Traversable iterable2 = T::TEST;
    private const iterable|array|Traversable iterable3 = T::TEST;
    private const null|false nullFalse = T::TEST; // PHP 8.2: OK
}

enum EnumTest {
    protected const callable TEST1 = T::TEST;
    protected const void TEST2 = T::TEST;
    protected const never TEST3 = T::TEST;
    protected const int|callable callable = T::TEST;
    protected const false false = T::TEST;
    protected const null null = T::TEST;
    protected const true true = T::TEST;
    protected const bool|false boolFalse = T::TEST;
    protected const true|bool trueBool = T::TEST;
    protected const bool|bool duplicatedBool = T::TEST;
    protected const true|false bothTrueAndFalse = T::TEST;
    protected const int|false|true bothTrueAndFalse2 = T::TEST;
    protected const int|INT duplicatedInt = T::TEST;
    protected const iterable|array iterable1 = T::TEST;
    protected const iterable|Traversable iterable2 = T::TEST;
    protected const iterable|array|Traversable iterable3 = T::TEST;
    protected const null|false nullFalse = T::TEST; // PHP 8.2: OK
}
