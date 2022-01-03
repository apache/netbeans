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

#include "file.h"

int globInt;

void A::f() {
    
    char* str = "string";
           int i = ::globInt;
    return;
}

void A::f2() {
    char c = ' ';
    return;
}

void globFoo() {
    
    int jOuter = 2;
    switch(j) {
        case 1: {
            char jInComopound=3;
            
        }
        case 2:
            char jNonCompound = 0;
            
            break;
        default:
            char jDeafult=4;
            
    }
}

int main(int argc, char** argv) {
    int value, *pointer;
    value = 0; pointer = &value;
     // <- test text is inserted here
    printf("%d\n", *pointer);
    void* pExtra;
    A a;
     // code completion tests insert some code here
    for (int i = 0; i < 10; i++) {
        int yyy = argc>0 ? static_cast<int>(pExtra) : *pointer;
    }
    return (0);
}
