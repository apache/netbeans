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

enum ImplementingEnum implements I {
    public function interfaceMethod1(): void {
    }

    public static function interfaceStaticMethod1(): void {
    }

    public function publicImplementingEnumMethod1(): void {
    }

    private function privateImplementingEnumMethod1(): void {
    }

    protected function protectedImplementingEnumMethod1(): void {
    }

}

class ImplementingClass implements I {
    public function interfaceMethod1(): void {
    }

    public static function interfaceStaticMethod1(): void {
    }

    public function publicImplementingClassMethod1(): void {
    }

    private function privateImplementingClassMethod1(): void {
    }

    protected function protectedImplementingClassMethod1(): void {
    }
}

class ExtendingClass extends ImplementingClass {
    public function interfaceMethod1(): void {
    }

    public static function interfaceStaticMethod1(): void {
    }

    public function publicExtingClassMethod1(): void {
    }

    private function privateExtingClassMethod1(): void {
    }

    protected function protectedExtingClassMethod1(): void {
    }
}
