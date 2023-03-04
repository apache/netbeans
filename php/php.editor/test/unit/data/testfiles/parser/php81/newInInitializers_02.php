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

namespace Test\Test1;
class Example {

    public string $field = "field";
    public static string $staticField = "staticField";
    public const CONSTANT = "CONSTANT";

    public static function staticMethod(): void {
        echo "staticMethod" . PHP_EOL;
    }

    public function method() {
        echo "method" . PHP_EOL;
    }

}

const CON = new Example();
CON->method();
CON?->method();
\Test\Test1\CON->method();
\Test\Test1\CON?->method();
echo CON->field . PHP_EOL;
echo CON?->field . PHP_EOL;
echo \Test\Test1\CON->field . PHP_EOL;
echo \Test\Test1\CON?->field . PHP_EOL;
// PHP Fatal error:  Uncaught Error: Class "CON" not found
//CON::staticMethod();
//echo CON::$staticField . PHP_EOL;
//echo CON::CONSTANT . PHP_EOL;

namespace Test;

Test1\CON->method();
Test1\CON?->method();
echo Test1\CON->field . PHP_EOL;
echo Test1\CON?->field . PHP_EOL;
