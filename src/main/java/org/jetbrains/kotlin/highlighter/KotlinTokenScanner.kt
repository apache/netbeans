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
package org.jetbrains.kotlin.highlighter

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.highlighter.netbeans.KotlinToken
import org.jetbrains.kotlin.highlighter.netbeans.KotlinTokenId
import org.jetbrains.kotlin.psi.KtFile
import org.netbeans.spi.lexer.LexerInput

class KotlinTokenScanner(val input: LexerInput?, text: String? = null) {

    private val kotlinTokensFactory = KotlinTokensFactory()
    private val ktFile: KtFile?
    val tokens = arrayListOf<KotlinToken<KotlinTokenId>>()
    
    init {
        if (text == null) {
            ktFile = KotlinPsiManager.INSTANCE.getParsedKtFileForSyntaxHighlighting(getTextToParse())
        } else {
            ktFile = KotlinPsiManager.INSTANCE.getParsedKtFileForSyntaxHighlighting(text)
        }
        createListOfKotlinTokens()
    }
    
    constructor(text: String) : this(null, text)
    
    private fun getTextToParse(): String {
        if (input == null) return ""
        val builder = StringBuilder()
        var character: Int
        
        do {
            character = input.read()
            builder.append(character.toChar())
        } while (character != LexerInput.EOF)
        
        input.backup(input.readLengthEOF())
        return builder.toString()
    }
    
    private fun createListOfKotlinTokens() {
        tokens.clear()
        if (ktFile == null) return
        
        var lastElement: PsiElement?
        var offset = 0
        
        while(true) {
            lastElement = ktFile.findElementAt(offset)
            if (lastElement != null) {
                offset = lastElement.textRange.endOffset
                val tokenType = kotlinTokensFactory.getToken(lastElement)
                tokens.add(KotlinToken(
                        KotlinTokenId(tokenType.name, tokenType.name, tokenType.getId()),
                        lastElement.text, tokenType))
            } else {
                tokens.add(KotlinToken(
                        KotlinTokenId(TokenType.EOF.name, TokenType.EOF.name, TokenType.EOF.getId()),
                        "", TokenType.EOF))
                break
            }
        }
    }
    
    fun getNextToken(): KotlinToken<KotlinTokenId>? {
        if (input == null) return null
        if (tokens.isNotEmpty()) {
            var ktToken = tokens.first()
            tokens.removeAt(0)
            var tokenLength = ktToken.length()
            while (tokenLength > 0) {
                input.read()
                tokenLength--
            }
            return ktToken
        } else {
            input.read()
            return KotlinToken(KotlinTokenId(TokenType.EOF.name, TokenType.EOF.name, TokenType.EOF.getId()),
                    "", TokenType.EOF)
        }
    }
}