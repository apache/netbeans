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
package org.netbeans.modules.cnd.refactoring.hints;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 */
public class IntroduceVariable2TestCase extends ProjectBasedTestCase {

    public IntroduceVariable2TestCase(String testName) {
        super(testName, true);
    }

    public void testQuote() throws Exception {
        performIntroduceVariable("Test2.cc", 8, 8, 8, 16, "int", "foo", 3); // a.foo( )
    }

    public void performIntroduceVariable(String source, int lineStart, int columnStart, int lineEnd, int colummEnd, String type, String name, int numnerOccurrences) throws Exception {
        File testSourceFile = getDataFile(source);
        int start = getOffset(testSourceFile, lineStart, columnStart);
        int end = getOffset(testSourceFile, lineEnd, colummEnd);
        CsmFile csmFile = getCsmFile(testSourceFile);
        Document doc = CsmUtilities.getDocument(csmFile);
        FileObject fileObject = CsmUtilities.getFileObject(csmFile);
        ExpressionFinder expressionFinder = new ExpressionFinder(doc, csmFile, start, start, end, new AtomicBoolean());
        ExpressionFinder.StatementResult res = expressionFinder.findExpressionStatement();
        assertTrue(expressionFinder.isExpressionSelection());
        CsmOffsetable applicableTextExpression = expressionFinder.applicableTextExpression();
        assertNotNull(applicableTextExpression);

        List<Pair<Integer, Integer>> occurrences = res.getOccurrences(applicableTextExpression);
        assertEquals(numnerOccurrences, occurrences.size());
        
        IntroduceVariableFix introduceVariableFix = new IntroduceVariableFix(res.getStatementInBody(), applicableTextExpression, doc, null, fileObject);
        String suggestName = introduceVariableFix.suggestName();
        assertEquals(name, suggestName);
        String suggestType = introduceVariableFix.suggestType();
        assertEquals(type, suggestType);
    }
}
