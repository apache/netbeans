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

namespace IntersectionTypes1;

interface Interface1
{
    public function interfaceMethod(Class1&Class2 $param): Class2&Class1;
}

class InterfaceImpl implements Interface1
{

    use Trait1;

    public Class1&Class2 $publicFieldInterfaceImpl;
    public static Class1&Class2 $publicStaticFieldInterfaceImpl;

    public function interfaceMethod(Class1&Class2 $object): Class2&Class1 {
        $this->publicFieldInterfaceImpl->publicMethodClass1();
        $this->publicFieldTrait1->publicMethodClass1();
        $this::$publicStaticFieldTrait1->publicMethodClass1();
        $this::publicStaticMethodTrait1()::publicStaticMethodClass1();
        self::$publicStaticFieldInterfaceImpl->publicMethodClass1();
        self::$publicStaticFieldInterfaceImpl::CONST_CLASS1;
        $object->publicMethodClass1()->publicMethodClass2();
        $object->publicMethodClass1()::CONST_CLASS2;
        $object::$publicFieldClass1->publicMethodClass1();
        $object::$publicFieldClass1::publicStaticMethodClass1();
        return new Class1();
    }

    /**
     * @param Class1&Class2 $object
     * @return Class2&Class1
     */
    public function testMethodPhpDoc($object) {
        $object->publicMethodClass2(); // phpdoc
        $object::publicStaticMethodClass2(); // phpdoc
    }

    public function testWithWhitespaces(Class1 & Class2 $object): Class2&Class1 {
        $object->publicMethodClass1(); // with whitespaces
        $object::publicStaticMethodClass1(); // with whitespaces
    }
}

class Class1
{

    public Class1&Class2 $publicFieldClass1;

    public const CONST_CLASS1 = "constant";

    public static Class1&Class2 $publicStaticFieldClass1;

    public function publicMethodClass1(): Class1 & Class2 { // with whitespaces
        return new Class2();
    }

    public static function publicStaticMethodClass1(): Class1&Class2 {
        return new Class2();
    }

}

class Class2
{

    public Class1&Class2 $publicFieldClass2;

    public const CONST_CLASS2 = "constant";

    public static Class1&Class2 $publicStaticFieldClass2;

    public function publicMethodClass2(): Class1&Class2 {
        return new Class2();
    }

    public static function publicStaticMethodClass2(): Class1&Class2 {
        return new Class2();
    }

}

trait Trait1
{

    public Class1 & Class2 $publicFieldTrait1; // with whitespaces
    public static Class1&Class2 $publicStaticFieldTrait1;

    public function publicMethodTrait1(
            Class2&Class1 $object
    ): Class2&Class1 {
        return new Class1();
    }

    public static function publicStaticMethodTrait1(): Class1&Class2 {
        return new Class2();
    }

}

$instance = new InterfaceImpl();
var_dump($instance->interfaceMethod(new Class1())->publicMethodClass1());
