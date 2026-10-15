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
package org.netbeans.modules.java.stackanalyzer;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import org.netbeans.modules.java.stackanalyzer.StackLineAnalyser.Link;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Analyser Stack Window implementation.
 *
 * @author Jan Becicka
 */
final class AnalyzeStackTopComponent extends TopComponent {

    private static AnalyzeStackTopComponent instance;
    private static final String PREFERRED_ID = "AnalyzeStackTopComponent";


    private AnalyzeStackTopComponent () {
        initComponents ();
        setName (NbBundle.getMessage (AnalyzeStackTopComponent.class, "CTL_AnalyzeStackTopComponent"));
        setToolTipText (NbBundle.getMessage (AnalyzeStackTopComponent.class, "HINT_AnalyzeStackTopComponent"));
        getActionMap ().put (DefaultEditorKit.pasteAction, new PasteAction ());
        insertButton.getActionMap ().put (DefaultEditorKit.pasteAction, new PasteAction ());
        scrollPane.getActionMap ().put (DefaultEditorKit.pasteAction, new PasteAction ());
        list.setBackground(UIManager.getColor("Tree.background"));
        list.getActionMap ().put (DefaultEditorKit.pasteAction, new PasteAction ());
        list.setCellRenderer (new AnalyserCellRenderer ());
        list.addKeyListener (new KeyAdapter () {

            @Override
            public void keyTyped (KeyEvent e) {
                if (e.getKeyChar () == KeyEvent.VK_ENTER) {
                    String currentLine = (String) list.getSelectedValue ();
                    open (currentLine);
                }
            }
        });
        list.addMouseListener (new MouseAdapter () {

            @Override
            public void mouseClicked (MouseEvent e) {
                if (e.getClickCount () != 1) {
                    return;
                }
                int i = list.locationToIndex (e.getPoint ());
                if (i < 0) {
                    return;
                }
                String currentLine = (String) list.getModel ().getElementAt (i);
                open (currentLine);
            }
        });
        list.addMouseMotionListener (new MouseAdapter () {

            @Override
            public void mouseMoved (MouseEvent e) {
                int i = list.locationToIndex (e.getPoint ());
                if (i >= 0) {
                    Rectangle r = list.getCellBounds (i, i);
                    if (r.contains (e.getPoint ())) {
                        String line = (String) list.getModel ().getElementAt (i);
                        Link link = StackLineAnalyser.analyse (line);
                        if (link != null) {
                            list.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }
                list.setCursor (Cursor.getDefaultCursor ());
            }

            @Override
            public void mouseExited (MouseEvent e) {
                list.setCursor (Cursor.getDefaultCursor ());
            }
        });
    }

    public void fill(BufferedReader r) {
        DefaultListModel model = new DefaultListModel ();
        fillListModel(r, model);
        if (!model.isEmpty()) {
            list.setModel (model);
        }
    }
    
    /**
     * Reads the lines from the supplied reader and fills the supplied
     * model with the lines.
     * @param r
     * @param model
     */
    static void fillListModel(BufferedReader r, DefaultListModel model) {
        String currentLine;
        String lastLine = null;
        try {
            while ((currentLine = r.readLine()) != null) {
                // strip ANSI symbols just in case the terminal/clipboard hasn't done that
                currentLine = currentLine.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "").strip();
                if (StackLineAnalyser.matches(currentLine)) {
                    if (lastLine != null) {
                        model.addElement(lastLine);
                    }
                    model.addElement(currentLine);
                    lastLine = null;
                } else {
                    if (lastLine == null) {
                        lastLine = currentLine;
                    } else {
                        if (lastLine.endsWith("at")) { // NOI18N
                            lastLine += " ";        // NOI18N
                        }
                        String together = lastLine + currentLine;
                        if (StackLineAnalyser.matches(together)) {
                            model.addElement(together);
                            lastLine = null;
                        } else {
                            model.addElement(lastLine);
                            lastLine = currentLine;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        insertButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList();

        setName("Form"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(insertButton, org.openide.util.NbBundle.getBundle(AnalyzeStackTopComponent.class).getString("AnalyzeStackTopComponent.insertButton.text")); // NOI18N
        insertButton.setName("insertButton"); // NOI18N
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        scrollPane.setName("scrollPane"); // NOI18N

        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setName("list"); // NOI18N
        scrollPane.setViewportView(list);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                    .addComponent(insertButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(insertButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                .addContainerGap())
        );

        insertButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AnalyzeStackTopComponent.class, "AnalyzeStackTopComponent.insertButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AnalyzeStackTopComponent.class, "AnalyzeStackTopComponent.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AnalyzeStackTopComponent.class, "AnalyzeStackTopComponent.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        try {
            Clipboard clipBoard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
            Transferable transferable = clipBoard.getContents (this);
            if (!transferable.isDataFlavorSupported (DataFlavor.stringFlavor)) {
                return;
            }
            Reader reader = DataFlavor.stringFlavor.getReaderForText (transferable);
            BufferedReader r = new BufferedReader (reader);
            fill(r);
        } catch (UnsupportedFlavorException | IOException ex) {
            Exceptions.printStackTrace (ex);
        }
    }//GEN-LAST:event_insertButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton insertButton;
    private javax.swing.JList list;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized AnalyzeStackTopComponent getDefault () {
        if (instance == null) {
            instance = new AnalyzeStackTopComponent ();
        }
        return instance;
    }

    /**
     * Obtain the AnalyzeStackTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized AnalyzeStackTopComponent findInstance () {
        TopComponent win = WindowManager.getDefault ().findTopComponent (PREFERRED_ID);
        if (win == null) {
            Logger.getLogger (AnalyzeStackTopComponent.class.getName ()).warning (
                "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            AnalyzeStackTopComponent analyzeStackTopComponent = getDefault();
            analyzeStackTopComponent.insertButtonActionPerformed(null);
            return analyzeStackTopComponent;
        }
        if (win instanceof AnalyzeStackTopComponent) {
            AnalyzeStackTopComponent analyzeStackTopComponent = (AnalyzeStackTopComponent) win;
            analyzeStackTopComponent.insertButtonActionPerformed(null);
            return analyzeStackTopComponent;
        }
        Logger.getLogger (AnalyzeStackTopComponent.class.getName ()).warning (
            "There seem to be multiple components with the '" + PREFERRED_ID +
            "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault ();
    }

    @Override
    public int getPersistenceType () {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened () {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed () {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace () {
        return new ResolvableHelper ();
    }

    @Override
    protected String preferredID () {
        return PREFERRED_ID;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.java.stackanalyzer.AnalyzeStackTopComponent"); //NOI18N
    }

    
    static final class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve () {
            return AnalyzeStackTopComponent.getDefault ();
        }
    }

    private void open (String line) {
        Link link = StackLineAnalyser.analyse (line);
        if (link != null) {
            link.show ();
        }
    }

    private class PasteAction extends AbstractAction {

        public PasteAction () {
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            insertButtonActionPerformed (null);
        }
    }
}
