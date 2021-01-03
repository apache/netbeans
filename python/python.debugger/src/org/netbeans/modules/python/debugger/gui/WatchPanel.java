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
package org.netbeans.modules.python.debugger.gui;

import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.text.EditorKit;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.editor.DialogBinding;

import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 * A GUI panel for customizing a Watch in python Debugging.

 */
public class WatchPanel {

    private JPanel panel;
    private JEditorPane editorPane;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }
    
    public static void setupContext(final JEditorPane editorPane, final ActionListener contextSetUp) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(PythonMIMEResolver.PYTHON_MIME_TYPE);
        editorPane.setEditorKit(kit);
        DebuggerEngine en = DebuggerManager.getDebuggerManager ().getCurrentEngine();
        if (EventQueue.isDispatchThread() && en != null) {
            RequestProcessor contextRetrievalRP = en.lookupFirst(null, RequestProcessor.class);
            if (contextRetrievalRP != null) {
                final DebuggerEngine den = en;
                contextRetrievalRP.post(new Runnable() {
                    @Override
                    public void run() {
                        final Context c = retrieveContext(den);
                        if (c != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                          @Override
                                public void run() {
                                    setupContext(editorPane, c.url, c.line);
                                    if (contextSetUp != null) contextSetUp.actionPerformed(null);
                                }
                            });
                        }
                    }
                });
                setupUI(editorPane);
                return ;
            } else {
                en = null;
            }
        }
        Context c = retrieveContext(en);
        if (c != null) {
            setupContext(editorPane, c.url, c.line);
        } else {
            setupUI(editorPane);
        }
        if (contextSetUp != null) contextSetUp.actionPerformed(null);
    }

    private static Context retrieveContext(DebuggerEngine en) {
      // TODO : check usage and return something consistent
      return null ;
    }
    
    public static void setupContext(JEditorPane editorPane, final String url, int line) {
        setupUI(editorPane);
        FileObject file;
        StyledDocument doc;
        try {
            file = URLMapper.findFileObject (new URL (url));
            if (file == null) {
                return;
            }
            try {
                final DataObject dobj = DataObject.find (file);
                final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                if (ec == null) {
                    return;
                }
                try {
                    doc = ec.openDocument();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                    return;
                }
            } catch (DataObjectNotFoundException ex) {
                // null dobj
                return;
            }
        } catch (MalformedURLException e) {
            // null dobj
            return;
        }
        try {
            final int offset = NbDocument.findLineOffset(doc, line);
            //editorPane.getDocument().putProperty(javax.swing.text.Document.StreamDescriptionProperty, dobj);
            DialogBinding.bindComponentToDocument(doc, offset, 0, editorPane);
        } catch (IndexOutOfBoundsException ioobex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioobex);
        }
    }
    
    private static void setupUI(final JEditorPane editorPane) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
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
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                  ex.printStackTrace(); 
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public JComponent getPanel() {
        if (panel != null) return panel;

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString ("CTL_Watch_Name")); // NOI18N
        editorPane = new JEditorPane();//expression); // NOI18N
        editorPane.setText(expression);

        ActionListener editorPaneUpdated = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editorPane.setText (expression);
                editorPane.selectAll ();
            }
        };
        setupContext(editorPane, editorPaneUpdated);
        
        JScrollPane sp = createScrollableLineEditor(editorPane);
        FontMetrics fm = editorPane.getFontMetrics(editorPane.getFont());
        int size = 2*fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent() + 2;
        Insets eInsets = editorPane.getInsets();
        Insets spInsets = sp.getInsets();
        sp.setPreferredSize(new Dimension(30*size,
                size + 2 +
                eInsets.bottom + eInsets.top +
                spInsets.bottom + spInsets.top));
        
        textLabel.setBorder (new EmptyBorder (0, 0, 5, 0));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add (BorderLayout.NORTH, textLabel);
        panel.add (BorderLayout.CENTER, sp);
        
        editorPane.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        editorPane.setText (expression);
        editorPane.selectAll ();

        textLabel.setLabelFor (editorPane);
        HelpCtx.setHelpIDString(editorPane, "debug.customize.watch");
        editorPane.requestFocus ();
        
        return panel;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }
    
    public static JScrollPane createScrollableLineEditor(JEditorPane editorPane) {
        editorPane.setKeymap(new FilteredKeymap(editorPane));
        final JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                
        editorPane.setBorder (
            new CompoundBorder (editorPane.getBorder(),
            new EmptyBorder (0, 0, 0, 0))
        );
        
        JTextField referenceTextField = new JTextField("M");
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(referenceTextField.getBackground());
        sp.setBorder(referenceTextField.getBorder());
        sp.setBackground(referenceTextField.getBackground());
        
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        panel.add(editorPane, gridBagConstraints);
        sp.setViewportView(panel);
        
        int preferredHeight = referenceTextField.getPreferredSize().height;
        if (sp.getPreferredSize().height < preferredHeight) {
            sp.setPreferredSize(referenceTextField.getPreferredSize());
        }
        sp.setMinimumSize(sp.getPreferredSize());
        
        setupUI(editorPane);
        
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
        return sp;
    }

    private static final class Context {
        public String url;
        public int line;
    }
    
}
