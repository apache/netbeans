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

function testReturnArray(): array&Iterator {}
function testReturnBool(): bool&Iterator {}
function testReturnCallable(): callable&Iterator {}
function testReturnFalse(): false&Iterator {}
function testReturnFloat(): float&Iterator {}
function testReturnInt(): int&Iterator {}
function testReturnIterable(): iterable&Iterator {}
function testReturnMixed(): mixed&Iterator {}
function testReturnNever(): never&Iterator {}
function testReturnNull(): null&Iterator {}
function testReturnObject(): object&Iterator {}
function testReturnParent(): parent&Iterator {}
function testReturnSelf(): self&Iterator {}
function testReturnStatic(): static&Iterator {}
function testReturnString(): string&Iterator {}
function testReturnTrue(): true&Iterator {}
function testReturnVoid(): void&Iterator {}

function testParamArray(Iterator&array $param): Iterator {}
function testParamBool(Iterator&bool $param): Iterator {}
function testParamCallable(Iterator&callable $param): Iterator {}
function testParamFalse(Iterator&false $param): Iterator {}
function testParamFloat(Iterator&float $param): float {}
function testParamInt(Iterator&int $param): int {}
function testParamIterable(Iterator&iterable $param): iterable {}
function testParamMixed(Iterator&mixed $param): mixed {}
function testParamNever(Iterator&never $param): never {}
function testParamNull(Iterator&null $param): void {}
function testParamObject(Iterator&object $param): object {}
function testParamParent(Iterator&parent $param): parent {}
function testParamSelf(Iterator&self $param): self {}
//function testParamStatic(Iterator&static $param): void {} // syntax error
function testParamString(Iterator&string $param): string {}
function testParamString(Iterator&true $param): true {}
function testParamVoid(Iterator&void $param): void {}
//function testReturnNullable(): ?Test&Iterator {} // syntax erro

function testReturnDuplicate(): Iterator&Iterator {}
function testParamDuplicate(Iterator&Iterator $param): void {}

class InvalidField {
    public array&Iterator $array;
    public bool&Iterator $bool;
    public callable&Iterator $callable;
    public false&Iterator $false;
    public float&Iterator $flaot;
    public int&Iterator $int;
    public iterable&Iterator $iterable;
    public mixed&Iterator $mixed;
    public never&Iterator $never;
    public null&Iterator $null;
    public object&Iterator $object;
    public parent&Iterator $parent;
    public self&Iterator $self;
//    public static&Iterator $static; // syntax error
    public string&Iterator $string;
    public true&Iterator $true;
    public void&Iterator $void;
    public Iterator&Iterator $duplicate;
}
