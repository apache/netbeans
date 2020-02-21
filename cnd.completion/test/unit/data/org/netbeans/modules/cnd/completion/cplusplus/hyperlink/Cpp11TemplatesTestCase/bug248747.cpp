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

namespace bug248747 {
    #define DEFINE_HAS_CATEGORY \
      template<typename _Tp>  \
      class __has_iterator_category_helper248747 : __sfinae_types248747 { \
          template<typename _Up> struct _Wrap_type {}; \
          template<typename _Up> static __one __test(_Wrap_type<typename _Up:: iterator_category >*); \
          template<typename _Up> static __two __test(...); \
          public: static constexpr bool value = sizeof(__test<_Tp>(0)) == 1; \
      }; \
      template<typename _Tp> struct __has_iterator_category248747 : integral_constant248747<bool, __has_iterator_category_helper248747 <_Tp>::value> {}

    namespace std248747 {

      template <typename Tp, Tp val> 
      struct integral_constant248747 {
          static constexpr Tp value = val;
      };

      struct __sfinae_types248747
      {
        typedef char __one;
        typedef struct { char __arr[2]; } __two;
      };

      DEFINE_HAS_CATEGORY;

      template<typename _Iterator,
               bool = __has_iterator_category248747<_Iterator>::value>
      struct __iterator_traits248747 { };

      template<typename _Iterator>
      struct __iterator_traits248747<_Iterator, true>
      {
        typedef typename _Iterator::iterator_category iterator_category;
        typedef typename _Iterator::pointer           pointer;
      };

      template<typename _Iterator>
      struct iterator_traits248747 : public __iterator_traits248747<_Iterator> { };

      template<typename _Tp>
      struct iterator_traits248747<_Tp*>
      {
        typedef int iterator_category;
        typedef _Tp* pointer;
      };  

      template<typename _Iterator, typename _Container>
      class __normal_iterator248747 
      {
        typedef std248747::iterator_traits248747<_Iterator> __traits_type;

      public:
        typedef typename __traits_type::iterator_category iterator_category;
        typedef typename __traits_type::pointer   	pointer;      
      };    

      template<typename _Iterator>
      class reverse_iterator248747
      {
        protected:        
          typedef std248747::iterator_traits248747<_Iterator> __traits_type;

        public:
          typedef typename __traits_type::pointer pointer;      
          pointer operator->() const;
      };

      template <typename Tp>
      struct vector248747 {
          typedef Tp* pointer;
          typedef __normal_iterator248747<pointer, vector248747> iterator;
          typedef reverse_iterator248747<iterator> reverse_iter;
      };
    }

    struct AAA248747 {
        int foo();
    };

    int main248747() {
        std248747::vector248747<AAA248747>::reverse_iter iter1;
        iter1->foo();
        return 0;    
    }           
}