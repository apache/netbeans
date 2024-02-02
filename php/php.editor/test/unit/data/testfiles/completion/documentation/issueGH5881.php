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

class GH5881 {
    /**
     * Property $prop.
     */
    public $prop;
    /**
     * Property $prop_aa.
     */
    public $prop_aa;
    /**
     * Property $prop_aa_bbb.
     */
    public $prop_aa_bbb;
    /**
     * Property $prop_aa_bb_cc.
     */
    public $prop_aa_bb_cc;

    /**
     * Method method().
     *
     * @return void
     */
    public function method(): void {
    }

    /**
     * Method method_aa().
     *
     * @return void
     */
    public function method_aa(): void {
    }

    /**
     * Method method_aa_bbb().
     *
     * @return void
     */
    public function method_aa_bbb(): void {
    }

    /**
     * Method method_aa_bb_cc().
     *
     * @return void
     */
    public function method_aa_bb_cc(): void {
    }
}
$test = new GH5881();
$prop1 = $test->prop_aa_bbb;
$prop2 = $test->prop_aa_bb_cc;
$test->method_aa_bbb();
$test->method_aa_bb_cc();
