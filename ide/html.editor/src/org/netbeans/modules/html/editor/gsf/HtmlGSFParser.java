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
package org.netbeans.modules.html.editor.gsf;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.html.editor.HtmlExtensions;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.foreign.UndeclaredContentResolver;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HtmlGSFParser extends Parser {

    private static final Logger TIMER = Logger.getLogger("TIMER.j2ee.parser"); // NOI18N
    
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private SyntaxAnalyzerResult result;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        parse(snapshot, event);
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        //avoid change of the mutable 'result' field during the following ternary operator execution.
        final SyntaxAnalyzerResult sar = result; 
        return cancelled.get() || (sar == null) ? null : HtmlParserResultAccessor.get().createInstance(sar);
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        if (CancelReason.SOURCE_MODIFICATION_EVENT == reason && event.sourceChanged()) {
            cancelled.set(true);
            result = null;
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    private void parse(Snapshot snapshot, SourceModificationEvent event) {
        cancelled.set(false);
        if (snapshot == null) {
            //#215101: calling "ParserManager.parseWhenScanFinished("text/html",someTask)" results into null snapshot passed here
            return;
        }

        HtmlSource source = new HtmlSource(snapshot);
        Source snapshotSource = snapshot.getSource();
        String sourceMimetype = snapshotSource != null ? snapshotSource.getMimeType() : snapshot.getMimeType(); //prefer source mimetype

        Collection<? extends HtmlExtension> exts = HtmlExtensions.getRegisteredExtensions(sourceMimetype);
        if (cancelled.get()) {
            return;
        }
        Collection<UndeclaredContentResolver> resolvers = new ArrayList<>();
        for (final HtmlExtension ex : exts) {
            resolvers.add(new UndeclaredContentResolver() {

                @Override
                public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
                    return ex.getUndeclaredNamespaces(source);
                }

                @Override
                public boolean isCustomTag(Named element, HtmlSource source) {
                    return ex.isCustomTag(element, source);
                }

                @Override
                public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
                    return ex.isCustomAttribute(attribute, source);
                }

            });
        }

        if (cancelled.get()) {
            return;
        }
        result = SyntaxAnalyzer.create(source).analyze(new AggregatedUndeclaredContentResolver(resolvers));

        if (TIMER.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "HTML parse result"); // NOI18N
            rec.setParameters(new Object[]{result});
            TIMER.log(rec);
        }

    }

    private static class AggregatedUndeclaredContentResolver implements UndeclaredContentResolver {

        private final Collection<UndeclaredContentResolver> resolvers;

        public AggregatedUndeclaredContentResolver(Collection<UndeclaredContentResolver> resolvers) {
            this.resolvers = resolvers;
        }

        @Override
        public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
            Map<String, List<String>> aggregated = new HashMap<>();
            for (UndeclaredContentResolver resolver : resolvers) {
                aggregated.putAll(resolver.getUndeclaredNamespaces(source));
            }
            return aggregated;
        }

        @Override
        public boolean isCustomTag(Named element, HtmlSource source) {
            for (UndeclaredContentResolver r : resolvers) {
                if (r.isCustomTag(element, source)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isCustomAttribute(Attribute attr, HtmlSource source) {
            for (UndeclaredContentResolver r : resolvers) {
                if (r.isCustomAttribute(attr, source)) {
                    return true;
                }
            }
            return false;
        }

    }

}
