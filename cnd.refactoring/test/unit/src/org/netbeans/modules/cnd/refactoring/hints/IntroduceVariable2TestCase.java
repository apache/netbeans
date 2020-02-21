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
