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

// New Types:
var obj = {};
var set = new Set();
set.add("one").add("two");
var weakSet = new WeakSet();
//weakSet.add("one").add("two");
var map = new Map();
map.set("oneKey", "oneValue");
map.set("twoKey", "twoValue");
var weakMap = new WeakMap();
//weakMap.set("oneKey", "oneValue");
//weakMap.set("twoKey", "twoValue");
var symbol = Symbol("symbolKey");
var promise = new Promise(function(resolve, reject) { resolve(true); });
var iter = ['a', 'b', 'c'].entries();   //console.log('set = '+set);
var gen = function*() {
    var pre = 0, cur = 1;
    for (;;) {
      var temp = pre;
      pre = cur;
      cur += temp;
      yield cur;
    }
  };
  
var it1 = iter.next();
var it2 = iter.next();
var it3 = iter.next();  //console.log('it1 = '+it1.value+", it2 = "+it2.value+', it3 = '+it3.value);
var it4 = iter.next(); //breakpoint

// Template Strings

const n = 10;
var ts = `The n = ${n}.`;
var ts2 = `Multi
line
String`;
ts2.length;
