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
class PropertyHooksClass {
    // invalid properties
    public $invalidEmptyHook {}
    private $invalidPrivateFinal {
        final get{} // invalid1
    }
    private $invalidPublic01 {
        public get; // invalid2
    }
    public $invalidStatic01 {
        static get {} // invalid3
    }
    public static $invalidStatic02 { // invalid4
        get{}
        set{}
    }
    public $invalidNonAbstract {
        get; // invalid5
        set; // invalid6
    }
    public $invalidGetParam {
        get() { // error but parser allows
            var_dump($value);
        }
    }
    public readonly int $invalidReadonly { get{} set{} } // invalid7
    public $invalidSetRef {
        set(&$value) {} // error but parser allows
    }
    public $invalidVariadic {
        set(...$value) {} // error but parser allows
    }
    public $invalidUnknownHook {
        unknown {} // error
    }
    public $prop;
}
