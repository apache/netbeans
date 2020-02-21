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

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.openide.util.Exceptions;

/**
 *
 */
@MimeRegistrations({
    // cnd source files
    @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.DOXYGEN_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_DOUBLE_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_SINGLE_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.PREPROC_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
})
public class CppDTIFactory implements DeletedTextInterceptor.Factory {

    @Override
    public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
        return new DeletedTextInterceptorImpl();
    }

    private static class DeletedTextInterceptorImpl implements DeletedTextInterceptor {
        private CppTypingCompletion.ExtraText rawStringExtraText;

        @Override
        public boolean beforeRemove(final Context context) throws BadLocationException {
            final CppTypingCompletion.ExtraText[] res = new CppTypingCompletion.ExtraText[]{null};
            BaseDocument doc = (BaseDocument) context.getDocument();
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {
                    try {
                        res[0] = CppTypingCompletion.checkRawStringRemove(context);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            rawStringExtraText = res[0];
            return false;
        }

        @Override
        public void remove(Context context) throws BadLocationException {
            if (rawStringExtraText != null) {
                BaseDocument doc = (BaseDocument) context.getDocument();
                String extraText = rawStringExtraText.getExtraText();
                if (extraText != null) {
                    doc.remove(rawStringExtraText.getExtraTextPostion(), extraText.length());
                }
            } else {
                if (context.isBackwardDelete()) {
                    BracketCompletion.charBackspaced((BaseDocument) context.getDocument(), context.getOffset() - 1, context.getText().charAt(0));
                }
            }
        }

        @Override
        public void afterRemove(Context context) throws BadLocationException {
        }

        @Override
        public void cancelled(Context context) {
        }
    }
}
