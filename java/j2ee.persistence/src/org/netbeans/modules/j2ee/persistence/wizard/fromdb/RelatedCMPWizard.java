/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Chris Webster, Martin Adamek, Andrei Badea
 */
public class RelatedCMPWizard implements TemplateWizard.Iterator {

    private static final String PROP_HELPER = "wizard-helper"; //NOI18N
    private static final String TYPE_JPA = "jpa"; // NOI18N
    private static final Lookup.Result<PersistenceGeneratorProvider> PERSISTENCE_PROVIDERS =
            Lookup.getDefault().lookupResult(PersistenceGeneratorProvider.class);
    private final String type;
    private WizardDescriptor.Panel[] panels;
    private int currentPanel = 0;
    private WizardDescriptor wizardDescriptor;
    private PersistenceGenerator generator;
    private RelatedCMPHelper helper;
    private ProgressPanel progressPanel;
    private Project project;
    private final RequestProcessor RP = new RequestProcessor(RelatedCMPWizard.class.getSimpleName(), 5);

    public static RelatedCMPWizard createForJPA() {
        return new RelatedCMPWizard(TYPE_JPA);
    }

    private static PersistenceGenerator createPersistenceGenerator(String type) {
        assert type != null;

        Collection<? extends PersistenceGeneratorProvider> providers = PERSISTENCE_PROVIDERS.allInstances();
        for (PersistenceGeneratorProvider provider : providers) {
            if (type.equals(provider.getGeneratorType())) {
                return provider.createGenerator();
            }
        }
        throw new AssertionError("Could not find a persistence generator of type " + type); // NOI18N
    }

    static RelatedCMPHelper getHelper(WizardDescriptor wizardDescriptor) {
        return (RelatedCMPHelper) wizardDescriptor.getProperty(PROP_HELPER);
    }

    public RelatedCMPWizard(String type) {
        this.type = type;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return currentPanel > 0;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentPanel--;
    }

    @Override
    public boolean hasNext() {
        return currentPanel < panels.length - 1;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentPanel++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Panel<WizardDescriptor> current() {
        return panels[currentPanel];
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel[] createPanels() {

        String wizardBundleKey = "Templates/Persistence/RelatedCMP"; // NOI18N
        String wizardTitle = NbBundle.getMessage(RelatedCMPWizard.class, wizardBundleKey);

        {
//            boolean showPUStep = false;
//            try {
//                showPUStep = !ProviderUtil.persistenceExists(project);
//            } catch (InvalidPersistenceXmlException ex) {
//                //it's handled one more time in appropriate panel, need nothing to do
//            }
//            if(showPUStep){
//                return new WizardDescriptor.Panel[] {
//                        new DatabaseTablesPanel.WizardPanel(wizardTitle),
//                        new EntityClassesPanel.WizardPanel(),
//                        new MappingOptionsPanel.WizardPanel(),
//                        new PersistenceUnitWizardDescriptor(project),
//                };
//            } else {
            return TYPE_JPA.equals(type) ? new WizardDescriptor.Panel[] {
                new DatabaseTablesPanel.WizardPanel(wizardTitle),
                new EntityClassesPanel.WizardPanel(),
                new MappingOptionsPanel.WizardPanel()
            } : new WizardDescriptor.Panel[]{
                new DatabaseTablesPanel.WizardPanel(null),
                new EntityClassesPanel.WizardPanel(false, false, true, false)
            } ;
        }
//            }
    }

    private String[] createSteps() {
           {
//            boolean showPUStep = false;
//            try {
//                showPUStep = !ProviderUtil.persistenceExists(project);
//            } catch (InvalidPersistenceXmlException ex) {//TODO add schema2bean exception
//                //it's handled one more time in appropriate panel, need nothing to do
//            }
//            if(showPUStep){
//                 return new String[] {
//                        NbBundle.getMessage(RelatedCMPWizard.class, "LBL_DatabaseTables"),
//                        NbBundle.getMessage(RelatedCMPWizard.class, "LBL_EntityClasses"),
//                        NbBundle.getMessage(RelatedCMPWizard.class, "LBL_MappingOptions"),
//                        NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"LBL_Step1")
//                };
//            } else {
            return TYPE_JPA.equals(type) ? new String[] {
                NbBundle.getMessage(RelatedCMPWizard.class, "LBL_DatabaseTables"),
                NbBundle.getMessage(RelatedCMPWizard.class, "LBL_EntityClasses"),
                NbBundle.getMessage(RelatedCMPWizard.class, "LBL_MappingOptions")
            } : new String[] {
                NbBundle.getMessage(RelatedCMPWizard.class, "LBL_DatabaseTables"),
                NbBundle.getMessage(RelatedCMPWizard.class, "LBL_EntityClasses")
            };
//            }
        }
    }

    @Override
    public final void initialize(TemplateWizard wiz) {
        wizardDescriptor = wiz;

        project = Templates.getProject(wiz);

        panels = createPanels();
        Wizards.mergeSteps(wizardDescriptor, panels, createSteps());


        generator = createPersistenceGenerator(type);

        FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
        FileObject configFilesFolder = PersistenceLocation.getLocation(project, targetFolder);

        helper = new RelatedCMPHelper(project, configFilesFolder, generator);

        wiz.putProperty(PROP_HELPER, helper);

        generator.init(wiz);
    }

    @Override
    public final void uninitialize(TemplateWizard wiz) {
        generator.uninit();
    }

    @Override
    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException {
        // create the pu first if needed
        if (helper.isCreatePU()) {
            Util.addPersistenceUnitToProjectRoot(project, helper.getLocation().getRootFolder(), Util.buildPersistenceUnitUsingData(project, getDefaultPersistenceUnitName(helper.getLocation().getRootFolder()), helper.getDatabaseConnection() != null ? helper.getTableSource().getName() : null, null, null));
        } else {
            Util.addPersistenceUnitToProject(project);
        }

        final String title = NbBundle.getMessage(RelatedCMPWizard.class, "TXT_EntityClassesGeneration");
        final ProgressContributor progressContributor = BasicAggregateProgressFactory.createProgressContributor(title);
        final AggregateProgressHandle handle =
                BasicAggregateProgressFactory.createHandle(title, new ProgressContributor[]{progressContributor}, null, null);
        progressPanel = new ProgressPanel();
        final JComponent progressComponent = AggregateProgressFactory.createProgressComponent(handle);

        final Runnable r = () -> {
            try {
                handle.start();
                createBeans(wiz, progressContributor);
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, null, ioe);
                NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } finally {
                generator.uninit();
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
                    RP.post(r);
                    progressPanel.open(progressComponent, title);
                } else {
                    first = false;
                    SwingUtilities.invokeLater(this);
                }
            }
        });

        // The commented code below is the ideal state, but since there is not way to request
        // TemplateWizard.Iterator.instantiate() be called asynchronously it
        // would cause the wizard to stay visible until the bean generation process
        // finishes. So for now just returning the package -- not a problem,
        // JavaPersistenceGenerator.createdObjects() returns an empty set anyway.

        // remember to wait for createBeans() to actually return!
        // Set created = generator.createdObjects();
        // if (created.size() == 0) {
        //     created = Collections.singleton(SourceGroupSupport.getFolderForPackage(helper.getLocation(), helper.getPackageName()));
        // }
        if (helper.isCreatePU() && helper.getDBSchemaFile() != null) {//for now open persistence.xml in case of schema was used, as it's 99% will require persistence.xml update
            DataObject dObj = null;
            try {
                dObj = ProviderUtil.getPUDataObject(project, helper.getLocation().getRootFolder(), null);
            } catch (InvalidPersistenceXmlException ex) {
            }
            if (dObj != null) {
                return Collections.<DataObject>singleton(dObj);
            }
        }

        return Collections.<DataObject>singleton(DataFolder.findFolder(
                SourceGroups.getFolderForPackage(helper.getLocation(), helper.getPackageName())));
    }
    
    private String getDefaultPersistenceUnitName(FileObject root) {
        ClassPath cp = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_SOURCE_PATH);
        if (cp != null) {
            FileObject owner = cp.findOwnerRoot(root);
            if (owner != null) {
                FileObject fo = root;
                FileObject prev = null;
                while (!fo.equals(owner)) {
                    prev = fo;
                    fo = fo.getParent();
                }
                if (prev != null && !prev.getName().isEmpty()) {
                    String candidateNameBase = Character.toUpperCase(prev.getName().charAt(0)) + prev.getName().substring(1) + "PU"; //NOI18N
                    try {
                        if (!ProviderUtil.persistenceExists(project, root)) {
                            return candidateNameBase;
                        }
                        PUDataObject pudo = ProviderUtil.getPUDataObject(project, root, null);
                        Persistence persistence = pudo.getPersistence();

                        int suffix = 2;
                        PersistenceUnit[] punits = persistence.getPersistenceUnit();
                        String candidateName = candidateNameBase;
                        while (!isUnique(candidateName, punits)) {
                            candidateName = candidateNameBase + suffix++;
                        }
                        return candidateName;
                    } catch (InvalidPersistenceXmlException ipex) {
                        // just log, the user is notified about invalid persistence.xml when
                        // the panel is validated
                        Logger.getLogger("global").log(Level.FINE, "Invalid persistence.xml found", ipex); //NOI18N
                    }
                }
            }
        }
        return null;
    }

    private boolean isUnique(String candidate, PersistenceUnit[] punits) {
        for (PersistenceUnit punit : punits) {
            if (candidate.equals(punit.getName())) {
                return false;
            }
        }
        return true;
    }

    private void createBeans(TemplateWizard wiz, ProgressContributor handle) throws IOException {
        try {
            handle.start(1); //TODO: need the correct number of work units here 
            handle.progress(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_SavingSchema"));
            progressPanel.setText(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_SavingSchema"));

            FileObject dbschemaFile = helper.getDBSchemaFile();
            if (dbschemaFile == null) {
                FileObject configFilesFolder = getHelper(wiz).getConfigFilesFolder();
                if (configFilesFolder == null) {
                    // if we got here, this must be an entity class library project or just a
                    // project without persistence.xml
                    configFilesFolder = PersistenceLocation.createLocation(project, wiz.getTargetFolder().getPrimaryFile());
                }
                if (configFilesFolder == null) {
                    String message = NbBundle.getMessage(RelatedCMPWizard.class, "TXT_NoConfigFiles");
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    return;
                }
            }

            String extracting = NbBundle.getMessage(RelatedCMPWizard.class, "TXT_ExtractingEntityClassesAndRelationships");

            handle.progress(extracting);
            progressPanel.setText(extracting);

            helper.buildBeans();

            FileObject pkg = SourceGroups.getFolderForPackage(helper.getLocation(), helper.getPackageName());
            generator.generateBeans(progressPanel, helper, dbschemaFile, handle);

        } finally {
            handle.finish();
            SwingUtilities.invokeLater( () -> progressPanel.close() );
        }
    }
}
