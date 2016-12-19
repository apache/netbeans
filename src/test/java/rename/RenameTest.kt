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
package rename

import com.intellij.psi.PsiElement
import javaproject.JavaProject
import javax.swing.text.Document
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.refactorings.rename.getTransaction
import org.jetbrains.kotlin.refactorings.rename.getRenameRefactoringMap
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.refactoring.spi.Transaction
import org.openide.filesystems.FileObject
import utils.getAllKtFilesInFolder
import utils.getCaret
import utils.getDocumentForFileObject

class RenameTest : NbTestCase("Rename Test") {

    private val project: Project
    private val renameDir: FileObject
    
    init {
        project = JavaProject.javaProject
        renameDir = project.projectDirectory.getFileObject("src").getFileObject("rename")
    }
    
    private fun doRefactoring(newName: String, fo: FileObject, psi: PsiElement) {
        val renameMap = getRenameRefactoringMap(fo, psi, newName)
        getTransaction(renameMap, newName, psi.text).commit()
    }
    
    private fun checkTextsEquality(actual: FileObject, supposed: FileObject) {
        val beforeDoc = getDocumentForFileObject(actual)
        val afterDoc = getDocumentForFileObject(supposed)
        
        val actualText = beforeDoc.getText(0, beforeDoc.length)
        val supposedText = afterDoc.getText(0, afterDoc.length)
        
        assertNotNull(actualText)
        assertNotNull(supposedText)
        
        assertEquals(supposedText, actualText)
    }
    
    private fun doTest(pack: String, name: String, newName: String) {
        val packFile = renameDir.getFileObject(pack)
        val before = packFile.getFileObject(name + ".kt")
        val beforeWithCaret = packFile.getFileObject(name + ".caret")
        
        val caretOffset = getCaret(getDocumentForFileObject(beforeWithCaret))
        assertNotNull(caretOffset)
        
        val ktFile = KotlinPsiManager.getParsedFile(before)
        assertNotNull(ktFile)
        
        val psi = ktFile!!.findElementAt(caretOffset)
        assertNotNull(psi)
        
        doRefactoring(newName, before, psi!!)
        
        for (file in getAllKtFilesInFolder(packFile)) {
            val fileName = file.name
            val afterFile = packFile.getFileObject(fileName + ".after")
            assertNotNull(afterFile)
            
            checkTextsEquality(file, afterFile)
        }
    }
    
    fun testSimpleCase() = doTest("simple", "file", "NewName")
    
    fun testSecondSimpleCase() = doTest("properties", "file", "someValue")
    
    fun testThirdSimpleCase() = doTest("simplesec", "file", "someValue")
    
    fun testFunctionParameterRenaming() = doTest("functionparameter", "file", "someValue")
    
    fun testFunctionRenaming() = doTest("function", "file", "fooFunc")
    
    fun testClassRenaming() = doTest("classrename", "file", "NewName")
    
    fun testMethodRenaming() = doTest("methodrename", "file", "notSoCoolFun")
    
    fun testForLoop() = doTest("forloop", "file", "arg1")
    
    fun testForLoop2() = doTest("forloop2", "file", "arg1")
    
    fun testRenameKotlinClassByConstructorRef() = doTest("classbyconstructor", "file", "KotlinRules")
    
}