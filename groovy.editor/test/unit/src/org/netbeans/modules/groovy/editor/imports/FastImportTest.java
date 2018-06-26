/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.imports;

import java.util.Collections;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test class for fast import feature.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class FastImportTest extends GroovyTestBase {

    private static final CompletionItem.TypeItem aaaType = new CompletionItem.TypeItem("tester.AAA", "AAA", 0, ElementKind.CLASS);
    private static final CompletionItem.TypeItem bbbType = new CompletionItem.TypeItem("tester.BBB", "BBB", 0, ElementKind.CLASS);
    private static final CompletionItem.TypeItem dddType = new CompletionItem.TypeItem("tester.DDD", "DDD", 0, ElementKind.CLASS);

    public FastImportTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            ClassPath.SOURCE,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(getDataFile("/testfiles/ccresult"))
            })
        );
    }

    public void testNoImportsYet_spaceAfterPackageStatement() throws Exception {
        checkResult("NoImportsSpaceAfterPackage", "    AAA^", aaaType);
    }

    // #228641
    public void testNoImportsYet_noSpaceAfterPackageStatement() throws Exception {
        checkResult("NoImportsNoSpaceAfterPackage", "    AAA^", aaaType);
    }

    public void testNoImportsYet_severalSpacesAfterPackageStatement() throws Exception {
        checkResult("NoImportsMoreSpacesAfterPackage", "    AAA^", aaaType);
    }

    // #234081
    public void testSeveralImports_firstPosition() throws Exception {
        checkResult("SeveralImportsFirstPosition", "    AAA^", aaaType);
    }

    // #234081
    public void testSeveralImports_middlePosition() throws Exception {
        checkResult("SeveralImportsMiddlePosition", "    BBB^", bbbType);
    }

    // #234081
    public void testSeveralImports_lastPosition() throws Exception {
        checkResult("SeveralImportsLastPosition", "    DDD^", dddType);
    }

    private void checkResult(String fileName, String caretLine, CompletionItem.TypeItem item) throws Exception {
        checkCompletionResult("testfiles/ccresult/" + fileName + ".groovy", caretLine, item);
    }
}
