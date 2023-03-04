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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.gsf;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.twig.editor.completion.TwigCompletionHandler;
import org.netbeans.modules.php.twig.editor.format.TwigFormatter;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;
import org.netbeans.modules.php.twig.editor.parsing.TwigParser;

@LanguageRegistration(mimeType = TwigLanguage.TWIG_MIME_TYPE, useCustomEditorKit = true)
public class TwigLanguage extends DefaultLanguageConfig {

    public static final String TWIG_MIME_TYPE = "text/x-twig"; // NOI18N
    public static final String TWIG_BLOCK_MIME_TYPE = "text/x-twig-block"; // NOI18N
    public static final String TWIG_VARIABLE_MIME_TYPE = "text/x-twig-variable"; // NOI18N

    public TwigLanguage() {
    }

    @Override
    public CommentHandler getCommentHandler() {
        return null;
    }

    @Override
    public Language getLexerLanguage() {
        return TwigTopTokenId.language();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isLetter(c);
    }

    @Override
    public String getDisplayName() {
        return "Twig"; //NOI18N
    }

    @Override
    public String getPreferredExtension() {
        return "twig"; //NOI18N
    }

    // Service registrations
    @Override
    public boolean isUsingCustomEditorKit() {
        return true;
    }

    @Override
    public Parser getParser() {
        return new TwigParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new TwigStructureScanner();
    }

    @Override
    public boolean hasHintsProvider() {
        return false;
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new TwigCompletionHandler();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new TwigFormatter();
    }
}
