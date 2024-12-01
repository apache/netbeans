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
package org.netbeans.modules.csl.editor.semantic;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.ColoringAttributes.Coloring;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.*;


/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public final class SemanticHighlighter extends IndexingAwareParserResultTask<ParserResult> {

    private static final Logger LOG = Logger.getLogger(SemanticHighlighter.class.getName());

    private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);
    
    /** Creates a new instance of SemanticHighlighter */
    SemanticHighlighter() {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

//    public Document getDocument() {
//        return snapshot.getSource().getDocument();
//    }
//
    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass () {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }


    public final void cancel() {
    }

    public @Override void run(ParserResult info, SchedulerEvent event) {
        Document doc = info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return;
        }
        SpiSupportAccessor.getInstance().setCancelSupport(cancel);
        try {
            long startTime = System.currentTimeMillis();

            Source source = info.getSnapshot().getSource();
            final SortedSet<SequenceElement> newColoring = new TreeSet<>();
            try {
                ParserManager.parse(Collections.singleton(source), (ResultIterator ri) -> processColorings(ri, newColoring));
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
                return;
            }

            long endTime = System.currentTimeMillis();
            Logger.getLogger("TIMER").log(Level.FINE, "Semantic (" + source.getMimeType() + ")", //NOI18N
                    new Object[] { source.getFileObject(), endTime - startTime});

            if (cancel.isCancelled()) {
                return;
            }

            final GsfSemanticLayer layer = GsfSemanticLayer.getLayer(SemanticHighlighter.class, doc);
            SwingUtilities.invokeLater(() -> {
                // XXX: parsingapi
                layer.setColorings(newColoring);
            });
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }
    }
    
    private void processColorings(ResultIterator resultIterator, SortedSet<SequenceElement> newColoring) throws ParseException  {
        String mimeType = resultIterator.getSnapshot().getMimeType();
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        if (language != null) {
            ColoringManager manager = language.getColoringManager();
            SemanticAnalyzer task = language.getSemanticAnalyzer();
            if (manager != null && task != null) {
                Parser.Result r = resultIterator.getParserResult();
                if (r instanceof ParserResult) {
                    process(language, (ParserResult) r, newColoring);
                }
            }
        }

        for(Embedding e : resultIterator.getEmbeddings()) {
            if (cancel.isCancelled()) {
                return;
            } else {
                processColorings(resultIterator.getResultIterator(e), newColoring);
            }
        }
    }

    private void process(Language language, ParserResult result, Set<SequenceElement> newColoring) throws ParseException {
        if (cancel.isCancelled()) {
            return;
        }

        ColoringManager manager = language.getColoringManager();
        SemanticAnalyzer task = language.getSemanticAnalyzer();
        
        // Allow language plugins to do their own analysis too
        try {
            task.run(result, null);
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "SemanticAnalyzer = " + 
                    task + "; Language = " + language +
                    " (mimetype = " + language.getMimeType() +
                    "; ParserResult = " + result +
                    "(mimepath = " + result.getSnapshot().getMimePath() + ")", ex);
        }

        if (cancel.isCancelled()) {
            task.cancel();
            return;
        }

        Map<OffsetRange,Set<ColoringAttributes>> highlights = task.getHighlights();
        if (highlights != null) {
            for (Map.Entry<OffsetRange, Set<ColoringAttributes>> entry : highlights.entrySet()) {

                OffsetRange range = entry.getKey();
                Set<ColoringAttributes> colors = entry.getValue();
                if (colors == null) {
                    continue;
                }

                Coloring c = manager.getColoring(colors);

                newColoring.add(new SequenceElement(language, range, c));

                if (cancel.isCancelled()) {
                    return;
                }
            }
        }

        return;
    }

}
