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

#include <stdio.h>

#define ONE 1L

namespace my_dummy_namespace {
    class A { 
    public:
        A(int var = 1): var_(var) {};
        ~A() {};

        inline void greetings() const {
            printf("%s", "hi");
        };

    private:
        int var_;
    };
}

class A { 
public:
    A(int var = 1): var_(var) {};
    ~A() {};
    
    inline void greetings() const {
        printf("%s", "hi");
    };
    
private:
    int var_;
};

int main(int argc, char** argv) {
    short s = -1;
    short int si = -2;
    unsigned short us = 1;
    short* ps = &s;
    signed char sch = -1;
    char ch = 0;
    unsigned char uch = 1;
    char* pch = &ch;
    long l = 1;
    long* pl = &l;
    unsigned long ul = 2;
    long long ll = -3;
    long int li = -3;
    unsigned long long ull = 4;
    float f = 12.8f;
    double d = 12.345;
    long double ld = 12.245245;
    int i = -1;
    unsigned int ui = 1;
    A a;
    void* void_ptr = &ll;
    const char* pch_ = "some string";
    
    // OK
    printf("No args");
    
    printf("%hd", si);
    printf("%hi", s);
    printf("%ho", us);
    printf("%hu", us);
    printf("%hX", us);
    printf("%'+0hd", s);
    printf("% #0ho", us);
    printf("%'-0hu", us);
    
    printf("%hhd", sch);
    printf("%hhi", sch);
    printf("%hho", uch);
    printf("%hhu", uch);
    printf("%hhx", uch);
    
    printf("%ld", li);
    printf("%ld", (long)i);
    printf("%ld", (long)   i);
    printf("%ld", (long)   (
            i  )    );
    printf("%li", l);
    printf("%lo", ul);
    printf("%lu", ul);
    printf("%lX", ul);
    
    printf("%lld", ll);
    printf("%lli", ll);
    printf("%llo", ull);
    printf("%llu", ull);
    printf("%llx", ull);
    
    printf("%d", i);
    printf("%i", i);
    printf("%o", ui);
    printf("%u", ui);
    printf("%X", ui);
    
    printf("%f", d);
    printf("%llf", d);
    printf("%e", ld);
    printf("%g", f);
    printf("%a", d);
    printf("%F", ld);
    printf("%E", f);
    printf("%G", d);
    printf("%A", ld);
    printf("%'-#0f", d);
    printf("%'-#0g", d);
    printf("%'-#0A", d);
    printf("%-#0e", d);
    
    printf("%c", i);
    printf("%c", ch);
    printf("%c", 'a');
    printf("%s", "test");
    printf("%s", pch_);
    printf("%+s", pch_);
    printf("%-c", 'a');
    printf("%p", void_ptr);
    printf("% p", void_ptr);
    printf("% p", &a);
    printf("%n", &i);
    
    printf("%ld", 12L + ONE);
    
    return 0;
}