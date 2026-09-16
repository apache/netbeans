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

trait PropertyHooksTrait {
    // valid properties
    public $valid01 {
        get {
            return $this->prop1;
        }
        set {
            $this->valid01 = $value;
        }
    }
    public int $valid02 = 1 {
        get {
            echo __METHOD__, "\n";
            return $this->valid02;
        }
        set($value){
            $this->valid02 = $value;
        }
    }
    public $valid03 = "string" {
        get {
            return $this->valid03;
        }
        set {}
    }
    public string $valid04 = self::CONSTANT {
        get => $this->valid04;
        set {}
    }
    public array $valid05 = [] {
        get => $this->valid05;
        set => $this->valid05 = $value;
    }
    public private(set) string $valid06 = self::CONSTANT {
        get {
            return $this->valid06 . "test";
        }
        set {}
    }
    public $valid07 { // virtual
        get => $this->test();
        set => $this->test() . $value;
    }
    public string $valid08 {
        set(string|array $param) {
            $this->valid08 = is_array($param) ? join(', ', $param) : $param;
        }
    }
    public $valid09 {
        #[Arri1] get {}
        #[Attr2] set {}
    }
    public $valid10 = 100 {
        get {
            yield 1;
            yield $this->valid10;
            yield 3;
        }
    }
    public $valid11 { // virtual
        get {
            yield 1;
            yield 2;
            yield 3;
        }
    }
    public $valid12 {
        set(#[SensitiveParameter] $value) {
            throw new Exception('test');
        }
    }
    public $valid13 {
        final get { return 100; }
    }
    final public $valid14 {
        final get => $this->valid14;
    }
    public $valid15 {
        &get => $this->valid15;
    }
    public $closure {
        get {
            return function () {
                return $this->closure;
            };
        }
    }
    public $arrowFunction {
        get {
            return fn() => $this->arrowFunction;
        }
    }
    private $propertyConst {
        get => __PROPERTY__;
    }
    var $var { get => 100; }
}
