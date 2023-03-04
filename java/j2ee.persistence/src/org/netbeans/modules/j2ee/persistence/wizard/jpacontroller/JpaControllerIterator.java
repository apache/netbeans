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
package org.netbeans.modules.j2ee.persistence.wizard.jpacontroller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Pavel Buzek
 */
public class JpaControllerIterator implements TemplateWizard.Iterator {

    private int index;
    private transient WizardDescriptor.Panel[] panels;
    private PersistenceUnitWizardDescriptor puPanel;
    private static final String[] EXCEPTION_CLASS_NAMES = {"IllegalOrphanException", "NonexistentEntityException", "PreexistingEntityException", "RollbackFailureException"};
    public static final String EXCEPTION_FOLDER_NAME = "exceptions"; //NOI18N
    private static String RESOURCE_FOLDER = "org/netbeans/modules/j2ee/persistence/wizard/jpacontroller/resources/"; //NOI18N

    @Override
    public Set instantiate(TemplateWizard wizard) throws IOException {
        final List<String> entities = (List<String>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
        final Project project = Templates.getProject(wizard);
        final FileObject jpaControllerPackageFileObject = Templates.getTargetFolder(wizard);
        final String jpaControllerPackage = (String) wizard.getProperty(WizardProperties.JPA_CONTROLLER_PACKAGE);

        boolean createPersistenceUnit = (Boolean) wizard.getProperty(org.netbeans.modules.j2ee.persistence.wizard.WizardProperties.CREATE_PERSISTENCE_UNIT);

        if (createPersistenceUnit) {
                PersistenceUnit punit = Util.buildPersistenceUnitUsingData(project, puPanel.getPersistenceUnitName(), puPanel.getPersistenceConnection()!=null ? puPanel.getPersistenceConnection().getName() : puPanel.getDatasource(), TableGeneration.NONE, puPanel.getSelectedProvider());
                ProviderUtil.setTableGeneration(punit, puPanel.getTableGeneration(), puPanel.getSelectedProvider());
                if (punit != null){
                    Util.addPersistenceUnitToProject( project, punit );
                }
        }

        final String title = NbBundle.getMessage(JpaControllerIterator.class, "TITLE_Progress_Jpa_Controller"); //NOI18N
        final ProgressContributor progressContributor = AggregateProgressFactory.createProgressContributor(title);
        final AggregateProgressHandle handle =
                AggregateProgressFactory.createHandle(title, new ProgressContributor[]{progressContributor}, null, null);
        final ProgressPanel progressPanel = new ProgressPanel();
        final JComponent progressComponent = AggregateProgressFactory.
            createProgressComponent(handle);
        
        final ProgressReporter reporter = new ProgressReporterDelegate( 
                progressContributor, progressPanel ); 

        final Runnable r = () -> {
            try {
                handle.start();
                int progressStepCount = getProgressStepCount(entities.size());
                progressContributor.start(progressStepCount);
                generateJpaControllers(reporter, entities, project,
                        jpaControllerPackage, jpaControllerPackageFileObject,
                        null, true);
                progressContributor.progress(progressStepCount);
            } catch (IOException ioe) {
                Logger.getLogger(JpaControllerIterator.class.getName()).log(Level.INFO, null, ioe);
                NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } finally {
                progressContributor.finish();
                SwingUtilities.invokeLater( () -> progressPanel.close() );
                handle.finish();
            }
        };

        // Ugly hack ensuring the progress dialog opens after the wizard closes. Needed because:
        // 1) the wizard is not closed in the AWT event in which instantiate() is called.
        //    Instead it is closed in an event scheduled by SwingUtilities.invokeLater().
        // 2) when a modal dialog is created its owner is set to the foremost modal
        //    dialog already displayed (if any). Because of #1 the wizard will be
        //    closed when the progress dialog is already open, and since the wizard
        //    is the owner of the progress dialog, the progress dialog is closed too.
        // The order of the events in the event queue:
        // -  this event
        // -  the first invocation event of our runnable
        // -  the invocation event which closes the wizard
        // -  the second invocation event of our runnable

        SwingUtilities.invokeLater(new Runnable() {

            private boolean first = true;

            @Override
            public void run() {
                if (!first) {
                    RequestProcessor.getDefault().post(r);
                    progressPanel.open(progressComponent, title);
                } else {
                    first = false;
                    SwingUtilities.invokeLater(this);
                }
            }
        });

        return Collections.singleton(DataFolder.findFolder(jpaControllerPackageFileObject));
    }

    public static int getProgressStepCount(int entityCount) {
        return EXCEPTION_CLASS_NAMES.length + entityCount + 2;
    }

    public static FileObject[] generateJpaControllers(ProgressReporter reporter,
            List<String> entities, Project project, String jpaControllerPackage, 
            FileObject jpaControllerPackageFileObject, 
            JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, 
            boolean evenIfExists) throws IOException 
    {
        int progressIndex = 0;
        String progressMsg = NbBundle.getMessage(JpaControllerIterator.class, 
                "MSG_Progress_Jpa_Exception_Pre"); //NOI18N
        reporter.progress(progressMsg, progressIndex++);

        FileObject exceptionFolder = jpaControllerPackageFileObject.
            getFileObject(EXCEPTION_FOLDER_NAME);
        if (exceptionFolder == null) {
            exceptionFolder = FileUtil.createFolder(jpaControllerPackageFileObject, 
                    EXCEPTION_FOLDER_NAME);
        }

        String exceptionPackage = jpaControllerPackage == null || 
            jpaControllerPackage.length() == 0 ? EXCEPTION_FOLDER_NAME : 
                jpaControllerPackage + "." + EXCEPTION_FOLDER_NAME;

        int exceptionClassCount = Util.isContainerManaged(project) ? 
                EXCEPTION_CLASS_NAMES.length : EXCEPTION_CLASS_NAMES.length - 1;
        for (int i = 0; i < exceptionClassCount; i++) {
            if (exceptionFolder.getFileObject(EXCEPTION_CLASS_NAMES[i], "java") == null) {
                progressMsg = NbBundle.getMessage(JpaControllerIterator.class, 
                        "MSG_Progress_Jpa_Now_Generating", 
                        EXCEPTION_CLASS_NAMES[i] + ".java");//NOI18N
                reporter.progress(progressMsg, progressIndex++);
                String content = JpaControllerUtil.readResource(
                        JpaControllerUtil.class.getClassLoader().
                        getResourceAsStream(RESOURCE_FOLDER + 
                                EXCEPTION_CLASS_NAMES[i] + ".java.txt"), "UTF-8"); //NOI18N
                content = content.replace("__PACKAGE__", exceptionPackage);
                FileObject target = FileUtil.createData(exceptionFolder, 
                        EXCEPTION_CLASS_NAMES[i] + ".java");//NOI18N
                String projectEncoding = JpaControllerUtil.
                    getProjectEncodingAsString(project, target);
                JpaControllerUtil.createFile(target, content, projectEncoding);  
            } else {
                reporter.progress(null, progressIndex++);
            }
        }

        progressMsg = NbBundle.getMessage(JpaControllerIterator.class, 
                "MSG_Progress_Jpa_Controller_Pre"); //NOI18N;
        reporter.progress(progressMsg, progressIndex++);

        int[] nameAttemptIndices = null;
        if (evenIfExists) {
            nameAttemptIndices = new int[entities.size()];
        }
        FileObject[] controllerFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String simpleControllerNameBase = simpleClassName + "JpaController"; //NOI18N
            String simpleControllerName = simpleControllerNameBase;
            if (evenIfExists) {
                while (jpaControllerPackageFileObject.getFileObject(
                        simpleControllerName, "java") != null && // NOI18N 
                        nameAttemptIndices[i] < 1000) 
                {
                    simpleControllerName = simpleControllerNameBase + ++nameAttemptIndices[i];
                }
            }
            if (jpaControllerPackageFileObject.getFileObject(simpleControllerName, 
                    "java") == null)            // NOI18N 
            {
                controllerFileObjects[i] = GenerationUtils.createClass(
                        jpaControllerPackageFileObject, simpleControllerName, null);
            }
        }

        if (embeddedPkSupport == null) {
            embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
        }

        for (int i = 0; i < controllerFileObjects.length; i++) {

            if (controllerFileObjects[i] == null) {
                reporter.progress(null, progressIndex++);
                continue;
            }
            String entityClass = entities.get(i);
            String controller = ((jpaControllerPackage == null || 
                    jpaControllerPackage.length() == 0) ? "" : 
                        jpaControllerPackage + ".") + controllerFileObjects[i].getName();

            progressMsg = NbBundle.getMessage(JpaControllerIterator.class, 
                    "MSG_Progress_Jpa_Now_Generating", 
                    controllerFileObjects[i].getName() + ".java");//NOI18N
            reporter.progress(progressMsg, progressIndex++);

            JpaControllerGenerator.generateJpaController(project, entityClass, 
                    controller, exceptionPackage, jpaControllerPackageFileObject, 
                    controllerFileObjects[i], embeddedPkSupport);
        }
        PersistenceUtils.logUsage(JpaControllerIterator.class, 
                "USG_PERSISTENCE_CONTROLLER_CREATED",           //NOI18N
                new Integer[]{controllerFileObjects.length});
        return controllerFileObjects;
    }


    @Override
    public void initialize(TemplateWizard wizard) {
        index = 0;
        // obtaining target folder
        Project project = Templates.getProject(wizard);

        WizardDescriptor.Panel secondPanel = new ValidationPanel(
                new PersistenceClientEntitySelection(NbBundle.getMessage(JpaControllerIterator.class, "LBL_EntityClasses"),
                new HelpCtx("org.netbeans.modules.j2ee.persistence.wizard.jpacontroller$"+PersistenceClientEntitySelection.class.getSimpleName()), wizard)); // NOI18N
        WizardDescriptor.Panel thirdPanel = new JpaControllerSetupPanel(project, wizard);
        String names[];
        //
        boolean noPuNeeded = true;
        try {
            noPuNeeded = ProviderUtil.persistenceExists(project, Templates.getTargetFolder(wizard)) || !ProviderUtil.isValidServerInstanceOrNone(project);
        } catch (InvalidPersistenceXmlException ex) {
            Logger.getLogger(JpaControllerIterator.class.getName()).log(Level.FINE, "Invalid persistence.xml: {0}", ex.getPath()); //NOI18N
        }
        if (!noPuNeeded) {
            puPanel = new PersistenceUnitWizardDescriptor(project);
            panels = new WizardDescriptor.Panel[]{secondPanel, thirdPanel, puPanel};
            names = new String[]{
                        NbBundle.getMessage(JpaControllerIterator.class, "LBL_EntityClasses"),
                        NbBundle.getMessage(JpaControllerIterator.class, "LBL_JpaControllerClasses"),
                        NbBundle.getMessage(JpaControllerIterator.class, "LBL_PersistenceUnitSetup")
                    };
        } else {

            panels = new WizardDescriptor.Panel[]{secondPanel, thirdPanel};
            names = new String[]{
                        NbBundle.getMessage(JpaControllerIterator.class, "LBL_EntityClasses"),
                        NbBundle.getMessage(JpaControllerIterator.class, "LBL_JpaControllerClasses")
                    };
        }

        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(JpaControllerIterator.class, "Templates/Persistence/JpaControllersFromEntities"));
        Wizards.mergeSteps(wizard, panels, names);
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(JpaControllerIterator.class, "LBL_WizardTitle_FromEntity");
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    /** 
     * A panel which checks that the target project has a valid server set
     * otherwise it delegates to the real panel.
     */
    private static class ValidationPanel extends DelegatingWizardDescriptorPanel {

        private ValidationPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }
    }
}
