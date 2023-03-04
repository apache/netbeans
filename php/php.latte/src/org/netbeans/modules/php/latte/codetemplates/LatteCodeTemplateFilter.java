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
package org.netbeans.modules.php.latte.codetemplates;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.latte.completion.LatteCompletionContext;
import org.netbeans.modules.php.latte.completion.LatteCompletionContextFinder;
import org.netbeans.modules.php.latte.parser.LatteParserResult;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class LatteCodeTemplateFilter extends UserTask implements CodeTemplateFilter {
    private static final Logger LOGGER = Logger.getLogger(LatteCodeTemplateFilter.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(LatteCodeTemplateFilter.class);
    private volatile boolean accept = true;
    private final int offset;
    private final Future<Future<Void>> future;

    private LatteCodeTemplateFilter(final JTextComponent component, int offset) {
        this.offset = offset;
        future = RP.submit(new Callable<Future<Void>>() {

            @Override
            public Future<Void> call() throws Exception {
                try {
                    return parseDocument(component.getDocument());
                } catch (ParseException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
                return null;
            }
        });
    }

    private Future<Void> parseDocument(final Document document) throws ParseException {
        return ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(document)), this);
    }

    @Override
    public boolean accept(CodeTemplate template) {
        try {
            future.get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return accept;
    }

    @Override
    public void run(ResultIterator resultIterator) throws Exception {
        assert resultIterator != null;
        Parser.Result parserResult = resultIterator.getParserResult();
        if (parserResult instanceof LatteParserResult) {
            LatteCompletionContext context = LatteCompletionContextFinder.find((LatteParserResult) parserResult, offset);
            accept = (LatteCompletionContext.MACRO.equals(context) || LatteCompletionContext.ALL.equals(context));
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {

        @Override
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new LatteCodeTemplateFilter(component, offset);
        }

    }

}
