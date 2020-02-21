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

class hello231328
{
public:
    int x;
    int y;
    int z;
    hello231328* (*func)(void);
    hello231328(int px, int py, int pz, void (*f)()) {
        x = px;
        y = py;
        z = pz;
        func = f;
    }
};

hello231328* foo231328()
{
    printf("hello world!");
}

template<typename _Tp> class vector231328 {
public:
    typedef _Tp   value_type;   
    typedef _Tp&  reference;
    void push_back(const value_type& __x) {
    }
    
    reference operator[](int __n) {
        return 0;
    }    
};

int main231328() 
{
    vector231328<hello231328> h;
    h.push_back(hello231328(0,0,0,foo231328));
    h[0].func()->x;
    return 0;
}
