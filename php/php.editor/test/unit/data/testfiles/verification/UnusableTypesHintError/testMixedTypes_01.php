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

class MixedType {

    private ?mixed $mixed; // can't use with "?"

    public function mixed(mixed $mixed): mixed|void { // can't use with "void"
        
    }

    public function mixed2(mixed|int $mixed): mixed {
        // can't use with union types
    }

    public function mixed3(mixed $mixed): null|mixed {
        // can't use with union types
        return null;
    }
}
