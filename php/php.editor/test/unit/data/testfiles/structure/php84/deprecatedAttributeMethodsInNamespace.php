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

namespace DeprecatedMethods;

class DeprecatedClassMethod {
    #[Attr]
    public function publicMethod():void {}
    private function privateMethod():void {}
    protected function protectedMethod():void {}
    public static function publicStaticMethod():void {}
    private static function privateStaticMethod():void {}
    protected static function protectedStaticMethod():void {}
    #[Deprecated]
    public function publicDeprecatedMethod():void {}
    #[Deprecated]
    private function privateDeprecatedMethod():void {}
    #[Deprecated]
    protected function protectedDeprecatedMethod():void {}
    #[Deprecated]
    public static function publicDeprecatedStaticMethod():void {}
    #[Deprecated]
    private static function privateDeprecatedStaticMethod():void {}
    #[Deprecated]
    protected static function protectedDeprecatedStaticMethod():void {}
    #[\Deprecated]
    public function publicDeprecatedMethod02():void {}
    #[\Deprecated]
    private function privateDeprecatedMethod02():void {}
    #[\Deprecated]
    protected function protectedDeprecatedMethod02():void {}
    #[\Deprecated]
    public static function publicDeprecatedStaticMethod02():void {}
    #[\Deprecated]
    private static function privateDeprecatedStaticMethod02():void {}
    #[\Deprecated]
    protected static function protectedDeprecatedStaticMethod02():void {}
}

interface DeprecatedInterfaceMethod {
    public function publicMethod():void;
    #[\Attr]
    public static function publicStaticMethod():void;
    #[Deprecated]
    public function publicDeprecatedMethod():void;
    #[Deprecated]
    public static function publicDeprecatedStaticMethod():void;
    #[\Deprecated]
    public function publicDeprecatedMethod02():void;
    #[\Deprecated]
    public static function publicDeprecatedStaticMethod02():void;
}

trait DeprecatedTraitMethod {
    public function publicTraitMethod():void {}
    private function privateTraitMethod():void {}
    protected function protectedTraitMethod():void {}
    public static function publicStaticTraitMethod():void {}
    private static function privateStaticTraitMethod():void {}
    protected static function protectedStaticTraitMethod():void {}
    #[Deprecated]
    public function publicDeprecatedTraitMethod():void {}
    #[Deprecated]
    private function privateDeprecatedTraitMethod():void {}
    #[Deprecated]
    protected function protectedDeprecatedTraitMethod():void {}
    #[Attr1]
    #[Attr2, Deprecated]
    public static function publicDeprecatedStaticTraitMethod():void {}
    #[Deprecated]
    private static function privateDeprecatedStaticTraitMethod():void {}
    #[Deprecated]
    protected static function protectedDeprecatedStaticTraitMethod():void {}
    /**
     * @deprecated since 1.0
     * @return void
     */
    #[\Deprecated]
    public function publicDeprecatedTraitMethod02():void {}
    #[\Deprecated]
    private function privateDeprecatedTraitMethod02():void {}
    #[\Deprecated]
    /**
     * @return void
     */
    protected function protectedDeprecatedTraitMethod02():void {}
    #[\Deprecated]
    public static function publicDeprecatedStaticTraitMethod02():void {}
    #[\Deprecated]
    private static function privateDeprecatedStaticTraitMethod02():void {}
    #[\Attr]
    #[\Deprecated]
    protected static function protectedDeprecatedStaticTraitMethod02():void {}
}

enum DeprecatedEnumMethod {
    public function publicEnumMethod():void {}
    private function privateEnumMethod():void {}
    protected function protectedEnumMethod():void {}
    public static function publicStaticEnumMethod():void {}
    private static function privateStaticEnumMethod():void {}
    protected static function protectedStaticEnumMethod():void {}
    #[Deprecated, Attr1]
    public function publicDeprecatedEnumMethod():void {}
    #[Deprecated]
    #[Attr2]
    private function privateDeprecatedEnumMethod():void {}
    #[Deprecated]
    protected function protectedDeprecatedEnumMethod():void {}
    #[Deprecated]
    public static function publicDeprecatedStaticEnumMethod():void {}
    #[Deprecated]
    private static function privateDeprecatedStaticEnumMethod():void {}
    #[Deprecated]
    protected static function protectedDeprecatedStaticEnumMethod():void {}
    #[\Deprecated]
    public function publicDeprecatedEnumMethod02():void {}
    #[\Deprecated]
    private function privateDeprecatedEnumMethod02():void {}
    #[\Deprecated]
    protected function protectedDeprecatedEnumMethod02():void {}
    #[\Deprecated]
    public static function publicDeprecatedStaticEnumMethod02():void {}
    #[\Deprecated]
    private static function privateDeprecatedStaticEnumMethod02():void {}
    #[\Deprecated]
    protected static function protectedDeprecatedStaticEnumMethod02():void {}
}

$anon = new class() {
    #[Attr]
    public function publicMethod():void {}
    private function privateMethod():void {}
    protected function protectedMethod():void {}
    public static function publicStaticMethod():void {}
    private static function privateStaticMethod():void {}
    protected static function protectedStaticMethod():void {}
    #[Deprecated]
    public function publicDeprecatedMethod():void {}
    #[Deprecated]
    private function privateDeprecatedMethod():void {}
    #[Deprecated]
    protected function protectedDeprecatedMethod():void {}
    #[Deprecated]
    public static function publicDeprecatedStaticMethod():void {}
    #[Deprecated]
    private static function privateDeprecatedStaticMethod():void {}
    #[Deprecated]
    protected static function protectedDeprecatedStaticMethod():void {}
    #[\Deprecated]
    public function publicDeprecatedMethod02():void {}
    #[\Deprecated]
    private function privateDeprecatedMethod02():void {}
    #[\Deprecated]
    protected function protectedDeprecatedMethod02():void {}
    #[\Deprecated]
    public static function publicDeprecatedStaticMethod02():void {}
    #[\Deprecated]
    private static function privateDeprecatedStaticMethod02():void {}
    #[\Deprecated]
    protected static function protectedDeprecatedStaticMethod02():void {}
};
