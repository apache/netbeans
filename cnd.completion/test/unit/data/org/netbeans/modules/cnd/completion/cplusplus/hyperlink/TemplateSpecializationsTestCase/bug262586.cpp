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

namespace bug262586 {

    namespace std262586 { 
        
      struct void_t {};
      
      template <typename _Iterator, typename = void_t>
      struct __iterator_traits262586 { 
      };
       
      template <typename _Iterator>
      struct __iterator_traits262586<_Iterator, void_t> { 
          typedef typename _Iterator::pointer pointer;
      };

      template<typename _Iterator>
      struct iterator_traits262586 : __iterator_traits262586<_Iterator> { 
      };
      
      template<typename _Tp>
      struct iterator_traits262586<_Tp*> { 
          typedef _Tp* pointer;
      };

      template<typename _Iterator>
      class __normal_iterator262586 
      {
        typedef std262586::iterator_traits262586<_Iterator> __traits_type;

      public:
        typedef typename __traits_type::pointer pointer;      
      };    

      template<typename _Iterator>
      class reverse_iterator262586
      {
        protected:         
          typedef std262586::iterator_traits262586<_Iterator> __traits_type;

        public:
          typedef typename __traits_type::pointer pointer;      
          pointer operator->() const;
      };
      
      template <typename BT>
      struct vector_base {
          typedef __normal_iterator262586<BT*> iterator;
      };

      template <typename Tp>
      struct vector262586 {
          typedef vector_base<Tp> _Base;
          typedef typename _Base::iterator iterator;
          typedef reverse_iterator262586<iterator> reverse_iter;
      };
    }

    struct AAA262586 { 
        int foo(); 
    }; 

    int main262586() {
        std262586::vector262586<AAA262586>::reverse_iter iter1;
        iter1->foo();
        return 0;     
    }           
    
    // Part 2 - bug with using declarations

    template <typename T>
    struct CollectionBase262586 {
        T get();
    };

    template <typename T>
    struct Collection262586 : protected CollectionBase262586<T> {
        using CollectionBase262586<T>::get;
    };

    int main262586_2() {
        Collection262586<AAA262586> col;
        col.get().foo();
        return 0;
    } 
}
