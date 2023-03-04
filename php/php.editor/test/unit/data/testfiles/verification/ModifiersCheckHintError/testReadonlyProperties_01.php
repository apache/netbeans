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

class ReadonlyProperties {

    // Fatal error: Readonly property ReadonlyProperties::$withDefaultVAlue
    //  cannot have default value
    readonly int $withDefaultValue0 = 1; // error
    public readonly int $withDefaultValue1 = 1; // error
    public readonly int $withDefaultValue2 = 1, $withDefaultValue3 = 2; // error

    // Fatal error: Readonly property ReadonlyProperties::$noType must have type
    readonly $noType1;
    protected readonly $noType2;
    protected readonly string|int $protectedReadonly;

    // promoted readonly properties
    public function __construct(
        // Fatal error: Readonly property ReadonlyProperties::$promotedPublicReadonly must have type
        readonly $promotedPublicReadonly = 0,
        public readonly $promotedPublicReadonly1 = 0,
        public readonly int $promotedPublicReadonly2 = 0,
    ) {}
}

trait ReadonlyPropertiesTrait {

    // Fatal error: Readonly property ReadonlyProperties::$withDefaultVAlue
    //  cannot have default value
    readonly int $withDefaultValue0 = 1; // error
    public readonly int $withDefaultValue = 1; // error
    public readonly int $withDefaultValue2 = 1, $withDefaultValue3 = 2; // error

    // Fatal error: Readonly property ReadonlyProperties::$noType must have type
    readonly $noType1;
    protected readonly $noType2;
    protected readonly string|int $protectedReadonly;

    // promoted readonly properties
    public function __construct(
        // Fatal error: Readonly property ReadonlyProperties::$promotedPublicReadonly1 must have type
        readonly $promotedPublicReadonly = 0,
        public readonly $promotedPublicReadonly1 = 0,
        public readonly int $promotedPublicReadonly2 = 0,
    ) {}
}
