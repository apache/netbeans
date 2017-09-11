/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.csl.core;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

public class GsfIndentTask implements IndentTask {

    private Context context;
    private Formatter formatter;
    
    GsfIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        //Formatter f = getFormatter();

        Formatter f = null;
        BaseDocument baseDoc = (BaseDocument)context.document();
        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, context.startOffset());
        if (context.endOffset()-context.startOffset() < 4) { // for line reindents etc.
            for (Language l : list) {
                if (l.getFormatter() != null) {
                    f = l.getFormatter();
                    break;
                }
            }
        }
        
        if (f == null) {
            f = getFormatter();
        }
        
        
        if (f != null) {
            f.reindent(context);
        }
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    private synchronized Formatter getFormatter() {
        if(formatter == null) {
            // XXX: Carefull here, generally context.mimePath() != mimeType. This
            // task's factory was created for a top level language (mimeType), but the task
            // itself can be used for an embedded language.
            // If the task is used for the document itself (not an embedded
            // section) Context.mimePath() == mimeType.
            // However, if it is used for an embedded section the Context.mimePath() gives
            // the mime path (languege path) of that section. Which is generally
            // something like 'application/x-httpd-eruby/text/x-ruby'. While the
            // task was registered for 'text/x-ruby'.
            // Therefore with the __current__ implementation of MimeLookupInitializerImpl
            // we can simply take the last component of Context.mimePath().
            MimePath mimePath = MimePath.parse(context.mimePath());
            String mimeType = mimePath.size() > 1 ? mimePath.getMimeType(mimePath.size() - 1) : mimePath.getPath();
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            formatter = language.getFormatter();
        }
        return formatter;
    }
}
