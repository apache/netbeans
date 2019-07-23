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
package highlighting

import javaproject.JavaProject
import javax.swing.text.Document
import org.jetbrains.kotlin.highlighter.KotlinTokenScanner
import org.jetbrains.kotlin.highlighter.TokenType
import org.jetbrains.kotlin.highlighter.netbeans.KotlinToken
import org.jetbrains.kotlin.highlighter.netbeans.KotlinTokenId
import org.netbeans.api.project.Project
import org.openide.filesystems.FileObject
import utils.*

class HighlightingTest : KotlinTestCase("Highlighting test", "highlighting") {

    fun doTest(fileName: String, vararg types: TokenType) {
        val doc = getDocumentForFileObject(dir, fileName)
        val tokens = KotlinTokenScanner(doc.getText(0, doc.length)).tokens
        assertNotNull(tokens)
        
        val parsedTypes = tokens.map{ it.type }
                .filter{ it != TokenType.EOF && it != TokenType.WHITESPACE 
                        && it != TokenType.UNDEFINED && it != TokenType.IDENTIFIER }
        
        
        assertEquals(parsedTypes, types.asList())
    }
    
    fun testBlockComment() = doTest("blockComment.kt", TokenType.MULTI_LINE_COMMENT)
    
    fun testForKeyword() = doTest("forKeyword.kt", TokenType.KEYWORD)
    
    fun testFunction() = doTest("function.kt", TokenType.KEYWORD, TokenType.KEYWORD)
    
    fun testGetterSetter() {
        doTest("getterSetter.kt", TokenType.KEYWORD, TokenType.KEYWORD, 
                TokenType.KEYWORD, TokenType.KEYWORD)
    }
    
    fun testCompanionObject() = doTest("companionObject.kt", TokenType.KEYWORD, TokenType.KEYWORD, TokenType.KEYWORD)
    
    fun testImportKeyword() = doTest("importKeyword.kt", TokenType.KEYWORD)
    
    fun testInKeyword() = doTest("inKeyword.kt", TokenType.KEYWORD)
    
    fun testInterfaceKeyword() = doTest("interfaceKeyword.kt", TokenType.KEYWORD)
    
    fun testKeywordWithText() = doTest("keywordWithText.kt", TokenType.KEYWORD)
    
    fun testOpenKeyword() = doTest("openKeyword.kt", TokenType.KEYWORD, TokenType.KEYWORD)
    
    fun testSingleLineComment() = doTest("singleLineComment.kt", TokenType.SINGLE_LINE_COMMENT)
    
    fun testSoftImportKeyword() = doTest("softImportKeyword.kt", TokenType.KEYWORD)
    
    fun testSoftKeywords() { 
        doTest("softKeywords.kt", TokenType.KEYWORD, TokenType.KEYWORD,
                TokenType.KEYWORD, TokenType.KEYWORD)
    }
    
    fun testStringInterpolation() {
        doTest("stringInterpolation.kt", TokenType.KEYWORD, TokenType.STRING,
                TokenType.STRING, TokenType.STRING)
    }
    
    fun testStringToken() = doTest("stringToken.kt", TokenType.STRING, TokenType.STRING, TokenType.STRING)
    
    fun testTextWithTokenInside() = doTest("textWithTokenInside.kt")
    
}