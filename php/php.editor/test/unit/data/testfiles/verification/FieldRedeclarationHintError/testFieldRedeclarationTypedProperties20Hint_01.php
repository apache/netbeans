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

class A
{
    public int $foo;
    private string $foo = "foo";
    protected int $foo;
    var bool $foo;
    public static ?string $foo;
    private static ?array $foo;
    protected static iterable $foo;
    static MyClass $foo;
    public int $FOO;

}

abstract class B
{
    private int $bar;
    protected ?string $bar;
    var bool $bar;
    public string $bar = 'bar';
    public static array $bar;
    private static ?array $bar;
    protected static float $bar;
    static self $bar;
    public parent $BAR;
}

trait T
{
    protected int $baz;
    public string $baz;
    var ?MyClass $baz;
    private object $baz;
    public static ?string $baz;
    protected static \Foo\Bar\MyClass $baz;
    private static ?\Foo\MyClass $baz;
    static int $baz;
    protected bool $BAZ;
}
