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
package org.jetbrains.kotlin.highlighter.netbeans

import org.jetbrains.kotlin.language.KotlinLanguageHierarchy
import org.netbeans.api.lexer.Language
import org.netbeans.api.lexer.TokenId

class KotlinTokenId(val tokenName: String, val category: String, val id: Int) : TokenId {
    
    companion object {
        fun getLanguage() = KotlinLanguageHierarchy().language()
    }
    
    override fun name() = tokenName
    override fun ordinal() = id
    override fun primaryCategory() = category
    
}