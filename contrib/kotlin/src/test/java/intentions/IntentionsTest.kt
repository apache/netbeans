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
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.hints.intentions.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser

class IntentionsTest : KotlinTestCase("Intentions test", "intentions") {

    private fun doTest(fileName: String, intention: Class<out ApplicableIntention>, applicable: Boolean = true) {
        val caret = getCaret(getDocumentForFileObject(dir, "$fileName.caret"))
        assertNotNull(caret)

        val doc = getDocumentForFileObject(dir, "$fileName.kt")
        val file = dir.getFileObject("$fileName.kt")
        val ktFile = KotlinPsiManager.getParsedFile(file)!!

        val resultWithProvider = KotlinParser.getAnalysisResult(ktFile, project)!!

        val psi = ktFile.findElementAt(caret) ?: assert(false)

        val applicableIntention = intention.constructors.first().newInstance(doc, resultWithProvider.analysisResult, psi) as ApplicableIntention

        assertEquals(applicable, applicableIntention.isApplicable(caret))

        applicableIntention.implement()
        assertTrue(doc.getText(0, doc.length) equalsWithoutSpaces dir.getFileObject("$fileName.after").asText())
    }

    fun testRemoveEmptyClassBody() = doTest("removeEmptyClassBody", RemoveEmptyClassBodyIntention::class.java)

    fun testToInfix() = doTest("toInfix", ToInfixIntention::class.java)

    fun testSpecifyType() = doTest("specifyType", SpecifyTypeIntention::class.java)

    fun testAddValToConstructorParameter() = doTest("addValToConstructorParameter", AddValToConstructorParameterIntention::class.java)

    fun testChangeReturnType() = doTest("changeReturnType", ChangeReturnTypeIntention::class.java)

    fun testConvertToSealedClass() = doTest("convertToSealedClass", ConvertEnumToSealedClassIntention::class.java)

    fun testConvertPropertyInitializerToGetter() = doTest("convertPropertyInitializerToGetter", ConvertPropertyInitializerToGetterIntention::class.java)

    fun testConvertToBlockBody() = doTest("convertToBlockBody", ConvertToBlockBodyIntention::class.java)

    fun testConvertToStringTemplate() = doTest("convertToStringTemplate", ConvertToStringTemplateIntention::class.java)

    fun testConvertToExpressionBody() = doTest("convertToExpressionBody", ConvertToExpressionBodyIntention::class.java)
 
    fun testConvertTwoComparisonsToRangeCheck() = doTest("convertTwoComparisons", ConvertTwoComparisonsToRangeCheckIntention::class.java)
    
    fun testMergeIfsIntention() = doTest("mergeIfs", MergeIfsIntention::class.java)
    
    fun testRemoveBraces() = doTest("removeBraces", RemoveBracesIntention::class.java)
    
    fun testRemoveParenthesesFromLambdaCall() = doTest("removeParenthesesFromLambdaCall", RemoveEmptyParenthesesFromLambdaCallIntention::class.java)
    
    fun testRemoveEmptyPrimaryConstructor() = doTest("removeEmptyPrimaryConstructor", RemoveEmptyPrimaryConstructorIntention::class.java)
    
    fun testRemoveEmptySecondaryConstructor() = doTest("removeEmptySecondaryConstructor", RemoveEmptySecondaryConstructorIntention::class.java)
   
    fun testRemoveExplicitType() = doTest("removeExplicitType", RemoveExplicitTypeIntention::class.java)
 
    fun testReplaceSizeCheckWithIsNotEmpty() = doTest("replaceSizeCheckWithIsNotEmpty", ReplaceSizeCheckWithIsNotEmptyIntention::class.java)
 
    fun testSplitIf() = doTest("splitIf", SplitIfIntention::class.java)
    
    fun testConvertToConcatenatedString() = doTest("convertToConcatenatedString", ConvertToConcatenatedStringIntention::class.java)

}