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
package org.netbeans.modules.languages.ini.csl;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.languages.ini.lexer.IniTokenId;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@LanguageRegistration(mimeType = IniLanguageConfig.MIME_TYPE)
public class IniLanguageConfig extends DefaultLanguageConfig {
    public static final String MIME_TYPE = "text/x-ini"; //NOI18N

    @Override
    public Language getLexerLanguage() {
        return IniTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "INI"; //NOI18N
    }

    @Override
    public String getLineCommentPrefix() {
        return ";"; //NOI18N
    }

}
