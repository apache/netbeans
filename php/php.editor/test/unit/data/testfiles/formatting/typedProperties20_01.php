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
class TypedPropertiesClass {

    public $withoutType;
    public   bool   $bool;
public                 int $int;
    public   float  $floatX, $floatY;
 private   string    $string;
    private array    $array;
 private    object $object;

 private iterable $iterable;
  protected  self $self;
    protected    parent $parent;
 protected  MyClass $myClass;
    public   ?\Foo\Bar\MyClass $myClass2;

          public static   $staticWithoutType;
    public static bool   $staticBool = true;



                     public static   int $staticInt = 1;
         public static  float $staticFloatX = 1.2, $staticFloatY;
    private static  ?string $staticString = "string";
  private static   array $staticArray = [0, 1, 2];
    private static  ?object  $staticObject = null;
   private static iterable   $staticIterable;
    protected static self    $staticSelf;
   protected static    parent    $staticParent;
   protected static    MyClass $staticMyClass;
public static    \Foo\Bar\MyClass $staticMyClass2;

   var    int   $varInt;

    // Handle these as error in UnusableTypesUnhandledError
   public callable $callble; // NG, Unusable type
   public void $void; // NG, Unusable type
}
