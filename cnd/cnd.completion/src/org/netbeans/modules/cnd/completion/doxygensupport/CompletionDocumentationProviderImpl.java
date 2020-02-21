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
