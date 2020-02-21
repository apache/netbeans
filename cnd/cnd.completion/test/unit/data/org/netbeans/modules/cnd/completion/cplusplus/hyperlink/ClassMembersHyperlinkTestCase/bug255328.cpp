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

struct MyStruct255328 {
    int field;
};

typedef struct { 
   int  value;
} S255328;

typedef struct {
   void *c;
   S255328 s[1]; 
} T255328;

T255328 s255328[]={
   [0] = {(void*)(0 ? 1 : 2, 3), {{.value = 0}}},
   [1] = {(void*)1, {{value : 0}}},
   {&(MyStruct255328) {.field = 0}, {{.value = 0}}}
};