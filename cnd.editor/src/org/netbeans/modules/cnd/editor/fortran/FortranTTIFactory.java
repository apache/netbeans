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
package org.netbeans.modules.cnd.editor.fortran;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.fortran.indent.FortranHotCharIndent;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.util.Exceptions;

/**
 *
 */
@MimeRegistration(mimeType = MIMENames.FORTRAN_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
public class FortranTTIFactory implements TypedTextInterceptor.Factory {

    @Override
    public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
        assert mimePath.getPath().equals(MIMENames.FORTRAN_MIME_TYPE);
        return new TypedTextInterceptorImpl();
    }

    private static class TypedTextInterceptorImpl implements TypedTextInterceptor {
        private int caretPosition;

        public TypedTextInterceptorImpl() {
        }

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            caretPosition = -1;
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
            BaseDocument doc = (BaseDocument) context.getDocument();
            if (!FortranBracketCompletion.INSTANCE.completionSettingEnabled()) {
                return;
            }
            char insertedChar = context.getText().charAt(0);
            switch(insertedChar) {
                case '(':
                        FortranBracketCompletion.INSTANCE.completeOpeningBracket(context);
                    break;
                case ')':
                    caretPosition = FortranBracketCompletion.INSTANCE.skipClosingBracket(context);
                    break;
            }
        }

        @Override
        public void afterInsert(final Context context) throws BadLocationException {
            final BaseDocument doc = (BaseDocument) context.getDocument();
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {
                    int offset = context.getOffset();
                    if (FortranHotCharIndent.INSTANCE.getKeywordBasedReformatBlock(doc, offset, context.getText())) {
                        Indent indent = Indent.get(doc);
                        indent.lock();
                        try {
                            doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
                            indent.reindent(offset);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
                            indent.unlock();
                        }
                    } else if (caretPosition != -1) {
                        context.getComponent().setCaretPosition(caretPosition);
                        caretPosition = -1;
                    } else {
                        try {
                            FortranBracketCompletion.INSTANCE.charInserted(doc,
                                    offset, context.getComponent().getCaret(), context.getText().charAt(0));
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }

        @Override
        public void cancelled(Context context) {
        }
    }
}
