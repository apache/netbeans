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

#[Interface1]
interface I {
    #[Constant1(1)]
    const I_CONST1 = "interface";
    #[Constant2("test")]
    public const I_CONST2 = "interface";
    #[Method1]
    public function method(#[Parameter1] $param1, #[Parameter2(), Parameter2("test")] $param2): void;
}

$ref = new \ReflectionClass(I::class);
$sources = [
    $ref,
    $ref->getReflectionConstant('I_CONST1'),
    $ref->getReflectionConstant('I_CONST2'),
    $ref->getMethod('method'),
    $ref->getMethod('method')->getParameters()[0],
    $ref->getMethod('method')->getParameters()[1],
];

foreach ($sources as $r) {
	$attr = $r->getAttributes();
	var_dump(get_class($r), count($attr));

    foreach ($attr as $a) {
        var_dump($a->getName(), $a->getArguments());
    }

    echo PHP_EOL;
}
