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
package org.netbeans.modules.hibernateweb.framework;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 * Provides Framework extension for Hibernate.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateWebModuleExtender extends WebModuleExtender {

    private HibernateConfigurationPanel configPanel = null;
    private static final String DEFAULT_CONFIG_FILENAME = "hibernate.cfg";
    private final String dialect = "hibernate.dialect";
    private final String driver = "hibernate.connection.driver_class";
    private final String url = "hibernate.connection.url";
    private final String userName = "hibernate.connection.username";
    private final String password = "hibernate.connection.password";
    
    private Logger logger = Logger.getLogger(HibernateWebModuleExtender.class.getName());

    public HibernateWebModuleExtender(boolean forNewProjectWizard,
            WebModule webModule, ExtenderController controller) {
        configPanel = new HibernateConfigurationPanel(this, controller, forNewProjectWizard);
        if (!forNewProjectWizard) {
            // Show the config panel for Proj. Customizer
            // Fill the panel with existing data.
            showConfigPanelForCustomizer(webModule);
        }
    }
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    @Override
    public JComponent getComponent() {
        return configPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void update() {
    }

    @Override
    public boolean isValid() {
        return configPanel.isPanelValid();
    }

    @Override
    public Set<FileObject> extend(WebModule webModule) {
        Project enclosingProject = Util.getEnclosingProjectFromWebModule(webModule);

        // when there is no enclosing project found empty set is returned
        if (enclosingProject!=null) {
            Sources sources = ProjectUtils.getSources(enclosingProject);
            try {
                SourceGroup[] javaSourceGroup = sources.getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                if (javaSourceGroup == null || javaSourceGroup.length == 0) {
                    javaSourceGroup = sources.getSourceGroups(
                            JavaProjectConstants.SOURCES_TYPE_JAVA);
                }
                if (javaSourceGroup != null && javaSourceGroup.length != 0) {
                    FileObject targetFolder = javaSourceGroup[0].getRootFolder();
                    CreateHibernateConfiguration createHibernateConfiguration =
                            new CreateHibernateConfiguration(targetFolder, enclosingProject);
                    targetFolder.getFileSystem().runAtomicAction(createHibernateConfiguration);

                    return createHibernateConfiguration.getCreatedFiles();
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return Collections.EMPTY_SET;
    }

    private void showConfigPanelForCustomizer(WebModule webModule) {
        Project enclosingProject = Util.getEnclosingProjectFromWebModule(webModule);
        HibernateEnvironment he = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
        List<FileObject> configFileObjects = he.getAllHibernateConfigFileObjects();
        for (FileObject configFile : configFileObjects) {
            if (configFile.getName().equals(DEFAULT_CONFIG_FILENAME)) {
                try {
                    HibernateCfgDataObject hibernateDO = (HibernateCfgDataObject) DataObject.find(configFile);
                    SessionFactory sessionFactory = hibernateDO.getHibernateConfiguration().getSessionFactory();
                    int index = 0;
                    for (String propValue : sessionFactory.getProperty2()) {
                        String propName = sessionFactory.getAttributeValue(SessionFactory.PROPERTY2, index++, "name");  //NOI18N

                        if (dialect.contains(propName)) {
                            configPanel.setDialect(propValue);
                        }
                        if (url.contains(propName)) {
                            configPanel.setDatabaseConnection(propValue);
                        }
                    }

                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
            configPanel.disable();
        }
    }

    private class CreateHibernateConfiguration implements FileSystem.AtomicAction {

        private FileObject targetFolder;
        private Project enclosingProject;
        private Set<FileObject> createdFilesSet = new LinkedHashSet<FileObject>();

        public CreateHibernateConfiguration(FileObject targetFolder, Project enclosingProject) {
            this.targetFolder = targetFolder;
            this.enclosingProject = enclosingProject;
        }

        public Set<FileObject> getCreatedFiles() {
            return createdFilesSet;
        }

        public void run() throws IOException {
            DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
            FileObject templateFileObject = FileUtil.getConfigFile("Templates/Hibernate/Hibernate.cfg.xml");  //NOI18N

            DataObject templateDataObject = DataObject.find(templateFileObject);


            DataObject newOne = templateDataObject.createFromTemplate(
                    targetDataFolder,
                    DEFAULT_CONFIG_FILENAME);
            SessionFactory sFactory = new SessionFactory();

            int row = 0;

            if (configPanel.getSelectedDialect() != null && !"".equals(configPanel.getSelectedDialect())) {
                row = sFactory.addProperty2(configPanel.getSelectedDialect());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", dialect);
            }

            if (configPanel.getSelectedDriver() != null && !"".equals(configPanel.getSelectedDriver())) {
                row = sFactory.addProperty2(configPanel.getSelectedDriver());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", driver);
            }
            if (configPanel.getSelectedURL() != null && !"".equals(configPanel.getSelectedURL())) {
                row = sFactory.addProperty2(configPanel.getSelectedURL());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", url);
            }

            if (configPanel.getUserName() != null && !"".equals(configPanel.getUserName())) {
                row = sFactory.addProperty2(configPanel.getUserName());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", userName);
            }

            if (configPanel.getPassword() != null && !"".equals(configPanel.getPassword())) {
                row = sFactory.addProperty2(configPanel.getPassword());
                sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", password);
            }


            HibernateCfgDataObject hdo = (HibernateCfgDataObject) newOne;
            hdo.addSessionFactory(sFactory);
            hdo.save();
            // Register Hibernate Library in the project if its not already registered.
            HibernateEnvironment hibernateEnvironment = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
            logger.info("Library registered : " + hibernateEnvironment.addHibernateLibraryToProject(hdo.getPrimaryFile()));
            // Register DB driver if possible.
            String selectedDriver = configPanel.getSelectedDriver();
            if(!hibernateEnvironment.canLoadDBDriver(hdo.getHibernateConfiguration()) && (selectedDriver != null)) {
                logger.info("DB Driver not registered with the project. Registering now..");
                logger.info("DB Driver registered : " + hibernateEnvironment.registerDBDriver(selectedDriver, hdo.getPrimaryFile()));
            }            
            createdFilesSet.add(hdo.getPrimaryFile());
        }
    }

}


