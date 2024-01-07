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

class ThisInStaticContext
{
    private int $test = 1;

    public static function getTest(): int {
        self::staticMethod($this); // error
        $anon = new class() {
            private int $field = 1;

            public function nestedMethod(): void {
                echo $this->field; // ok
            }

            public static function nestedStaticMethod(): void {
                echo $this->field; // error
            }

        };
        return $this->test; // error
    }


    public static function staticMethod(ThisInStaticContext $test): void {
    }

    public function method(): void {
        $this->test; // ok
        $anon = new class() {
            private int $field = 1;

            public static function nestedStaticMethod(): void {
                echo $this->field; // error
            }

            public function nestedMethod(): void {
                echo $this->field; // ok
            }
        };
    }
}
echo ThisInStaticContext::getTest();

// anonymous class
$anon = new class() {
    private int $test = 1;

    public static function test(): void {
        $this->test; // error
    }

    public function method(): void {
        $this->test(); // ok
    }
};

$this->HTML->something(); // ok(ignore)
