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
