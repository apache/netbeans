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

function typesTest() {
  let a1 = [];
  let a2 = [1, 2, [3, 4]];
  let b1 = true;
  let b2 = false;
  let c1 = new TestClass();
  let i1 = 0;
  let i2 = 42;
  let i3 = 42.42;
  let i4 = -0.0;
  let i5 = 1/i4;
  let i6 = 1/0.0;
  let i7 = -1/0.0;
  let i8 = 0.0/0.0;
  let aSparse = [1, 2];
  aSparse[10] = 10;
  let s1 = "String";
  let f1 = function pow2(x) {
    return x*x;
  };
  let d1 = new Date(1000000000);
  let undef;
  let nul = null;
  let sy = Symbol('symbolic');
  let o1 = {};
  let o2 = new TestFncProp();
  o2.fncProp = "Property";
  o2.a = "A";
  let map = new Map();
  map.set("key1", 42);
  map.set("key2", "v24");
  debugger;
  f1(5);
}

function TestFncProp() {
}

class TestClass {
}

typesTest();
