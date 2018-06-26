/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.test.web.core.syntax;

import java.io.PrintStream;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/** Syntax utils class.
 *
 * @author  mf100882
 */
public class SyntaxUtils {

    /** Dumps token context names for given {@link org.netbeans.editor.TokenContextPath} */
    public static void dumpTokenContextPath(TokenContextPath tcp, PrintStream out) {
        TokenContext[] tcs = tcp.getContexts();
        for(int i = 0; i < tcs.length; i++ ) {
            String tcClassName = tcs[i].getClass().getName();
            tcClassName = tcClassName.substring(tcClassName.lastIndexOf(".") + 1);
            out.print(tcClassName + ( (i < (tcs.length - 1)) ? ", " : ""));
        }
    }
    
    /** converts \n to <NL> \t to <TAB> etc... */
    public static String normalize(String s, String[][] translationTable) {
        StringBuffer normalized = new StringBuffer();
        for(int i = 0; i < s.length(); i++) {
            String ch = s.substring(i,i+1);
            for(int j = 0; j < normalizeTable.length; j++) {
                if(ch.equals(normalizeTable[j][0])) ch = normalizeTable[j][1];
            }
            normalized.append(ch);
        }
        return normalized.toString();
    }
    
    /** the some as {@ling normalize(String s, String[][] translationTable)} 
     * but uses default translation table. */
    public static String normalize(String s) {
        return normalize(s, normalizeTable);
    }
    
    public static final String[][] normalizeTable = {{"\n", "<NL>"},
                                                      {"\t", "<TAB>"}};
    
}
