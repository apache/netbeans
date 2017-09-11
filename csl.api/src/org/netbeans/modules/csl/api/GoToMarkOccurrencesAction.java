/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
