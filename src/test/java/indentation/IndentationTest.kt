/*******************************************************************************
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
 *******************************************************************************/
package indentation

import javaproject.JavaProject
import javax.swing.text.StyledDocument
import org.jetbrains.kotlin.formatting.KotlinIndentStrategy
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.openide.filesystems.FileObject
import utils.getCaret
import utils.getDocumentForFileObject

class IndentationTest : NbTestCase("Indentation test") {

    val project: Project
    val indentationDir: FileObject
    
    init {
        project = JavaProject.INSTANCE.javaProject
        indentationDir = project.projectDirectory.getFileObject("src").getFileObject("indentation")
    }
    
    fun doTest(fileName: String) {
        val doc = getDocumentForFileObject(indentationDir, fileName) as StyledDocument
        val offset = getCaret(doc) + 1
        doc.remove(offset - 1, "<caret>".length)
        doc.insertString(offset - 1, "\n", null)
        
        val strategy = KotlinIndentStrategy(doc, offset)
        val newOffset = strategy.addIndent()
        
        val doc2 = getDocumentForFileObject(indentationDir, fileName.replace(".kt", ".after"))
        val expectedOffset = getCaret(doc2)
        
        assertEquals(expectedOffset, newOffset)
    }
    
    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(indentationDir)
    }
    
    fun testAfterOneOpenBrace() = doTest("afterOneOpenBrace.kt")
    
    fun testBeforeFunctionStart() = doTest("beforeFunctionStart.kt")
    
    fun testBetweenBracesOnDifferentLines() = doTest("betweenBracesOnDifferentLine.kt")
    
    fun testBreakLineAfterIfWithoutBraces() = doTest("breakLineAfterIfWithoutBraces.kt")
    
    fun testAfterOperatorIfWithoutBraces() = doTest("afterOperatorIfWithoutBraces.kt")
    
    fun testAfterOperatorWhileWithoutBraces() = doTest("afterOperatorWhileWithoutBraces.kt")
    
    fun testBeforeCloseBrace() = doTest("beforeCloseBrace.kt")
    
    fun testContinuationAfterDotCall() = doTest("continuationAfterDotCall.kt")
    
    fun testContinuationBeforeFunName() = doTest("continuationBeforeFunName.kt")
    
    fun testBeforeNestedCloseBrace() = doTest("beforeNestedCloseBrace.kt")
    
    fun testBeforeTwiceNestedCloseBrace() = doTest("beforeTwiceNestedCloseBrace.kt")
    
    fun testAfterEquals() = doTest("afterEquals.kt")
    
    fun testIndentBeforeWhile() = doTest("indentBeforeWhile.kt")
    
    fun testLineBreakSaveIndent() = doTest("lineBreakSaveIndent.kt")
    
    fun testNestedOperatorsWithBraces() = doTest("nestedOperatorsWithBraces.kt")
    
    fun testNestedOperatorsWithoutBraces() = doTest("nestedOperatorsWithoutBraces.kt")
    
    fun testNewLineInParameters() = doTest("newLineInParameters.kt")
    
    fun testNewLineWhenCaretAtPosition0() = doTest("newLineWhenCaretAtPosition0.kt")
    
}