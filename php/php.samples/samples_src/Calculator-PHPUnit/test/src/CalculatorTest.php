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

use PHPUnit\Framework\TestCase;

class CalculatorTest extends TestCase {

    /**
     * @var Calculator
     */
    protected $object;

    /**
     * Sets up the fixture, for example, opens a network connection.
     * This method is called before a test is executed.
     */
    protected function setUp(): void {
        $this->object = new Calculator;
    }

    /**
     * Tears down the fixture, for example, closes a network connection.
     * This method is called after a test is executed.
     */
    protected function tearDown(): void {
        
    }

    public function testPlus() {
        $this->assertEquals(
                0,
                $this->object->plus(0, 0)
        );
    }

    public function testPlus2() {
        $this->assertEquals(
                1,
                $this->object->plus(0, 1)
        );
    }

    public function testPlus3() {
        $this->assertEquals(
                1,
                $this->object->plus(1, 0)
        );
    }

    public function testPlus4() {
        $this->assertEquals(
                2,
                $this->object->plus(1, 1)
        );
    }

    public function testMinus() {
        $this->assertEquals(
                0,
                $this->object->minus(0, 0)
        );
    }

    public function testMinus2() {
        $this->assertEquals(
                -1,
                $this->object->minus(0, 1)
        );
    }

    public function testMinus3() {
        $this->assertEquals(
                1,
                $this->object->minus(1, 0)
        );
    }

    public function testMinus4() {
        $this->assertEquals(
                0,
                $this->object->minus(1, 1)
        );
    }

    public function testMultiply() {
        $this->assertEquals(
                0,
                $this->object->multiply(0, 0)
        );
    }

    public function testMultiply2() {
        $this->assertEquals(
                0,
                $this->object->multiply(0, 1)
        );
    }

    public function testMultiply3() {
        $this->assertEquals(
                0,
                $this->object->multiply(1, 0)
        );
    }

    public function testMultiply4() {
        $this->assertEquals(
                1,
                $this->object->multiply(1, 1)
        );
    }

    public function testMultiply5() {
        $this->assertEquals(
                6,
                $this->object->multiply(3, 2)
        );
    }

    public function testDivide() {
        $this->assertEquals(
                0,
                $this->object->divide(0, 1)
        );
    }

    public function testDivide2() {
        $this->assertEquals(
                1,
                $this->object->divide(1, 1)
        );
    }

    public function testDivide3() {
        $this->assertEquals(
                3,
                $this->object->divide(6, 2)
        );
    }

    public function testModulo() {
        // Remove the following lines when you implement this test.
        $this->markTestIncomplete(
                'This test has not been implemented yet.'
        );
    }

}
