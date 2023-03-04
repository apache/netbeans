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

class UnusableTypes {

    private int|callable $callable;
    private false $false;
    private null $null;
    private true $true;
    private bool|false $boolFalse;
    private true|bool $trueBool;
    private bool|bool $duplicatedBool;
    private true|false $bothTrueAndFalse;
    private int|false|true $bothTrueAndFalse2;
    private int|INT $duplicatedInt;
    private iterable|array $iterable1;
    private iterable|Traversable $iterable2;
    private iterable|array|Traversable $iterable3;
    private null|false $nullFalse; // PHP 8.2: OK

    public function returnFalse(): true {
    }

    public function returnFalse(): false {
    }

    public function returnNull(): null {
    }

    public function returnDuplicatedType(): UnionType2|UnionType2 {
    }

    public function parameterTrue(true $true) {
    }

    public function parameterFalse(false $false) {
    }

    public function parameterNull(null $null) {
    }

    public function parameterDuplicatedType(UnionType2|UnionType2 $duplicatedType) {
    }

    public function voidInUnionType(): void|int {
    }
}

class UnionType1
{
    public function method1(): void {
        
    }
}

class UnionType2
{
    public function method2(): void {
        
    }
}

class TraversableImpl implements Traversable {
    
}
