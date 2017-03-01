/*******************************************************************************
 * Copyright 2000-2017 JetBrains s.r.o.
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
package structurescanner

import javaproject.JavaProject
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.structurescanner.*
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.netbeans.modules.csl.api.StructureItem
import org.openide.filesystems.FileObject

class FoldingTest : NbTestCase("Folding test") {
    
    private val project: Project
    private val foldingDir: FileObject

    init {
        project = JavaProject.javaProject
        foldingDir = project.projectDirectory.getFileObject("src").getFileObject("folding")
    }
    
    private fun doTest(fileName: String, codeBlocks: Int = 0, comments: Int = 0) {
        val file = foldingDir.getFileObject("$fileName.kt")
        assertNotNull(file)
        
        val folds = KotlinStructureScanner().foldMap(file)
        
        assertEquals(codeBlocks, folds["codeblocks"]?.size)
        assertEquals(comments, folds["comments"]?.size)
    }
    
    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(foldingDir)
    }
    
    fun testLicenseHeader() = doTest("licenseHeader", comments = 1)
    
    fun testFunction() = doTest("function", codeBlocks = 1)
    
    fun testFunctionWithBody() = doTest("functionWithBody", codeBlocks = 2)
    
    fun testClass() = doTest("class", codeBlocks = 6, comments = 2)
    
}