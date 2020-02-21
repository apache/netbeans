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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
