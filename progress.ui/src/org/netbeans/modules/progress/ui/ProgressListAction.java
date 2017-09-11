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

package org.netbeans.modules.progress.ui;

import org.netbeans.modules.progress.spi.Controller;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.modules.progress.spi.SwingController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@ActionID(id = "org.netbeans.modules.progress.ui.ProgressListAction", category = "Window")
@ActionRegistration(displayName = "#CTL_ProcessListAction", lazy=false)
@ActionReference(position = 1000, name = "ProgressListAction", path = "Menu/Window/Tools")
public class ProgressListAction extends AbstractAction implements ListDataListener, Runnable {

    /** Creates a new instance of ProcessListAction */
    public ProgressListAction() {
        this(NbBundle.getMessage(ProgressListAction.class, "CTL_ProcessListAction"));
    }
    
    public ProgressListAction(String name) {
        putValue(NAME, name);
//        putValue(MNEMONIC_KEY, new Integer((int)NbBundle.getMessage(ProgressListAction.class, "ProcessListAction.mnemonic").charAt(0)));
        Controller.getDefault().getModel().addListDataListener(this);
        updateEnabled();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
       //need to invoke later becauseotherwise the awtlistener possibly catches a mouse event
        SwingUtilities.invokeLater(this);
    }
    
    public void run() {
        ((ProgressUIWorkerWithModel)SwingController.getDefault().getVisualComponent()).showPopup();
    }

    private void updateEnabled() {
        setEnabled(Controller.getDefault().getModel().getSize() != 0);
    }    

    public void contentsChanged(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalAdded(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

}
