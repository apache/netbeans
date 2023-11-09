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
namespace InitializeField;

use \Test\Foo;
use \Test\Bar;
use \Test\Baz;

class DnfTypes
{
    function __construct(
            (Foo&Bar)|Baz $param1,
            (\Test\Foo&Bar)|(Foo&Baz) $param2,
            Foo|(Foo&Baz) $param3,
            Foo|(Foo&Baz)|null $param4,
    ) {
    }
}

new IntersectionTypes(new Foo(), new Bar(), new Baz(), null);

namespace Test;

class Foo {}
class Bar {}
class Baz {}
