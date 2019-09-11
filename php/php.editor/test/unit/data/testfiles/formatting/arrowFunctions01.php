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

const CONSTANT_INT = 1000;
$y = 5;

     $fn0 =     fn()    =>     100;

$fn1a = fn(    int     $x) =>    $x   +    $y;

$fn1b = function($x) use ($y) {
    return $x + $y;
};

$fn2 = fn ( int  $a,         ArrowFunctions $b) => $a +    $b->getNumber() * $y;

      fn (array $x) => $x; // parameter type

fn( int $x):     int     => $x; // return type

    fn(   ?array   $z  , int    $x):    ?int =>   $x +   count($z); // parameter and return type

        fn(   $x    =     100    )    =>    $x; // default value

   fn(   &$x   ) =>    $x; // reference

   fn&(   $x) =>    $x; // reference

fn&(   &$x   )    =>    $x; // reference

fn(   $x,    ...$reset    ) =>    $reset; // variadics

fn(   $x,    &...$reset   ) =>    $reset; // reference variadics

fn()     :    int => CONSTANT_INT;

fn()   :     ArrowFunctions     =>    ArrowFunctions::new();

function (): ArrowFunctions {
    return ArrowFunctions::new();
};

// nest
    $af =     fn()    =>     fn() =>     $y;
    $af =     fn()    =>     
                 fn() =>     $y;
    (fn()    =>    fn()   =>   $y)()();
             (fn() =>    function() use ($y)    {return $y;})()();

               $af = fn(   int $x):     callable => fn (    int     $z): int     =>  $x +  $y  *  $z;
   $af = fn(    ArrowFunctions    $x): callable => fn(     ?ArrowFunctions     $z    ):    ?ArrowFunctions =>    new ArrowFunctions();
fn() => function($x) use ($y) {
    return $x + $y;
            };

$fn3 = function ($x) use ($y) {
                    return      fn()    =>     $x + $y;
};
$fn4 = function ($x) use ($y) {
    return     fn($x) =>    $x +     $y;
};

class ArrowFunctions
{

    public function test() {
        $y = 100;
        $af =    fn():    ?ArrowFunctions => $this;
             $af =    fn() => $$variable;
                 $af = fn() => self::class;
              $af = fn(   $x    ):     ?int =>   $x + $y;
        $af =     static        fn() =>     isset($this); // static
    }

    public function getNumber(): int {
        return 100;
    }

    public static function new() {
        return new ArrowFunctions();
    }
}
