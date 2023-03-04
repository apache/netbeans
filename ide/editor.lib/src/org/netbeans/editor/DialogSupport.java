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

package org.netbeans.editor;

import java.awt.Dialog;
import java.awt.event.*;
import javax.swing.*;

/**
 * DialogSupport is factory based class for creating dialogs of certain
 * behaviour. It is intended to be used whenever editor needs to popup a dialog.
 * It presents a way for changing the implementation of the dialog depending
 * on the enviroment the Editor is embeded in.
 *
 * @author  pnejedly
 * @version 1.0
 * @deprecated See org.openide.spi.editor.lib2.DialogFactory. DialogSupport has
 * no public replacement.
 */
@Deprecated
public class DialogSupport {

    /** Noone needs to instantiate the dialog support */
    private DialogSupport() {
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
    public static Dialog createDialog( String title, JPanel panel, boolean modal,
                JButton[] buttons, boolean sidebuttons, int defaultIndex, int cancelIndex,
                ActionListener listener
    ) {
        return org.netbeans.modules.editor.lib2.DialogSupport.getInstance().createDialog(
            title, panel, modal, buttons, sidebuttons, defaultIndex, cancelIndex, listener
        );
    }
    
    /** The method for setting custom factory for creating dialogs via
     * the {@link #createDialog(java.lang.String, javax.swing.JPanel, boolean, javax.swing.JButton[], boolean, int, int, java.awt.event.ActionListener) createDialog} method.
     * If no factory is set, the {@link DialogSupport.DefaultDialogFactory DefaultDialogFactory}
     * will be used.
     * @param factory the {@link DialogSupport.DialogFactory DialogFactory}
     * implementation that will be responsible for providing dialogs.
     *
     * @see DialogSupport.DialogFactory
     * @see DialogSupport.DefaultDialogFactory
     */
    public static void setDialogFactory( DialogFactory factory ) {
        org.netbeans.modules.editor.lib2.DialogSupport.getInstance().setExternalDialogFactory(new Wrapper(factory));
    }
    
    /**
     * DialogFactory implementation is a class responsible for providing
     * proper implementation of Dialog containing required widgets.
     * It can provide the dialog itself or delegate the functionality
     * to another piece of code, e.g some windowing system. 
     */
    public static interface DialogFactory {
        
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
         * @param cancelIndex The index of cancel button - the button that will
         *    be <I>pressed</I> when closing the dialog.
         * @param listener The listener which will be notified of all button
         *    events.
         * @return newly created <CODE>Dialog</CODE>
         */
        public Dialog createDialog( String title, JPanel panel, boolean modal,
                JButton[] buttons, boolean sidebuttons, int defaultIndex,
                int cancelIndex, ActionListener listener );
    } // End of DialogFactory interface
    
    private static final class Wrapper implements org.netbeans.modules.editor.lib2.DialogFactory {
        
        private DialogFactory origFactory;
        
        public Wrapper(DialogFactory origFactory) {
            this.origFactory = origFactory;
        }
        
        public Dialog createDialog(
            String title, JPanel panel, boolean modal, 
            JButton[] buttons, boolean sidebuttons, int defaultIndex, 
            int cancelIndex, ActionListener listener)
        {
            return origFactory.createDialog(title, panel, modal, 
                buttons, sidebuttons, defaultIndex, cancelIndex, listener);
        }
    } // End of Wraper class
    
}
