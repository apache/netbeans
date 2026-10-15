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

abstract class ParentClass {
    /**
     * inheritdocMethod description.
     *
     * @param (A&B|C) $param Description
     * @return null|(B&C) Description
     */
    public abstract function inheritdocMethod((A&B)|C $param): null|(B&C);

}

/**
 * Description of CCDoc
 *
 * @method (A&B)|C methodTag(A|(B&C&D)|C $param1, (A&B)|null $param2) test description
 * @method static (A&B)|C staticMethodTag(A|(B&C&D)|C $param1, (A&B)|null $param2) static test description
 * @property (A&B)|(A&C) $property Description
 */
class DNFTypes extends ParentClass {

    private (A&B)|C $privateField;
    /**
     * @var (A&B&C)|(A&B)|(A&C)
     */
    private $phpdocField;
    private static null|(A&B) $privateStaticField;
    /**
     * @var null|(A&B) test description
     */
    private static $phpdocStaticField;

    public function methodTest((A&B)|null $param1, null|(B&C) $param2 = null): (A&B&C)|A {
    }

    /**
     * Description.
     *
     * @param (A&B)|null $param1
     * @param null|(B&C) $param2
     * @return (A&B&C)|A
     */
    public function phpdocMethodTest($param1, $param2 = null) {
    }

    /**
     * @param (A&B)|null $param1 description
     */
    public static function staticMethodTest((A&B)|null $param1, null|(B&C) $param2 = null): (A&\B&C)|A {
    }

    /**
     * PhpDoc static method.
     *
     * @param (A&B)|null $param1 param1 description
     * @param null|(B&C) $param2 param2 description
     * @return (A&\B&C)|A
     */
    public static function phpdocStaticMethodTest($param1, $param2 = null) {
    }

    /**
     * {@inheritDoc}
     */
    public function inheritdocMethod((A&B)|C $param): null|(B&C) {
    }

    public function test(): U&V {
        $this->property;
        $this->methodTag($param1, $param2);
        self::staticMethodTag($param1, $param2);
        $this->methodTest(null);
        $this->phpdocMethodTest(null);
        $this->inheritdocMethod(null);
        self::staticMethodTest(null);
        self::phpdocStaticMethodTest(null);
        $this->privateField;
        $this->phpdocField;
        static::$privateStaticField;
        static::$phpdocStaticField;
    }
}
