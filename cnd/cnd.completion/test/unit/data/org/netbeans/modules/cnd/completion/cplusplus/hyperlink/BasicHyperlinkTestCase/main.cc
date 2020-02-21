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

int boo(int aa, double bb) { 
    int kk = aa + bb;
    double res = 1;
    for (int ii = kk; ii > 0; ii--) {
        res *= ii;
    }
    return res;
}

void method_name_with_underscore() {
    method_name_with_underscore();
}

const int VALUE = 10;
const int VALUE_2 = 10 + VALUE;

void fun(char* aaa, char**bbb) {
    int iiii = fun(null, null);
}

void sameNameDiffScope(int name) {
    if (name++) {
        string name;
        name = "name";
    } else if (name++) {
        char* name;
        strlen(name);     
    }
    name--;
    
    char* globalvar = 0;
    fun(::globalvar, &globalvar);
    ::globalvar = ++::globalvar + globalvar;
    int (*funPtr)();
}

char* globalvar;
