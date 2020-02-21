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

namespace bug238847_3 {
    
    //==========================================================================
    // Allocators

    template <typename T>  
    struct my_allocator_238847_3 {

        typedef T value_type;

        typedef T* pointer;

        int foo();

        template <typename T1>
        struct my_rebind {
            typedef my_allocator_238847_3<T1> other;
        };

    };
    
    template<typename _Alloc, typename _Tp>
    struct my_alloc_rebind_238847_3
    {
      typedef typename _Alloc::template my_rebind<_Tp>::other __type;
    };    

    template <typename _Alloc>
    struct my_allocator_traits_238847_3 {

        typedef typename _Alloc::value_type value_type;

    private: 
            
        template<typename _Tp> 
        static typename _Tp:: pointer _S_pointer_helper (_Tp*); 
        
        static value_type* _S_pointer_helper (...); 
        
        typedef decltype( _S_pointer_helper ((_Alloc*)0)) __pointer ;
        
    public:

        typedef __pointer pointer;


        template<typename _Tp>
        using rebind_alloc = typename my_alloc_rebind_238847_3<_Alloc, _Tp>::__type;
    };

    template<typename _Alloc>
     struct my_alloc_traits_238847_3 {
        typedef _Alloc allocator_type;
        typedef my_allocator_traits_238847_3<_Alloc>           _Base_type;

        typedef typename _Base_type::value_type         value_type;

        typedef typename _Base_type::pointer            pointer;

        template<typename _Tp>
        struct rebind_helper
        { typedef typename _Base_type::template rebind_alloc<_Tp> other; };    
    };    
    
    //==========================================================================
    // Iterators

    template <typename T>
    struct type_traits_238847_3 {

    };

    template<typename _Tp>
    struct type_traits_238847_3<_Tp*> {

        typedef _Tp& reference;

        typedef _Tp* pointer;

    };

    template <class Iterator>
    struct my_iterator_238847_3 {

        typename type_traits_238847_3<Iterator>::pointer get();

    };

    //==========================================================================
    // Vector
    
    template <typename T, typename Allocator>
    struct vector_base_238847_3 {

        typedef typename my_alloc_traits_238847_3<Allocator>::template rebind_helper<T>::other _Tp_alloc_type;

        typedef typename my_alloc_traits_238847_3<_Tp_alloc_type>::pointer pointer;    

    };    
    
    template <typename T, typename Alloc = my_allocator_238847_3<T> >
    struct vector_238847_3 {
        
        typedef vector_base_238847_3<T, Alloc> _Base;
        
        typedef typename _Base::pointer pointer;
        
        typedef my_iterator_238847_3<pointer> iterator;
        
    };

    struct MyClass_238847_3 {
        void boo() {}
    };

    int main_238847_3()
    {
        vector_238847_3<MyClass_238847_3>::pointer ptr;
        ptr->boo();

        vector_238847_3<MyClass_238847_3>::iterator iter;
        iter.get()->boo();
        
        return 0; 
    }  
} 