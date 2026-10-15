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
            Object o = clazz.getDeclaredConstructor().newInstance();
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
