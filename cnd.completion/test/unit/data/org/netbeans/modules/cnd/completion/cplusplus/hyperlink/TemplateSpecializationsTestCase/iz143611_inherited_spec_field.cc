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

template<typename K, typename V = K> class pair {
    K first;
    V second;
};

template<typename T> struct base {
    T param_t;
};

template <> struct base<int> {
    int param_int;
};

template <> struct base<pair<char, int> > {
    int param_char_int;
};

int foo() {
    // use main template
    base<char> bc;
    if (bc.param_t)
        return 0;

    // use specialization for int
    base<int> bi;
    if (bi.param_int)
        return 0;

    // use specialization for pair <char, int>
    base<pair<char, int> > bci;
    if (bci.param_char_int)
        return 0;

    return 1;
}
