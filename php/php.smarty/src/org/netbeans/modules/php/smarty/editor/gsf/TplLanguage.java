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
package org.netbeans.modules.php.smarty.editor.gsf;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.parser.TplParser;

@LanguageRegistration(mimeType=TplDataLoader.MIME_TYPE, useCustomEditorKit=true) //NOI18N
public class TplLanguage extends DefaultLanguageConfig {

    public TplLanguage() {
    }

    @Override
    public CommentHandler getCommentHandler() {
        return null;
    }

    @Override
    public Language getLexerLanguage() {
        return TplTopTokenId.language();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isLetter(c);
    }

    @Override
    public String getDisplayName() {
        return "TPL";
    }

    @Override
    public String getPreferredExtension() {
        return "tpl"; // NOI18N
    }

    // Service registrations
    @Override
    public boolean isUsingCustomEditorKit() {
        return true;
    }

    @Override
    public Parser getParser() {
        return new TplParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new TplStructureScanner();
    }

    @Override
    public boolean hasHintsProvider() {
        return false;
    }

}
