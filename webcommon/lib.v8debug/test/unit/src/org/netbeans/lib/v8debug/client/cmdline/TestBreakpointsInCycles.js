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

var n = 10;

var i, d;

d = 1;

for (i = 1; i <= n; i++) {
    d *= i;     // breakpoint when "d > 700";
                // Expecting d = 720, i = 7, hit count = 1
}

n = 15;

for (i = 1; i <= n; i++) {
    d /= i;     // breakpoint when "d < 1", with ignore count = 2
                // Expecting d < 1, i = 11+2+1, hit count = 3
    //print ("i = "+i+", d = "+d);
}

d = 1;
n = 100;

for (i = 1; i <= n; i++) {
    d += i/n;
}
