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
import org.netbeans.modules.csl.api.StructureItem
import org.openide.filesystems.FileObject
import utils.KotlinTestCase

class StructureScannerTest : KotlinTestCase("StructureScanner test", "structureScanner") {
    
    private val StructureItem.allItems: List<StructureItem> 
        get() = arrayListOf(this).apply { addAll(nestedItems.flatMap { it.allItems }) }
    
    private fun doTest(fileName: String, functions: Int = 0, properties: Int = 0, classes: Int = 0) {
        val file = dir.getFileObject("$fileName.kt")
        assertNotNull(file)
        
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        val resultWithProvider = KotlinAnalyzer.analyzeFile(project, ktFile)
        
        val items = KotlinStructureScanner().structureItems(file, resultWithProvider.analysisResult.bindingContext)
                .flatMap { it.allItems }
        
        assertEquals(functions, items.filterIsInstance<KotlinFunctionStructureItem>().size)
        assertEquals(properties, items.filterIsInstance<KotlinPropertyStructureItem>().size)
        assertEquals(classes, items.filterIsInstance<KotlinClassStructureItem>().size)
    }
    
    fun testEmpty() = doTest("empty")
    
    fun testSimple() = doTest("simple", functions = 1)
    
    fun testSeveralFunctions() = doTest("severalFunctions", functions = 2)
    
    fun testObject() = doTest("object", classes = 1, functions = 1)
    
    fun testClassWithSeveralMembers() = doTest("classWithSeveralMembers", classes = 2, functions = 1, properties = 1)
    
    fun testSeveralClasses() = doTest("severalClasses", classes = 4, functions = 3, properties = 3)
    
}