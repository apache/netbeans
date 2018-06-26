/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor.indent;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;

/**
 *
 * @author Roman Svitanic
 */
public class JadeFormatter implements Formatter {

    private static final int INDENT_SIZE = 4; // XXX should be an option
    private static final int CONTINUATION_SIZE = 8; // XXX should be an option
    private static final Logger LOG = Logger.getLogger(JadeFormatter.class.getName());

    public JadeFormatter() {
        LOG.log(Level.FINE, "Jade Formatter: {0}", this.toString()); //NOI18N
    }

    @Override
    public void reformat(Context context, ParserResult compilationInfo) {
        // leave formatting as is
    }

    @Override
    public void reindent(Context context) {
        String mimeType = getMimeTypeAtOffset(context.document(), context.startOffset());
        String lineEndMimeType = getMimeTypeAtOffset(context.document(), context.endOffset() - 1);
        if (JadeTokenId.JADE_MIME_TYPE.equals(mimeType) && mimeType.equals(lineEndMimeType)) {
            IndentationCounter.Indentation indent = new IndentationCounter((BaseDocument) context.document()).count(context.caretOffset());
            indent.modify(context);
        }
    }

    @Override
    public boolean needsParserResult() {
        return false;
    }

    @Override
    public int indentSize() {
        return INDENT_SIZE;
    }

    @Override
    public int hangingIndentSize() {
        return CONTINUATION_SIZE;
    }

    private static String getMimeTypeAtOffset(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsl = th.embeddedTokenSequences(offset, false);
        if (tsl != null && tsl.size() > 0) {
            TokenSequence<?> tokenSequence = tsl.get(tsl.size() - 1);
            return tokenSequence.language().mimeType();
        }
        return null;
    }

}
