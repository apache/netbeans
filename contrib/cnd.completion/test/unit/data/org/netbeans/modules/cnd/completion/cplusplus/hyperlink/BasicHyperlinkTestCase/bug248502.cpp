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

namespace bug248502 {
    struct W248502 {
        void func();
    };

    W248502* f_248502(int var);
    int f1_248502(int var);

    template <int Val>
    struct Holder248502 {
        static const int value = Val;
    };

    template <int Val>
    struct Data248502 {
        int operator-(int other);
        int operator+(int other);
        int operator*(int other);
        int operator&(int other);
    };

    int main248502() {
        int i = 1;  
        f_248502(i + 6)->func(); //ok 
        f_248502(i * 6)->func(); //ok 
        f_248502(i * (6))->func(); //ok 
        f_248502(i * -6)->func(); //ok 
        f_248502(i - 6)->func(); //ok 
        f_248502(i & 6)->func(); //ok 
        f_248502(i & (6))->func(); //ok 
        f_248502(i & -6)->func(); //ok         
        f_248502(Holder248502<6>::value + 6)->func(); //ok 
        f_248502(Holder248502<6>::value * 6)->func(); //ok 
        f_248502(Holder248502<6>::value * (6))->func(); //ok 
        f_248502(Holder248502<6>::value * -6)->func(); //ok        
        f_248502(Holder248502<6>::value - 6)->func(); //ok 
        f_248502(Holder248502<6>::value & 6)->func(); //ok 
        f_248502(Holder248502<6>::value & (6))->func(); //ok 
        f_248502(Holder248502<6>::value & -6)->func(); //ok             
        f_248502(f1_248502(3) + 6)->func(); //ok 
        f_248502(f1_248502(3) * 6)->func(); //ok 
        f_248502(f1_248502(3) * (6))->func(); //ok  
        f_248502(f1_248502(3) * -6)->func(); //ok 
        f_248502(f1_248502(3) - 6)->func(); //ok 
        f_248502(f1_248502(3) & 6)->func(); //ok 
        f_248502(f1_248502(3) & (6))->func(); //ok  
        f_248502(f1_248502(3) & -6)->func(); //ok     
        f_248502(Data248502<5>() + 6)->func(); //ok 
        f_248502(Data248502<5>() * 6)->func(); //ok 
        f_248502(Data248502<5>() * (6))->func(); //ok  
        f_248502(Data248502<5>() * -6)->func(); //ok         
        f_248502(Data248502<5>() - 6)->func(); //ok 
        f_248502(Data248502<5>() & 6)->func(); //ok 
        f_248502(Data248502<5>() & (6))->func(); //ok  
        f_248502(Data248502<5>() & -6)->func(); //ok             
    }
}