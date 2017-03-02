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
package semantic

import javaproject.JavaProject
import javax.swing.text.Document
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.netbeans.modules.csl.api.ColoringAttributes
import org.netbeans.modules.csl.api.OffsetRange
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.highlighter.semanticanalyzer.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.utils.ProjectUtils
import utils.carets
import utils.getDocumentForFileObject

typealias Attr = KotlinHighlightingAttributes

class SemanticAnalyzerTest : NbTestCase("SemanticAnalyzer test") {
    private val project: Project
    private val semanticDir: FileObject

    init {
        project = JavaProject.javaProject
        semanticDir = project.projectDirectory.getFileObject("src").getFileObject("semantic")
    }
    
    private infix fun Int.to(end: Int) = OffsetRange(this, end)
    
    private fun List<Int>.toOffsetRanges(): List<OffsetRange> {
        val ranges = arrayListOf<OffsetRange>()
        
        var i = 0
        while (i < size - 1) {
            ranges.add(this[i] to this[i + 1])
            i += 2
        }
        
        return ranges
    }
    
    private fun attrs(fileName: String, attrs: List<Attr>, withSmartCast: Boolean = false): Map<OffsetRange, Set<ColoringAttributes>> {
        val doc = getDocumentForFileObject(semanticDir, "$fileName.caret")
        val ranges = doc.carets().toOffsetRanges()
        
        return hashMapOf<OffsetRange, Set<ColoringAttributes>>().apply {
            attrs.forEachIndexed { i, it ->
                put(ranges[i], it.styleKey.let { 
                    if (withSmartCast) it.toMutableSet().apply { addAll(Attr.SMART_CAST.styleKey) } else it
                })
            }
        }
    }
    
    private fun doTest(fileName: String, vararg attrs: Attr, withSmartCast: Boolean = false) {
        val file = semanticDir.getFileObject("$fileName.kt")
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        
        val resultWithProvider = KotlinAnalyzer.analyzeFile(project, ktFile)
        KotlinParser.setAnalysisResult(ktFile, resultWithProvider)
        
        val highlights = KotlinSemanticAnalyzer().let {
            it.highlight(resultWithProvider.analysisResult, ktFile)
            it.highlights
        }
        
        assertTrue(highlights.entries.containsAll(attrs(fileName, attrs.toList(), withSmartCast).entries))
    }    
    
    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(semanticDir)
    }
    
    fun testEmpty() = doTest("empty")
    
    fun testSimpleClass() = doTest("simpleClass", Attr.CLASS)
    
    fun testClass() = doTest("class", Attr.CLASS, Attr.FINAL_FIELD)
    
    fun testFunctionWithLocalVariables() = doTest("functionWithLocalVariables", Attr.FUNCTION_DECLARATION,
            Attr.LOCAL_FINAL_VARIABLE, Attr.LOCAL_VARIABLE)
    
    fun testAnnotation() = doTest("annotation", Attr.ANNOTATION)
    
    fun testDeprecated() = doTest("deprecated", Attr.DEPRECATED)
    
    fun testSmartCast() = doTest("smartCast", Attr.LOCAL_FINAL_VARIABLE, withSmartCast = true)
    
}