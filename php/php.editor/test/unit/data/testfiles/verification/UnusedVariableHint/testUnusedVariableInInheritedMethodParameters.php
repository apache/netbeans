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

abstract class ParentClass {

    abstract function test1($param1, $param2);

    abstract function test2($param1, $param2);

    abstract function test3($param1, $param2);

    public function parentMethod(string $param1, int $param2): string {
        return $param1 . $param2;
    }

}

interface InterfaceTest {

    public function interfaceMethod(string $test): void;
}

class ChildClass extends ParentClass implements InterfaceTest {

    public function interfaceMethod(string $test): void {
        
    }

    public function parentMethod(string $param1, int $param2): string {
        
    }

    public function test($param1, string $param2): void {
        
    }

    public function test1($param1, $param2) {
        
    }

    public function test2($param1, $param2) {
        $anon = function($anonParam) {
            
        };
    }

    public function test3($param1, $param2) {
        
    }

}
