/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.knockout.api;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.html.knockout.KODataBindLanguageHierarchy;
import org.netbeans.modules.html.knockout.KOUtils;

/**
 * key: value, key: value, ...
 *
 * @author Marek Fukala
 */
public enum KODataBindTokenId implements TokenId {
    
    KEY("key"),
    VALUE("value"),
    COLON("operator"),
    COMMA("operator"),
    ERROR("error"),
    WS("whitespace"); //NOI18N

    private static final Language<KODataBindTokenId> language = new KODataBindLanguageHierarchy().language();
    
    @MimeRegistration(mimeType = KOUtils.KO_DATA_BIND_MIMETYPE, service = Language.class)
    public static Language<KODataBindTokenId> language() {
        return language;
    }
    
    private String category;

    private KODataBindTokenId(String category) {
        this.category = category;
    }
    
    @Override
    public String primaryCategory() {
        return category;
    }
    
}
