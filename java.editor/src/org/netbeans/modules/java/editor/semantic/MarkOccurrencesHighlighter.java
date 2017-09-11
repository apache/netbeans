/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.semantic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.java.editor.base.options.MarkOccurencesSettingsNames;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class MarkOccurrencesHighlighter extends MarkOccurrencesHighlighterBase {

    private FileObject file;

    MarkOccurrencesHighlighter(FileObject file) {
        this.file = file;
    }
    
    public static final Color ES_COLOR = new Color( 175, 172, 102 ); // new Color(244, 164, 113);
    static ColoringAttributes.Coloring MO = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.MARK_OCCURRENCES);

    protected void process(CompilationInfo info, Document doc, SchedulerEvent event) {
        if (doc == null) {
            Logger.getLogger(MarkOccurrencesHighlighter.class.getName()).log(Level.FINE, "SemanticHighlighter: Cannot get document!");
            return ;
        }
        
        int caretPosition = event instanceof CursorMovedSchedulerEvent ? 
            ((CursorMovedSchedulerEvent) event).getCaretOffset () :
            CaretAwareJavaSourceTaskFactory.getLastPosition(file);//XXX

        Object prop = doc.getProperty(GoToMarkOccurrencesAction.markedOccurence);
        if (prop != null && ((long[])prop)[0] == DocumentUtilities.getDocumentVersion(doc) && ((long[])prop)[1] == caretPosition) {
            return;
        }

        Preferences node = MarkOccurencesSettings.getCurrentNode();

        if (!node.getBoolean(MarkOccurencesSettingsNames.ON_OFF, true)) {
            getHighlightsBag(doc).clear();
            OccurrencesMarkProvider.get(doc).setOccurrences(Collections.<Mark>emptySet());
            return ;
        }

        long start = System.currentTimeMillis();

        if (isCancelled())
            return;

        List<int[]> bag = processImpl(info, node, doc, caretPosition);

        if (isCancelled())
            return;

        Logger.getLogger("TIMER").log(Level.FINE, "Occurrences",
            new Object[] {NbEditorUtilities.getFileObject(doc), (System.currentTimeMillis() - start)});

        if (bag == null) {
            if (node.getBoolean(MarkOccurencesSettingsNames.KEEP_MARKS, true)) {
                return ;
            }

            bag = new ArrayList<int[]>();
        }

        Collections.sort(bag, new Comparator<int[]>() {
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });

        Iterator<int[]> it = bag.iterator();
        int[] last = it.hasNext() ? it.next() : null;
        List<int[]> result = new ArrayList<int[]>(bag.size());

        while (it.hasNext()) {
            int[] current = it.next();

            if (current[0] < last[1]) {
                //merge the highlights:
                last[1] = Math.max(current[1], last[1]);
            } else {
                result.add(last);
                last = current;
            }
        }

        if (last != null) {
            result.add(last);
        }

        OffsetsBag obag = new OffsetsBag(doc);

        obag.clear();

        AttributeSet attributes = ColoringManager.getColoringImpl(MO);

        for (int[] span : result) {
            int convertedStart = info.getSnapshot().getOriginalOffset(span[0]);
            int convertedEnd   = info.getSnapshot().getOriginalOffset(span[1]);

            if (convertedStart != (-1) && convertedEnd != (-1)) {
                obag.addHighlight(convertedStart, convertedEnd, attributes);
            }
        }

        if (isCancelled())
            return;
        
        getHighlightsBag(doc).setHighlights(obag);
        OccurrencesMarkProvider.get(doc).setOccurrences(OccurrencesMarkProvider.createMarks(doc, bag, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP")));
        
    }

    static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MarkOccurrencesHighlighter.class);

        if (bag == null) {
            doc.putProperty(MarkOccurrencesHighlighter.class, bag = new OffsetsBag(doc, false));

            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            final OffsetsBag bagFin = bag;
            DocumentListener l = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void removeUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void changedUpdate(DocumentEvent e) {}
            };

            doc.addDocumentListener(l);

            if (stream instanceof DataObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "MarkOccurrences Highlights Bag", new Object[] {((DataObject) stream).getPrimaryFile(), bag}); //NOI18N
                Logger.getLogger("TIMER").log(Level.FINE, "MarkOccurrences Highlights Bag Listener", new Object[] {((DataObject) stream).getPrimaryFile(), l}); //NOI18N
            }
        }

        return bag;
    }
}
