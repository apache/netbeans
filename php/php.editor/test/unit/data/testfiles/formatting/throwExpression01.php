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

   throw  new  Exception();

if (true) {
       throw    new  Exception();
}

try {
    $invalid = true;
    if ($invalid) {
throw new Exception();
    }
} catch (Exception $ex) {
    var_dump($ex->getMessage());
}

$condition = true;
   $condition    &&   throw new Exception();
false    ||    throw new Exception();
true and throw new Exception();
$condition     or    throw new Exception();
$assignement = true    && throw    new   Exception();

null ??    throw    new Exception();
$assignement = null      ??    throw  new Exception();

"string" ?:    throw new Exception();
$assignement = "string" ?:throw new Exception();

$condition ?     throw new    Exception() :   throw    new Exception();
$assignement = $condition ?    throw new Exception() : throw new Exception();

$callable = fn() =>    throw    new    Exception();
$array = [   throw new   Exception()];

     throw    $exception   =   new Exception();
throw    ($exception    =    new Exception());

   throw null ?? new Exception();
throw     (null ?? new Exception());

throw     $instance->createException();
throw   (   $instance->createException());

throw   static::createException();
    throw      (static::createException());
   throw     Ex::createException();
                    throw       (           Ex::createException()   );

throw     $condition1    && $condition2 ? new Exception1() : new Exception2();
throw    ($condition1 && $condition2 ? new Exception1() : new Exception2());

$condition1 || throw     new Exception() && $condition2 || throw new Exception();
$condition1 || (   throw new Exception() && $condition2 || (throw        new Exception())  );
