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

namespace bug242417 {   
    
    struct CastStruct242417 {
        CastStruct242417(int a) {}
    };
    typedef CastStruct242417 CSAlias242417;

    struct AAA1_242417 {
        template <class T> 
        using BBB1 = T;

        operator CSAlias242417();

        int xx;
        AAA1_242417() {xx = 1;}
    };

    AAA1_242417::operator BBB1<CastStruct242417>() {
        return xx + 10;
    }     
 
    struct AAA2_242417 {    
        operator CSAlias242417();    
    };

    AAA2_242417::operator CastStruct242417() { // click on operator doesn't navigate to the declaration
        return AAA2_242417();
    }
    
    namespace DDD_242417 { 

        struct Test_242417 {};

        typedef Test_242417 TEST_242417;

        struct AAA3_242417 {
            operator Test_242417();
        };
    }     


    DDD_242417::AAA3_242417::operator DDD_242417::TEST_242417() {
        return Test_242417(); 
    }       
    
    template <typename T>
    using Identity_242417 = T;
    
    struct AAA4 {
        template <typename T>
        operator T();
    }; 

    template <typename T>
    AAA4::operator Identity_242417<T>() {
        return T();
    }
    
    namespace RRR_242417 {
        struct AAA5_242417 {
            operator CastStruct242417();
        };
    }
    
    bug242417::RRR_242417::AAA5_242417::operator bug242417::Identity_242417<bug242417::CastStruct242417>() {
        return CastStruct242417();
    }    
}