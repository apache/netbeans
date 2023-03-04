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

// PHP 8.1 Never type
// https://wiki.php.net/rfc/noreturn_type

function returnType(): never { // func
}

function invalidInParameter(never $never): never { // func
}

class TestClass {
    public function returnType(): never { // class
    }
    public function invalidInParameter(never $never): never { // class
    }
}

trait TestTrait {
    public function returnType(): never { // trait
    }
    public function invalidInParameter(never $never): never { // trait
    }
}

interface TestInterface {
    public function returnType(): never; // interface
    public function invalidInParameter(never $never): never; // interface
}
