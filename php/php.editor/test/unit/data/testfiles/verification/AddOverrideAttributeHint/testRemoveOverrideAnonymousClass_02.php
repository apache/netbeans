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

class RemoveOverrideAnonymousClass2 {

    public function test(): void {
        $anon1 = new class() {

            public function anonymousMethod1(): void {
            }

            public function anonymousMethod2(): void {
                $anon2 = new class() {

                    public function anonymousMethod1(): void {
                    }

                    #[Attr1]
                    #[Override]
                    public static function anonymousStaticMethod1(): void {
                    }

                    public function anonymousMethod2(): void {
                    }
                };
            }

            public function anonymousStaticMethod1(): void {
            }
        };
    }
}
