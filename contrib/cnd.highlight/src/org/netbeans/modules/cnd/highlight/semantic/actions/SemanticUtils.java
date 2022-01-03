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
package org.netbeans.modules.cnd.highlight.semantic.actions;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.highlight.semantic.MarkOccurrencesHighlighter;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 */
public class SemanticUtils {

    @SuppressWarnings("empty-statement")
    private static int findOccurrencePosition(boolean directionForward, Document doc, int curPos) {
        PositionsBag bag = MarkOccurrencesHighlighter.getHighlightsBag(doc);
        HighlightsSequence hs = bag.getHighlights(0, doc.getLength());
        
        if (hs.moveNext()) {
            if (directionForward) {
                int firstStart = hs.getStartOffset(), firstEnd = hs.getEndOffset();
                
                while (hs.getStartOffset() <= curPos && hs.moveNext());
                
                if (hs.getStartOffset() > curPos) {
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
                    while (hs.moveNext());
                    if (!(hs.getEndOffset() >= curPos && hs.getStartOffset() <= curPos)) {
                        return hs.getStartOffset();
                    }
                } else if (stuck) {
                    // just move to previous occurrence
                    return last;
                } else {
                    // it was last occurrence in the file
                    return current;
                }
            }
        }
        return -1;
    }


    /*package*/ static void navigateToOccurrence(boolean next) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        // check whether current file is C/C++
        DataObject dobj = activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fo = (dobj == null) ? null : dobj.getPrimaryFile();
        String mime = (fo == null) ? "" : fo.getMIMEType();
        if (MIMENames.isHeaderOrCppOrC(mime)) {
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            JEditorPane pane = NbDocument.findRecentEditorPane(ec);
            if (pane != null) {
                Document doc = ec.getDocument();
                int position = pane.getCaretPosition();
                int goTo = findOccurrencePosition(next, doc, position);
                if (goTo > 0) {
                    pane.setCaretPosition(goTo);
                } else {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(SemanticUtils.class, "cpp-no-marked-occurrence"));
                }
            }
        }

    }
}
