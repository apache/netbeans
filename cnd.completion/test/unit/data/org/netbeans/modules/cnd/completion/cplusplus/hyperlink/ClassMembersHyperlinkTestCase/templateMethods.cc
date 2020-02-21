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

template <typename T> class C2 {
    template<class TT, int j> int A();
    int B();
};

class D2 {
    template<class TT> int A();
    int B();
};

template<class P>
template<typename PP, int k>
int
C2<P>::A() {
    PP p;
    return 0;
}

template<class P>
int
C2<P>::B() {
    return 0;
}

int
D2::B() {
    return 0;
}

template<class PP>
int
D2::A() {
    return 0;
}

int main(){
    return 0; 
}