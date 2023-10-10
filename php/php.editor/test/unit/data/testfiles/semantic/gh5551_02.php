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
class TestClass {
    public function testMethod(string $test): string {
        return $test;
    }
}

interface TestInterface {
    public function testMethod(string $test): string;
}

class GH5551 {

    private function usedPrivateMethod1(): TestClass {
        return new TestClass();
    }

    private function usedPrivateMethod2(): TestInterface {
        return new class implements TestInterface {

            private function unusedPrivateMethod(): void {
            }

            private function usedPrivateMethod(): void {
            }

            public function testMethod(string $test): string {
                $this->usedPrivateMethod();
                return $test;
            }
        };
    }

    private function unusedPrivateMethod(): void {
    }

    public function test() {
        echo $this->usedPrivateMethod1()->testMethod("test1"), PHP_EOL;
        echo $this->usedPrivateMethod2()->testMethod("test2"), PHP_EOL;
    }
}
