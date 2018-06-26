/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.php.findusages;

import java.util.Collections;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.refactoring.php.RefactoringTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class FindUsagesTestBase extends RefactoringTestBase {

    public FindUsagesTestBase(String testName) {
        super(testName);
    }

    @Override
    protected String getTestFolderPathSuffix() {
        return "findusages/" + getTestName();
    }

    protected void findUsages(final String caretLine) throws Exception {
        final String exactFileName = getTestPath();
        final String result = getTestResult(exactFileName, caretLine);
        assertDescriptionMatches(exactFileName, result, true, ".findUsages");
    }

    private String getTestResult(final String exactFileName, final String caretLine) throws Exception {
        FileObject testFile = getTestFile(exactFileName);
        Source testSource = getTestSource(testFile);
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final String[] result = new String[1];
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(final ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof PHPParseResult);
                PHPParseResult phpResult = (PHPParseResult)r;
                StringBuilder sb = new StringBuilder();
                WhereUsedSupport wus = WhereUsedSupport.getInstance(phpResult, caretOffset);
                for (FileObject fileObject : wus.getRelevantFiles()) {
                    wus.collectUsages(fileObject);
                }
                WhereUsedSupport.Results results = wus.getResults();
                for (WhereUsedElement whereUsedElement : results.getResultElements()) {
                    sb.append("Display text: ");
                    sb.append(whereUsedElement.getDisplayText());
                    sb.append("\n");
                    sb.append("File name: ");
                    sb.append(whereUsedElement.getFile().getNameExt());
                    sb.append("\n");
                    sb.append("Name: ");
                    sb.append(whereUsedElement.getName());
                    sb.append("\n");
                    sb.append("Position: ");
                    sb.append("BEGIN: ").append(whereUsedElement.getPosition().getBegin().getOffset()).append(" END: ").append(whereUsedElement.getPosition().getEnd().getOffset());
                    sb.append("\n\n");
                }
                result[0] = sb.toString().trim();
            }
        });
        return result[0];
    }

}
