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

package org.netbeans.modules.editor.fold.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.lib2.view.DocumentView;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Component that displays a collapsed fold preview.
 *
 * @author Miloslav Metelka
 */
final class FoldToolTip extends JPanel {
    private int editorPaneWidth;

    public FoldToolTip(JEditorPane editorPane, final JEditorPane foldPreviewPane, Color borderColor) {
        setLayout(new BorderLayout());
        add(foldPreviewPane, BorderLayout.CENTER);
        putClientProperty("tooltip-type", "fold-preview"); // Checked in NbToolTip

        addGlyphGutter(foldPreviewPane);
        
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // Deactivate the view hierarchy immediately for foldPreviewPane
                final DocumentView docView = DocumentView.get(foldPreviewPane);
                if (docView != null) {
                    docView.runTransaction(new Runnable() {
                        @Override
                        public void run() {
                            docView.updateLengthyAtomicEdit(+100); // Effectively disable any VH updates
                        }
                    });
                }
                // Remove the listener
                FoldToolTip.this.removeAncestorListener(this);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });

        editorPaneWidth = editorPane.getSize().width;

        setBorder(new LineBorder(borderColor));
        setOpaque(true);
    }
    
    private void addGlyphGutter(JTextComponent jtx) {
        ClassLoader cls = Lookup.getDefault().lookup(ClassLoader.class);
        Class clazz;
        Class editorUiClass;
        
        JComponent gutter = null;
        try {
            clazz = Class.forName("org.netbeans.editor.GlyphGutter", true, cls); // NOI18N
            editorUiClass = Class.forName("org.netbeans.editor.EditorUI", true, cls); // NOI18N
            // get the factory instance
            Object o = clazz.newInstance();
            Method m = clazz.getDeclaredMethod("createSideBar", JTextComponent.class); // NOI18N
            gutter = (JComponent)m.invoke(o, jtx);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (gutter != null) {
            add(gutter, BorderLayout.WEST);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        // Return width like for editor pane which forces the PopupManager to display
        // the tooltip to align exacty with the text (below/above).
        prefSize.width = Math.min(prefSize.width, editorPaneWidth);
        return prefSize;
    }

}
