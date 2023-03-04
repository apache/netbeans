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
