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

struct bug199079_Z {
    int i;
};

template <class T> struct bug199079_B {
};

template <> struct bug199079_B<int> {
    typedef bug199079_Z btype;
};

template <class T> struct bug199079_A {    
    typedef bug199079_B<T> bt;
    typedef typename bt::btype atype;
};

template <class T> struct bug199079_AA {    
    typedef bug199079_A<T> at;
    typedef typename at::atype aatype;    
};

int main(int argc, char** argv) {
    bug199079_AA<int>::aatype a;
    a.i++; // unresolved i

    return 0;
}