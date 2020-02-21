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