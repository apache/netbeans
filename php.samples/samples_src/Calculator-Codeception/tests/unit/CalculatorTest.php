<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
