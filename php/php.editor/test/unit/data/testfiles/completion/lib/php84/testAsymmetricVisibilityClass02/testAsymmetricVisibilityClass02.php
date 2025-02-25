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

class Foo {
    const IMPLICIT_PUBLIC_FOO_CONST = "implicit public foo const";
    public const string PUBLIC_FOO_CONST = "public foo const";
    protected const string PROTECTED_FOO_CONST = "protected foo const";
    private const string PRIVATE_FOO_CONST = "private foo const";

    public(set) int $iPublicPublicSetFooField;
    private(set) int $iPublicPrivateSetFooField;
    protected(set) int $iPublicProtectedSetFooField;
    public private(set) int $publicFooField = 1;
    protected private(set) int $protectedFooField = 2;
    private int $privateFooField = 3;

    public static string $publicStaticFooField = "public static foo field";
    protected static string $protectedStaticFooField = "protected static foo field";
    private static string $privafooaticFooField = "private static foo field";

    public function publicFooMethod(): string {
        return "public foo method";
    }

    protected function protectedFooMethod(): string {
        return "protected foo method";
    }

    private function privateFooMethod(): string {
        return "private foo method";
    }

    public static function publicStaticFooMethod(): string {
        return "public static foo method";
    }

    protected static function protectedStaticFooMethod(): string {
        return "protected static foo method";
    }

    private static function privafooaticFooMethod(): string {
        return "private static foo method";
    }
}

class Bar {
    const IMPLICIT_PUBLIC_BAR_CONST = "implicit public bar const";
    public const string PUBLIC_BAR_CONST = "public bar const";
    protected const string PROTECTED_BAR_CONST = "protected bar const";
    private const string PRIVATE_BAR_CONST = "private bar const";

    public(set) int $iPublicPublicSetBarField;
    private(set) int $iPublicPrivateSetBarField;
    protected(set) int $iPublicProtectedSetBarField;
    public private(set) int $publicBarField = 1;
    protected private(set) int $protectedBarField = 2;
    private int $privateBarField = 3;

    public static string $publicStaticBarField = "public static bar field";
    protected static string $protectedStaticBarField = "protected static bar field";
    private static string $privabaraticBarField = "private static bar field";

    public function publicBarMethod(): string {
        return "public bar method";
    }

    protected function protectedBarMethod(): string {
        return "protected bar method";
    }

    private function privateBarMethod(): string {
        return "private bar method";
    }

    public static function publicStaticBarMethod(): string {
        return "public static bar method";
    }

    protected static function protectedStaticBarMethod(): string {
        return "protected static bar method";
    }

    private static function privabaraticBarMethod(): string {
        return "private static bar method";
    }
}

trait TestTrait {
    public(set) Foo $iPublicPublicSetTrait;
    private(set) string|Foo $iPublicPrivateSetTrait = "";
    protected(set) Bar|string $iPublicProtectedSet1Trait = "", $iPublicProtectedSet2Trait = "";
    public protected(set) Bar $publicProtectedSetTrait;
    public private(set) Bar $publicPrivateSetTrait;
    private private(set) string $privatePrivateSeTraitt;
    protected private(set) readonly int|Foo $protectedPrivateSetTrait;
    final protected private(set) int|Foo $finalProtectedPrivateSetTrait;
    final public private(set) readonly Bar|(ParentClass & Foo) $finalPublicPrivateSetReadonlyTrait;
}

class ParentClass {

    use TestTrait;

    public(set) Foo $iPublicPublicSet;
    private(set) string|Foo $iPublicPrivateSet = "";
    protected(set) Bar|string $iPublicProtectedSet1 = "", $iPublicProtectedSet2 = "";
    public protected(set) Bar $publicProtectedSet;
    public private(set) Bar $publicPrivateSet;
    private private(set) string $privatePrivateSet;
    protected private(set) readonly int|Foo $protectedPrivateSet;
    final protected private(set) int|Foo $finalProtectedPrivateSet;
    final public private(set) readonly Bar|(ParentClass & Foo) $finalPublicPrivateSetReadonly;

    public function __construct() {
        $this->iPublicPublicSet = new Foo();
        $this->iPublicPrivateSet = new Foo();
        $this->iPublicProtectedSet1 = new Bar();
        $this->iPublicProtectedSet2 = new Bar();
        $this->publicProtectedSet = new Bar();
        $this->publicPrivateSet = new Bar();
        $this->protectedPrivateSet = new Foo();
        $this->finalProtectedPrivateSet = new Foo();
        $this->finalPublicPrivateSetReadonly = new Bar();
    }

    public function test(): void {
        $this->privatePrivateSet; // test: all fields (parent class)
    }
}

$parent = new ParentClass();
$parent->finalPublicPrivateSetReadonly; // test: only public methods (parent class)
$parent->iPublicPrivateSet;
$parent->iPublicProtectedSet1;
$parent->iPublicProtectedSet2;
$parent->publicPrivateSet;
$parent->publicProtectedSet;
$parent->iPublicPublicSet;

class ChildClass extends ParentClass {

    public function test(): void {
        $this->publicPrivateSet; // test: only public and protected (child class)
        $this->publicProtectedSet->publicBarMethod(); // test: only public Bar (child class)
    }
}

class AsymmetricVisibilityPromoted {
    public function __construct(
        public(set) Foo $iPublicPublicSet, // constructor
        private(set) string|int $iPublicPrivateSet, // constructor
        protected(set) string|int $iPublicProtectedSet1, // constructor
        public protected(set) Bar $publicProtectedSet, // constructor
        protected private(set) readonly int $protectedPrivateSet, // constructor
        private private(set) int $privatePrivateSet, // constructor
    ) {}

    private function test(): void {
        $this->iPublicPrivateSet; // test: all fields (promoted)
    }
}

$promoted = new AsymmetricVisibilityPromoted(new Foo(), "test", "test", new Bar(), 0);
$promoted->iPublicPrivateSet; // test: only public fields (promoted)

new ChildClass()->test();
