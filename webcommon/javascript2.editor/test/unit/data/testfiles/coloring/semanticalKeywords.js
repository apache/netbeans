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
export {a as x1};
export {a as x2} from 'uuu';
export * from 'ooo';

import i1 from 'ddd';
import * as star from 'fff';

import {i2 as i3} from 'sss';

let test = 7;

class Test {
    static test1() {
        
    }
    
    as() {
        //ok
    }
    
    from() {
        //ok
    }
    
    test1() {
        let a = 7;
        let from = 0;
        let as = 0;
    }
}

const test2 = 7;
