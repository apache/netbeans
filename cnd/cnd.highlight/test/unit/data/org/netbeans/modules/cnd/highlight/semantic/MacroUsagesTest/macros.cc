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

#ifndef NODEF
HI
#endif
H2
        
#define INT int        
#define HI cout << "hi";
#define H2 HI HI int x; HI

#ifndef W1
HI
#endif
H2
H23
        
#define FOUR 4
        
#if FOUR > 5
        HI    
#elif FOUR+FOUR > 7
        HI
#else
        H2
#endif
        
INT main(INT argc, char**argv) {
    H2
}

#define X
#define MACRO(x) "keyword.cc"
#include MACRO(X)
