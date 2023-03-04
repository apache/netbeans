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

enum Simple {

    case CASE1;
    case CASE2;
    const CONSTANT1 = [
        [0, 1],
        [0, 1]
    ];

    public function publicMethod(): void {
    }

    public function publicMethodWithParam(int $int): void {
    }

    public static function publicStaticMethod(): void {
        $i = new Simple();
        $i::CASE1;
        $i::CASE1::CASE2;
        $i::CASE1::CASE2::CONSTANT1;
        $i::CASE1::CASE2::CONSTANT1[0];
        $i::CASE1::CASE2::CONSTANT1[0][0];
        $i::CONSTANT1;
        $i::CONSTANT1[self::CONSTANT1];
        $i::CONSTANT1[0][0];
        $i::CASE1->publicMethod();
        $i::CASE1->publicMethodWithParam(1);
        $i::CASE1?->publicMethod();
        $i::CASE1::publicStaticMethod();
        $i::CASE1::CASE2->publicMethod();
        $i::CASE1::CASE2?->publicMethod();
        $i::CASE1::CASE2::publicStaticMethod();
        $i::publicStaticMethod();
        Simple::CASE1;
        Simple::CASE1::CASE2;
        Simple::CASE1::CASE2::CONSTANT1;
        Simple::CASE1::CASE2::CONSTANT1[0];
        Simple::CASE1::CASE2::CONSTANT1[0][0];
        Simple::CONSTANT1;
        Simple::CONSTANT1[self::CONSTANT1];
        Simple::CONSTANT1[0][0];
        Simple::CASE1->publicMethod();
        Simple::CASE1->publicMethodWithParam($test);
        Simple::CASE1?->publicMethod();
        Simple::CASE1::publicStaticMethod();
        Simple::CASE1::CASE2->publicMethod();
        Simple::CASE1::CASE2?->publicMethod();
        Simple::CASE1::CASE2::publicStaticMethod();
        Simple::publicStaticMethod();
        self::CASE1;
        self::CASE1::CASE2;
        self::CASE1::CASE2::CONSTANT1;
        self::CASE1::CASE2::CONSTANT1[0];
        self::CASE1::CASE2::CONSTANT1[0][0];
        self::CONSTANT1;
        self::CONSTANT1[self::CONSTANT1];
        self::CONSTANT1[0][0];
        self::CASE1->publicMethod();
        self::CASE1?->publicMethod();
        self::CASE1::publicStaticMethod();
        self::CASE1::CASE2->publicMethod();
        self::CASE1::CASE2?->publicMethod();
        self::CASE1::CASE2::publicStaticMethod();
        self::publicStaticMethod();
        static::CASE1;
        static::CASE1::CASE2;
        static::CASE1::CASE2::CONSTANT1;
        static::CASE1::CASE2::CONSTANT1[0];
        static::CASE1::CASE2::CONSTANT1[0][0];
        static::CONSTANT1;
        static::CONSTANT1[self::CONSTANT1];
        static::CONSTANT1[0][0];
        static::CASE1->publicMethod();
        static::CASE1?->publicMethod();
        static::CASE1::publicStaticMethod();
        static::CASE1::CASE2->publicMethod();
        static::CASE1::CASE2?->publicMethod();
        static::CASE1::CASE2::publicStaticMethod();
        static::publicStaticMethod();
    }
}
