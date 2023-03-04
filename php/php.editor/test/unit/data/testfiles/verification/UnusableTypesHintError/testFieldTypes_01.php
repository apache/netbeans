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
class TypedPropertiesClass
{
    public $publicInt;
    public int $publicIntType;
    public self $publicSelfType;
    public parent $publicParentType;
    var int $varIntType;
    private string $privateStringType;
    protected MyClass $protectedMyClassType;
    public \Foo\Bar\MyClass $publicMyClassType2;
    public float $x, $y;

    public static int $publicStaticIntType = 0;
    private static ?string $privateStaticStringType = null;
    protected static MyClass $protectedStaticMyClassType;
    public static \Foo\Bar\MyClass $publicStaticMyClassType;

    public callable $callbleType; // error, Unusable type
    public void $voidType; // error, Unusable type
}

trait TypedPropertiesTrait
{
    public $publicInt;
    public int $publicIntType;
    public self $publicSelfType;
    public parent $publicParentType;
    var int $varIntType;
    private string $privateStringType;
    protected MyClass $protectedMyClassType;
    public \Foo\Bar\MyClass $publicMyClassType2;
    public float $x, $y;

    public static int $publicStaticIntType = 0;
    private static ?string $privateStaticStringType = null;
    protected static MyClass $protectedStaticMyClassType;
    public static \Foo\Bar\MyClass $publicStaticMyClassType;

    public callable $callbleType; // error, Unusable type
    public void $voidType; // error, Unusable type
}
