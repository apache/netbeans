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

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
class Test1 {

    public int $a;
    private folat $b;
    protected string $c;
    protected ?string $d;

}

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
class Test2 {

    public static int $a;
    private static float $b;
    protected static ?string $c;
    protected static string $d;

}

/**
 * @param $a
 * @property $b Description
 * @property-read $c
 * @property-write $d Description
 */
class Test3 {

    public string $a;
    private array $b;
    protected int $c;
    protected bool $d;

}

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
trait Trait1 {

    public int $a;
    private float $b;
    protected string $c;
    protected ?string $d;

}

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
trait Trait2 {

    public static int $a;
    private static float $b;
    protected static string $c;
    protected static string $d;

}

/**
 * @param $a
 * @property $b Description
 * @property-read $c
 * @property-write $d Description
 */
trait Trait3 {

    public Test3 $a;
    private ?Test3 $b;
    protected int $c;
    protected array $d;

}
