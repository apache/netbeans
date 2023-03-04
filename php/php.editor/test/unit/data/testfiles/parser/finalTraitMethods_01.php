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

trait FinalMethod {

    final private function __construct() {
    }

    final function finalMethod(int $test): void {
    }

    final public function finalPublicMethod(int $test): void {
    }

    final private function finalPrivateMethod($test): void {
    }

    final protected function finalProtectedMethod(string $test): void {
    }

    public final function publicFinalMethod(int $test): void {
    }

    private final function privateFinalMethod($test): void {
    }

    protected final function protectedFinalMethod(string $test): void {
    }

    final static function finalStaticMethod(): void {
    }

    final public static function finalPublicStaticMethod(int $test): void {
    }

    final private static function finalPrivateStaticMethod($test): void {
    }

    final protected static function finalProtectedStaticMethod(string $test): void {
    }

    final static public function finalStaticublicMethod(int $test): void {
    }

    final static private function finalStatiPrivateMethod($test): void {
    }

    final static protected function finalStatiProtectedMethod(string $test): void {
    }

    public final static function publicFinalStaticMethod(int $test): void {
    }

    private final static function privateFinalStaticMethod($test): void {
    }

    protected final static function protectedFinalStaticMethod(string $test): void {
    }

    public static final function publicStaticFinalMethod(int $test): void {
    }

    private static final function privateStaticFinalMethod($test): void {
    }

    protected static final function protectedStaticFinalMethod(string $test): void {
    }

    static public final function staticPublicFinalMethod(int $test): void {
    }

    static private final function staticPrivateFinalMethod($test): void {
    }

    static protected final function staticProtectedFinalMethod(string $test): void {
    }

    static final public function staticFinalPublicMethod(int $test): void {
    }

    static final private function staticFinalPrivateMethod($test): void {
    }

    static final protected function staticFinalProtectedMethod(string $test): void {
    }

}
