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

struct Foo257821 {
    int foo1;
};

struct Bar257821 {
    int bar1;
    int bar2;
};

struct Bar257821 baz2_257821 =
{
    .bar1 = sizeof (struct Foo257821),
    .bar2 = 0,  // .bar2 is NOT resolved
};

void *baz3_257821 = (struct Foo257821[])
{
    { 
        .foo1 = 0,  // .foo1 is NOT resolved
    }, 
}; 
