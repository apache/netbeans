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

package org.netbeans.modules.editor.lib2;

import java.awt.Insets;
import java.awt.Dialog;
import java.awt.event.*;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.openide.util.Lookup;

/**
 * DialogSupport is factory based class for creating dialogs of certain
 * behaviour. It is intended to be used whenever editor needs to popup a dialog.
 * It presents a way for changing the implementation of the dialog depending
 * on the enviroment the Editor is embeded in.
 *
 * @author  pnejedly
 * @version 1.0
 */
public final class DialogSupport {

    private static DialogSupport instance;
    
    private DialogFactory externalFactory;
    private Lookup.Result<DialogFactory> result;

    public static synchronized DialogSupport getInstance() {
        if (instance == null) {
            instance = new DialogSupport();
        }
        return instance;
    }
    
    /** Noone needs to instantiate the dialog support */
    private DialogSupport() {
        result = Lookup.getDefault().lookup(new Lookup.Template<DialogFactory>(DialogFactory.class));
    }

    /** 
     * The method for creating a dialog with specified properties.
     * @param title The title of created dialog.
     * @param panel The content of the dialog to be displayed.
     * @param modal Whether the dialog should be modal.
     * @param buttons The array of JButtons to be added to the dialog.
     * @param sidebuttons The buttons could be placed under the panel (false),
     *      or on the right side of the panel (true).
     * @param defaultIndex The index of default button in the buttons array,
     *    if <CODE>index < 0</CODE>, no default button is set.
     * @param cancelIndex The index about cancel button - the button that will
     *    be <I>pressed</I> when closing the dialog.
     * @param listener The listener which will be notified of all button
     *    events.
     * @return newly created <CODE>Dialog</CODE>
     */
    public Dialog createDialog( 
        String title, JPanel panel, boolean modal,
        JButton[] buttons, boolean sidebuttons, int defaultIndex, int cancelIndex,
        ActionListener listener
    ) {
        DialogFactory factory = null;
        
        if( externalFactory != null ) {
            factory = externalFactory;
        } else {
            Collection<? extends DialogFactory> factories = result.allInstances();
            if (factories.isEmpty()) {
                factory = new DefaultDialogFactory();
            } else {
                factory = factories.iterator().next();
            }
        }
        
        return factory.createDialog(title, panel, modal, buttons, sidebuttons,
                defaultIndex, cancelIndex, listener );
    }
    
    /** The method for setting custom factory for creating dialogs via
     * the {@link #createDialog(java.lang.String, javax.swing.JPanel, boolean, javax.swing.JButton[], boolean, int, int, java.awt.event.ActionListener) createDialog} method.
     * If no factory is set, the {@link DialogSupport.DefaultDialogFactory DefaultDialogFactory}
     * will be used.
     * 
     * <p><b>IMPORTANT:</b> This method is here only for supporting the backwards
     * compatibility of the {@link org.netbeans.editor.DialogSupport} class.
     * 
     * @param factory the {@link DialogSupport.DialogFactory DialogFactory}
     * implementation that will be responsible for providing dialogs.
     */
    public void setExternalDialogFactory( DialogFactory factory ) {
        externalFactory = factory;
    }
    
    /** The DialogFactory that will be used to create Dialogs if no other
     * DialogFactory is set to DialogSupport.
     */
    private static class DefaultDialogFactory extends WindowAdapter implements DialogFactory, ActionListener {
        
        private JButton cancelButton;
        
        /** Create a panel with buttons that will be placed according
         * to the required alignment */
        private JPanel createButtonPanel( JButton[] buttons, boolean sidebuttons ) {
            int count = buttons.length;
            
            JPanel outerPanel = new JPanel( new BorderLayout() );
            outerPanel.setBorder( new EmptyBorder( new Insets(
                    sidebuttons ? 5 : 0, sidebuttons ? 0 : 5, 5, 5 ) ) );

            LayoutManager lm = new GridLayout( // GridLayout makes equal cells
                    sidebuttons ? count : 1, sidebuttons ? 1 : count,  5, 5 );
                
            JPanel innerPanel = new JPanel( lm );
            
            for( int i = 0; i < count; i++ ) innerPanel.add( buttons[i] );
            
            outerPanel.add( innerPanel,
                sidebuttons ? BorderLayout.NORTH : BorderLayout.EAST ) ;
            return outerPanel;
        }
        
        public Dialog createDialog( String title, JPanel panel, boolean modal,
                JButton[] buttons, boolean sidebuttons, int defaultIndex,
                int cancelIndex, ActionListener listener ) {

            // create the dialog with given content
            JDialog d = new JDialog( (javax.swing.JFrame)null, title, modal );
            d.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
            d.getContentPane().add( panel, BorderLayout.CENTER);
            
            // Add the buttons to it
            JPanel buttonPanel = createButtonPanel( buttons, sidebuttons );
            String buttonAlign = sidebuttons ? BorderLayout.EAST : BorderLayout.SOUTH;
            d.getContentPane().add( buttonPanel, buttonAlign );

            // add listener to buttons
            if( listener != null ) {
                for( int i = 0; i < buttons.length; i++ ) {
                    buttons[i].addActionListener( listener );
                }
            }

            // register the default button, if available
            if( defaultIndex >= 0 ) {
                d.getRootPane().setDefaultButton( buttons[defaultIndex] );
            }
            
            // register the cancel button helpers, if available
            if( cancelIndex >= 0 ) {
                cancelButton = buttons[cancelIndex];
                // redirect the Esc key to Cancel button
                d.getRootPane().registerKeyboardAction(
                    this,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
                );

                // listen on windowClosing and redirect it to Cancel button
                d.addWindowListener( this );
            }

            d.pack();
            return d;
        }
        
        public void actionPerformed(ActionEvent evt) {
            cancelButton.doClick( 10 );
        }

        public void windowClosing( WindowEvent evt ) {
            cancelButton.doClick( 10 );
        }
    } // End of DefaultDialogFactory class
}
