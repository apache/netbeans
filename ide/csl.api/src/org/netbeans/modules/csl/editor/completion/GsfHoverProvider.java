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
package org.netbeans.modules.csl.editor.completion;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.lsp.HoverProvider;

/**
 *
 * @author Dusan Balek
 */
public final class GsfHoverProvider implements HoverProvider {

    @Override
    public CompletableFuture<String> getHoverContent(Document doc, int offset) {
        try {
            GsfCompletionProvider.JavaCompletionQuery query = new GsfCompletionProvider.JavaCompletionQuery(CompletionProvider.DOCUMENTATION_QUERY_TYPE, offset);
            AsyncCompletionTask task = new AsyncCompletionTask(query);
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result result = resultIterator.getParserResult(offset);
                    if (result instanceof ParserResult) {
                        query.resolveDocumentation((ParserResult) result);
                    }
                }
            });
            CompletionDocumentation documentation = query.getDocumentation();
            return CompletableFuture.completedFuture(documentation != null ? documentation.getText() : null);
        } catch (ParseException ex) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
