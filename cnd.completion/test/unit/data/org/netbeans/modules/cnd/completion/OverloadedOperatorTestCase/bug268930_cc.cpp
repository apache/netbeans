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

namespace bug268930 {
    typedef unsigned long int my_size_t;

    struct AAA268930 {
        AAA268930(const char *str);
        const char* c_str();
    };

    AAA268930 operator "" _my_str(const char * str, my_size_t length) {
        return AAA268930(str);
    }

    unsigned long long int operator "" _km(unsigned long long int v) {
        return v;
    }

    template <char...Chars>
    unsigned long long int operator "" _km_raw() {
        return 0;
    }

    char operator "" _km_char(char v) {
        return v;
    } 

    long double operator "" _my_float(long double param) {
        return param;
    }

    long double operator "" _my_float_raw(const char *param) {
        return 0;
    }

    char operator "" _chr(char v) {
        return v;
    } 

    wchar_t operator "" _wchr(wchar_t v) {
        return v;
    } 

    void foo268930(const char*);
    void foo268930(AAA268930);

    int main268930() {
        'A'_chr;
        L'A'_wchr;
        int _km = 1;
        _km = 123_km + 1; 
        _km = 123_km_raw + 1; 
        _km = 'A'_km_char + 1; 
        float _my_float = 0;
        _my_float = 1.0_my_float;
        _my_float = 1.0_my_float_raw;
        foo268930("bla");
        foo268930("bla"_my_str);
        foo268930("bla"_my_str.c_str());
        return 0;
    }
}