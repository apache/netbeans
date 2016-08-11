/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package formatting;

import com.intellij.psi.PsiFile;
import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.formatting.KotlinFormatterUtils;
import org.jetbrains.kotlin.formatting.NetBeansDocumentFormattingModel;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import utils.TestUtils;

/**
 *
 * @author Alexander.Baratynski
 */
public class FormattingTest extends NbTestCase {
    
    private final Project project;
    private final FileObject formattingDir;
    
    public FormattingTest() {
        super("Formatting test");
        project = JavaProject.INSTANCE.getJavaProject();
        formattingDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("formatting");
    }
    
    private void doTest(String fileName) {
        try {
            Document doc = TestUtils.getDocumentForFileObject(formattingDir, fileName);
            FileObject file = ProjectUtils.getFileObjectForDocument(doc);
            PsiFile parsedFile = ProjectUtils.getKtFile(doc.getText(0, doc.getLength()), file);
            String code = parsedFile.getText();
            
            KotlinFormatterUtils.formatCode(code, parsedFile.getName(), project, "\n");
            String formattedCode = NetBeansDocumentFormattingModel.getNewText();
            
            Document doc2 = TestUtils.getDocumentForFileObject(formattingDir, fileName.replace(".kt", ".after"));
            String after = doc2.getText(0, doc2.getLength());
            
            assertEquals(after, formattedCode);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(formattingDir);
    }
    
    @Test
    public void testBlockCommentBeforeDeclaration() {
        doTest("blockCommentBeforeDeclaration.kt");
    }
    
    @Test
    public void testClassesAndPropertiesFormatTest() {
        doTest("classesAndPropertiesFormatTest.kt");
    }
    
    @Test
    public void testCommentOnTheLastLineOfLambda() {
        doTest("commentOnTheLastLineOfLambda.kt");
    }
    
    @Test
    public void testIndentInDoWhile() {
        doTest("indentInDoWhile.kt");
    }
    
    @Test
    public void testIndentInIfExpressionBlock() {
        doTest("indentInIfExpressionBlock.kt");
    }
    
    @Test
    public void testIndentInPropertyAccessor() {
        doTest("indentInPropertyAccessor.kt");
    }
    
    @Test
    public void testIndentInWhenEntry() {
        doTest("indentInWhenEntry.kt");
    }
    
    @Test
    public void testInitIndent() {
        doTest("initIndent.kt");
    }
    
    @Test
    public void testLambdaInBlock() {
        doTest("lambdaInBlock.kt");
    }
    
    @Test
    public void testNewLineAfterImportsAndPackage() {
        doTest("newLineAfterImportsAndPackage.kt");
    }
    
    @Test
    public void testObjectsAndLocalFunctionsFormat() {
        doTest("objectsAndLocalFunctionsFormatTest.kt");
    }
    
    @Test
    public void testPackageFunctions() {
        doTest("packageFunctionsFormatTest.kt");
    }
    
    @Test
    public void testClassInBlockComment() {
        doTest("withBlockComments.kt");
    }
    
    @Test
    public void testJavaDoc() {
        doTest("withJavaDoc.kt");
    }
    
    @Test
    public void testLineComments() {
        doTest("withLineComments.kt");
    }
    
    @Test
    public void testMutableVariable() {
        doTest("withMutableVariable.kt");
    }
    
    @Test
    public void testWhitespaceBeforeBrace() {
        doTest("withWhitespaceBeforeBrace.kt");
    }
    
    @Test
    public void testWhithoutComments() {
        doTest("withoutComments.kt");
    }
    
}
