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

namespace bug256058 {
    void printf256058(...);

    template <typename T>
    struct TemplateStruct256058 {};

    namespace firstNs256058 {
        struct AAA {};

        enum MyEnum {
            MY_VAL1,
            MY_VAL2
        };

        void foo256058(AAA *var) {
            printf256058("Called foo(AAA*)\n");
        }
    }

    namespace secondNs256058 {
        struct BBB {
            void roo() {}
        };

        struct CCC : firstNs256058::AAA {
            struct EEE {};

            friend void roo256058(secondNs256058::CCC::EEE *var);

            friend void moo256058(secondNs256058::CCC::EEE *var) {
                printf256058("Called friend moo(secondNs::CCC::EEE *)\n");
            }
        };

        namespace inner {
            struct DDD {};

            template <typename T>
            static void doo256058(T &var) {
                printf256058("Called doo(T&)\n");
            }
        }

        inline namespace inlined_inner {
            struct FFF {};

            void too256058(BBB&) {
                printf256058("Called too(BBB&)\n");
            }
        }

        void roo256058(secondNs256058::CCC::EEE *var) {
            printf256058("Called friend roo(secondNs::CCC::EEE *)\n");
        }

        void zoo(inner::DDD&) {
            printf256058("Called zoo(DDD&)\n");
        }

        void hoo256058(FFF&) {
            printf256058("Called hoo(FFF&)\n");
        }
    }

    namespace firstNs256058 {
        secondNs256058::BBB operator+(AAA &var1, AAA &var2) {
            printf256058("Called +(AAA&, AAA&)\n");
            return secondNs256058::BBB();
        }

        void boo256058(secondNs256058::CCC *var) {
            printf256058("Called boo(CCC*)\n");
        }

        void coo256058(MyEnum) {
            printf256058("Called coo(MyEnum&)\n");
        }
    }

    typedef typename firstNs256058::AAA type256058;

    int main256058() {
        type256058 aaa;
        foo256058(&aaa);
        (aaa + aaa).roo();

        firstNs256058::MyEnum myEnum = firstNs256058::MY_VAL1;
        coo256058(myEnum);

        secondNs256058::CCC ccc; 
        boo256058(&ccc);

        secondNs256058::inner::DDD ddd;
        doo256058(ddd);

        secondNs256058::CCC::EEE eee;
        roo256058(&eee);
        moo256058(&eee);

        TemplateStruct256058<TemplateStruct256058<secondNs256058::inner::DDD> > tpl;
        doo256058(tpl);

        secondNs256058::FFF fff;
        hoo256058(fff);

        secondNs256058::BBB bbb;
        too256058(bbb);

        return 0; 
    }     
}