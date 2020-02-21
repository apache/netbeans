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

namespace bug235462 {
    //#include <iostream>
    namespace std {
        static struct _cout {
            template <class T>
            _cout operator<<(T t);
        } cout;
        
        static const char* endl = "\n";
    }

    struct AAA {};
    struct BBB : AAA {};

    template <typename T1, typename T2>
    void roo(T1 t1, T2 t2) {
        std::cout << "roo 1" << std::endl;
    }

    template <typename T1, typename T2>
    void roo(T1 *t1, T2 *t2) {
        std::cout << "roo 2" << std::endl;
    }

    template <typename T1, typename T2>
    void roo(const T1 *t1, const T2 *t2) {
        std::cout << "roo 3" << std::endl;
    }

    template <typename T>
    void roo(T t1, T t2) {
        std::cout << "roo 4" << std::endl;
    }

    void roo(AAA t1, BBB t2) {
        std::cout << "roo 5" << std::endl;
    }

    void roo(int t1, int t2) {
        std::cout << "roo 6" << std::endl;
    }

    void roo(float t1, int t2) {
        std::cout << "roo 7" << std::endl;
    }

    void roo(double t1, int t2) {
        std::cout << "roo 8" << std::endl;
    }

    void roo(int t1, float t2) {
        std::cout << "roo 9" << std::endl;
    }

    void zoo(char t1, float t2) {
        std::cout << "zoo 1" << std::endl;
    }

    void zoo(int t1, double t2) {
        std::cout << "zoo 2" << std::endl;
    }

    void zoo(AAA a, BBB *b) {
        std::cout << "zoo 3" << std::endl;
    }

    template <typename T>
    void zoo(T a, BBB *b) {
        std::cout << "zoo 4" << std::endl;
    }

    template <typename T>
    void zoo(AAA a, T b) {
        std::cout << "zoo 5" << std::endl;
    }

    int main() {
        AAA a;
        AAA *pa;
        const AAA *pca;
        BBB b;
        BBB *pb;
        const BBB *pcb;    

        roo(a, pa);
        roo(pa, pb);
        roo(pca, pcb);
        roo(a, a);
        roo(a, b);

        int ip;
        float fp;
        double dp;
        char cp;
        unsigned char ucp;
        bool bp;

        roo(ip, ip);
        roo(fp, ip);
        roo(dp, ip);
        roo(ip, fp);

        zoo(cp, fp);
        zoo(cp, dp);
        zoo(a, pb);
        zoo(b, pb);
        zoo(b, b);
        
        roo((const AAA*)pa, (const BBB*)pb);

        return 0;
    } 
}