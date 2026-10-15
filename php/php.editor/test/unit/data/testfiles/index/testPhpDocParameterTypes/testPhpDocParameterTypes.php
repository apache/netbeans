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

/**
 * test phpdoc
 * @param (callable(Test,bool):T)|(callable(Test,bool):T)|Test<T> $param
 * @return void
 */
function parameterType($param): void {
}

class TestClass {
    /**
     * test phpdoc
     * @param int-mask-of<self::Test1|self::Test2|self::Test3> $param1
     * @param (callable(Test,bool):T)|(callable(Test,bool):T)|Test<T> $param2
     * @return (Test&Y)|Z|X
     */
    public function parameterType($param1, $param2): (Test&Y)|Z|X {
    }
}
