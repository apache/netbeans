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
                    int pos = Utilities.getRowStartFromLineOffset(doc, line - 1);
                    
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
