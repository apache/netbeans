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
package org.netbeans.modules.java.hints.infrastructure;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorPositionRefresherHelper.DocumentVersionImpl;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper.DocumentVersion;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="text/x-java", service=PositionRefresherHelper.class)
public class ErrorPositionRefresherHelper extends PositionRefresherHelper<DocumentVersionImpl> {
    public ErrorPositionRefresherHelper() {
        super(ErrorHintsProvider.class.getName());
    }
    
    @Override
    protected boolean isUpToDate(Context context, Document doc, DocumentVersionImpl oldVersion) {
        List<ErrorDescription> errors = oldVersion.errorsContent;

        for (ErrorDescription ed : errors) {
            if (ed.getRange().getBegin().getOffset() <= context.getPosition() && context.getPosition() <= ed.getRange().getEnd().getOffset()) {
                if (!ed.getFixes().isComputed()) return false;
            }
        }

        return true;
    }

    @Override
    public List<ErrorDescription> getErrorDescriptionsAt(CompilationInfo info, Context context, Document doc) throws Exception {
        DocumentVersionImpl upToDateDocumentVersion = getUpToDateDocumentVersion(context, doc);
        List<ErrorDescription> errors = upToDateDocumentVersion != null ? upToDateDocumentVersion.errorsContent : null;

        if (errors == null) {
            errors = new ErrorHintsProvider().computeErrors(info, doc, context.getPosition(), org.netbeans.modules.java.hints.errors.Utilities.JAVA_MIME_TYPE);
        }
        // see #242116; the ErrorHintsProvider might call cancel on itself when doc is closed or changes in some ways and then returns null
        if (errors == null) {
            return null;
        }
        for (ErrorDescription ed : errors) {
            if (ed.getRange().getBegin().getOffset() <= context.getPosition() && context.getPosition() <= ed.getRange().getEnd().getOffset()) {
                if (!ed.getFixes().isComputed()) {
                    ((CreatorBasedLazyFixList) ed.getFixes()).compute(info, context.getCancel());
                }
            }
        }

        return errors;
    }

    static void setVersion(Document doc, List<ErrorDescription> errors) {
        for (PositionRefresherHelper h : MimeLookup.getLookup("text/x-java").lookupAll(PositionRefresherHelper.class)) {
            if (h instanceof ErrorPositionRefresherHelper) {
                ((ErrorPositionRefresherHelper) h).setVersion(doc, new DocumentVersionImpl(doc, errors));
            }
        }
    }

    static final class DocumentVersionImpl extends DocumentVersion {

        private final List<ErrorDescription> errorsContent;

        public DocumentVersionImpl(Document doc, List<ErrorDescription> errorsContent) {
            super(doc);
            this.errorsContent = errorsContent;
        }
    }
}
