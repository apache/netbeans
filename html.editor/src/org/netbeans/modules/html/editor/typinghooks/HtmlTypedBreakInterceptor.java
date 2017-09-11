/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
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
            int from = Utilities.getRowStart(doc, offset);
            int to = Utilities.getRowEnd(doc, offset);
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
