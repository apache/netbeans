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
// https://issues.apache.org/jira/browse/NETBEANS-4503
class NETBEANS4503Test
{

    public function testMethod() {
        $test = "test";
        function testNestedFunction() {
            echo "testNestedFunction()" . PHP_EOL;
            $e = new Example();
            $e->test(); // test1

            function testNestedFunction2() {
                echo "testNestedFunction2()" . PHP_EOL;
                $ex = new Example();
                $ex->test(); // test2
            }
            testNestedFunction2(); // test3
        }
        testNestedFunction(); // test4
        testNestedFunction2();
    }

    public function testMethod2() {
        testNestedFunction2();
    }

}

class Example
{
    public function test() {
    }
}

function testGlobalFunction() {
    echo "testGlobalFunction()" . PHP_EOL;

    function testNestedGlobalFunction() {
        echo "testNestedGlobalFunction()" . PHP_EOL;
        $example = new Example();
        $example->test(); // test5
    }
    testNestedGlobalFunction();

}

$test = new NETBEANS4503Test();
$test->testMethod();
$test->testMethod2();
testNestedFunction2(); // test6
//testNestedFunction(); error becuase the testNestedFunction is redeclare
testGlobalFunction();
testNestedGlobalFunction();

// Output
//
//testNestedFunction()
//testNestedFunction2()
//testNestedFunction2()
//testNestedFunction2()
//testNestedFunction2()
//testGlobalFunction()
//testNestedGlobalFunction()
//testNestedGlobalFunction()
