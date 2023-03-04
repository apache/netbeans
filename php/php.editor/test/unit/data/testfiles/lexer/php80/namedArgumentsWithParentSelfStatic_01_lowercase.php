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

#[A(parent: parent::VALUE, static: static::VALUE, self: self::VALUE)]
#[A(
    parent: parent::VALUE,
    static: static::VALUE,
    self: self::VALUE
)]
class TestClass extends ParentClass {
    public function test(): parent {
        parent::testParent(parent: parent::VALUE, static: static::VALUE, self: self::VALUE);
        static::testStatic(static: "test", self: "test", parent: "test");
        self::testSelf(
                self: "test",
                parent: "test",
                static: "test"
        );
        $this->test2(parent::VALUE, static::VALUE, self::VALUE);
        $this->test2(
                parent::VALUE,
                static::VALUE,
                self::VALUE
        );
    }
}
