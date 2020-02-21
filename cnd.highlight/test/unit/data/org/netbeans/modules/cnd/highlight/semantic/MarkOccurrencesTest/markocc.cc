/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
