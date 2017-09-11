/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

class ClusterizeWizardPanel2
implements WizardDescriptor.Panel<Clusterize>, PropertyChangeListener {
    private Component component;
    Clusterize settings;
    private boolean valid;
    private ChangeListener listener;

    ClusterizeWizardPanel2() {
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ClusterizeVisualPanel2(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.ClusterizeWizardPanel2");
    }

    void setValid(boolean valid) {
        this.valid = valid;
        ChangeListener l = this.listener;
        if (l != null) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        assert this.listener == null;
        this.listener = l;
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        if (l == this.listener) {
            this.listener = null;
        }
    }

    @Override
    public void readSettings(Clusterize settings) {
        this.settings = settings;
        settings.modules.addPropertyChangeListener(this);
        propertyChange(null);
    }

    @Override
    public void storeSettings(Clusterize settings) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (settings == null) {
            setValid(false);
            return;
        }
        ClusterizeInfo ci = settings.modules;
        setValid(ci.getSelectedFilesCount() > 0);
        if (!isValid()) {
            settings.wizardDescriptor.getNotificationLineSupport().setErrorMessage(
                NbBundle.getMessage(ClusterizeWizardPanel2.class, "MSG_ClusterizeNothingSelected")
            );
        } else {
            settings.wizardDescriptor.getNotificationLineSupport().clearMessages();
        }
    }
}

