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

class GH7140 {

    public function test1(): void {
        $this->test->test(Test::class,
            function (): Test {
                return new Test(
                test: 0,
                );
            });
    }

    public function test2(): void {
        test(Test::class,
            function (): Test {
                return new Test(
                test: 0,
                );
            }
            );
    }

    public function test3(): Test {
        return new Test(
            test: 0,
        );
    }
}

function test(): Test {
        test(Test::class,
            function (): Test {
                return new Test(
                test: 0,
                );
            }
            );
}
        test(Test::class,
            function (): Test {
                return new Test(
                test: 0,
                );
            }
            );

// formatted
class GH7140_2 {

    public function test1(): void {
        $this->test->test(Test::class,
            function (): Test {
                return new Test(
                    test: 0,
                );
            });
    }

    public function test2(): void {
        test(Test::class,
            function (): Test {
                return new Test(
                    test: 0,
                );
            }
        );
    }

    public function test3(): Test {
        return new Test(
            test: 0,
        );
    }
}

function test(): Test {
    test(Test::class,
        function (): Test {
            return new Test(
                test: 0,
            );
        }
    );
}

test(Test::class,
    function (): Test {
        return new Test(
            test: 0,
        );
    }
);
