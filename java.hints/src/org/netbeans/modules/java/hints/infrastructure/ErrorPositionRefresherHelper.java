/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
