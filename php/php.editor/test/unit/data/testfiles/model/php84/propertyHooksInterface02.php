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

interface InterfaceA {
    public string $a1 {
        get;
    }
    public int $a2 {
        set;
    }
    public $a3 {
        get;
        set;
    }
}

interface InterfaceB extends InterfaceA {
    public string $b1 {
        get;
    }
    public int $b2 {
        set;
    }
    public $b3 {
        get;
        set;
    }
}

interface InterfaceX {
    public string $x1 {
        get;
    }
    public int $x2 {
        set;
    }
    public $x3 {
        get;
        set;
    }
}

interface InterfaceY extends InterfaceB {
    public string $y1 {
        get;
    }
    public int $y2 {
        set;
    }
    public $y3 {
        get;
        set;
    }
}

interface InterfaceZ extends InterfaceX, InterfaceY {
    public string $z1 {
        get;
    }
    public int $z2 {
        set;
    }
    public $z3 {
        get;
        set;
    }
}
