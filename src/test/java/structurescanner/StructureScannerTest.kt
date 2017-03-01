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
import org.jetbrains.kotlin.structurescanner.KotlinStructureScanner
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.openide.filesystems.FileObject

class StructureScannerTest : NbTestCase("StructureScanner test") {
    
    private val project: Project
    private val structureScannerDir: FileObject

    init {
        project = JavaProject.javaProject
        structureScannerDir = project.projectDirectory.getFileObject("src").getFileObject("structureScanner")
    }
    
    private fun doTest(fileName: String, expectedNumOfItems: Int) {
        val file = structureScannerDir.getFileObject("$fileName.kt")
        assertNotNull(file)
        
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        val resultWithProvider = KotlinAnalyzer.analyzeFile(project, ktFile)
        
        val items = KotlinStructureScanner().structureItems(file, resultWithProvider.analysisResult.bindingContext)
        assertEquals(items.size, expectedNumOfItems)
    }
    
    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(structureScannerDir)
    }
    
    fun testSimple() = doTest("simple", 1)
    
    fun testSeveralFunctions() = doTest("severalFunctions", 2)
    
}