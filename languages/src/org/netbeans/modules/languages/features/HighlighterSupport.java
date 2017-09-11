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
