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
package org.jetbrains.kotlin.diagnostics.netbeans.textinterceptor

import javax.swing.text.Document
import javax.swing.text.JTextComponent
import org.netbeans.modules.csl.api.KeystrokeHandler
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.spi.ParserResult

class KotlinKeystrokeHandler : KeystrokeHandler {
    
    fun checkNextChar(doc: Document, caretOffset: Int, ch: String, 
                      target: JTextComponent): Boolean {
        if (doc.getText(caretOffset, 1) == ch) {
            target.caretPosition = caretOffset + 1
            return true
        }
        return false
    }
    
    fun insert(doc: Document, caretOffset: Int, ch: String,
               target: JTextComponent): Boolean {
        doc.insertString(caretOffset + 1, ch, null)
        target.caretPosition = caretOffset + 1
        return false
    }
    
    override fun beforeCharInserted(doc: Document, caretOffset: Int, 
                                    target: JTextComponent, ch: Char): Boolean {
        if (ch !in arrayOf('"', '\'', ')', '}',']')) return false
        
        return checkNextChar(doc, caretOffset, ch.toString(), target)
    }
    
    override fun afterCharInserted(doc: Document, caretOffset: Int, 
                                    target: JTextComponent, ch: Char): Boolean {
        return when(ch) {
            '(' -> insert(doc, caretOffset, ")", target)
            '{' -> insert(doc, caretOffset, "}", target)
            '[' -> insert(doc, caretOffset, "]", target)
            '"' -> insert(doc, caretOffset, "\"", target)
            '\'' -> insert(doc, caretOffset, "'", target)
            else -> false
        }
    }
    
    override fun charBackspaced(doc: Document, caretOffset: Int, target: JTextComponent, ch: Char) = false
    override fun beforeBreak(doc: Document, caretOffset: Int, target: JTextComponent) = -1
    override fun findMatching(doc: Document, caretOffset: Int) = OffsetRange.NONE
    override fun findLogicalRanges(info: ParserResult, caretOffset: Int) = emptyList<OffsetRange>()
    override fun getNextWordOffset(doc: Document, caretOffset: Int, reverse: Boolean) = -1
}