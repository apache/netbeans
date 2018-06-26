/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
