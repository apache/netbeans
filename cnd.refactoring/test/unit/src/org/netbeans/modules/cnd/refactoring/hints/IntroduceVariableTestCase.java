/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
