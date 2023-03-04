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
trait ExampleTrait {

    const IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';
    public const PUBLIC_TRAIT = 'ExampleTrait public';
    protected const PROTECTED_TRAIT = 'ExampleTrait protected';
    private const PRIVATE_TRAIT = 'ExampleTrait private';

    public function method(): void {
        echo self::IMPLICIT_PUBLIC_TRAIT . PHP_EOL;
        echo static::PRIVATE_TRAIT . PHP_EOL;
        echo $this::PROTECTED_TRAIT . PHP_EOL;
    }

}

trait ExampleTrait2 {

    use ExampleTrait;

    public function test(): void {
        echo self::IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // use
        echo static::PRIVATE_TRAIT . PHP_EOL; // use
        echo $this::PROTECTED_TRAIT . PHP_EOL; // use
    }
}

class ExampleClass {

    use ExampleTrait;
    public const PUBLIC_CLASS = "ExampleClass public";

    public function test(): void {
        echo self::IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // class
        echo static::PRIVATE_TRAIT . PHP_EOL; // class
        echo $this::PROTECTED_TRAIT . PHP_EOL; // class
    }
}

class Child extends ExampleClass {

    public function test(): void {
        echo self::IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // child
        echo parent::PUBLIC_TRAIT . PHP_EOL; // child
    }
}

echo ExampleClass::IMPLICIT_PUBLIC_TRAIT . PHP_EOL;
//$i = new ExampleClass();
//$i::PUBLIC_TRAIT;
//
//$c = new Child();
//$c::PUBLIC_TRAIT;
