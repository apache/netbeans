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

const CONSTANT = "global";

class Visibility {

    var $var = "public";
    public $public = "public";
    public $public1 = "public", $public2 = "public";
    private $private = "private";
    protected $protected = "protected";
    public static $publicStatic = "public";
    public static $publicStatic1 = "public", $publicStatic2 = "public";
    private static $privateStatic = "private";
    protected static $protectedStatic = "protected";

    const IMPLICIT_CONST = "implicit";
    public const PUBLIC_CONST = "public";
    private const PRIVATE_CONST = "private";
    protected const PROTECTED_CONST = "protected";

    function implicitMethod($param) {
    }

    public function publicMethod($param) {
    }

    private function privateMethod($param) {
    }

    protected function protectedMethod($param) {
    }

    static function implicitStaticMethod($param) {
    }

    public static function publicStaticMethod($param) {
    }

    private static function privateStaticMethod($param) {
    }

    protected static function protectedStaticMethod($param) {
    }

}

abstract class AbstractVisibilityClass {

    abstract function abstractImplicitPublic();
    abstract public function abstractPublic();
    abstract protected function abstractProtected();
    // can't be declared private
    // abstract private function abstractPrivate();
    abstract static function abstractImplicitPublicStatic();
    abstract public static function abstractPublicStatic();
    abstract protected static function abstractProtectedStatic();
}

interface VisibilityInterface {

    const INTERFACE_IMPLICIT_CONST = "implicit";
    public const INTERFACE_PUBLIC_CONST = "implicit";

    function interfaceImplicitMethod($param);

    public function interfacePublicMethod($param);

    static function interfaceImplicitStaticMethod();

    public static function interfaceImplicitPublicStaticMethod();
}
