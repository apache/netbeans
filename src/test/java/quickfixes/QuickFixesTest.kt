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
package quickfixes

import org.jetbrains.kotlin.utils.ProjectUtils
import utils.*
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.diagnostics.netbeans.parser.*
import org.jetbrains.kotlin.hints.fixes.*

class QuickFixesTest : KotlinTestCase("Quick Fixes test", "quickfixes") {
    
    private fun doTest(fileName: String, fix: Class<out KotlinQuickFix>) {
        val doc = getDocumentForFileObject(dir, "$fileName.kt")
        val file = dir.getFileObject("$fileName.kt")
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        val project = ProjectUtils.getKotlinProjectForFileObject(file)
        
        val resultWithProvider = KotlinParser.getAnalysisResult(ktFile, project)!!
        val parserResult = KotlinParserResult(null, resultWithProvider, ktFile, file, project)
        
        val error = parserResult.getDiagnostics().first()
        val quickFix = fix.constructors.first().newInstance(error, parserResult) as KotlinQuickFix
        
        assertTrue(quickFix.isApplicable())
        
        quickFix.implement()
        assertTrue(doc.getText(0, doc.length) equalsWithoutSpaces dir.getFileObject("$fileName.after").asText())
    }
    
    fun testRemoveUnnecessarySafeCall() = doTest("removeUnnecessaryCall", RemoveUnnecessarySafeCallFix::class.java)
    
    fun testRemoveUselessCastFix() = doTest("removeUselessCast", RemoveUselessCastFix::class.java)
    
    fun testRemoveUselessElvisFix() = doTest("removeUselessElvis", RemoveUselessElvisFix::class.java)
    
    fun testImplementMembersFix() = doTest("implementMembers", ImplementMembersFix::class.java)
    
}