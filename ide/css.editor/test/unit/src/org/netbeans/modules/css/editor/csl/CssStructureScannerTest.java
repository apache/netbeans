/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
