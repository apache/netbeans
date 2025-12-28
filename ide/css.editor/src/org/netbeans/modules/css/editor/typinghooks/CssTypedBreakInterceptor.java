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
package org.netbeans.modules.css.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 */
public class CssTypedBreakInterceptor implements TypedBreakInterceptor {

    private static final String TWO_CURLY_BRACES_IMAGE = "{}"; //NOI18N

    private Position[] reformat;

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        int offset = context.getBreakInsertOffset();
        BaseDocument doc = (BaseDocument) context.getDocument();
        if (offset > 0 && offset < doc.getLength()) { //check corners
            String text = doc.getText(offset - 1, 2); //get char before and after
            if (TWO_CURLY_BRACES_IMAGE.equals(text)) { //NOI18N
                //context.setText("\n\n", 1, 1, 0, 2);
                //reformat workaround -- the preferred 
                //won't work as the reformatter will not reformat the line with the closing tag
                int from = LineDocumentUtils.getLineStartOffset(doc, offset);
                int to = LineDocumentUtils.getLineEndOffset(doc, offset);
                reformat = new Position[]{doc.createPosition(from), doc.createPosition(to)};
                context.setText("\n\n", 1, 1);
            }
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        if (reformat != null) {
            final Position[] range = reformat;
            reformat = null;
            BaseDocument doc = (BaseDocument) context.getDocument();
            final Indent indent = Indent.get(doc);
            indent.lock();
            try {
                doc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            indent.reindent(range[0].getOffset(), range[1].getOffset());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                });
            } finally {
                indent.unlock();
            }
        }
    }

    @Override
    public void cancelled(Context context) {
    }

    @MimeRegistration(mimeType = "text/css", service = TypedBreakInterceptor.Factory.class)
    public static final class Factory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new CssTypedBreakInterceptor();
        }

    }
}
