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

class UnionTypesClass
{
    private int|float $property;
    protected static string|bool|null $staticProperty;

    public function method(int|float $number): Foo|Bar|null {
        return null;
    }

    public static function staticMethod(iterable|null $iterable): \Test\Foo|Bar {
        return new Bar();
    }
}

abstract class UnionTypesAbstractClass
{
    private int|float $property;
    protected static string|bool|null $staticProperty;

    abstract public function method(int|float $number): Foo|Bar|null;

    abstract protected static function staticMethod(iterable|null $iterable): \Test\Foo|Bar;
}

interface UnionTypesInterface
{
    public function method(int|float $number): Foo|Bar|null;

    public static function staticMethod(iterable|null $iterable): \Test\Foo|Bar;
}

trait UnionTypesTrait
{
    private int|float $property;
    protected static string|bool|null $staticProperty;

    public function method(int|float $number): Foo|Bar|null {
        return null;
    }

    public static function staticMethod(iterable|null $iterable): \Test\Foo|Bar {
        return new Bar();
    }
}
