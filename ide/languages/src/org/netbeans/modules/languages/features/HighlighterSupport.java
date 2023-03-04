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

package org.netbeans.modules.languages.features;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.Highlighting.Highlight;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.windows.TopComponent;
import java.awt.Color;
import java.util.Iterator;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


/**
 */
public class HighlighterSupport {
    
    private Color       color;
    private Highlight   highlight;
    
    
    public HighlighterSupport (Color c) {
        color = c;
    }
    
    public void highlight (Document doc, int start, int end) {
        removeHighlight ();
        highlight = Highlighting.getHighlighting (doc).highlight (
            start, 
            end,
            getHighlightAS ()
        );
        refresh (doc, start);
    }
    
    private static void refresh (final Document doc, final int offset) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Iterator it = TopComponent.getRegistry ().getOpened ().iterator ();
                while (it.hasNext ()) {
                    TopComponent tc = (TopComponent) it.next ();
                    EditorCookie ec = tc.getLookup ().lookup (EditorCookie.class);
                    if (ec == null) continue;
                    JEditorPane[] eps = ec.getOpenedPanes ();
                    if (eps == null) continue;
                    int i, k = eps.length;
                    for (i = 0; i < k; i++) {
                        if (eps [i].getDocument () == doc) {
                            final JEditorPane ep = eps [i];
                            if (ep != null)
                                try {
                                    ep.scrollRectToVisible (ep.modelToView (offset));
                                } catch (BadLocationException ex) {
                                    ErrorManager.getDefault ().notify (ex);
                                }
                        }
                    }
                }
            }
        });
    }
    
    public void removeHighlight () {
        if (highlight == null) return;
        highlight.remove ();
        highlight = null;
    }
    
    private AttributeSet highlightAS = null;
    
    private AttributeSet getHighlightAS () {
        if (highlightAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Background, color);
            highlightAS = as;
        }
        return highlightAS;
    }
    
}
