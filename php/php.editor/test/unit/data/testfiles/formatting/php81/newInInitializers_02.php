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

   static $staticVariable = new StaticVariable;
static     $staticVariable = new StaticVariable();
   static $staticVariable =     new StaticVariable(1);
     static $staticVariable =   new   StaticVariable(  x :   1);

   const CONSTANT = new Constant;
   const CONSTANT =    new Constant();
const CONSTANT = new    Constant   (  "test",   "constant");
   const CONSTANT = new Constant(  test: "test",    constant  : "constant");

function   func1(   $param =    new Func) {}
   function func2(  $param = new    Func()) {}
   function  func3(   $param = new Func(1)) {}
function func4(   $param = new Func( test: 1)) {}

#[AnAttribute(  new Foo)]
#[AnAttribute(new   Foo())]
#[AnAttribute(new   Foo( 1 ))]
  #[AnAttribute(new Foo( x: 1))]
class Test {

    public function __construct(
          public $prop1 =  new   Foo,
          public    $prop2 = new Foo(),
         public $prop3 = new Foo  ("test"),
         public $prop4 = new    Foo(test: "test"),
    ) {
    }

}

class ParentClass {
}

class ChildClass {
    public static function method(  $self =  new self,  $parent =   new   parent) {
    }
}
