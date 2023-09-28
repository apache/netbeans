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
class X {}
class Y {}
class Z {}
class Test {}

class TestClass {
    private (X&Y)|Z $dnfType1;
    private X|(X&Y)|Z $dnfType2;
    /**
     * @var X|(X&Y&Z)
     */
    private $phpDocType1;
    public function __construct(
            private X|(X&Y&Z) $promotedField1,
            private (X&Z)|(X&Y&Z) $promotedField2,
            (X&Z)|Z $field,
    ) {
    }
}

trait TestTrait {
    private (X&Y)|(X&Test) $dnfType1;
    private (X&Y)|Y|(X&Y&Z) $dnfType2;
    private (X&Y)|(Y&Z)|(X&Y&Z) $dnfType3;
    private X|(Y&\Z)|Test $dnfType4;
}

/**
 * @property $prop Description
 * @property (X&Y)|Test $prop1 Description
 * @property-read X|(Y&Z) $prop2 Description
 * @property-write X|(Y&Z)|Test $prop3 Description
 * @property (X&Z)|(Y&Test) $prop4 Description
 * @param X|(Y&Z) $param
 */
class Properties {}
