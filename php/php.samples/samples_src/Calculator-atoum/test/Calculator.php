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
namespace tests\unit;

require_once __DIR__ . '/../vendor/autoload.php';

use \mageekguy\atoum;
use \Calculator as TestedClass;

/**
 * Simple class for our unit tests.
 */
class Calculator extends atoum\test {
    /**
     * @dataProvider plusProvider
     */
    public function testPlus($a, $b, $result) {
        $this
            ->if($calculator = new TestedClass())
            ->then()
                ->integer($calculator->plus($a, $b))->isEqualTo($result)
        ;
    }

    protected function plusProvider() {
        return array(
            array(0, 0, 0),
            array(0, 1, 1),
            array(1, 0, 1),
            array(1, 1, 2)
        );
    }

    /**
     * @dataProvider minusProvider
     */
    public function testMinus($a, $b, $result) {
        $this
            ->if($calculator = new TestedClass())
            ->then()
                ->integer($calculator->minus($a, $b))->isEqualTo($result)
        ;
    }

    protected function minusProvider() {
        return array(
            array(0, 0, 0),
            array(0, 1, -1),
            array(1, 0, 1),
            array(1, 1, 0)
        );
    }

    /**
     * @dataProvider multiplyProvider
     */
    public function testMultiply($a, $b, $result) {
        $this
            ->if($calculator = new TestedClass())
            ->then()
                ->integer($calculator->multiply($a, $b))->isEqualTo($result)
        ;
    }

    protected function multiplyProvider() {
        return array(
            array(0, 0, 0),
            array(0, 1, 0),
            array(1, 0, 0),
            array(1, 1, 1),
            array(3, 2, 6)
        );
    }

    /**
     * @dataProvider divideProvider
     */
    public function testDivide($a, $b, $result) {
        $this
            ->if($calculator = new TestedClass())
            ->then()
                ->integer($calculator->divide($a, $b))->isEqualTo($result)
        ;
    }

    protected function divideProvider() {
        return array(
            array(0, 1, 0),
            array(1, 1, 1),
            array(6, 2, 3)
        );
    }

    public function testDivideByZero() {
        $this
            ->if($calculator = new TestedClass())
            ->then()
                ->exception(function() use ($calculator) {
                    $calculator->divide(rand(0, PHP_INT_MAX), 0);
                })
                    ->isInstanceOf('\\InvalidArgumentException')
                    ->hasMessage('Cannot divide by zero')
        ;
    }
}
