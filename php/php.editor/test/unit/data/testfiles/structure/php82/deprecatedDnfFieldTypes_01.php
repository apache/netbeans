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
/**
 * @deprecated
 */
class DeprecatedType {}

class TestClass {
    private (X&Y)|DeprecatedType $dnfType1;
    private X|(X&DeprecatedType)|Z $dnfType2;
    /**
     * @var X|(X&DeprecatedType&Z)
     */
    private $phpDocType1;
    public function __construct(
            private DeprecatedType|(X&Y&Z) $promotedField1,
            private (X&DeprecatedType)|(X&Y&Z) $promotedField2,
            (DeprecatedType&Z)|Z $field,
    ) {
    }
}

trait TestTrait {
    private (X&Y)|(DeprecatedType&Test) $dnfType1;
    private (X&Y)|DeprecatedType|(X&Y&Z) $dnfType2;
    private (X&Y)|(DeprecatedType&Z)|(X&Y&Z) $dnfType3;
    private X|(Y&\DeprecatedType)|Test $dnfType4;
}

/**
 * @property $prop Description
 * @property (X&Y)|DeprecatedType $prop1 Description
 * @property-read DeprecatedType|(Y&Z) $prop2 Description
 * @property-write X|(DeprecatedType&Z)|Test $prop3 Description
 * @property (X&DeprecatedType)|(Y&Test) $prop4 Description
 * @param X|(Y&DeprecatedType) $param
 */
class Properties {}
