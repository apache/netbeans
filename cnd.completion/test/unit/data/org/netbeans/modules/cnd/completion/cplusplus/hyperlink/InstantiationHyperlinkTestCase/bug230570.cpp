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

namespace {

    struct MyClass {
        void boo() {}
    };


    template <typename T>  
    struct my_allocator {

        typedef T* pointer;


        template <typename T1>
        struct rebind {
            typedef my_allocator<T1> other;
        };

    };


    template <typename T>
    struct type_traits {

    };

    template<typename _Tp>
    struct type_traits<_Tp*> {

        typedef MyClass& reference;

        typedef MyClass* pointer;

    };

    template <class Iterator>
    struct my_iterator {

        Iterator myIter;

        my_iterator(Iterator iter) : myIter(iter) {}

        typename type_traits<Iterator>::reference operator*() { 
            return *myIter; 
        }

        typename type_traits<Iterator>::reference get() {
            return *myIter;
        }

    };

    template <typename T, typename Allocator>
    struct vector_base {

        typedef typename Allocator::template rebind<T>::other T_Alloc_Types;

    };

    template <class T, class Allocator> 
    struct my_vector {

        typedef typename vector_base<T, Allocator>::T_Alloc_Types T_Alloc_Types;

        typedef typename T_Alloc_Types::pointer pointer;

        typedef my_iterator<pointer> iterator;

    }; 

    template <class T>
    typename my_vector<T, my_allocator<T> >::iterator dereference() {

    }

    int main()
    {
        dereference<MyClass>().get().boo(); // boo is unresolved
        return 0;
    }

}