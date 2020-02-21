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

//#include <stdio.h>

namespace iz143977_1 {

    class NullType {};
    class EmptyType {};
    template <class T1, class T2> class DefaultFactoryError {};
    template <class T, class U> struct Typelist {};

    struct FactoryImplBase {
        typedef EmptyType Parm1;
        typedef EmptyType Parm2;
    };

    template <typename AP, typename Id, typename TList>
    struct FactoryImpl {
    };

    template<typename AP, typename Id>
    struct FactoryImpl<AP, Id, NullType>
            : public FactoryImplBase
    {
    };

    template <typename AP, typename Id, typename P1 >
    struct FactoryImpl<AP,Id, Typelist<P1, NullType> >
                : public FactoryImplBase
    {
        virtual ~FactoryImpl() {}
        virtual AP* CreateObject(const Id& id,Parm1 ) = 0;
    };


    template
    <
        class AbstractProduct,
        typename IdentifierType,
        typename CreatorParmTList = NullType,
        template<typename, class> class FactoryErrorPolicy = DefaultFactoryError
    >
    class Factory : public FactoryErrorPolicy<IdentifierType, AbstractProduct>
    {
        typedef FactoryImpl< AbstractProduct, IdentifierType, CreatorParmTList > Impl;
        
        typedef typename Impl::Parm1 Parm1; // Parm1 should be resooved
        typedef typename Impl::Parm2 Parm2; // Parm2 should be resooved
    };
}
    using namespace iz143977_1;
    
//int main(int argc, char** argv) {
//    Factory<int, int> f2;
//    return 0;
//}

