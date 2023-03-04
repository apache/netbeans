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
package org.netbeans.modules.php.editor.indent;

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
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.indent.IndentationCounter.Indentation;

/**
 * Formatting and indentation for PHP.
 *
 * @author Tor Norbye
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPFormatter implements Formatter {

    private static final Logger LOG = Logger.getLogger(PHPFormatter.class.getName());

    public PHPFormatter() {
        LOG.log(Level.FINE, "PHP Formatter: {0}", this.toString()); //NOI18N
    }

    @Override
    public boolean needsParserResult() {
        return true;
    }

    @Override
    public void reindent(final Context context) {
        String mimeType = getMimeTypeAtOffset(context.document(), context.startOffset());
        String mimePath = context.mimePath(); // avoid to call twice
        if (FileUtils.PHP_MIME_TYPE.equals(mimeType) && FileUtils.PHP_MIME_TYPE.equals(mimePath)) {
            Indentation indent = new IndentationCounter((BaseDocument) context.document()).count(context.caretOffset());
            indent.modify(context);
        }
    }

    @Override
    public void reformat(Context context, ParserResult info) {
        long start = System.currentTimeMillis();

        (new TokenFormatter()).reformat(context, info);

        if (LOG.isLoggable(Level.FINE)) {
            long end = System.currentTimeMillis();
            LOG.log(Level.FINE, "Reformat took: {0} ms", (end - start)); //NOI18N
        }
    }

    @Override
    public int indentSize() {
        return CodeStyle.get((Document) null).getIndentSize();
    }

    @Override
    public int hangingIndentSize() {
        return CodeStyle.get((Document) null).getContinuationIndentSize();
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
