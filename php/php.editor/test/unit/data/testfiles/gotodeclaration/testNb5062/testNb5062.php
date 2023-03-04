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

class Test1
{

    private function getTest2() {
        return Test2::returnStatic();
    }

    private function getTest2ReturnType() {
        return Test2::returnStaticReturnType();
    }

    private function getTest2PHPDoc() {
        return Test2::returnStaticPHPDoc();
    }

    private function getTest2Self() {
        return Test2::returnSelf();
    }

    private function getTest2SelfReturnType() {
        return Test2::returnSelfReturnType();
    }

    private function getTest2SelfPHPDoc() {
        return Test2::returnSelfPHPDoc();
    }

    public function testMethod() {
        echo "Test1" . PHP_EOL;
    }

    public function test() {
        $static1 = $this->getTest2();
        $static1->testMethod();

        $static2 = $this->getTest2ReturnType();
        $static2->testMethod();

        $static3 = $this->getTest2PHPDoc();
        $static3->testMethod();

        $self1 = $this->getTest2Self();
        $self1->testMethod();

        $self2 = $this->getTest2SelfReturnType();
        $self2->testMethod();

        $self3 = $this->getTest2SelfPHPDoc();
        $self3->testMethod();
    }

}

class Test2
{

    public static function returnStatic() {
        return new static();
    }

    public static function returnStaticReturnType(): static {
    }

    /**
     * @return static
     */
    public static function returnStaticPHPDoc() {
    }

    public static function returnSelf() {
        return new self();
    }

    public static function returnSelfReturnType(): self {
    }

    /**
     * @return self
     */
    public static function returnSelfPHPDoc() {
    }

    public function testMethod() { // Test2
        echo "Test2" . PHP_EOL;
    }

}
