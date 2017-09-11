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

package org.netbeans.modules.javascript2.debug.ui;

import java.awt.EventQueue;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.editor.EditorUI;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public final class EditorContextSetter {
    
    private static final RequestProcessor RP = new RequestProcessor(EditorContextSetter.class);
    
    private EditorContextSetter() {}
    
    public static void setContext(final JEditorPane editorPane,
                                  final EditorContextProvider ecp,
                                  final Runnable setUpCallback) {
        if (EventQueue.isDispatchThread()) {
            if (ecp != null) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        final Pair<Line.Part, FileObject> context = getContext(ecp);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setupContext(editorPane, context);
                                if (setUpCallback != null) {
                                    setUpCallback.run();
                                }
                            }
                        });
                    }
                });
            } else {
                setupContext(editorPane, null);
                Line line = EditorContextDispatcher.getDefault().getMostRecentLine();
                line.createPart(0, 0);
            }
        } else {
            final Pair<Line.Part, FileObject> context = getContext(ecp);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setupContext(editorPane, context);
                    if (setUpCallback != null) {
                        setUpCallback.run();
                    }
                }
            });
        }
    }
    
    private static Pair<Line.Part, FileObject> getContext(EditorContextProvider ecp) {
        Line.Part lp = ecp.getContext();
        if (lp == null) {
            Line line = EditorContextDispatcher.getDefault().getMostRecentLine();
            if (line != null) {
                lp = line.createPart(0, 0);
            }
        }
        FileObject file;
        if (lp != null) {
            file = lp.getLine().getLookup().lookup(FileObject.class);
        } else {
            file = null;
        }
        return Pair.of(lp, file);
    }

    private static void setupContext(final JEditorPane editorPane, Pair<Line.Part, FileObject> context) {
        assert SwingUtilities.isEventDispatchThread();
        if (context != null) {
            final Line.Part lp = context.first();
            final FileObject file = context.second();
            //System.err.println("WatchPanel.setupContext("+file+", "+line+", "+offset+")");
            // Do the binding for text files only:
            if (file != null && file.getMIMEType().startsWith("text/")) { // NOI18N
                String origText = editorPane.getText();
                DialogBinding.bindComponentToFile(file,
                                                  lp.getLine().getLineNumber(),
                                                  lp.getColumn(),
                                                  lp.getLength(),
                                                  editorPane);
                Document editPaneDoc = editorPane.getDocument();
                //editPaneDoc.putProperty("org.netbeans.modules.editor.java.JavaCompletionProvider.skipAccessibilityCheck", "true");
                editorPane.setText(origText);
            }
        }
        setupUI(editorPane);
    }
    
    private static void setupUI(final JEditorPane editorPane) {
        assert SwingUtilities.isEventDispatchThread();
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
        if (eui == null) {
            return ;
        }
        editorPane.putClientProperty(
            "HighlightsLayerExcludes", //NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
        );
        // Do not draw text limit line
        try {
            java.lang.reflect.Field textLimitLineField = EditorUI.class.getDeclaredField("textLimitLineVisible"); // NOI18N
            textLimitLineField.setAccessible(true);
            textLimitLineField.set(eui, false);
        } catch (Exception ex) {}
        editorPane.repaint();
    }

    public interface EditorContextProvider {
        
        Line.Part getContext();
    }
}
