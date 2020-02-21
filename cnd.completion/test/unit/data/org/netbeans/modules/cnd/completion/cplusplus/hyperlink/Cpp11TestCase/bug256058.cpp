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