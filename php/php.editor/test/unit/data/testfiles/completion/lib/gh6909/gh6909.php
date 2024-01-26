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
class GH6909Instance {
    public function Type(): int {
        return 0;
    }

    public function test(): void {
        $this->Type()->test;
        $this->mtd()->test;
        $this->fn()->test;
        $this->fld()->test;
        $this->var()->test;
        $this->array()->test;
        $this->type->test;
    }
}

class GH6909Static {
    public static function type(): int {
        return 0;
    }

    public function test(): void {
        self::type()->test;
        self::mtd()->test;
        self::fn()->test;
        self::fld()->test;
        GH6909Static::var()->test;
        static::array()->test;
    }
}

class GH6909InstanceReturnType {
    public function Type(): ExampleClass {
        return new Example();
    }

    public function test(): void {
        $this->Type()->example;
        $this->Type()::EXAMPLE;
    }
}

class GH6909StaticReturnType {
    public static function Type(): ExampleClass {
        return new Example();
    }

    public function test(): void {
        self::Type()->example;
        self::Type()::EXAMPLE;
    }
}

class GH6909FieldType {
    private ExampleClass $type;

    public function test(): void {
        $this->type->example;
    }
}

class ExampleClass {
    public int $example;
    public const EXAMPLE = 1;
    public function method(): void {}
    public static function staticMethod(): void {}
}
