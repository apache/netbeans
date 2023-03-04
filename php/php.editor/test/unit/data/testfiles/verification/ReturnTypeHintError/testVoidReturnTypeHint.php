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

function invalidVoidReturnType1(): ?void { // invalidVoidReturnType1 func
}

function invalidVoidReturnType2(): ?void { // invalidVoidReturnType2 func
    return;
}

function voidReturnType1(): void {
    return;
}

function voidReturnType2(): void {
}

function voidReturnType3(): void {
    return true; // voidReturnType3 func
}

function voidReturnType4(): void {
    $foo = "foo";
    return $foo; // voidReturnType4 func
}

function voidReturnType5(): void {
    return new VoidReturnTypeHint(); // voidReturnType5 func
}

function returnType1(): VoidReturnTypeHint {
    return new VoidReturnTypeHint();
}

class VoidReturnTypeHint {

    public function voidReturnType1(): void {
        if (true) {
            return;
        }
    }

    public function voidReturnType2(): void {
        return;
    }

    public function voidReturnType3(): void {
        return new VoidReturnTypeHint(); // voidReturnType3 class
    }

    public function voidReturnType4(): void {
        foreach ($array as $value) {
            return new VoidReturnTypeHint(); // voidReturnType4 class
        }
    }

    public function anon1(): VoidReturnTypeHint {
        $anon = function(): void {
            return;
        };
        return new VoidReturnTypeHint();
    }

    public function anon2(): VoidReturnTypeHint {
        $anon = function(): void {
        };
        return new VoidReturnTypeHint();
    }

    public function anon3(): VoidReturnTypeHint {
        $anon = function(): void {
            return "anon"; // anon3 class
        };
        return new VoidReturnTypeHint();
    }

    public function anon4(): void {
        $anon = function(): void {
            return "anon"; // anon4 class
        };
        return new VoidReturnTypeHint(); // anon4 class
    }

}

trait VoidReturnTypeHintTrait {

    public function voidReturnType1(): void {
    }

    public function voidReturnType2(): void {
        return;
    }

    public function voidReturnType3(): void {
        switch ($variable) {
            case 1:
                break;
            case 2:
                return;
            default:
                break;
        }
    }

    public function voidReturnType4(): void {
        return new VoidReturnTypeHint(); // voidReturnType4 trait
    }

    public function voidReturnType5(): void {
        while (true) {
            return "test"; // voidReturnType5 trait
        }
        return 1; // voidReturnType5 trait
    }

    public function anon1(): VoidReturnTypeHint {
        $anon = function(): ?void { // anon1 trait
        };
        return new VoidReturnTypeHint();
    }

    public function anon2(): VoidReturnTypeHint {
        $anon = function(): ?void { // anon2 trait
            return;
        };
        return new VoidReturnTypeHint();
    }

}

interface VoidReturnTypeHintInterface {
    public function voidReturnType1(): void;
    public function voidReturnType2(): ?void; // voidReturnType2 interface
}
