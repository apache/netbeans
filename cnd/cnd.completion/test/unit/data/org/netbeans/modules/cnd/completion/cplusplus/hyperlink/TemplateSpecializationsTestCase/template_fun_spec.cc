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

//#include <stdio.h>

template<typename K, typename V = K> class pair {
    K first;
    V second;
};

// main template declaration
template<class T> const char* func(T p);

// partial specialization declaration #1
template<class T> const char* func(T* p);

// partial specialization declaration #2
template<class T> const char* func(pair<T,T> p);

// full specialization declaration #1
template<> const char* func(char p);

// full specialization declaration #2
template<> const char* func(pair<char, char>);

int main(int argc, char** argv) {
    printf("%s\n", func(argc)); // prints "base"
    printf("%s\n", func(argv)); // prints "pointer"
    printf("%s\n", func('c'));  // prints "char"
    pair<char, char> pc;
    printf("%s\n", func(pc));   // prints "pair<char, char>"
    pair<int, int> pi;
    printf("%s\n", func(pi));   // prints "pair"
    return 0;
}

// main template definition
template<class T> const char* func(T p) {
    return "base";
};

// partial specialization definition #1
template<class T> const char* func(T* p) {
    return "pointer";
};

// partial specialization definition #2
template<class T> const char* func(pair<T,T> p) {
    return "pair";
};

// full specialization definition #1
template<> const char* func(char p) {
    return "char";
};
 
// full specialization definition #3
template<> const char* func(pair<char,char> p) {
    return "pair<char, char>";
};
