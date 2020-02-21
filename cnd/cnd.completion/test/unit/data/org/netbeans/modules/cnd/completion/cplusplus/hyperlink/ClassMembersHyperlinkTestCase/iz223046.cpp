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

namespace std {
    template <class T> class vector {
    };
}

struct strA {
    int par;
};

struct strB {
    char par;
};

class testing {
public:
    virtual void function(const std::vector<strB> &arg1, int arg2, int arg3, const bool arg4) {};
    virtual void function(const std::vector<strA> &arg1, int arg2, int arg3, const bool arg4) {};
    void foo(int);
};


void testing::foo(int argc) {

    std::vector<strA> vec_a;
    std::vector<strB> vec_b;
    testing the_testing;

    if(argc == 2) {
        function(vec_a, 0, 1, false);
        this->function(vec_a, 0, 1, false);
        the_testing.function(vec_a, 0, 1, false);
    } else {
        function(vec_b, 1, 2, true);
        this->function(vec_b, 1, 2, true);
        the_testing.function(vec_b, 1, 2, true);
    }
}
