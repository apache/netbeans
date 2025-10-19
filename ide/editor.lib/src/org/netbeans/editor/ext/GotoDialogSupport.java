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

package org.netbeans.editor.ext;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.DialogSupport;
import org.netbeans.editor.EditorState;
import org.openide.util.NbBundle;

/**
 * Support for displaying goto dialog
 *
 * @author Miloslav Metelka, Petr Nejedly
 * @version 1.00
 */

public class GotoDialogSupport implements ActionListener {
    
    /** The EditorSettings key storing the last location of the dialog. */
    private static final String BOUNDS_KEY = "GotoDialogSupport.bounds-goto-line"; // NOI18N
    
    private JButton[] gotoButtons;
    private GotoDialogPanel gotoPanel;
    private static Dialog gotoDialog;
    
    public GotoDialogSupport() {
        ResourceBundle bundle = NbBundle.getBundle(org.netbeans.editor.BaseKit.class);
        JButton gotoButton = new JButton(bundle.getString("goto-button-goto") ); // NOI18N
        JButton cancelButton = new JButton(bundle.getString("goto-button-cancel") ); // NOI18N
        gotoButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_goto-button-goto")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_goto-button-cancel")); // NOI18N
        //        gotoButton.setMnemonic( bundle.getString("goto-button-goto-mnemonic").charAt(0)); //NOI18N
        
        gotoButtons = new JButton[] { gotoButton, cancelButton };
        gotoPanel = new GotoDialogPanel();
        
        gotoPanel.getGotoCombo().getEditor().getEditorComponent().addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent evt) { }
            public void keyReleased(KeyEvent evt) { }
            public void keyTyped(KeyEvent evt) {
                if (evt.getKeyChar() == '\n') {
                    actionPerformed(
                            new ActionEvent(gotoButtons[0], 0, null));
                }
//                if (!Character.isDigit(evt.getKeyChar()) && !Character.isISOControl(evt.getKeyChar())) {
//                    evt.consume();
//                    Component c = evt.getComponent();
//                    if (c != null) {
//                        c.getToolkit().beep();
//                    }
//                }
            }
        });
        
    }
    
    protected synchronized Dialog createGotoDialog() {
        if( gotoDialog == null ) {
            gotoDialog = DialogSupport.createDialog(
                    NbBundle.getBundle(org.netbeans.editor.BaseKit.class).getString( "goto-title" ), // NOI18N
                    gotoPanel, false, // non-modal
                    gotoButtons, false, // sidebuttons,
                    0, // defaultIndex = 0 => gotoButton
                    1, // cancelIndex = 1 => cancelButton
                    this //listener
                    );
            
            gotoDialog.pack();
            
            // Position the dialog according to the history
            Rectangle lastBounds = (Rectangle)EditorState.get( BOUNDS_KEY );
            if( lastBounds != null ) {
                gotoDialog.setBounds( lastBounds );
            } else {  // no history, center it on the screen
                Dimension dim = gotoDialog.getPreferredSize();
                int x;
                int y;
                JTextComponent c = EditorRegistry.lastFocusedComponent();
                Window w = c != null ? SwingUtilities.getWindowAncestor(c) : null;
                if (w != null) {
                    x = Math.max(0, w.getX() + (w.getWidth() - dim.width) / 2);
                    y = Math.max(0, w.getY() + (w.getHeight() - dim.height) / 2);
                } else {
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    x = Math.max(0, (screen.width - dim.width) / 2);
                    y = Math.max(0, (screen.height - dim.height) / 2);
                }
                gotoDialog.setLocation( x, y );
            }
            
            return gotoDialog;
        } else {
            gotoDialog.setVisible(true);
            gotoDialog.toFront();
            return null;
        }
    }
    
    protected synchronized void disposeGotoDialog() {
        if( gotoDialog != null ) {
            EditorState.put( BOUNDS_KEY, gotoDialog.getBounds() );
            gotoDialog.dispose();
            Utilities.returnFocus();
        }
        
        gotoDialog = null;
    }
    
    
    public void showGotoDialog(final KeyEventBlocker blocker) {
        Dialog dialog = createGotoDialog();
        if( dialog == null ) { // already visible
            // TODO:beep()
            return;
        }
        
        dialog.setVisible(true);
        gotoPanel.popupNotify(blocker);
        
        WindowAdapter winAdapt = new WindowAdapter(){
            public @Override void windowClosing(WindowEvent evt) {
                disposeGotoDialog();
            }
            
            public @Override void windowClosed(WindowEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        if (blocker!=null){
                            blocker.stopBlocking(false);
                        }
//                        Utilities.returnFocus();
                    }
                });
            }
        };
        dialog.addWindowListener(winAdapt);
    }
    
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == gotoButtons[0] || src == gotoPanel ) { // Find button
            if (performGoto()) {
                gotoPanel.updateHistory(); //A.N.: support for history
                disposeGotoDialog();
            }
        } else { // Cancel button
            disposeGotoDialog();
        }
    }
    
    /**
     * Get text value of the 
     * @return text value of goto field.
     * @since
     */
    protected final String getGotoValueText() {
        return gotoPanel.getValue();
    }
    
    /** Perform the goto operation.
     * @return whether the dialog should be made invisible or not
     */
    protected boolean performGoto() {
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        if (c != null) {
            try {
                int line = Integer.parseInt(getGotoValueText());

                //issue 188976
                if (line==0)
                    line = 1;
                //end of issue 188976
                
                BaseDocument doc = Utilities.getDocument(c);
                if (doc != null) {
                    int rowCount = Utilities.getRowCount(doc);
                    if (line > rowCount)
                        line = rowCount;
                    
                    // Obtain the offset where to jump
                    int pos = LineDocumentUtils.getLineStartFromIndex(doc, line - 1);
                    
                    BaseKit kit = Utilities.getKit(c);
                    if (kit != null) {
                        Action a = kit.getActionByName(ExtKit.gotoAction);
                        if (a instanceof ExtKit.GotoAction) {
                            pos = ((ExtKit.GotoAction)a).getOffsetFromLine(doc, line - 1);
                        }
                    }
                    
                    if (pos != -1) {
                        Caret caret = c.getCaret();
                        caret.setDot(pos);
                    } else {
                        c.getToolkit().beep();
                        return false;
                    }
                }
            } catch (NumberFormatException e) {
                c.getToolkit().beep();
                return false;
            }
        }
        return true;
    }
    
}
