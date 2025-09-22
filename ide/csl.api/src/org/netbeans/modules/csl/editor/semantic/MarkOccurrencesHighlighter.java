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

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.ColoringAttributes.Coloring;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public final class MarkOccurrencesHighlighter extends ParserResultTask<ParserResult> {

    private static final Logger LOG = Logger.getLogger(MarkOccurrencesHighlighter.class.getName());
    
    //private FileObject file;
    private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);
    private final Language language;
    private final Snapshot snapshot;

    static final Coloring MO = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.MARK_OCCURRENCES);
    
    /** Creates a new instance of SemanticHighlighter */
    MarkOccurrencesHighlighter(Language language, Snapshot snapshot) {
        this.language = language;
        this.snapshot = snapshot;
    }
    
    public static final Color ES_COLOR = new Color( 175, 172, 102 ); // new Color(244, 164, 113);
    
//    public Document getDocument() {
//        return snapshot.getSource().getDocument(false);
//    }
//
    @Override
    public void run(ParserResult info, SchedulerEvent event) {
        SpiSupportAccessor.getInstance().setCancelSupport(cancel);
        try {
            Document doc = snapshot.getSource().getDocument(false);

            if (doc == null) {
                LOG.log(Level.INFO, "MarkOccurencesHighlighter: Cannot get document!"); //NOI18N
                return ;
            }

            if (!(event instanceof CursorMovedSchedulerEvent)) {
                return;
            }

            int caretPosition = ((CursorMovedSchedulerEvent) event).getCaretOffset();

            if (cancel.isCancelled()) {
                return;
            }

            int snapshotOffset = info.getSnapshot().getEmbeddedOffset(caretPosition);

            if (snapshotOffset == -1) {
                // caret offset not part of this lang embedding, ignore, since
                // we cannot assume identifiers in different languages match.
                return;
            }

            List<OffsetRange> bag = processImpl(info, doc, caretPosition);
            if (cancel.isCancelled()) {
                //the occurrences finder haven't found anything, just ignore the result
                //and keep the previous occurrences
                return ;
            }

            GsfSemanticLayer layer = GsfSemanticLayer.getLayer(MarkOccurrencesHighlighter.class, doc);
            SortedSet<SequenceElement> seqs = new TreeSet<>(SequenceElement.POSITION_ORDER);

            for (OffsetRange range : bag) {
                if (range != OffsetRange.NONE) {
                    try {
                        seqs.add(new SequenceElement(language, doc.createPosition(range.getStart()), doc.createPosition(range.getEnd()), MO));
                    } catch (BadLocationException ex) {}
                }
            }
            layer.setColorings(seqs);

            OccurrencesMarkProvider.get(doc).setOccurrences(OccurrencesMarkProvider.createMarks(doc, bag, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP")));
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }
    }
    
    @NonNull
    List<OffsetRange> processImpl(ParserResult info, Document doc, int caretPosition) {
        OccurrencesFinder finder = language.getOccurrencesFinder();
        if(finder == null) {
            return List.of();
        }

        finder.setCaretPosition(caretPosition);
        try {
            finder.run(info, null);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }

        if (cancel.isCancelled()) {
            finder.cancel();
        }

        Map<OffsetRange, ColoringAttributes> highlights = finder.getOccurrences();

        // Many implementatios of the OccurencesFinder don't follow the contract,
        // that getOccurrences must not return null. Instead of blowing up with
        // a NullPointer exception, log that problem and continue execution.
        if (highlights == null) {
            LOG.log(Level.WARNING, "org.netbeans.modules.csl.api.OccurrencesFinder.getOccurrences() non-null contract violation by {0}", language.getMimeType());
            highlights = Map.of();
        }

        return List.copyOf(highlights.keySet());
    }
    
    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass () {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final void cancel() {
    }

    public static AbstractHighlightsContainer getHighlightsBag(Document doc) {
        GsfSemanticLayer highlight = GsfSemanticLayer.getLayer(MarkOccurrencesHighlighter.class, doc);
        return highlight;
    }
}
