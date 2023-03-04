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

$name="Junichi";
$age=100;
echo <<<'NOWDOC'
Name: $name<br>
My age is: $age<br>
NOWDOC;
$name="Jun";

// defferent indentation for body(spaces) ending marker(tabs)
// allow this in the lexer but spaces and tabs MUST NOT be intermixed
$x = <<<'ENDOFNOWDOC'
    This is another nowdoc test.
    With another line in it. {$test} $test ${test} variable test.
    test. $object->field
		ENDOFNOWDOC;

// body(tabs) ending marker(spaces)
$y = <<<'ENDOFNOWDOC'
		This is another nowdoc test.
		With another line in it. {$test} $test ${test} variable test.
		test. $object->field
    ENDOFNOWDOC;
