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

namespace Test1 {

    use Test2\Foo;
    use Test2\Bar;
    use Test2\Baz;

    interface ImplementMethodTest {

        public function testMethod((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3, Foo|(Bar&Baz)|null $param4): Baz|(Foo&Bar);
    }

}

namespace Test2 {

    use Test1\ImplementMethodTest;

    class Implement implements ImplementMethodTest {
    }

    class Foo {}

    class Bar {}

    class Baz {}

    $instance = new Implement();
    $instance->testMethod(null);
}
