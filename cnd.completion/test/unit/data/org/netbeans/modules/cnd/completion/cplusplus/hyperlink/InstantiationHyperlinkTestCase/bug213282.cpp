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

template<const char C0, const char C1 = '\0', const char C2 = '\0', const char
C3 = '\0', const char C4 = '\0', const char C5 = '\0', const char C6 = '\0',
const char C7 = '\0', const char C8 = '\0', const char C9 = '\0',
    const char C10 = '\0', const char C11 = '\0', const char C12 = '\0', const
char C13 = '\0', const char C14 = '\0', const char C15 = '\0', const char C16 =
'\0', const char C17 = '\0', const char C18 = '\0', const char C19 = '\0',
    const char C20 = '\0', const char C21 = '\0', const char C22 = '\0', const
char C23 = '\0', const char C24 = '\0', const char C25 = '\0', const char C26 =
'\0', const char C27 = '\0', const char C28 = '\0', const char C29 = '\0',
    const char C30 = '\0', const char C31 = '\0', const char C32 = '\0', const
char C33 = '\0', const char C34 = '\0', const char C35 = '\0', const char C36 =
'\0', const char C37 = '\0', const char C38 = '\0', const char C39 = '\0',
    const char C40 = '\0', const char C41 = '\0', const char C42 = '\0', const
char C43 = '\0', const char C44 = '\0', const char C45 = '\0', const char C46 =
'\0', const char C47 = '\0', const char C48 = '\0', const char C49 = '\0',
    const char C50 = '\0', const char C51 = '\0', const char C52 = '\0', const
char C53 = '\0', const char C54 = '\0', const char C55 = '\0', const char C56 =
'\0', const char C57 = '\0', const char C58 = '\0', const char C59 = '\0',
    const char C60 = '\0', const char C61 = '\0', const char C62 = '\0', const
char C63 = '\0', const char C64 = '\0', const char C65 = '\0', const char C66 =
'\0', const char C67 = '\0', const char C68 = '\0', const char C69 = '\0'
    >
    struct bug213282_Hash {
    template< char c, unsigned int last_value>
        struct Calc {
        static const unsigned int value = ( c == 0 ) ? last_value : (
last_value *0x1234 + ( unsigned int )c ); // just test
    };
    static const unsigned int value =
        Calc<C69, Calc<C68, Calc<C67, Calc<C66, Calc<C65, Calc<C64, Calc<C63,
Calc<C62,
        Calc<C61, Calc<C60, Calc<C59, Calc<C58, Calc<C57, Calc<C56, Calc<C55,
Calc<C54,
        Calc<C53, Calc<C52, Calc<C51, Calc<C50, Calc<C49, Calc<C48, Calc<C47,
Calc<C46,
        Calc<C45, Calc<C44, Calc<C43, Calc<C42, Calc<C41, Calc<C40, Calc<C39,
Calc<C38,
        Calc<C37, Calc<C36, Calc<C35, Calc<C34, Calc<C33, Calc<C32, Calc<C31,
Calc<C30,
        Calc<C29, Calc<C28, Calc<C27, Calc<C26, Calc<C25, Calc<C24, Calc<C23,
Calc<C22,
        Calc<C21, Calc<C20, Calc<C19, Calc<C18, Calc<C17, Calc<C16, Calc<C15,
Calc<C14,
        Calc<C13, Calc<C12, Calc<C11, Calc<C10, Calc<C9, Calc<C8, Calc<C7,
Calc<C6,
        Calc<C5, Calc<C4, Calc<C3, Calc<C2, Calc<C1, Calc<C0, 0 >::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value>::value>::value>::value>
        ::value>::value>::value>::value>::value;
};

int bug213282_main() {
    bug213282_Hash<'A','B','C','D'> hs;
    hs.value;
    
    return 0;
}
