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

namespace bug243083 {
    template <typename EntryType> 
    struct SpecEntryTraits243083 {
      typedef EntryType DeclType;
    };

    template <typename T>
    struct XXX243083 {
        typedef T type;
    };

    template <typename EntryType,
              typename _SETraits = SpecEntryTraits243083<EntryType>,
              typename _DeclType = typename _SETraits::DeclType>
    struct SpecIterator243083 {
        typedef _SETraits SETraits;
        typedef _DeclType DeclType1;    
        typedef typename XXX243083<typename _SETraits::DeclType>::type DeclType2;   

        DeclType1 *operator*() const {
            return 0;
        }
        DeclType2 *operator->() const { 
            return 0; 
        }    
    }; 

    struct AAA243083 {
        int boo() {
            return 1;
        }
    };

    typedef SpecIterator243083<AAA243083> spec_iterator243083;

    void mainddd243083() {
        spec_iterator243083 I;
        I->boo(); // boo is unresolved with warning
        (*I)->boo(); // boo is unresolved with warning
    }

    // Test case for bug about infinite instantiation
    template <typename T>
    struct convert_bgl_params_to_boost_parameter243083 {
      typedef convert_bgl_params_to_boost_parameter243083<typename T::next_type> rest_conv;
      static T conv243083(const T& x) {
        return T(x.m_value, rest_conv::conv243083(x.m_base));
      }
    };   
}