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

// PHP 8.0 Allow ::class on objects
// https://wiki.php.net/rfc/class_name_literal_on_object

class Test
{
    public function noReturnTypes(): void {
    }

    public function newInstance(): Test {
        return $this;
    }

    public function withThis(): void {
        var_dump($this::class); // test6
    }
}

$test = new Test;
var_dump($test::class); // test1
$reference =& $test;
var_dump($reference::class); // test2
var_dump((new Test)::class); // test3

function test(): Test {
    return new Test();
}

var_dump(test()::class); // test4

var_dump($test->newInstance()::class); // test5

try {
    var_dump($test->noReturnTypes()::class); // test7 Type Error
} catch (TypeError $ex) {
    echo $ex->getMessage() . PHP_EOL;
}
