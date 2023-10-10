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
trait ExampleTrait {

    const IMPLICIT_PUBLIC = 'implicit public';
    public const PUBLIC = 'public';
    protected const PROTECTED = 'protected';
    private const PRIVATE = 'private';

    public function method(): void {
//        echo ExampleTrait::IMPLICIT_PUBLIC . PHP_EOL; // fatal error
        echo self::PUBLIC . PHP_EOL;
        echo static::PRIVATE . PHP_EOL;
        echo $this::PROTECTED . PHP_EOL;
    }
}

trait ExampleTrait2 {

    use ExampleTrait;

    #[TestAttribute(test: "test")]
    const IMPLICIT_PUBLIC2 = 'implicit public';
    public const PUBLIC2 = 'public';
    protected const PROTECTED2 = 'protected';
    /**
     * const
     */
    private const PRIVATE2 = 'private';
    public function test(): void {
        echo self::IMPLICIT_PUBLIC . PHP_EOL;
    }
}

class ExampleClass {

    use ExampleTrait;

    public function test(): void {
        echo self::PUBLIC . PHP_EOL;
        echo self::PRIVATE . PHP_EOL;
        echo self::PROTECTED . PHP_EOL;
    }
}

class Child extends ExampleClass {

    public function test(): void {
        echo parent::PUBLIC . PHP_EOL;
//        echo parent::PRIVATE . PHP_EOL; // fatal error
        echo parent::PROTECTED . PHP_EOL;
    }
}

//echo ExampleTrait::PUBLIC . PHP_EOL; // fatal error
echo ExampleClass::PUBLIC . PHP_EOL;
echo (new ExampleClass)::PUBLIC . PHP_EOL;
$i = new ExampleClass();
$i->test();

$c = new Child();
$c->test();
