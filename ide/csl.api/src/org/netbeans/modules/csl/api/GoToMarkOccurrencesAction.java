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

package org.netbeans.modules.csl.api;

import java.awt.event.ActionEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.csl.editor.semantic.MarkOccurrencesHighlighter;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * @todo The Java implementation changed to jumping to the
 *   END of identifiers in this integration:
 *    http://hg.netbeans.org/main/rev/8f417bdb256d
 *  to handle bug 136665 - should we do the same to be
 *  consistent?
 *
 * @author Vladimir Voskresensky
 * @deprecated use {@link CslActions#createGoToMarkOccurrencesAction(boolean) } instead.
 */
@Deprecated
public final class GoToMarkOccurrencesAction extends BaseAction {

    private static final String prevActionName = EditorActionNames.gotoPrevOccurrence;
    private static final String nextActionName = EditorActionNames.gotoNextOccurrence;

    private final boolean next;

    public GoToMarkOccurrencesAction(boolean nextOccurrence) {
        super(getNameString(nextOccurrence));
        this.next = nextOccurrence;
        putValue(SHORT_DESCRIPTION, getDefaultShortDescription());
    }

    public void actionPerformed(ActionEvent evt, JTextComponent txt) {
        navigateToOccurence(next, txt);
    }

    @Override
    protected Object getDefaultShortDescription() {
        return NbBundle.getMessage(GoToMarkOccurrencesAction.class, getNameString(next));
    }
    
    private static String getNameString(boolean nextOccurrence) {
        return nextOccurrence ? nextActionName : prevActionName;
    }
    
    @SuppressWarnings("empty-statement")
    private static int findOccurrencePosition(boolean directionForward, Document doc, int curPos) {
        AbstractHighlightsContainer bag = MarkOccurrencesHighlighter.getHighlightsBag(doc);
        HighlightsSequence hs = bag.getHighlights(0, doc.getLength());

        if (hs.moveNext()) {
            if (directionForward) {
                int firstStart = hs.getStartOffset(), firstEnd = hs.getEndOffset();
                boolean hasElements = true;
                while (hs.getStartOffset() <= curPos && (hasElements = hs.moveNext()));

                if (hasElements && hs.getStartOffset() > curPos) {
                    // we found next occurrence
                    return hs.getStartOffset();
                } else if (!(firstEnd >= curPos && firstStart <= curPos)) {
                    // cyclic jump to first occurrence unless we already there
                    return firstStart;
                }
            } else {
                int current = hs.getStartOffset(), last;
                boolean stuck = false;
                do {
                    last = current;
                    current = hs.getStartOffset();
                } while (hs.getEndOffset() < curPos && (stuck = hs.moveNext()));

                if (last == current) {
                    // we got no options to jump, cyclic jump to last in file unless we already there
                    int lastSO = current;
                    int lastEO = Integer.MAX_VALUE;
                    while (hs.moveNext()) {
                        lastSO = hs.getStartOffset();
                        lastEO = hs.getEndOffset();
                    }
                    if (!(lastEO >= curPos && lastSO <= curPos)) {
                        return lastSO;
                    }
                } else if (stuck) {
                    // just move to previous occurence
                    return last;
                } else {
                    // it was last occurence in the file
                    return current;
                }
            }
        }
        return -1;
    }

    private static void navigateToOccurence(boolean next, JTextComponent txt) {
        if (txt != null && txt.getDocument() != null) {
            Document doc = txt.getDocument();
            int position = txt.getCaretPosition();
            int goTo = findOccurrencePosition(next, doc, position);
            if (goTo > 0) {
                txt.setCaretPosition(goTo);
            } else {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToMarkOccurrencesAction.class, "csl-no-marked-occurrence"));
            }
        }

    }    
}
