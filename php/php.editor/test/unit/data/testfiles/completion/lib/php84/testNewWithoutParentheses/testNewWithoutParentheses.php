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
        echo new Test()->publicTestMethod();
        echo new Test()::publicStaticTestMethod();

        $example = new Example()->publicMethod();
        $example = new Example()::IMPLICIT_PUBLIC_CONST;

        $example = new Example()->returnThis()?->privateField;
        $example = new Example()->returnThis()->publicMethod();
        $example = new Example()->returnThis()::$protectedStaticField;
        $example = new Example()?->returnThis()::PRIVATE_CONST;

        $example = new Example()->test->publicTestField;
        $example = new Example()?->test->publicTestMethod();
        $example = new Example()->test::PUBLIC_TEST_CONST;
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

new Example()?->returnThis()?->publicMethod(); // test
new Example()->returnThis()::publicStaticMethod(); // test

new Example()->test?->publicTestField; // test
new Example()->test->publicTestMethod(); // test
new Example()?->test::PUBLIC_TEST_CONST; // test
new Example()->test::$publicStaticTestField; // test

echo new Example()->test();

// anonymous class
echo new class {
    const IMPLICIT_PUBLIC_CONST = "implicit public const";
    public const string PUBLIC_CONST = "public const";
    protected const string PROTECTED_CONST = "protected const";
    private const string PRIVATE_CONST = "private const";

    public int $publicField = 1;
    protected int $protectedField = 2;
    private int $privateField = 3;

    public static string $publicStaticField = "public static field";
    protected static string $protectedStaticField = "protected static field";
    private static string $privateStaticField = "private static field";

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
}::PUBLIC_CONSTANT;

$anon = new class {
    const IMPLICIT_PUBLIC_CONST2 = "implicit public const";
    public const string PUBLIC_CONST2 = "public const";
    protected const string PROTECTED_CONST2 = "protected const";
    private const string PRIVATE_CONST2 = "private const";

    public int $publicField2 = 1;
    protected int $protectedField2 = 2;
    private int $privateField2 = 3;

    public static string $publicStaticField2 = "public static field";
    protected static string $protectedStaticField2 = "protected static field";
    private static string $privateStaticField2 = "private static field";

    public function publicMethod2(): string {
        return "public method";
    }

    protected function protectedMethod2(): string {
        return "protected method";
    }

    private function privateMethod2(): string {
        return "private method";
    }

    public static function publicStaticMethod2(): string {
        return "public static method";
    }

    protected static function protectedStaticMethod2(): string {
        return "protected static method";
    }

    private static function privateStaticMethod2(): string {
        return "private static method";
    }
}->publicField2;

echo new class {
    public string $publicField3 = 'public field';
    public function publicMethod3(): self {}
    public static function publicStaticMethod3(): self {}
}::publicStaticMethod3()->publicMethod3()->publicField3;

echo new class {
    public string $publicField4 = 'public field';
    public function publicMethod4(): self {}
    public static function publicStaticMethod4(): self {}
}->publicMethod4()::publicStaticMethod4()->publicField4;

echo new class {
    public const string PUBLIC_CONSTANT5 = 'public constant';
    public function publicMethod5(): Test {}
    public static function publicStaticMethod5(): self {}
}->publicMethod5()::publicStaticTestMethod5();

class Anon {
    public function test(): void {
        $anon = new class() {
            const IMPLICIT_PUBLIC_CONST6 = "implicit public const";
            public const string PUBLIC_CONST6 = "public const";
            protected const string PROTECTED_CONST6 = "protected const";
            private const string PRIVATE_CONST6 = "private const";

            public int $publicField6 = 1;
            protected int $protectedField6 = 2;
            private int $privateField6 = 3;

            public static string $publicStaticField6 = "public static field";
            protected static string $protectedStaticField6 = "protected static field";
            private static string $privateStaticField6 = "private static field";

            public function publicMethod6(): string {
                return "public method";
            }

            protected function protectedMethod6(): string {
                return "protected method";
            }

            private function privateMethod6(): string {
                return "private method";
            }

            public static function publicStaticMethod6(): string {
                return "public static method";
            }

            protected static function protectedStaticMethod6(): string {
                return "protected static method";
            }

            private static function privateStaticMethod6(): string {
                return "private static method";
            }
        }::publicStaticMethod6();

        $anon = new class() extends Test {
            public const string PUBLIC_CONSTANT7 = 'public constant';
            public function publicMethod7(): Test {}
            public static function publicStaticMethod7(): self {}
        }->publicMethod7()::publicStaticTestMethod7();

    }
}
