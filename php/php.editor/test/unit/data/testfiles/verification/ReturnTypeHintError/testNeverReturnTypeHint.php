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

function invalidNeverReturnType1(): ?never { // invalidNeverReturnType1 func
}

function invalidNeverReturnType2(): ?never { // invalidNeverReturnType2 func
    return;
}

function neverReturnType1(): never {
    return;
}

function neverReturnType2(): never {
}

function neverReturnType3(): never {
    return true; // neverReturnType3 func
}

function neverReturnType4(): never {
    $foo = "foo";
    return $foo; // neverReturnType4 func
}

function neverReturnType5(): never {
    return new NeverReturnTypeHint(); // neverReturnType5 func
}

function returnType1(): NeverReturnTypeHint {
    return new NeverReturnTypeHint();
}

class NeverReturnTypeHint {

    public function neverReturnType1(): never {
        if (true) {
            return;
        }
    }

    public function neverReturnType2(): never {
        return;
    }

    public function neverReturnType3(): never {
        return new NeverReturnTypeHint(); // neverReturnType3 class
    }

    public function neverReturnType4(): never {
        foreach ($array as $value) {
            return new NeverReturnTypeHint(); // neverReturnType4 class
        }
    }

    public function anon1(): NeverReturnTypeHint {
        $anon = function(): never {
            return;
        };
        return new NeverReturnTypeHint();
    }

    public function anon2(): NeverReturnTypeHint {
        $anon = function(): never {
        };
        return new NeverReturnTypeHint();
    }

    public function anon3(): NeverReturnTypeHint {
        $anon = function(): never {
            return "anon"; // anon3 class
        };
        return new NeverReturnTypeHint();
    }

    public function anon4(): never {
        $anon = function(): never {
            return "anon"; // anon4 class
        };
        return new NeverReturnTypeHint(); // anon4 class
    }

}

trait NeverReturnTypeHintTrait {

    public function neverReturnType1(): never {
    }

    public function neverReturnType2(): never {
        return;
    }

    public function neverReturnType3(): never {
        switch ($variable) {
            case 1:
                break;
            case 2:
                return;
            default:
                break;
        }
    }

    public function neverReturnType4(): never {
        return new NeverReturnTypeHint(); // neverReturnType4 trait
    }

    public function neverReturnType5(): never {
        while (true) {
            return "test"; // neverReturnType5 trait
        }
        return 1; // neverReturnType5 trait
    }

    public function anon1(): NeverReturnTypeHint {
        $anon = function(): ?never { // anon1 trait
        };
        return new NeverReturnTypeHint();
    }

    public function anon2(): NeverReturnTypeHint {
        $anon = function(): ?never { // anon2 trait
            return;
        };
        return new NeverReturnTypeHint();
    }

}

interface NeverReturnTypeHintInterface {
    public function neverReturnType1(): never;
    public function neverReturnType2(): ?never; // neverReturnType2 interface
}
