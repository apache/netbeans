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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.editor.bracesmatching;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.WeakListeners;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Vita Stejskal
 */
public class BracesMatchHighlighting extends AbstractHighlightsContainer 
    implements ReleasableHighlightsContainer, ChangeListener, PropertyChangeListener, HighlightsChangeListener, DocumentListener 
{
    private static final Logger LOG = Logger.getLogger(BracesMatchHighlighting.class.getName());
    
    private static final String BRACES_MATCH_COLORING = "nbeditor-bracesMatching-match"; //NOI18N
    private static final String BRACES_MISMATCH_COLORING = "nbeditor-bracesMatching-mismatch"; //NOI18N
    
    private static final String BRACES_MATCH_MULTICHAR_COLORING = "nbeditor-bracesMatching-match-multichar"; //NOI18N
    private static final String BRACES_MISMATCH_MULTICHAR_COLORING = "nbeditor-bracesMatching-mismatch-multichar"; //NOI18N

    private final JTextComponent component;
    private final Document document;
    
    private Caret caret = null;
    private ChangeListener caretListener;
    
    private final OffsetsBag bag;
    private final AttributeSet bracesMatchColoring;
    private final AttributeSet bracesMismatchColoring;
    private final AttributeSet bracesMatchMulticharColoring;
    private final AttributeSet bracesMismatchMulticharColoring;
    
    private boolean released;

    public BracesMatchHighlighting(JTextComponent component, Document document) {
        this.document = document;

        String mimeType = getMimeType(component);
        MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);

        // Load the colorings
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        AttributeSet match = fcs.getFontColors(BRACES_MATCH_COLORING);
        AttributeSet mismatch = fcs.getFontColors(BRACES_MISMATCH_COLORING);
        AttributeSet matchMultichar = fcs.getFontColors(BRACES_MATCH_MULTICHAR_COLORING);
        AttributeSet mismatchMultichar = fcs.getFontColors(BRACES_MISMATCH_MULTICHAR_COLORING);
        this.bracesMatchColoring = match != null ? match : SimpleAttributeSet.EMPTY;
        this.bracesMismatchColoring = mismatch != null ? mismatch : SimpleAttributeSet.EMPTY;
        this.bracesMatchMulticharColoring = matchMultichar != null ? matchMultichar : SimpleAttributeSet.EMPTY;
        this.bracesMismatchMulticharColoring = mismatchMultichar != null ? mismatchMultichar : SimpleAttributeSet.EMPTY;
        
        // Create and hook up the highlights bag
        this.bag = new OffsetsBag(document, true);
        this.bag.addHighlightsChangeListener(this);
        
        // Hook up the component
        this.component = component;
        this.component.addPropertyChangeListener(WeakListeners.propertyChange(this, this.component));

        // Hook up the caret
        this.caret = component.getCaret();
        if (this.caret != null) {
            this.caretListener = WeakListeners.change(this, this.caret);
            this.caret.addChangeListener(caretListener);
        }

        // Refresh the layer
        refresh();
    }

    // ------------------------------------------------
    // AbstractHighlightsContainer implementation
    // ------------------------------------------------
    
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }

    // ------------------------------------------------
    // HighlightsChangeListener implementation
    // ------------------------------------------------
    
    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
// XXX: not neccessary
//        final int startOffset = event.getStartOffset();
//        final int endOffset = event.getEndOffset();
//        
//        SwingUtilities.invokeLater(new Runnable() {
//            private boolean inDocumentRender = false;
//            public void run() {
//                if (inDocumentRender) {
//                    fireHighlightsChange(startOffset, endOffset);
//                } else {
//                    inDocumentRender = true;
//                    document.render(this);
//                }
//            }
//        });
    }

    // ------------------------------------------------
    // DocumentListener implementation
    // ------------------------------------------------
    
    public void insertUpdate(DocumentEvent e) {
        refresh();
    }

    public void removeUpdate(DocumentEvent e) {
        refresh();
    }

    public void changedUpdate(DocumentEvent e) {
        refresh();
    }
    
    // ------------------------------------------------
    // ChangeListener implementation
    // ------------------------------------------------
    
    public void stateChanged(ChangeEvent e) {
        refresh();
    }

    // ------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || "caret".equals(evt.getPropertyName())) { //NOI18N
            if (caret != null) {
                caret.removeChangeListener(caretListener);
                caretListener = null;
            }
            
            caret = component.getCaret();
            
            if (caret != null) {
                caretListener = WeakListeners.change(this, caret);
                caret.addChangeListener(caretListener);
            }
            
            refresh();
        } else if (MasterMatcher.PROP_SEARCH_DIRECTION.equals(evt.getPropertyName()) ||
                   MasterMatcher.PROP_CARET_BIAS.equals(evt.getPropertyName()) ||
                   MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD.equals(evt.getPropertyName()) ||
                   MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD.equals(evt.getPropertyName())
        ) {
            refresh();
        }
    }

    // ------------------------------------------------
    // private implementation
    // ------------------------------------------------
    
    private void refresh() {
        if (released) {
            return; // No longer notify the matcher since it would leak to memory leak of MasterMatcher.lastResult
        }

        Caret c = this.caret;
        if (c == null) {
            bag.clear();
        } else {
            MasterMatcher.get(component).highlight(
                document,
                c.getDot(), 
                bag, 
                bracesMatchColoring, 
                bracesMismatchColoring,
                bracesMatchMulticharColoring,
                bracesMismatchMulticharColoring
            );
        }
    }
    
    private static String getMimeType(JTextComponent component) {
        Document doc = component.getDocument();
        String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }

    @Override
    public void released() {
        released = true;
//        component.removePropertyChangeListener(this);
//        bag.removeHighlightsChangeListener(this);
//        document.removeDocumentListener(this);
    }
    
    public static final class Factory implements HighlightsLayerFactory {
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer [] {
                HighlightsLayer.create(
                    "org-netbeans-modules-editor-bracesmatching-BracesMatchHighlighting", //NOI18N
                    ZOrder.SHOW_OFF_RACK.forPosition(400), 
                    true, 
                    new BracesMatchHighlighting(context.getComponent(), context.getDocument())
                )
            };
        }
    } // End of Factory class
}
