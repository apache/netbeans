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

trait ThisInStaticContextTrait
{
    private int $test = 1;

    public static function staticMethod(): void {
        $closure = function() {
            echo $this->test; // error
        };
        $staticClosure = static function() {
            echo $this->test; // error
            $anon = new class() {
                private int $test = 1;
                public function method() {
                    $closure = function() {
                        $this->test; // ok
                    };
                    $staticClosure = static function() {
                        $this->test; // error
                        $closure2 = function() {
                            $this->test; // error
                        };
                    };
                    $staticClosure();
                    return $this; // ok
                }
                public static function staticMethod() {
                    $this->method(); // error
                }
            };
        };
        $staticClosure();
        $arrowFunction = fn() => $this; // error
        $staticArrowFunction = static fn() => $this; // error
        $staticArrowFunction()->test;
    }

    public function method(): void {
        $this->test; // ok
        $closure = function() {
            $this->test; // ok
            $nestedStaticClosure = static function() {
                $this->test; // error
            };
        };
        $staticClosure = static function() {
            echo $this->test; // error
            $closuer2 = function() {
                $this->test; // error
            };
            $anon = new class() {
                private int $test = 1;
                public function method() {
                    $closure = function() {
                        $this->test; // ok
                    };
                    $staticClosure = static function() {
                        $this->test; // error
                        $closure2 = function() {
                            $this->test; // error
                        };
                    };
                    $staticClosure();
                    return $this; // ok
                }
                public static function staticMethod() {
                    $this->method(); // error
                }
            };
            $nestedArrow = fn() => $this; // error
        };
        $staticClosure();
        $arrowFunction = fn() => $this; // ok
        $staticArrowFunction = static fn() => $this; // error
        $staticArrowFunction = static fn() => $anon = new class() {
            private int $test = 1;
            public function method() {
                $closure = function () {
                    $this->test; // ok
                };
                $staticClosure = static function () {
                    $this->test; // error
                    $closure2 = function () {
                        $this->test; // error
                    };
                };
                $staticClosure();
                return $this; // ok
            }

            public static function staticMethod() {
                $this->method(); // error
            }
        };
        $staticArrowFunction()->test;
        $this->test; // ok
    }
}
