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
package formatting

import com.intellij.psi.PsiFile
import javaproject.JavaProject
import javax.swing.text.Document
import org.jetbrains.kotlin.formatting.KotlinFormatterUtils
import org.jetbrains.kotlin.formatting.NetBeansDocumentFormattingModel
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.openide.filesystems.FileObject
import utils.*

/**
 *
 * @author Alexander.Baratynski
 */
class FormattingTest : NbTestCase("Formatting test") {
    private val project: Project
    private val formattingDir: FileObject

    init {
        project = JavaProject.javaProject
        formattingDir = project.projectDirectory.getFileObject("src").getFileObject("formatting")
    }

    private fun doTest(fileName: String) {
        val doc = getDocumentForFileObject(formattingDir, fileName)
        val file = ProjectUtils.getFileObjectForDocument(doc)
        val parsedFile = ProjectUtils.getKtFile(doc.getText(0, doc.length), file)
        val code = parsedFile.text
            
        val formattedCode = KotlinFormatterUtils.formatCode(code, parsedFile.name, project, "\n")
        val doc2 = getDocumentForFileObject(formattingDir, fileName.replace(".kt", ".after"))
        val after = doc2.getText(0, doc2.length)
        assertEquals(after, formattedCode)
    }

    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(formattingDir)
    }

    fun testBlockCommentBeforeDeclaration() = doTest("blockCommentBeforeDeclaration.kt")

    fun testClassesAndPropertiesFormatTest() = doTest("classesAndPropertiesFormatTest.kt")

    fun testCommentOnTheLastLineOfLambda() = doTest("commentOnTheLastLineOfLambda.kt")

    fun testIndentInDoWhile() = doTest("indentInDoWhile.kt")

    fun testIndentInIfExpressionBlock() = doTest("indentInIfExpressionBlock.kt")

    fun testIndentInPropertyAccessor() = doTest("indentInPropertyAccessor.kt")

    fun testIndentInWhenEntry() = doTest("indentInWhenEntry.kt")

    fun testInitIndent() = doTest("initIndent.kt")

    fun testLambdaInBlock() = doTest("lambdaInBlock.kt")

    fun testNewLineAfterImportsAndPackage() = doTest("newLineAfterImportsAndPackage.kt")

    fun testObjectsAndLocalFunctionsFormat() = doTest("objectsAndLocalFunctionsFormatTest.kt")

    fun testPackageFunctions() = doTest("packageFunctionsFormatTest.kt")

    fun testClassInBlockComment() = doTest("withBlockComments.kt")

    fun testJavaDoc() = doTest("withJavaDoc.kt")

    fun testLineComments() = doTest("withLineComments.kt")

    fun testMutableVariable() = doTest("withMutableVariable.kt")

    fun testWhitespaceBeforeBrace() = doTest("withWhitespaceBeforeBrace.kt")

    fun testWhithoutComments() = doTest("withoutComments.kt")

}