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

namespace bug235120_2 {
    struct Foo_235120_2 {
        int aaa;
    };
    
    struct FooPtr_235120_2 {
        int bbb;
    };
    
    struct Boo_235120_2 {
        int ccc;
    };
    
    template<typename _Tp>
    struct remove_reference_235120_2
    { typedef _Tp   type; };

    template<typename _Tp>
    struct remove_reference_235120_2<_Tp&>
    { typedef _Tp   type; };    
    
    template <typename T>
    struct delete_default_235120_2 {};
    
    template <>
    struct delete_default_235120_2<Foo_235120_2> {
        typedef FooPtr_235120_2* pointer;
    };
    
    template <typename _Tp, typename _Dp = delete_default_235120_2<_Tp>>
    class UniquePointer_235120_2
    {
      class _Pointer
      {
        template<typename _Up>
        static typename _Up::pointer __test(typename _Up::pointer*);

        template<typename _Up>
        static _Tp* __test(...);

        typedef typename remove_reference_235120_2<_Dp>::type _Del;

      public:
        typedef decltype(__test<_Del>(0)) type;
      };
      
    public:      
      typedef typename _Pointer::type pointer;
    };       
    
    int test_235120_2() {
        UniquePointer_235120_2<Foo_235120_2>::pointer uptr1;
        uptr1->bbb;
        UniquePointer_235120_2<Boo_235120_2>::pointer uptr2;
        uptr2->ccc;
    }
}      