/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.csl;

import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class CssStructureScannerTest extends CssModuleTestBase {
    
    public CssStructureScannerTest(String name) {
        super(name);
    }

    public void testFolds() throws BadLocationException, ParseException {
        assertFolds("div { \n }\n.class { \n }", new int[][]{{4,9}, {17,22}});
        //           0123456 789 0123456789 012
    
    }
    
    public void testIssue199420() throws BadLocationException, ParseException {
        assertFolds("div { \n }\n", new int[][]{{4,9}});

        assertFolds("div { \n }", new int[][]{{4,9}});
    }
    
    private static final String FOLD_TYPE_NAME = "codeblocks";
    
    public void assertFolds(String code, int[][] expected) throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse(code);
//        TestUtil.dumpResult(result);
        
        StructureScanner scanner = new CssStructureScanner();
        Map<String, List<OffsetRange>> folds = scanner.folds(result);
        
        List<OffsetRange> ranges = folds.get(FOLD_TYPE_NAME);
        assertNotNull("No folds found but some were expected!", ranges);
        
        assertEquals(String.format("Returned %s folds but %s were expected!", expected.length, ranges.size()), expected.length, ranges.size());
        
        for(int i = 0; i < expected.length; i++) {
            int[] range = expected[i];
            OffsetRange exp = ranges.get(i);
            String msg = String.format("Expected fold range <%s,%s> but was <%s,%s>", range[0], range[1], exp.getStart(), exp.getEnd());
            assertEquals(msg, range[0], exp.getStart());
            assertEquals(msg, range[1], exp.getEnd());
        }
        
    }
    
}
