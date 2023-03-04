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

use Calculator;

class CalculatorTest extends \Codeception\TestCase\Test {

    /**
     * @var UnitTester
     */
    protected $tester;

    /**
     * @var Calculator
     */
    private $calculator;


    protected function setUp() {
        $this->calculator = new Calculator();
    }

    protected function tearDown() {
    }

    protected function _before() {
    }

    protected function _after() {
    }

    // tests
    public function testPlus() {
        $this->assertEquals(1, $this->calculator->plus(-1, 2));
        $this->assertEquals(2, $this->calculator->plus(1, 1));
        $this->assertEquals(1, $this->calculator->plus(2, -1));
        $this->assertEquals(-10, $this->calculator->plus(0, -10));
        $this->assertEquals(10, $this->calculator->plus(10, -0));
    }

    public function testMinus() {
        $this->assertEquals(0, $this->calculator->minus(0, 0));
        $this->assertEquals(0, $this->calculator->minus(1, 1));
        $this->assertEquals(-1, $this->calculator->minus(0, 1));
        $this->assertEquals(1, $this->calculator->minus(1, 0));
        $this->assertEquals(-3, $this->calculator->minus(-2, 1));
        $this->assertEquals(4, $this->calculator->minus(1, -3));
        $this->assertEquals(1, $this->calculator->minus(-2, -3));
    }

    public function testMultiply() {
        $this->assertEquals(6, $this->calculator->multiply(2, 3));
        $this->assertEquals(0, $this->calculator->multiply(0, 3));
        $this->assertEquals(0, $this->calculator->multiply(3, 0));
        $this->assertEquals(-9, $this->calculator->multiply(3, -3));
        $this->assertEquals(-6, $this->calculator->multiply(-3, 2));
        $this->assertEquals(12, $this->calculator->multiply(-3, -4));
    }

    public function testDivide() {
        $this->assertEquals(0, $this->calculator->divide(0, 3));
        $this->assertEquals(2, $this->calculator->divide(6, 3));
        $this->assertEquals(-4, $this->calculator->divide(-12, 3));
        $this->assertEquals(-1, $this->calculator->divide(3, -3));
        $this->assertEquals(3, $this->calculator->divide(-9, -3));
    }

    /**
     * @expectedException InvalidArgumentException
     */
    public function testDivideZero() {
        $this->calculator->divide(6, 0);
    }

}
