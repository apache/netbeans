/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

namespace bug235120 {
    struct AAA_235120 {
        int foo();
    };

    struct BBB_235120 {
        int boo();
    };

    AAA_235120 operator+(AAA_235120 a, BBB_235120 b);

    BBB_235120 operator+(BBB_235120 a, AAA_235120 b);

    template <typename T1, typename T2>
    struct DDD_235120 {
        T1 a;
        T2 b;

        decltype(a + b) doo();
        decltype(b + a) goo();

        auto hoo() -> decltype(a + b);
        auto joo() -> decltype(b + a);
    };

    template <typename T1, typename T2>
    auto roo_235120(T1 a, T2 b) -> decltype(a + b);

    int zoo_235120() {
        DDD_235120<AAA_235120, BBB_235120> var;
        var.doo().foo();
        var.goo().boo();
        var.hoo().foo();
        var.joo().boo();

        AAA_235120 a;
        BBB_235120 b;
        roo_235120<AAA_235120, BBB_235120>(a, b).foo();
    }

    // ================= Unique ptr test case =================
    struct Foo_Content235120 {
        int abc;
    };
    
    struct Foo_235120
    {
        typedef Foo_Content235120* Pointer;
    };

    struct Bar_235120
    {
        int abc;
    };

    template <typename T>
    class SFINAE_235120 //Substitution Failure Is Not An Error.
    {
        template <typename U>
        static typename U::Pointer test(typename U::Pointer);

        template <typename U>
        static T* test(...);

    public:
        typedef decltype(test<T>(nullptr)) Pointer;
    };

    int main_235120(int argc, char ** argv) 
    {
        SFINAE_235120<Foo_235120>::Pointer foo = new Foo_Content235120();
        foo->abc = 11; //Unable to resolve identifier abc.
        SFINAE_235120<Bar_235120>::Pointer bar = new Bar_235120();
        bar->abc = 11; //Unable to resolve identifier abc.
        return 0;
    } 
    
    // === Typedef in decltype ===

    struct SimpleType_235120 {
        int foo();
    };

    template <typename T>
    struct TypesHolder_235120 {
        typedef T type;
        typedef decltype(*((type*)0)) declared_type;
    }; 

    int testTypesHolder_235120() {
        SimpleType_235120 var;
        TypesHolder_235120<SimpleType_235120>::declared_type &ref = var;
        ref.foo();
    }

    // === Iterators like in range based for statement ===

    struct Item_235120 {
        int foo();
    };

    template <typename T>
    struct Iterator_235120 {
        T& operator * ();
        T* operator -> ();

        T& get();
        void next(); //++
    };

    template <typename T>
    struct Collection_235120 {
        typedef Iterator_235120<T> iterator;
        iterator begin();
    };

    template <typename T>
    auto begin_235120(T param) -> decltype(param.begin());

    void testIterators_235120() {
        Collection_235120<Item_235120> a;
        begin_235120<Collection_235120<Item_235120>>(a)->foo();
        begin_235120(a)->foo();
    }
}