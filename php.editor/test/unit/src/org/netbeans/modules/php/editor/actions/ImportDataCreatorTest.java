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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.actions.FixUsesAction.Options;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ImportDataCreatorTest extends PHPTestBase {

    public ImportDataCreatorTest(String testName) {
        super(testName);
    }

    public void testImportData_01() throws Exception {
        performTest("Homepage^Presenter");
    }

    public void testImportData_02() throws Exception {
        performTest("Homepage^Presenter");
    }

    public void testImportData_03() throws Exception {
        performTest("Homepage^Presenter");
    }

    public void testImportData_04() throws Exception {
        // see issue #219548
        performTest("Homepage^Presenter");
    }

    public void testImportData_05() throws Exception {
        performTest("Homepage^Presenter", new Options(true, false, false, false, false));
    }

    public void testImportData_06() throws Exception {
        performTest("Homepage^Presenter", new Options(false, false, false, false, false));
    }

    public void testImportData_07() throws Exception {
        performTest("class ^Blah");
    }

    public void testImportData_08() throws Exception {
        // see issue #219548
        performTest("class ^Blah");
    }

    public void testImportData_09_issue209408() throws Exception {
        performTest("class ^ClassName");
    }

    public void testImportData_issue227304() throws Exception {
        performTest("class Your^Class extends MyClass {");
    }

    public void testImportData_issue246537a() throws Exception {
        performTest("class My^Class {");
    }

    public void testImportData_issue246537b() throws Exception {
        performTest("class My^Class {");
    }

    private void performTest(String caretLine) throws Exception {
        performTest(caretLine, null);
    }

    private void performTest(String caretLine, Options options) throws Exception {
        String exactFileName = getTestPath();
        ImportData testResult = getTestResult(exactFileName, caretLine, options);
        String target = createResultString(testResult);
        assertDescriptionMatches(exactFileName, target, false, ".importData");
    }

    private ImportData getTestResult(String fileName, String caretLine, final Options options) throws Exception {
        FileObject testFile = getTestFile(fileName);

        Source testSource = getTestSource(testFile);
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final ImportData[] result = new ImportData[1];
        Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                if (r != null) {
                    assertTrue(r instanceof ParserResult);
                    PHPParseResult phpResult = (PHPParseResult)r;
                    Map<String, List<UsedNamespaceName>> usedNames = new UsedNamesCollector(phpResult, caretOffset).collectNames();
                    FileScope fileScope = phpResult.getModel().getFileScope();
                    NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, caretOffset);
                    Options currentOptions = options;
                    if (currentOptions == null) {
                        Document document = phpResult.getSnapshot().getSource().getDocument(false);
                        CodeStyle codeStyle = CodeStyle.get(document);
                        currentOptions = new Options(codeStyle, fileScope.getFileObject());
                    }
                    ImportData importData = new ImportDataCreator(
                            usedNames,
                            ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(phpResult)),
                            namespaceScope.getNamespaceName(),
                            currentOptions).create();
                    importData.caretPosition = caretOffset;
                    result[0] = importData;
                }
            }
        });
        if (!future.isDone()) {
            future.get();
        }

        return result[0];
    }

    private String createResultString(ImportData testResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("Caret position: ").append(testResult.caretPosition);
        sb.append("\nShould show uses panel: ").append(testResult.shouldShowUsesPanel ? "true" : "false");
        sb.append("\nDefaults:\n");
        for (ImportData.DataItem dataItem : testResult.getItems()) {
            sb.append(" ").append(dataItem.getDefaultVariant().getName()).append("\n");
        }
        sb.append("\nNames:\n");
        for (ImportData.DataItem dataItem : testResult.getItems()) {
            sb.append(" ").append(dataItem.getTypeName()).append("\n");
        }
        sb.append("\nVariants:\n");
        for (ImportData.DataItem dataItem : testResult.getItems()) {
            for (ItemVariant itemVariant : dataItem.getVariants()) {
                sb.append(" ").append(itemVariant.getName()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    protected FileObject[] createSourceClassPathsForTest() {
        final File folder = new File(getDataDir(), getTestFolderPath());
        return new FileObject[]{FileUtil.toFileObject(folder)};
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))
            })
        );
    }

    private String getTestFolderPath() {
        return "testfiles/actions/" + getTestName();//NOI18N
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getName() + ".php";//NOI18N
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

}
