<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
