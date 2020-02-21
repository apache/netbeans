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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.completion.doxygensupport;

import java.io.IOException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.text.Document;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils;
import org.netbeans.modules.cnd.completion.doxygensupport.DoxygenDocumentation.CompletionDocumentationImpl;
import org.netbeans.modules.cnd.spi.model.services.CsmDocProvider;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = CsmDocProvider.class)
public class CompletionDocumentationProviderImpl implements CsmDocProvider {

    @Override
    public CompletionDocumentation createDocumentation(CsmObject csmObject, CsmFile csmFile) {
        return createDocumentationImpl(csmObject, csmFile);
    }

    @Override
    public CharSequence getDocumentation(CsmObject obj, CsmFile file) {
        CompletionDocumentation doc = createDocumentationImpl(obj, file);
        return doc.getText();
    }

    @Override
    public CompletionTask createDocumentationTask(CsmObject csmObject) {
        if (csmObject == null) {
            return null;
        }

        CsmFile csmFile = getCsmFile(csmObject);
        return csmFile == null? null : new AsyncCompletionTask(new DocQuery(csmObject, csmFile));
    }

    private static CsmFile getCsmFile(CsmObject csmObject) {
        CsmFile csmFile = null;
        if (CsmKindUtilities.isOffsetable(csmObject)) {
            CsmOffsetable csmOffsetable = (CsmOffsetable) csmObject;
            csmFile = csmOffsetable.getContainingFile();
        }
        return csmFile;
    }

    private static CompletionDocumentation createDocumentationImpl(CsmObject obj, CsmFile file) {
        String errorText = null;
        CompletionDocumentationImpl codeDocumentation = null;
        CsmCompletionUtils.DocProviderList docProviderList = CsmCompletionUtils.getDocProviderList();
        for(CsmCompletionUtils.DocProvider provider : docProviderList.getProviders()) {
            if (docProviderList.isEnabled(provider)) {
                switch (provider) {
                    case SourceCode:{
                        codeDocumentation = DoxygenDocumentation.create(obj);
                        if (codeDocumentation != null && codeDocumentation.getKind() == CppTokenId.DOXYGEN_COMMENT) {
                            return codeDocumentation;
                        }
                        break;
                    }
                    case Manual: {
                        CompletionDocumentation manDocumentation = null;
                        try {
                            manDocumentation = ManDocumentation.getDocumentation(obj, file);
                        } catch (IOException ioe) {
                            errorText = ioe.getMessage();
                        }
                        if (manDocumentation != null) {
                            return manDocumentation;
                        }
                        break;
                    }
                }
            }
        }
        if (codeDocumentation != null) {
            return codeDocumentation;
        }
        StringBuilder w = new StringBuilder();
        w.append("<p>").append(getString("NO_DOC_FOUND")).append("</p>"); // NOI18N
        if (errorText != null) {
            w.append("<p>").append(errorText).append("</p>"); // NOI18N
        }
        return new EmptyCompletionDocumentationImpl(w.toString());
    }

    private static class DocQuery extends AsyncCompletionQuery {

        private final CsmObject obj;
        private final CsmFile file;

        public DocQuery(CsmObject obj, CsmFile file) {
            this.obj = obj;
            this.file = file;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CompletionDocumentation documentation = createDocumentationImpl(obj, file);

            if (documentation != null) {
                resultSet.setDocumentation(documentation);
            }

            resultSet.finish();
        }
    }

    private static final class EmptyCompletionDocumentationImpl implements CompletionDocumentation {

        private final String text;

        public EmptyCompletionDocumentationImpl(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    private static String getString(String s) {
        return NbBundle.getMessage(CompletionDocumentationProviderImpl.class, s);
    }
}
