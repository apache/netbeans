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

namespace iz143977_3 {

    class NullType {};

    template <class T1, class T2> struct FactoryImpl {};
    
    template<class T> struct FactoryImpl<T, NullType> {
        typedef NullType Parm_null;
    };

    template<class T> struct FactoryImpl<T, int> {
        typedef NullType Parm_int;
    };
    
    template<class T, class T2 = NullType, class T3 = int> struct Factory {
        typedef FactoryImpl<T, NullType> Impl1;
        typedef FactoryImpl<T, int> Impl2;
        typedef FactoryImpl<T, T2> Impl3;
        typedef FactoryImpl<T, T3> Impl4;
        typedef typename Impl1::Parm_null Parm1;    // Parm_null should be resolved
        typedef typename Impl2::Parm_int Parm1;     // Parm_int should be resolved
        typedef typename Impl3::Parm_null Parm1;    // Parm_null should be resolved
        typedef typename Impl4::Parm_int Parm1;     // Parm_int should be resolved
    };
}
