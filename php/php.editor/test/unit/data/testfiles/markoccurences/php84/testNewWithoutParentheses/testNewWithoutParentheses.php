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
 */

class Test {
    const IMPLICIT_PUBLIC_TEST_CONST = "implicit public test const";
    public const string PUBLIC_TEST_CONST = "public test const";
    protected const string PROTECTED_TEST_CONST = "protected test const";
    private const string PRIVATE_TEST_CONST = "private test const";

    public int $publicTestField = 1;
    protected int $protectedTestField = 2;
    private int $privateTestField = 3;

    public static string $publicStaticTestField = "public static test field";
    protected static string $protectedStaticTestField = "protected static test field";
    private static string $privateStaticTestField = "private static test field";

    public function publicTestMethod(): string {
        return "public test method";
    }

    protected function protectedTestMethod(): string {
        return "protected test method";
    }

    private function privateTestMethod(): string {
        return "private test method";
    }

    public static function publicStaticTestMethod(): string {
        return "public static test method";
    }

    protected static function protectedStaticTestMethod(): string {
        return "protected static test method";
    }

    private static function privateStaticTestMethod(): string {
        return "private static test method";
    }
}

class Example {
    const IMPLICIT_PUBLIC_CONST = "implicit public const";
    public const string PUBLIC_CONST = "public const";
    protected const string PROTECTED_CONST = "protected const";
    private const string PRIVATE_CONST = "private const";

    public int $publicField = 1;
    protected int $protectedField = 2;
    private int $privateField = 3;
    public Test $test;

    public static string $publicStaticField = "public static field";
    protected static string $protectedStaticField = "protected static field";
    private static string $privateStaticField = "private static field";

    public function __construct() {
        $this->example = $this;
        $this->test = new Test();
    }

    public function test(): string|int {
        $example = new Example()::IMPLICIT_PUBLIC_CONST;
        $example = new Example()::PUBLIC_CONST;
        $example = new Example()::PROTECTED_CONST;
        $example = new Example()::PRIVATE_CONST;

        $example = new Example()->publicField;
        $example = new Example()->protectedField;
        $example = new Example()->privateField;

        $example = new Example()::$publicStaticField;
        $example = new Example()::$protectedStaticField;
        $example = new Example()::$privateStaticField;

        $example = new Example()->publicMethod();
        $example = new Example()->protectedMethod();
        $example = new Example()->privateMethod();

        $example = new Example()::publicStaticMethod();
        $example = new Example()::protectedStaticMethod();
        $example = new Example()::privateStaticMethod();

        $example = new Example()->returnThis()?->privateField;
        $example = new Example()->returnThis()->publicMethod();
        $example = new Example()->returnThis()::$protectedStaticField;
        $example = new Example()?->returnThis()::PRIVATE_CONST;
        $example = new Example()->returnThis()::privateStaticMethod();

        $example = new Example()->test->publicTestField;
        $example = new Example()->test->publicTestMethod();
        $example = new Example()->test::PUBLIC_TEST_CONST;
        $example = new Example()->test::$publicStaticTestField;
        $example = new Example()->test::publicStaticTestMethod();
        return $example;
    }

    public function returnThis(): self {
        return $this;
    }

    public function publicMethod(): string {
        return "public method";
    }

    protected function protectedMethod(): string {
        return "protected method";
    }

    private function privateMethod(): string {
        return "private method";
    }

    public static function publicStaticMethod(): string {
        return "public static method";
    }

    protected static function protectedStaticMethod(): string {
        return "protected static method";
    }

    private static function privateStaticMethod(): string {
        return "private static method";
    }
}

new Example()->publicField; // test
new Example()::$publicStaticField; // test
new Example()::IMPLICIT_PUBLIC_CONST; // test
new Example()::PUBLIC_CONST; // test
new Example()->publicMethod(); // test
new Example()::publicStaticMethod(); // test

new Example()->returnThis()->publicField; // test
new Example()->returnThis()::$publicStaticField; // test
new Example()->returnThis()::IMPLICIT_PUBLIC_CONST; // test
new Example()->returnThis()::PUBLIC_CONST; // test
new Example()->returnThis()->publicMethod(); // test
new Example()->returnThis()::publicStaticMethod(); // test

new Example()->test->publicTestField; // test
new Example()->test->publicTestMethod(); // test
new Example()->test::PUBLIC_TEST_CONST; // test
new Example()->test::$publicStaticTestField; // test
new Example()->test::publicStaticTestMethod(); // test

echo new Example()->test();

// anonymous class
echo new class {
    public const string PUBLIC_CONSTANT = 'public constant'; // anon1
}::PUBLIC_CONSTANT;

echo new class {
    public string $publicField = 'public field'; // anon2
}->publicField;

echo new class {
    public static string $publicStaticField = 'public static field'; // anon3
}::$publicStaticField;

echo new class {
    public function publicMethod(): void {} // anon4
}->publicMethod();

echo new class {
    public static function publicStaticMethod(): void {} // anon5
}::publicStaticMethod();

echo new class {
    public string $publicField = 'public field'; // anon6
    public function publicMethod(): self {} // anon6
    public static function publicStaticMethod(): self {} // anon6
}::publicStaticMethod()->publicMethod()->publicField;

echo new class {
    public string $publicField = 'public field'; // anon7
    public function publicMethod(): self {} // anon7
    public static function publicStaticMethod(): self {} // anon7
}->publicMethod()::publicStaticMethod()->publicField;

echo new class {
    public const string PUBLIC_CONSTANT = 'public constant'; // anon8
    public function publicMethod(): self {} // anon8
    public static function publicStaticMethod(): self {} // anon8
}->publicMethod()::publicStaticMethod()::PUBLIC_CONSTANT;
