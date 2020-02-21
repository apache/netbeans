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