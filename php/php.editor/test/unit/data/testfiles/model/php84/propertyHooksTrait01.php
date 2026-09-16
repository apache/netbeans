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

trait Trait00 {
    public $t0_01_public;
    private $t0_02_private;
    protected $t0_03_protected;
}

trait Trait01 {
    public $t1_01_public {
        get {
            return $this->prop1;
        }
        set {
            $this->valid01 = $value;
        }
    }
    private int $t1_02_private {
        get {
            return $this->prop1;
        }
        set {
            $this->valid01 = $value;
        }
    }
    protected string|int $t1_03_protected {
        get {}
        set {}
    }
    public abstract $t1_04_public_abstract {
        get;
        set;
    }
    public $t1_05_public;
    private $t1_06_private;
    protected $t1_07_protected;
}

trait Trait02 {
    public $t2_01_public {
        get {}
    }
    private int $t2_02_private {
        set {}
    }
    protected string|int $t2_03_protected {
        get {}
        set {}
    }
    public abstract $t2_04_public_abstract {
        get;
        set;
    }
    public $t2_05_public;
    private $t2_06_private;
    protected $t2_07_protected;
}

trait Trait03 {
    public $t3_01_public {
        get {}
    }
    private int $t3_02_private {
        set {}
    }
    protected string|int $t3_03_protected {
        get {}
        set {}
    }
    public abstract $t3_04_public_abstract {
        get;
        set;
    }
    public $t3_05_public;
    private $t3_06_private;
    protected $t3_07_protected;
}

trait Trait04 {
    use Trait03;
    public $t4_01_public {get {}}
    private int $t4_02_private {set {}}
    protected string|int $t4_03_protected {get {}set {}}
    public abstract $t4_04_public_abstract {get;set;}
    public $t4_05_public;
    private $t4_06_private;
    protected $t4_07_protected;
    public $t3_05_public;
}

trait Trait05 {
    use Trait02, Trait04;
    public $t5_01_public {get {}}
    private int $t5_02_private {set {}}
    protected string|int $t5_03_protected {#[A]get {}set {}}
    public abstract $t5_04_public_abstract {get;set;}
    public $t5_05_public;
    private $t5_06_private;
    protected $t5_07_protected;
    public $t4_05_public;
}

abstract class TestClass {
    use Trait05;
    public $t4_05_public;
}

class Child extends TestClass {
    use Trait00;
    public $t1_04_public_abstract {
        get => 1;
        set {}
    }
    public $t2_04_public_abstract {
        get => 1;
        set {}
    }
    public $t3_04_public_abstract {
        get => 1;
        set {}
    }
    public $t4_04_public_abstract {
        get => 1;
        set {}
    }
    public $t5_04_public_abstract {
        get => 1;
        set {}
    }
    public $t0_01_public;
    public $t3_05_public;
    public $t4_05_public;
    protected $t4_07_protected;
    private $t5_06_private;
}
