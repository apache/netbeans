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

//multiple callables
$result = "Example"
    |> 'strtoupper'
    |> str_shuffle(...)
    |> fn($x) => trim($x)
    |> function(string $x): string {return strtolower($x);}
    |> new ExampleClass()
    |> [ExampleClass::class, 'staticMethod']
    |> new ExampleClass()->hash(...)
    |> my_function(...);

echo $result;

function my_function(string $x): string {
    return str_replace("le", "les", $x);
}

class ExampleClass {
    public function __invoke(string $x): string {
        return strtoupper($x);
    }

    public function hash(string $x): string {
        return hash('sha256', $x);
    }

    public static function staticMethod(string $x): string {
        return str_replace('E', 'O', $x);
    }
}