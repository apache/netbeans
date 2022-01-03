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

#include "newfile1.cpp"

#define MOO 3

class Foo {
    int boo;
public:
    Foo();
    Foo(int _boo);

    void doFoo(int moo);
};

Foo::Foo(): boo(0) {
}

Foo::Foo(int _boo) {
    boo = _boo;
}

void Foo::doFoo(int moo) {
    int goo = MOO;
    boo = moo + goo;
    int bar = 1;
    ::bar = ::bar + bar + 1;
    func(::bar);
}

#ifdef MOO

#  elif BOO

#if BOO != 0

#endif

# else

#ifndef INTERNAL

# endif

#endif

namespace N1
{
    int fooN1(int par0 /* = 0 */); // no highlighting
    int fooN1(int par0 /* = 0 */);

    int fooN1(int par0 /* = 0 */) {

    }


    class AAA {
        void const_fun(int i) ;
        void const_fun(int i) const ;
    };


    void AAA::const_fun(int i) {

    }

    void AAA::const_fun(int i) const {

    }
}

struct A {
    int a;
    A(int i) {
        a = i;
    }
};

int main() {
    A a(1);
    a.a++;
}

void stringsTest() {
    char* ss = "string literal";    

    'char literal';
}

#define STR "string literal"

#define CMD 'char literal'

void charTest() {
    char* ss = (char*) 'char literal';    

    "string literal";
}

struct NameId {

};

// Name Table
class NameTable
{
public:
    NameId AddSymbol();
    NameId AddSymbol(const std::string &s);
    NameId AddSymbol(const std::string &s, const std::string &busHead, 
                    int index1, int index2, const std::string &busTail);
    NameId AddSymbol(const std::string &s, const std::vector<std::string>
&bits);
    void AddTable (const NameTable &other);
}; // class NameTabl

NameId NameTable::AddSymbol()
{

}

NameId NameTable::AddSymbol(const std::string &s)
{

}

NameId NameTable::AddSymbol(const std::string &s, const std::string &busHead,
                            int index1, int index2, const std::string &busTail)
{

}

NameId NameTable::AddSymbol(const std::string &s, const std::vector<std::string> &bits)
{

}

void NameTable::AddTable (const NameTable &other) {
    NameId oneParam = this->AddSymbol(std::string("Default"));
    NameId empty = this->AddSymbol();
    NameId twoParams = this->AddSymbol(std::string("Default"), std::vector<std::string>());
    NameId moreParams = this->AddSymbol(std::string("Default"), std::string("second"), 1, 3, std::string("tree"));

}


void checkDifferentScopes() {
    {
        int xx;
        for (int xx = 1; xx > 0; xx--) {
            xx = -xx;
        }
        xx = 10;
    } 
    while (true) {
        int xx;
        xx = 10;
        break;
    }

}

typedef int int8_t, int32_t;

typedef struct {

    int8_t  Type;
    int32_t Width;
    int32_t Height;
} Object1231272;

typedef struct {

    int8_t  Type;
    int32_t Width;
    int32_t Height;
} Object2231272;

int main231272(int argc, char**argv) {
    // Prints welcome message...
    Object1231272 o1;
    Object2231272 o2;

    o2.Type = o1.Type;
    o2.Width = o1.Width;
    o2.Height = o1.Height;

    return o2.Width;
}
