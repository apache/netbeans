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

package org.netbeans.modules.profiler.oql.language;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author Jan Jancura
 */
@MIMEResolver.ExtensionRegistration(
    displayName="#OQLResolver",
    extension="oql",
    mimeType="text/x-oql",
    position=945
)
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.lexer.LanguageProvider.class)
public class OQLLanguageProvider extends LanguageProvider {
    
    public Language<OQLTokenId> findLanguage (String mimeType) {
        if ("text/x-oql".equals (mimeType))
            return new OQLLanguageHierarchy ().language ();
        return null;
    }

    @Override
    public LanguageEmbedding<?> findLanguageEmbedding (
        Token token,
        LanguagePath arg1,
        InputAttributes arg2
    ) {
        if (token.id() == OQLTokenId.JSBLOCK) {
            Language lang = Language.find("text/javascript");
            if(lang == null) {
                return null; //no language found
            } else {
                return LanguageEmbedding.create(lang, 0, 0, true);
            }
        }
        return null;
    }
}


