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
interface Iface {
    public int $i01 {get; set;}
    public string $i02 {set;}
    public int|string $i03 {get;}
}

abstract class AbstractClass implements Iface {
    public abstract $a01 { // OK
        get;
        set;
    }
    public abstract $a02 { // OK
        get;
        set {
            echo __METHOD__ . PHP_EOL;
        }
    }
    public abstract $a03 { // OK
        get {
            echo __METHOD__ . PHP_EOL;
        }
        set;
    }
    protected abstract int $a04 { #[Attr] &get; set; } // OK
    public string $a05 { // OK
        get {
            echo __METHOD__ . PHP_EOL;
        }
        set {
            echo __METHOD__ . PHP_EOL;
        }
    }
    private string $ap01 {
        get{} set{}
    }
}

class Impl extends AbstractClass {
    public int $impl01 = 1;
}
