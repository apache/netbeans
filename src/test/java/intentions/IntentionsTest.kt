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
package intentions

import utils.*
import javaproject.JavaProject
import javax.swing.text.Document
import org.netbeans.api.project.Project
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.hints.intentions.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.utils.ProjectUtils

class IntentionsTest : KotlinTestCase("Intentions test", "intentions") {
    
    private fun doTest(fileName: String, intention: Class<out ApplicableIntention>, applicable: Boolean = true) {
        val caret = getCaret(getDocumentForFileObject(dir, "$fileName.caret"))
        assertNotNull(caret)
        
        val doc = getDocumentForFileObject(dir, "$fileName.kt")
        val file = dir.getFileObject("$fileName.kt")
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        
        val resultWithProvider = KotlinAnalyzer.analyzeFile(project, ktFile)
        KotlinParser.setAnalysisResult(ktFile, resultWithProvider)
        
        val psi = ktFile.findElementAt(caret) ?: assert(false)
        
        val applicableIntention = intention.constructors.first().newInstance(doc, resultWithProvider.analysisResult, psi) as ApplicableIntention
        
        assertEquals(applicable, applicableIntention.isApplicable(caret))
    }
 
    fun testRemoveBraces() = doTest("removeEmptyClassBody", RemoveEmptyClassBodyIntention::class.java)
   
}