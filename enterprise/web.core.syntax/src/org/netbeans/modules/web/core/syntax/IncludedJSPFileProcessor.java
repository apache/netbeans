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

package org.netbeans.modules.web.core.syntax;

import java.util.Collection;
import java.util.Collections;
import javax.swing.text.BadLocationException;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import static org.netbeans.api.jsp.lexer.JspTokenId.JavaCodeType;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
class IncludedJSPFileProcessor extends JSPProcessor {
    private StringBuilder importsDeclarations = new StringBuilder();
    private StringBuilder declarations = new StringBuilder();
    private StringBuilder scriptlets = new StringBuilder();
    private Collection<String> processedIncludes;

    public IncludedJSPFileProcessor(BaseDocument doc, Collection<String> processedFiles) {
        this.doc = doc;
        this.processedIncludes = processedFiles;
    }

    @Override
    protected void renderProcess() throws BadLocationException {
        processIncludes(false, null);

        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence(); //get top level token sequence
        if (!tokenSequence.moveNext()) {
            return; //no tokens in token sequence
        }

        /**
         * process java code blocks one by one
         * note: We count on the fact the scripting language in JSP is Java
         */
        do {
            Token token = tokenSequence.token();

            if (token.id() == JspTokenId.SCRIPTLET) {
                JavaCodeType blockType = (JavaCodeType) token.getProperty(JspTokenId.SCRIPTLET_TOKEN_TYPE_PROPERTY);
                StringBuilder buff = blockType == JavaCodeType.DECLARATION ? declarations : scriptlets;

                if (blockType != JavaCodeType.EXPRESSION) {
                    buff.append(token.text() + "\n"); //NOI18N
                }
            }
        } while (tokenSequence.moveNext());


        importsDeclarations.append(createImplicitImportStatements(Collections.<String>emptyList()));
        // no need to do it, the JSP parser will return the beans for the including page
        //declarations.append(createBeanVarDeclarations());
    }

    public String getDeclarations() {
        return declarations.toString();
    }

    public String getImports() {
        return importsDeclarations.toString();
    }

    public String getScriptlets() {
        return scriptlets.toString();
    }

    @Override
    protected void processIncludedFile(IncludedJSPFileProcessor includedJSPFileProcessor) {
        declarations.append(includedJSPFileProcessor.getDeclarations());
        includedJSPFileProcessor.getImports();
    }

    @Override
    protected Collection<String> processedIncludes() {
        return processedIncludes;
    }


}
