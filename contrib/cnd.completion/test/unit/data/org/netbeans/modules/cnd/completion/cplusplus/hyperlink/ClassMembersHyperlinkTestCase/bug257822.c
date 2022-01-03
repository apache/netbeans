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

struct Foo257822
{
    int foo1; 
};

void *baz1_257822 = &(struct Foo257822)
{
    .foo1 = 0,  // .foo1 is NOT resolved
}; 
   
struct Bar257822
{ 
    int foo1;
};

struct Bar257822 *baz3_257822 = (void *)&(struct Foo257822)
{
    .foo1 = 0,  // .foo1 is resolved
};

void *baz4_257822 = (void *)&(struct Foo257822)
{
    .foo1 = 0,  // .foo1 is NOT resolved
}; 

void *baz5_257822 = (&(struct Foo257822){.foo1 = 0}); 