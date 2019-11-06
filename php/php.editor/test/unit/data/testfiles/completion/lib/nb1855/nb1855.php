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

class ParentClass {

    public const PUBLIC_PARENT_CONSTANT = "pubic constant";
    protected const PROTECTED_PARENT_CONSTANT = "protected constatnt";
    private const PRIVATE_PARENT_CONSTANT = "private constant";

    public $publicParentProperty;
    protected $protectedParentProperty;
    private $privateParentProperty;
    public static $publicStaticParentProperty;
    protected static $protectedParentStaticProperty;
    private static $privateParentStaticProperty;

    public function publicParentMethod($param) {
        
    }

    protected function protectedParentMethod($param) {
        
    }

    private function privateParentMethod($param) {
        
    }

    public static function publicParentStaticMethod($param, $param2) {
        
    }

    protected static function protectedParentStaticMethod($param, $param2) {
        
    }

    private static function privateParentStaticMethod($param, $param2) {
        
    }

}

trait Trait1855 {

    public $publicTraitProperty;
    protected $protectedTraitProperty;
    private $privateTraitProperty;
    public static $publicStaticTraitProperty;
    protected static $protectedTraitStaticProperty;
    private static $privateTraitStaticProperty;

    public function publicTraitMethod($param) {
          // trait 1
        $p; // trait 2
        protectedTraitMethod($param); // trait 3
    }

    protected function protectedTraitMethod($param) {
        
    }

    private function privateTraitMethod($param) {
        
    }

    public static function publicTraitStaticMethod($param, $param2) {
        
    }

    protected static function protectedTraitStaticMethod($param, $param2) {
        
    }

    private static function privateTraitStaticMethod($param, $param2) {
        
    }

}

class Class1855 extends ParentClass {

    public const PUBLIC_CONSTANT = "pubic constant";
    protected const PROTECTED_CONSTANT = "protected constatnt";
    private const PRIVATE_CONSTANT = "private constant";
    public const TEST1 = ;
    public const TEST2 = pri;

    public $publicProperty;
    protected $protectedProperty;
    private $privateProperty;
    public static $publicStaticProperty;
    protected static $protectedStaticProperty;
    private static $privateStaticProperty;

    use Trait1855;

    public function test() {
          // test1
        $ // test2
        pri // test3
    }

    public function publicMethod($param) {
        
    }

    protected function protectedMethod($param) {
        
    }

    private function privateMethod($param) {
        
    }

    public static function publicStaticMethod($param, $param2) {
        
    }

    protected static function protectedStaticMethod($param, $param2) {
        
    }

    private static function privateStaticMethod($param, $param2) {
        
    }

}
