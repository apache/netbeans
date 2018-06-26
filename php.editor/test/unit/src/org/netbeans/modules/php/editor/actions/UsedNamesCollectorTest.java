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
package org.netbeans.modules.php.editor.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UsedNamesCollectorTest extends PHPTestBase {

    public UsedNamesCollectorTest(String testName) {
        super(testName);
    }

    public void testUseCase_01() throws Exception {
        performTest("useCase_01", "Homepage^Presenter");
    }

    public void testUseCase_02() throws Exception {
        performTest("useCase_02", "Homepage^Presenter");
    }

    public void testUseCase_03() throws Exception {
        performTest("useCase_03", "Homepage^Presenter");
    }

    public void testUseCase_04() throws Exception {
        performTest("useCase_04", "Homepage^Presenter");
    }

    public void testUseCase_05() throws Exception {
        performTest("useCase_05", "Homepage^Presenter");
    }

    public void testUseCase_06() throws Exception {
        performTest("useCase_06", "Homepage^Presenter");
    }

    public void testUseCase_07() throws Exception {
        performTest("useCase_07", "class Class^Name {");
    }

    public void testUseCase_08() throws Exception {
        performTest("useCase_08", "class Class^Name {");
    }

    public void testIssue209755() throws Exception {
        performTest("issue209755", "class Class^Name {");
    }

    protected void performTest(String fileName, String caretLine) throws Exception {
        String exactFileName = "testfiles/actions/" + fileName + ".php";
        Map<String, List<UsedNamespaceName>> testResult = getTestResult(exactFileName, caretLine);
        String target = createResultString(testResult);
        assertDescriptionMatches(exactFileName, target, false, ".usedNames");
    }

    protected Map<String, List<UsedNamespaceName>> getTestResult(String fileName, String caretLine) throws Exception {
        FileObject testFile = getTestFile(fileName);

        Source testSource = getTestSource(testFile);
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final Map<String, List<UsedNamespaceName>> result = new HashMap<>();
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                if (r != null) {
                    assertTrue(r instanceof ParserResult);
                    PHPParseResult phpResult = (PHPParseResult) r;
                    result.putAll(new UsedNamesCollector(phpResult, caretOffset).collectNames());
                }
            }
        });

        return result;
    }

    private String createResultString(Map<String, List<UsedNamespaceName>> testResult) {
        StringBuilder sb = new StringBuilder();
        SortedSet<String> keys = new TreeSet<>(testResult.keySet());
        for (String key : keys) {
            sb.append("Name: ").append(key).append("\n");
            for (UsedNamespaceName usedNamespaceName : testResult.get(key)) {
                sb.append(" ").append(usedNamespaceName.getName()).append(" --> ").append(usedNamespaceName.getReplaceName()).append(":").append(usedNamespaceName.getOffset()).append("\n");
            }
        }

        return sb.toString();
    }
}
