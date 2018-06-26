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

package org.netbeans.modules.tomcat5.ui.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Add Tomcat wizard descriptor panel implementation.
 */
class InstallPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List listeners = new ArrayList();
    private WizardDescriptor wizard;
    private InstallPanelVisual component;

    public InstallPanel() {
        super();
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public void storeSettings(Object settings) {
    }

    public void readSettings(Object settings) {
        wizard = (WizardDescriptor)settings;
    }

    public boolean isValid() {
        boolean result = getVisual().hasValidData();
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(
                getVisual().isInfoMessage() ? WizardDescriptor.PROP_INFO_MESSAGE : WizardDescriptor.PROP_ERROR_MESSAGE,
                getVisual().getErrorMessage());
        return result;
    }

    public java.awt.Component getComponent() {
        if (component == null) {
            component = new InstallPanelVisual();
            component.addChangeListener(this);
        }

        return component;
    }

    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx("tomcat_addinstall"); // NOI18N
    }

    public void stateChanged(javax.swing.event.ChangeEvent event) {
        fireChange(event);
    }

    public InstallPanelVisual getVisual() {
        return (InstallPanelVisual)getComponent();
    }

    private void fireChange(ChangeEvent event) {
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext())
            ((ChangeListener)iter.next()).stateChanged(event);
    }
}
