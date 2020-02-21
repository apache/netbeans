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

static void static_foo1(){
}

typedef void (*pf_Static) ();

struct C_Static {
    static pf_Static f;
};

pf_Static C_Static::f = static_foo1; // unresolved

struct S_Static {
    void (*f)();
};

static void static_foo2(){
}

struct CC_Static {
    static S_Static s;
};

S_Static CC_Static::s =
{
    static_foo2 // unresolved
};

int main() {
    CC_Static::s.f();
    C_Static::f();
    return 0;
}