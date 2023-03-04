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

// readonly property must have type
readonly class ReadonlyClass {
    public $field;
    public static $staticField;
    public static int $staticIntField;
}

readonly class ReadonlyPromotedConstructor {
    public function __construct(
        private $field
    ) {}
}

class NonReadonlyParentClass {}
readonly class ReadonlyParentClass {}
interface Iface {}

// readonly class cannot extends non-readonly class
readonly class ReadonlyChildClass extends NonReadonlyParentClass implements Iface {}
// non-readonly class cannot extend readonly class
class NonReadonlyChildClass extends ReadonlyParentClass {}

// #[AllowDynamicProperties] cannot apply to readonly classes
#[AllowDynamicProperties]
readonly class ReadonlyClass {}

#[AllowDynamicProperties]
readonly final class ReadonlyFinalClass {}

#[AllowDynamicProperties]
final readonly class FinalReadonlyClass {}

#[AllowDynamicProperties]
readonly abstract class ReadonlyAbstractClass {}

#[AllowDynamicProperties]
abstract readonly class AbstractReadonlyClass {}

#[AllowDynamicProperties]
class AllowDynamicPropertiesTest {}
