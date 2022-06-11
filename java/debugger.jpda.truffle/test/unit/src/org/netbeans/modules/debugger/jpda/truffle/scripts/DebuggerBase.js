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

statement = "First Statement";

function fnc1() {
  let a = 20;
  let o = {};
  o.ao = "AO";
  let arr = [];
  arr = [5, 4, 3, 2, 1]

  return 30;
}

function fnc2(n) {

  let n1 = n + 1;
  let f2 = 0;
  if (n1 <= 10) {
    f2 = fnc2(n1) + 1;
  }
  return f2;
}

ga = 6;
fnc1();

for (let i of [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]) {

  fnc2(i);

}
