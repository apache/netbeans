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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.WizardDescriptor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerLocationPanel implements WizardDescriptor.FinishablePanel, ChangeListener {

    private static final String J2SE_PLATFORM_VERSION_17 = "1.7"; // NOI18N

    private final WildflyInstantiatingIterator instantiatingIterator;

    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private final transient Set listeners = new HashSet(1);

    public AddServerLocationPanel(WildflyInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }

    @Override
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }

    private void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_jboss_location"); //NOI18N
    }

    @Override
    public boolean isValid() {
        String locationStr = component.getInstallLocation();
        if (locationStr == null || locationStr.trim().length() < 1) {
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    NbBundle.getMessage(AddServerLocationPanel.class, "MSG_SpecifyServerLocation")); // NOI18N
            return false;
        }

        File path = new File(locationStr);
        if (!WildflyPluginUtils.isGoodJBServerLocation(path)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidServerLocation")); // NOI18N
            return false;
        }

        // test if IDE is run on correct JDK version
        if (!runningOnCorrectJdk(path)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidJDK"));
            return false;
        }

        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        WildflyPluginProperties.getInstance().setInstallLocation(component.getInstallLocation());
        WildflyPluginProperties.getInstance().setConfigLocation(component.getConfigurationLocation());
        WildflyPluginProperties.getInstance().setDomainLocation(component.getConfigurationLocation());
        WildflyPluginProperties.getInstance().saveProperties();
        instantiatingIterator.setInstallLocation(locationStr);
        instantiatingIterator.setAdminPort("" + WildflyPluginProperties.getInstance().getAdminPort());
        return true;
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }

    @Override
    public void storeSettings(Object settings) {
        String installLocation = ((AddServerLocationVisualPanel) getComponent()).getInstallLocation();
        if (installLocation == null) {
            return;
        }
        instantiatingIterator.setInstallLocation(installLocation);
        instantiatingIterator.setConfigFile(
                ((AddServerLocationVisualPanel) getComponent()).getConfigurationLocation());
        instantiatingIterator.setServer("standalone");
        String serverPath = ((AddServerLocationVisualPanel) getComponent()).getInstallLocation() + File.separatorChar + "standalone";
        instantiatingIterator.setServerPath(serverPath);
        instantiatingIterator.setDeployDir(WildflyPluginUtils.getDeployDir(serverPath));
        instantiatingIterator.setAdminPort("" + WildflyPluginProperties.getInstance().getAdminPort());
        instantiatingIterator.setHost("localhost");
        instantiatingIterator.setPort("8080");
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    private boolean runningOnCorrectJdk(File path) {
        SpecificationVersion defPlatVersion = JavaPlatformManager.getDefault()
                .getDefaultPlatform().getSpecification().getVersion();
        // WF10 requires JDK8+
        if (!J2SE_PLATFORM_VERSION_17.equals(defPlatVersion.toString())) {
            return true;
        }
        WildflyPluginUtils.Version version = WildflyPluginUtils.getServerVersion(path);
        return version != null && version.compareToIgnoreUpdate(WildflyPluginUtils.WILDFLY_10_0_0) < 0;
    }
}
