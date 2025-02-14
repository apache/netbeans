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

interface PropertyHookInterface {
    // valid properties
    public string $prop1 {
        get;
    }
    final public int $prop2 {
        set;
    }
    public $prop3 {
        get;
        set;
    }
    public $ref { &get; }

    // invalid properties
    abstract public $invalid01 { get; set; } // error but parser allows
    protected $invalid02 {get; set;} // error but parser allows
    private $invalid03 { // error but parser allows
        get;
        set;
    }
    public $invalid04 { final get; } // error but parser allows
    final public $invalid05 { get; set; } // error but parser allows
    public $invalid06 {
        get {} // error but parser allows
    }
    public $invalid07 {
        set {} // error but parser allows
    }
    public $invalid08 {
        get {} // error but parser allows
        set {} // error but parser allows
    }
    public private(set) int $invalid09 {
        set;
    }
    final public int $invalid10 { // error but parser allows
        set;
    }
    public readonly int $invalid10 { // error but parser allows
        get;
    }
}
