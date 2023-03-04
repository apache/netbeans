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

trait TestTrait {

    public int $publicTraitField = 1;
    private int $privateTraitField = 1;
    protected int $protectedTraitField = 1;
    public static int $publicStaticTraitField = 1;
    private static int $privateStaticTraitField = 1;
    protected static int $protectedStaticTraitField = 1;

    public function publicTraitMethod() {
        echo "publicTraitMethod" . PHP_EOL;
    }

    private function privateTraitMethod() {
        echo "privateTraitMethod" . PHP_EOL;
    }

    protected function protectedTraitMethod() {
        echo "protectedTraitMethod" . PHP_EOL;
    }

    public static function publicStaticTraitMethod() {
        echo "publicStaticTraitMethod" . PHP_EOL;
    }

    private static function privateStaticTraitMethod() {
        echo "privateStaticTraitMethod" . PHP_EOL;
    }

    protected static function protectedStaticTraitMethod() {
        echo "protectedStaticTraitMethod" . PHP_EOL;
    }

}

class A {

    use TestTrait;

    public int $publicClassField = 1;
    private int $privateClassField = 1;
    protected int $protectedClassField = 1;
    public static int $publicStaticClassField = 1;
    private static int $privateStaticClassField = 1;
    protected static int $protectedStaticClassField = 1;

    public function publicClassMethod() {
        echo "publicClassMethod" . PHP_EOL;
    }

    private function privateClassMethod() {
        echo "privateClassMethod" . PHP_EOL;
    }

    protected function protectedClassMethod() {
        echo "protectedClassMethod" . PHP_EOL;
    }

    public static function publicStaticClassMethod() {
        echo "publicStaticClassMethod" . PHP_EOL;
    }

    private static function privateStaticClassMethod() {
        echo "privateStaticClassMethod" . PHP_EOL;
    }

    protected static function protectedStaticClassMethod() {
        echo "protectedStaticClassMethod" . PHP_EOL;
    }

    protected function protectedTraitMethod() {
        echo "protectedTraitMethod" . PHP_EOL;
    }

}

class B extends A {

    protected function protectedTraitMethod() {
        parent::protectedTraitMethod();
    }

}
