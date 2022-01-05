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

// options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 2);
// (FmtOptions.BLANK_LINES_AFTER_FUNCTION = 1)

class AttributeSyntax {
    #[A(1)]
    public function test1(): void {
    }
    #[A(1)] // comment
    public function test2(): void {
    }


    /**
     * comment
     */
    #[A(1)]
    public function test3(): void {
    }
    #[A(1)]
    /**
     * comment1
     */
    public function test4(): void {
    }

    /**
     * comment1
     */
    #[A(1)]
    /**
     * comment2
     */
    public function test5(): void {}

    #[A(1)]
    // comment
    public function test6(): void {}
    // comment
    #[A]
    public function test7(): void {}

#[A(1)]
#[A(2)]

    public function test8(): void {}
}

    #[A(1)]
    function test1(): void {
    }
    #[A(1)] // comment
    function test2(): void {
    }


    /**
     * comment
     */
    #[A(1)]
    function test3(): void {
    }
    #[A(1)]
    /**
     * comment1
     */
    function test4(): void {
    }

    /**
     * comment1
     */
    #[A(1)]
    /**
     * comment2
     */
    function test5(): void {}

    #[A(1)]
    // comment
    function test6(): void {}
    // comment
    #[A]
    function test7(): void {}

#[A(1)]
#[A(2)]

    function test8(): void {}
