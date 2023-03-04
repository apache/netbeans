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
class InvocationComment {
  public function foo() {return $this;}
  public static function bar() {}
}
$obj = new InvocationComment();
$obj/**/?->foo(); // test1
$obj /**/?->foo(); // test2
$obj/**/ ?->foo(); // test3
$obj /* aa */ ?->foo(); // test4
$obj/**/?-> /**/foo(); // test5
$obj/**/?-> /**/ foo(); // test6
$obj/**/?->/**/ foo(); // test7
$obj/**/?->/**/foo(); // test8
$obj/**/?-> /* aa */foo(); // test9
$obj/**/?-> /* aa */ foo(); // test10
$obj/**/?->/* aa */foo(); // test11
$obj/**/?->/* aa */ foo(); // test12
$obj/**/?->
 /* aa */
 foo();
$obj/**/?->
 /** aa */
 foo();
$obj/**/?->
 /**/
 foo();
$obj/**/?->
 // aa
 foo();
$obj/**/?->
 // aa
foo();
$obj/**/?->
// aa
foo();
