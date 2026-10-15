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
package org.netbeans.modules.html.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 */
public class HtmlTypedBreakInterceptor implements TypedBreakInterceptor {

    private Position[] reformat;
    
    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    //called *before* line break is inserted
    @Override
    public void insert(MutableContext context) throws BadLocationException {
        BaseDocument doc = (BaseDocument) context.getDocument();
        int offset = context.getBreakInsertOffset();
        
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument)doc, offset, HTMLTokenId.language());
        if (ts == null) {
            return;
        }
        ts.move(offset);
        String closingTagName = null;
        int end = -1;
        if (ts.moveNext() && ts.token().id() == HTMLTokenId.TAG_OPEN_SYMBOL &&
                ts.token().text().toString().equals("</")) {
            if (ts.moveNext() && ts.token().id() == HTMLTokenId.TAG_CLOSE) {
                closingTagName = ts.token().text().toString();
                end = ts.offset()+ts.token().text().length();
                ts.movePrevious();
                ts.movePrevious();
            }
        }
        if (closingTagName == null) {
            return;
        }
        boolean foundOpening = false;
        if (ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL &&
                ts.token().text().toString().equals(">")) {
            while (ts.movePrevious()) {
                if (ts.token().id() == HTMLTokenId.TAG_OPEN) {
                    if (ts.token().text().toString().equals(closingTagName)) {
                        foundOpening = true;
                    }
                    break;
                }
            }
        }
        if (foundOpening) {
            context.setText("\n\n", 1, 1);
            //reformat workaround -- the preferred 
            //context.setText("\n\n", 1, 1, 0, 2);
            //won't work as the reformatter will not reformat the line with the closing tag
            int from = LineDocumentUtils.getLineStartOffset(doc, offset);
            int to = LineDocumentUtils.getLineEndOffset(doc, offset);
            reformat = new Position[]{doc.createPosition(from), doc.createPosition(to)};
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        if(reformat != null) {
            final Position[] range = reformat;
            reformat = null;
            BaseDocument doc = (BaseDocument)context.getDocument();
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
        //no-op
    }
    
     @MimeRegistration(mimeType = "text/html", service = TypedBreakInterceptor.Factory.class)
    public static final class Factory implements TypedBreakInterceptor.Factory {
        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new HtmlTypedBreakInterceptor();
        }
    }
    
}
