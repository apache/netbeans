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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.csl.editor.semantic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private int version;
    static Coloring MO = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.MARK_OCCURRENCES);
    
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
            if(bag == null) {
                //the occurrences finder haven't found anything, just ignore the result
                //and keep the previous occurrences
                return ;
            }

            if (cancel.isCancelled()) {
                return;
            }

            GsfSemanticLayer layer = GsfSemanticLayer.getLayer(MarkOccurrencesHighlighter.class, doc);
            SortedSet seqs = new TreeSet<SequenceElement>();

            if (bag.size() > 0) {
                for (OffsetRange range : bag) {
                    if (range != OffsetRange.NONE) {
                        SequenceElement s = new SequenceElement(language, range, MO);
                        seqs.add(s);
                    }
                }
            }

            layer.setColorings(seqs, version++);

            OccurrencesMarkProvider.get(doc).setOccurrences(OccurrencesMarkProvider.createMarks(doc, bag, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP")));
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }
    }
    
    @NonNull
    List<OffsetRange> processImpl(ParserResult info, Document doc, int caretPosition) {
        OccurrencesFinder finder = language.getOccurrencesFinder();
        assert finder != null;
        
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

        return highlights == null ? null : new ArrayList<OffsetRange>(highlights.keySet());
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
