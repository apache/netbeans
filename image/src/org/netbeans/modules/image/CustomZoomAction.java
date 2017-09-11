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


package org.netbeans.modules.image;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/** Action that can always be invoked and work procedurally.
 *
 * @author  Lukas Tadial
 */
@ActionID(id = "org.netbeans.modules.image.CustomZoomAction", category = "View")
@ActionRegistration(lazy = false, displayName = "#LBL_CustomZoom")
public class CustomZoomAction extends CallableSystemAction {

    
    /** Generated serial version UID. */
    static final long serialVersionUID = 8247068408606777895L;
    
    
    /** Actually performs action. */
    public void performAction () {
        final Dialog[] dialogs = new Dialog[1];
        final CustomZoomPanel zoomPanel = new CustomZoomPanel();

        zoomPanel.setEnlargeFactor(1);
        zoomPanel.setDecreaseFactor(1);
        
        DialogDescriptor dd = new DialogDescriptor(
            zoomPanel,
            NbBundle.getBundle(CustomZoomAction.class).getString("LBL_CustomZoomAction"),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                        int enlargeFactor = 1, decreaseFactor = 1;
                        
                        try {
                            enlargeFactor = zoomPanel.getEnlargeFactor();
                            decreaseFactor = zoomPanel.getDecreaseFactor();
                        } catch (NumberFormatException nfe) {
                            notifyInvalidInput();
                            return;
                        }
                        
                        // Invalid values.
                        if(enlargeFactor == 0 || decreaseFactor == 0) {
                            notifyInvalidInput();
                            return;
                        }
                        
                        performZoom(enlargeFactor, decreaseFactor);
                        
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }        
                
                private void notifyInvalidInput() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getBundle(CustomZoomAction.class).getString("MSG_InvalidValues"),
                        NotifyDescriptor.ERROR_MESSAGE
                    ));
                }
                
            } // End of annonymnous ActionListener.
        );
        dialogs[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialogs[0].setVisible(true);
        
    }

    /** Performs customized zoom. */
    private void performZoom(int enlargeFactor, int decreaseFactor) {
        TopComponent currentComponent = TopComponent.getRegistry().getActivated();
        if(currentComponent instanceof ImageViewer)
            ((ImageViewer)currentComponent).customZoom(enlargeFactor, decreaseFactor);
    }

    /** Gets action name. Implements superclass abstract method. */
    public String getName () {
        return NbBundle.getBundle(CustomZoomAction.class).getString("LBL_CustomZoom");
    }
    
    /** Gets action help context. Implemenets superclass abstract method.*/
    public HelpCtx getHelpCtx () {
        return new HelpCtx(CustomZoomAction.class);
    }
    
    /** Gets icon resource. Overrides superclass method. */
    protected String iconResource() {
        return "org/netbeans/modules/image/customZoom.gif"; // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
