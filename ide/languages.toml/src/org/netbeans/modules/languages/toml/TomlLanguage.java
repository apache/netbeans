/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.languages.toml;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;

/**
 *
 * @author lkishalmi
 */
@LanguageRegistration(mimeType = TomlTokenId.TOML_MIME_TYPE) //NOI18N
public class TomlLanguage  extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return TomlTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "TOML"; //NOI18N
    }

    @Override
    public String getLineCommentPrefix() {
        return "#"; // NOI18N
    }
}
