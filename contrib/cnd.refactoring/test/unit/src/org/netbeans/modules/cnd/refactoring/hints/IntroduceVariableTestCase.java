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
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.test.RefactoringBaseTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class IntroduceVariableTestCase  extends RefactoringBaseTestCase {

    public IntroduceVariableTestCase(String testName) {
        super(testName);
    }

    @Override
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

    public void testQuote() throws Exception {
        performIntroduceVariable("cpu.cc", 60, 13, 60, 24, "int", "TypeID"); // GetTypeID()
        performIntroduceVariable("cpu.cc", 87, 13, 87, 28, "int", "CategoryID"); // GetCategoryID()

        performIntroduceVariable("customer.cc", 58, 15, 58, 28, "string", "name"); // customer.name
        performIntroduceVariable("customer.cc", 58, 52, 58, 69, "int", "discount"); // customer.discount

        performIntroduceVariable("memory.cc", 58, 18, 58, 34, "int", "Units"); // 200 * GetUnits()

        performIntroduceVariable("module.cc", 70, 20, 70, 40, "const char*", "Description"); // obj.GetDescription()
        performIntroduceVariable("module.cc", 74, 22, 74, 44, "int", "SupportMetric"); // obj.GetSupportMetric()
    }

    public void performIntroduceVariable(String source, int lineStart, int columnStart, int lineEnd, int colummEnd, String type, String name) throws Exception {
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
        
        IntroduceVariableFix introduceVariableFix = new IntroduceVariableFix(res.getStatementInBody(), applicableTextExpression, doc, null, fileObject);
        String suggestName = introduceVariableFix.suggestName();
        assertEquals(name, suggestName);
        String suggestType = introduceVariableFix.suggestType();
        assertEquals(type, suggestType);
    }
}
