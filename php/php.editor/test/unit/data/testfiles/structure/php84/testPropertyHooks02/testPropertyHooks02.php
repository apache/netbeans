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
namespace Test;

interface Interface1 {
    public $i1_01 {
        get; set;
    }
    public $i1_02 {
        get; set;
    }
}

interface Interface2 extends Interface1 {
    public $i2_01 {
        get; set;
    }
    public $i2_02 {
        get; set;
    }
    public $i2_03 {
        get; set;
    }
}

interface Interface3 extends Interface1 {
    public $i3_01 {
        get; set;
    }
    public $i3_02 {
        get; set;
    }
    public $i3_03 {
        get; set;
    }
}

trait Trait1 {
    use Trait2;
    public int $t1_01 {set{}}
    private int $t1_02 {get{}}
    protected int $t1_03 {get{} set{}}
}

trait Trait2 {
    public int $t2_01 {get{} set{}}
    private int $t2_02 {set{}}
    protected int $t2_03 {get{} set{}}
}

trait Trait3 {
    public int $t3_01 {get{}}
    private int $t3_02 {get{} set{}}
    protected int $t3_03 {set{}}
}

abstract class AbstractClass implements Interface2, Interface3 {
    public $ac_01 {
        get {
            return $this->prop1;
        }
        set {
            $this->valid01 = $value;
        }
    }
    public $ac_02 { get {} set {} }
    public $ac_03 { get {} set {} }
    public $ac_04;
    protected $ac_05;
    private $ac_private_01 {get{} set{}}
}

abstract class AbstractTest extends AbstractClass {
    public int $prop = 100;
}

class Child extends AbstractTest implements Interface1{
    use Trait1, Trait3;
    public int $prop {
        get => parent::$prop::get();
        set {
            parent::$prop::set($value);
        }
    }
}
