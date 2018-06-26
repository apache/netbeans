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

package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * FinishableProxyWizardPanel.java - used decorator pattern to enable to finish 
 * the original wizard panel, that is not finishable
 * 
 *
 * @author mkuchtiak
 */
public class FinishableProxyWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    private WizardDescriptor.Panel<WizardDescriptor> original;
    private SourceGroup[] sourceGroups;
    private boolean checkSourceGroups;
    private WizardDescriptor settings;
    /** Creates a new instance of ProxyWizardPanel */

    public FinishableProxyWizardPanel(WizardDescriptor.Panel<WizardDescriptor> original) {
        this.original = original;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("EI_EXPOSE_REP2")
    public FinishableProxyWizardPanel(WizardDescriptor.Panel<WizardDescriptor> original, SourceGroup[] sourceGroups, boolean checkSourceGroups) {
        this.original=original;
        this.sourceGroups = sourceGroups;
        this.checkSourceGroups = checkSourceGroups;
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        original.addChangeListener(l);
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        original.removeChangeListener(l);
    }

    public void storeSettings(WizardDescriptor settings) {
        original.storeSettings(settings);
    }

    public void readSettings(WizardDescriptor settings) {
        this.settings = settings;
        original.readSettings(settings);
    }

    public boolean isValid() {
        String warningMessage = null;
        if (sourceGroups != null && sourceGroups.length == 0) {
            if (checkSourceGroups) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(FinishableProxyWizardPanel.class, "ERR_NoSources")); // NOI18N
                return false;
            } else {
                warningMessage = NbBundle.getMessage(FinishableProxyWizardPanel.class, "MSG_NoSources");
            }
        }
        boolean valid = original.isValid();
        if (valid) {
            if (warningMessage != null) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, warningMessage);
            }
        }
        return valid;
    }

    public boolean isFinishPanel() {
        return true;
    }

    public java.awt.Component getComponent() {
        return original.getComponent();
    }

    public org.openide.util.HelpCtx getHelp() {
        return original.getHelp();
    }
    
}
