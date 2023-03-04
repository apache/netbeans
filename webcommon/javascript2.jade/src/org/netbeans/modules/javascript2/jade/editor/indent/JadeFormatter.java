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
