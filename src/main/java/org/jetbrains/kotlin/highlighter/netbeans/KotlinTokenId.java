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
package org.jetbrains.kotlin.highlighter.netbeans;

import org.jetbrains.kotlin.language.KotlinLanguageHierarchy;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 * Custom class for NetBeans {@link TokenId} class.
 * @author Александр
 */
public class KotlinTokenId implements TokenId {

    private final String name;
    private final String primaryCategory;
    private final int id;
    
    /**
     * Constructor.
     * @param name token name.
     * @param primaryCategory token category.
     * @param id token id.
     */
    public KotlinTokenId(String name, String primaryCategory, int id){
        this.name = name;
        this.primaryCategory = primaryCategory;
        this.id = id;
    }
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public int ordinal() {
        return id;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public static Language<KotlinTokenId> getLanguage(){
        return new KotlinLanguageHierarchy().language();
    }
    
}
