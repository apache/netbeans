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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class MdbLocationPanel implements WizardDescriptor.FinishablePanel {

    private MdbLocationPanelVisual locationPanel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final WizardDescriptor wizardDescriptor;
    private final EJBNameOptions ejbNames;

    public MdbLocationPanel(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
        this.ejbNames = new EJBNameOptions();
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    @Override
    public boolean isValid() {
        Project project = Templates.getProject(wizardDescriptor);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        String j2eeVersion = j2eeModuleProvider.getJ2eeModule().getModuleVersion();
        if (!EjbJar.VERSION_3_2.equals(j2eeVersion) && !EjbJar.VERSION_3_1.equals(j2eeVersion) && !EjbJar.VERSION_3_0.equals(j2eeVersion) && !EjbJar.VERSION_2_1.equals(j2eeVersion)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(MdbLocationPanel.class,"MSG_WrongJ2EESpecVersion")); //NOI18N
            return false;
        }

        FileObject targetFolder = (FileObject) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_FOLDER);
        if (targetFolder != null) {
            String targetName = (String) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_NAME);
            String name = ejbNames.getMessageDrivenEjbClassPrefix() + targetName + ejbNames.getMessageDrivenEjbClassSuffix();
            if (targetFolder.getFileObject(name + ".java") != null) { // NOI18N
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                        NbBundle.getMessage(MdbLocationPanel.class, "ERR_FileAlreadyExists", name + ".java")); //NOI18N
                return false;
            }
        }

        // component/panel validation
        getComponent();
        if (locationPanel.getDestination() == null) {
            wizardDescriptor.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE, //NOI18N
                    NbBundle.getMessage(MdbLocationPanel.class, "ERR_NoDestinationSelected"));
            return false;
        }

        if (!locationPanel.isServerConfigured()) {
            wizardDescriptor.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE, //NOI18N
                    NbBundle.getMessage(MdbLocationPanel.class, "ERR_MissingServer"));
        }
        return true;
    }

    @Override
    public void readSettings(Object settings) {
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor descriptor = (WizardDescriptor) settings;
        locationPanel.store(descriptor);
    }

    @Override
    public boolean isFinishPanel() {
        return isValid();
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MessageEJBWizardPanel");
    }

    @Override
    public java.awt.Component getComponent() {
        if (locationPanel == null) {
            final Project project = Templates.getProject(wizardDescriptor);
            final J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            MessageDestinationUiSupport.DestinationsHolder holder =
                    MessageDestinationUiSupport.getDestinations(project, j2eeModuleProvider);
            locationPanel = MdbLocationPanelVisual.newInstance(
                    project,
                    j2eeModuleProvider,
                    holder.getModuleDestinations(),
                    holder.getServerDestinations());
            locationPanel.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            if (event.getPropertyName().equals(MdbLocationPanelVisual.SCANNED)) {
                                MessageDestinationUiSupport.DestinationsHolder destinations =
                                        MessageDestinationUiSupport.getDestinations(project, j2eeModuleProvider);
                                locationPanel.refreshDestinations(
                                        destinations.getModuleDestinations(),
                                        destinations.getServerDestinations());
                            }
                            fireChangeEvent();
                        }
                    });
        }
        return locationPanel;
    }

    /**
     * @see MessageDestinationPanel#getDestination()
     */
    public MessageDestination getDestination() {
        return locationPanel.getDestination();
    }

    protected static ClasspathInfo getClassPathInfo(Project project) {
        return ClasspathInfo.create(
                getClassPath(project, ClassPath.BOOT),
                getClassPath(project, ClassPath.COMPILE),
                getClassPath(project, ClassPath.SOURCE));
    }

    /**
     * Returns classpath for given type.
     * @param type a classpath type such as {@link ClassPath#COMPILE}
     * @return generated read-only project's classpath of given type
     */
    private static ClassPath getClassPath(Project project, String type) {
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) {
            return null;
        }

        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) {
            return null;
        }

        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA); //NOII18N
        ClassPath[] paths = new ClassPath[sourceGroups.length];
        int i = 0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[i] = provider.findClassPath(rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath(paths);
    }
}
