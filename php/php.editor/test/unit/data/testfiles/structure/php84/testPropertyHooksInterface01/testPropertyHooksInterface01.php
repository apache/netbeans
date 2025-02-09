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

interface Interface00 {
    public int $i0_01 {set;}
    public int $i0_02 {get;}
    public int $i0_03 {get;set;}
}

interface Interface01 {
    public int $i1_01 {set;}
    public int $i1_02 {get;}
    public int $i1_03 {get;set;}
}

interface Interface02 extends Interface01 {
    public string $i2_01 {get;set;}
    public string $i2_02 {get;}
    public string $i2_03 {set;}
    public int $i1_01 {set;}
}

interface Interface03 {
    public int $i3_01 {get;set;}
    public int $i3_02 {get;set;}
    public int $i3_03 {get;set;}
}

interface Interface04 extends Interface03{
    public bool $i4_01 {set;}
    public bool $i4_02 {get;}
    public bool $i4_03 {get;set;}
}

interface Interface05 extends Interface04, Interface02 {
    public int|string $i5_01 {get;set;}
    public int|string $i5_02 {get;set;}
    public int|string $i5_03 {get;set;}
    public bool $i4_02 {get;}
}

interface InterfaceEx extends Interface01, Interface02, Interface05 {
    public int $iex_01 {get;set;}
    public int $iex_02 {get;set;}
    public int $i1_01 {get;set;}
    public string $i2_01 {get;set;}
    public int $i3_03 {get;}
}
