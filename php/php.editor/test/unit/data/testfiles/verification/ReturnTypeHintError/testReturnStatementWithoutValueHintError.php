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

function ret1(): void
{
    if (false) {
        return; 
    }
    return 10;
}

function ret2(): never
{
    if (false) {
        return; 
    }
    return 10;
}

function ret3(): int
{
    if (false) {
        return;
    }
    return 10;
}

function ret4()
{
    if (false) {
        return; 
    }
    return 10;
}

function ret5(): int|string
{
    if (false) {
        return; 
    }
    return 'string';
}

function ret6(): ClassA&ClassB
{
    if (false) {
        return;
    }
    return new ClassA();
}

function ret7(): ?int
{
    if (false) {
        return;
    }
    return 10;
}

function ret8(): int|null
{
    if (false) {
        return;
    }
    return null;
}

function ret9(): false
{
    if (false) {
        return;
    }
    return false;
}

$anon1 = function(): string {
    return;
};

$anon2 = function(): string {
    return 'string';
};

$anon3 = function() {
    return;
};

class ClassA {}
class ClassB extends ClassA {}


class TestClass 
{
    public function ret1(): string 
    {
        return;
    }
    
    public function ret2(): string
    {
        return 'string';
    }
    
    public function ret3()
    {
        return;
    }
}
