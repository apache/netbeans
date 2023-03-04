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
package org.netbeans.modules.web.core.syntax.gsf;

import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;

@LanguageRegistration(mimeType={"text/x-jsp", "text/x-tag"}, useCustomEditorKit=true) //NOI18N
public class JspLanguage extends DefaultLanguageConfig {
    
    //XXX no line comment in jsp!
    private static final String LINE_COMMENT_PREFIX = "<%--";
    
    public JspLanguage() {
    }

    @Override
    public Language getLexerLanguage() {
        return JspTokenId.language();
    }

    @Override
    public String getLineCommentPrefix() {
        return LINE_COMMENT_PREFIX;
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isLetter(c);
    }

    @Override
    public String getDisplayName() {
        return "JSP";
    }

    @Override
    public String getPreferredExtension() {
        return "jsp"; // NOI18N
    }

    @Override
    public boolean isUsingCustomEditorKit() {
        return true;
    }

    // Service Registrations
    
    @Override
    public Parser getParser() {
        // we need the parser and the StructureScanner to enable navigator of embedded languages
        return new JspGSFParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new JspStructureScanner();
    }

    @Override
    public CommentHandler getCommentHandler() {
        return new JspCommentHandler();
    }

}
