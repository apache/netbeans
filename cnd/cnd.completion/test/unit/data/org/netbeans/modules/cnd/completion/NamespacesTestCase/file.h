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

namespace S1 {
    int i1;
    void f1();
    namespace S2 {
        int i2;
        void f2();
    }
    
    struct str {
        int i;
    } q;
}

namespace S3 {
    namespace S4 {
        class S4Class {
            public:
                void s4ClassFun();
                static void s4ClassStFun();
        };
        namespace S5 {
            class S5Class {
                public:
                    void s5ClassFun();
                    static S5Class* stS5ClassFun();
                    static S5Class* pPtrS5Class;
                    static S5Class  s5Class;
                protected:
                    void s5ClassFunProt();
                    static S5Class* stS5ClassFunProt();
                    static S5Class* pPtrS5ClassProt;
                    static S5Class  s5ClassProt;
                private:
                    void s5ClassFunPriv();
                    static S5Class* stS5ClassFunPriv();
                    static S5Class* pPtrS5ClassPriv;
                    static S5Class  s5ClassPriv;            
                };
        }
        S5::S5Class* S4Fun() {
        }
        S4Class* pPtrS4Class;
        S4Class  s4Class;
    }
}