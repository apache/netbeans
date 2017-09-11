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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.wizards;

import org.netbeans.modules.autoupdate.ui.*;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallUnitWizard {
    
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    
    /** Creates a new instance of InstallUnitWizard */
    public UninstallUnitWizard () {}
    
    public boolean invokeWizard () {
        return invokeWizardImpl (true, null);
    }
    
    public boolean invokeWizard (boolean doEnable) {
        return invokeWizardImpl (null, doEnable ? Boolean.TRUE : Boolean.FALSE);
    }
    
    private boolean invokeWizardImpl (Boolean doUninstall, Boolean doEnable) {
        assert doUninstall != null || doEnable != null : "At least one action is enabled";
        assert ! (doUninstall != null && doEnable != null) : "Only once action is enabled";
        assert doUninstall == null || Containers.forUninstall () != null : "The OperationContainer<OperationSupport> forUninstall must exist!";
        assert doUninstall != null || !doEnable || (doEnable && Containers.forEnable () != null) : "The OperationContainer<OperationSupport> forEnable must exist!";
        assert doUninstall != null || doEnable || (! doEnable && Containers.forDisable () != null) : "The OperationContainer<OperationSupport> forDisable must exist!";
        
        UninstallUnitWizardModel model = new UninstallUnitWizardModel (doUninstall != null
                ? OperationWizardModel.OperationType.UNINSTALL : doEnable ? OperationWizardModel.OperationType.ENABLE : OperationWizardModel.OperationType.DISABLE);
        WizardDescriptor.Iterator<WizardDescriptor> iterator = new UninstallUnitWizardIterator (model);
        WizardDescriptor wizardDescriptor = new WizardDescriptor (iterator);
        wizardDescriptor.setModal (true);
        
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wizardDescriptor.setTitleFormat (new MessageFormat("{1}"));
        wizardDescriptor.setTitle (NbBundle.getMessage (UninstallUnitWizard.class, "UninstallUnitWizard_Title"));
        
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        dialog.setVisible (true);
        dialog.toFront ();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        log.log (Level.FINE, "InstallUnitWizard returns with value " + wizardDescriptor.getValue ());
        return !cancelled;
    }
    
}
