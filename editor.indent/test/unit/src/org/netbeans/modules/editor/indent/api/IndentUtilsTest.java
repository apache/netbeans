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

package org.netbeans.modules.editor.indent.api;

import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.ArrayUtilities;

/**
 *
 * @author Miloslav Metelka
 */
public class IndentUtilsTest extends NbTestCase {
    
    private static final String MIME_TYPE = "text/x-test";

    public IndentUtilsTest(String name) {
        super(name);
    }

    public void testIndentUtils() throws Exception {
        // Test empty indent
        assertSame("", IndentUtils.createIndentString(0, true, 8));
        assertSame("", IndentUtils.createIndentString(0, false, 4));
        
        // Test <tabSize
        String s;
        assertEquals("   ", s = IndentUtils.createIndentString(3, false, 4));
        // Test caching of indent strings
        assertSame(s, IndentUtils.createIndentString(3, false, 4));
        
        // Test ==tabSize
        assertEquals("\t", s = IndentUtils.createIndentString(4, false, 4));
        assertSame(s, IndentUtils.createIndentString(4, false, 4));

        // Test >tabSize
        assertEquals("\t  ", s = IndentUtils.createIndentString(6, false, 4));
        assertSame(s, IndentUtils.createIndentString(6, false, 4));
        assertEquals("\t\t\t   ", s = IndentUtils.createIndentString(15, false, 4));
        assertSame(s, IndentUtils.createIndentString(15, false, 4));
        
        // Test spaces-only
        assertEquals("          ", s = IndentUtils.createIndentString(10, true, 4));
        assertEquals(s, IndentUtils.createIndentString(10, true, 4));
        
        // Test many (non-cached) spaces
        int testUncachedIndent = 90;
        StringBuilder sb = new StringBuilder(testUncachedIndent);
        ArrayUtilities.appendSpaces(sb, testUncachedIndent);
        assertEquals(sb.toString(), IndentUtils.createIndentString(testUncachedIndent, true, 4));
        
        // Test long (non-cached) tab indent
        int i = testUncachedIndent;
        sb.setLength(0);
        while (i >= 8) {
            sb.append('\t');
            i -= 8;
        }
        ArrayUtilities.appendSpaces(sb, i);
        assertEquals(sb.toString(), IndentUtils.createIndentString(testUncachedIndent, false, 8));
        
        // Test cache limits (#124352)
        assertEquals("\t   ", IndentUtils.createIndentString(11, false, 8));
    }

}
