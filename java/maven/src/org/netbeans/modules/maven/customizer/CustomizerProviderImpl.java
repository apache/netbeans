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

package org.netbeans.modules.maven.customizer;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom2.DefaultJDOMFactory;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.JDOMFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.maven.customizer.Bundle.*;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;

/**
 * maven implementation of CustomizerProvider, handles the general workflow,
 *for panel creation delegates to M2CustomizerPanelProvider instances.
 * @author Milos Kleint 
 */
@ProjectServiceProvider(
    service = {
        CustomizerProvider.class,
        CustomizerProvider2.class,
        CustomizerProviderImpl.class
    },
    projectType = "org-netbeans-modules-maven"
)
public class CustomizerProviderImpl implements CustomizerProvider2 {
    public static final HelpCtx HELP_CTX = new HelpCtx("maven_settings");
    
    private final Project project;
    private ModelHandle handle;
    private ModelHandle2 handle2;
    
    private static final String BROKEN_NBACTIONS = "BROKENNBACTIONS";  //NOI18N
    
    public CustomizerProviderImpl(Project project) {
        this.project = project;
    }
    
    @Override
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    
    public void showCustomizer( String preselectedCategory ) {
        showCustomizer( preselectedCategory, null );
    }
    
    @Messages({
               "# {0} - project display name",
               "TIT_Project_Properties=Project Properties - {0}", 
               "ERR_MissingPOM=Project's pom.xml file contains invalid xml content. Please fix the file before proceeding.",
               "TXT_Unloadable=Project is unloadable, you have to fix the problems before accessing the project properties dialog. Show Problem Resolution dialog?",
               "TIT_Unloadable=Project unlodable"
    })
    @Override
    public void showCustomizer( final String preselectedCategory, String preselectedSubCategory ) {
        if (project.getLookup().lookup(NbMavenProject.class).isUnloadable()) {
            NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(TXT_Unloadable(), TIT_Unloadable());
            nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                ProjectProblems.showCustomizer(project);
            }
            return;
        }
        try {
            POMModel mdl = init();
            //#171958 start
            if (!mdl.getState().equals(State.VALID)) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(ERR_MissingPOM(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }
            //#171958 end

//            handle.getPOMModel().startTransaction();
//            project.getLookup().lookup(MavenProjectPropsImpl.class).startTransaction();
            Mutex.EVENT.readAccess(new Runnable() {

                @Override
                public void run() {
                    assert EventQueue.isDispatchThread();
                    OptionListener listener = new OptionListener();
                    MavenProjectPropertiesUiSupport uiSupport = new MavenProjectPropertiesUiSupport(handle2, project);
                    Lookup context = Lookups.fixed(new Object[] { project, handle, handle2, uiSupport});
                    Dialog dialog = ProjectCustomizer.createCustomizerDialog("Projects/org-netbeans-modules-maven/Customizer", //NOI18N
                                                     context,
                                                     preselectedCategory,
                                                     new ActionListener() {
                                                        @Override
                                                        public void actionPerformed(ActionEvent ae) {
                                                            //noop
                                                        }
                                                    }, listener, HELP_CTX);
                    dialog.setTitle( TIT_Project_Properties(ProjectUtils.getInformation(project).getDisplayName()));
                    dialog.setModal(true);
                    dialog.setVisible(true);
                }
            });
        } catch (FileNotFoundException ex) {
            if ("No pom file exists.".equals(ex.getMessage())) { //NOI18N
                //#157020
                return;
            }
            Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.SEVERE, "Cannot show project customizer", ex);
        } catch (IOException ex) {
            Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.SEVERE, "Cannot show project customizer", ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.SEVERE, "Cannot show project customizer", ex);
        } 
    }
    
    private POMModel init() throws XmlPullParserException, IOException {
        FileObject pom = FileUtil.toFileObject(project.getLookup().lookup(NbMavenProjectImpl.class).getPOMFile());
        if (pom == null || !pom.isValid()) {
            throw new FileNotFoundException("No pom file exists."); //NOI18N
        }
        ModelSource source = Utilities.createModelSource(pom);
        POMModel model = POMModelFactory.getDefault().createFreshModel(source);
        
        Map<String, ActionToGoalMapping> mapps = new HashMap<String, ActionToGoalMapping>();
        NetbeansBuildActionXpp3Reader reader = new NetbeansBuildActionXpp3Reader();
        List<ModelHandle.Configuration> configs = new ArrayList<ModelHandle.Configuration>();
        ModelHandle.Configuration active = null;
        M2ConfigProvider provider = project.getLookup().lookup(M2ConfigProvider.class);
        M2Configuration act = provider.getActiveConfiguration();
        M2Configuration defconfig = provider.getDefaultConfig();
        mapps.put(defconfig.getId(), reader.read(new StringReader(defconfig.getRawMappingsAsString())));
        ModelHandle.Configuration c = ModelHandle.createDefaultConfiguration();
        configs.add(c);
        if (act.equals(defconfig)) {
            active = c;
        }

        for (M2Configuration config : provider.getSharedConfigurations()) {
            mapps.put(config.getId(), reader.read(new StringReader(config.getRawMappingsAsString())));
            c = ModelHandle.createCustomConfiguration(config.getId());
            c.setActivatedProfiles(config.getActivatedProfiles());
            c.setProperties(config.getProperties());
            c.setShared(true);
            configs.add(c);
            if (act.equals(config)) {
                active = c;
            }
        }
        for (M2Configuration config : provider.getNonSharedConfigurations()) {
            mapps.put(config.getId(), reader.read(new StringReader(config.getRawMappingsAsString())));
            c = ModelHandle.createCustomConfiguration(config.getId());
            c.setActivatedProfiles(config.getActivatedProfiles());
            c.setProperties(config.getProperties());            
            c.setShared(false);
            configs.add(c);
            if (act.equals(config)) {
                active = c;
            }
        }

        for (M2Configuration config : provider.getProvidedConfigurations()) {
            mapps.put(config.getId(), reader.read(new StringReader(config.getRawMappingsAsString())));
            c = ModelHandle.createDefaultConfiguration();
            CustomizerProviderImpl.ACCESSOR.setConfigurationId(c, config.getId());
            String dn = config.getDisplayName();
            if (!config.getId().equals(dn)) {
                c.setDisplayName(dn);
            }
            configs.add(c);
            if (act.equals(config)) {
                active = c;
            }
        }

        for (M2Configuration config : provider.getProfileConfigurations()) {
            mapps.put(config.getId(), reader.read(new StringReader(config.getRawMappingsAsString())));
            c = ModelHandle.createProfileConfiguration(config.getId());
            configs.add(c);
            if (act.equals(config)) {
                active = c;
            }
        }

        if (active == null) { //#152706
            active = configs.get(0); //default if current not found..
        }
        
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        List<String> actionNames = ap == null ? Collections.emptyList() : Arrays.asList(ap.getSupportedActions());

        handle = ACCESSOR.createHandle(model,
                project.getLookup().lookup(NbMavenProject.class).getMavenProject(), mapps, configs, active,
                project.getLookup().lookup(MavenProjectPropsImpl.class));
        handle2 = ACCESSOR2.createHandle(model,
                project.getLookup().lookup(NbMavenProject.class).getMavenProject(), mapps, new ArrayList<ModelHandle2.Configuration>(configs), active,
                actionNames,
                project.getLookup().lookup(MavenProjectPropsImpl.class));
        return model;
    }
    
    public static ModelAccessor ACCESSOR = null;
    public static ModelAccessor2 ACCESSOR2 = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class c = ModelHandle.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        c = ModelHandle2.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
    }    
    }    
    
    
    public abstract static class ModelAccessor {
        public abstract void setConfigurationId(ModelHandle.Configuration cfg, String id);
        
        public abstract ModelHandle createHandle(POMModel model, MavenProject proj, Map<String, ActionToGoalMapping> mapp,
                List<ModelHandle.Configuration> configs, ModelHandle.Configuration active, MavenProjectPropsImpl auxProps);
        
    }
        
    public abstract static class ModelAccessor2 {
        
        public abstract ModelHandle2 createHandle(POMModel model, MavenProject proj, Map<String, ActionToGoalMapping> mapp,
                List<ModelHandle2.Configuration> configs, ModelHandle2.Configuration active, List<String> allActions, MavenProjectPropsImpl auxProps);
        
        public abstract TreeMap<String, String> getModifiedAuxProps(ModelHandle2 handle, boolean shared);
        
        public abstract boolean isConfigurationModified(ModelHandle2 handle);
        
        public abstract boolean isModified(ModelHandle2 handle, ActionToGoalMapping mapp);
        
        public abstract List<String> getAllActions(ModelHandle2 handle);
        
        }
        
    /** Listens to the actions on the Customizer's option buttons
        ONLY STORE listener now.
     */
    private class OptionListener implements ActionListener {
        
        OptionListener() {
        }
        
        
        // Listening to OK button ----------------------------------------------
        
        @Override
        public void actionPerformed( ActionEvent e ) {
                final FileObject pom = FileUtil.toFileObject(project.getLookup().lookup(NbMavenProjectImpl.class).getPOMFile());
                if (pom == null || !pom.isValid()) {
                    return; //TODO
                }
                
                        try {
                    writeAll(handle2, project.getLookup().lookup(NbMavenProjectImpl.class));
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                            //TODO error reporting on wrong model save
                        }
                    }
            }
        
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }

   static void writeAll(ModelHandle2 handle, NbMavenProjectImpl project) throws IOException {
       //save configs before pom, to save reloads in case both pom and configs were changed.
       boolean performConfigsInvokedReload = false;
        M2ConfigProvider prv = project.getLookup().lookup(M2ConfigProvider.class);
        if (ACCESSOR2.isConfigurationModified(handle)) {
            List<M2Configuration> shared = new ArrayList<M2Configuration>();
            List<M2Configuration> nonshared = new ArrayList<M2Configuration>();
            for (ModelHandle2.Configuration mdlConf : handle.getConfigurations()) {
                if (!mdlConf.isDefault() && !mdlConf.isProfileBased()) {
                    M2Configuration c = new M2Configuration(mdlConf.getId(), project.getProjectDirectory());
                    c.setActivatedProfiles(mdlConf.getActivatedProfiles());
                    c.setProperties(mdlConf.getProperties());
                    if (mdlConf.isShared()) {
                        shared.add(c);
                    } else {
                        nonshared.add(c);
                    }
                }
            }
            prv.setConfigurations(shared, nonshared, true);
            performConfigsInvokedReload = true;
        }
        final FileObject pom = FileUtil.toFileObject(project.getLookup().lookup(NbMavenProjectImpl.class).getPOMFile());
        Utilities.performPOMModelOperations(pom, handle.getPOMOperations());

        AuxiliaryConfiguration aux = project.getLookup().lookup(AuxiliaryConfiguration.class);
        if (!ACCESSOR2.getModifiedAuxProps(handle, true).isEmpty()) {
            MavenProjectPropsImpl.writeAuxiliaryData(aux, ACCESSOR2.getModifiedAuxProps(handle, true), true);
        }
        if (!ACCESSOR2.getModifiedAuxProps(handle, false).isEmpty()) {
            MavenProjectPropsImpl.writeAuxiliaryData(aux, ACCESSOR2.getModifiedAuxProps(handle, false), false);
        }

        if (ACCESSOR2.isModified(handle, handle.getActionMappings())) {
            writeNbActionsModel(project, handle.getActionMappings(), M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
        }

        //TODO we need to set the configurations for the case of non profile configs
        String id = handle.getActiveConfiguration() != null ? handle.getActiveConfiguration().getId() : M2Configuration.DEFAULT;
        for (M2Configuration m2 : prv.getConfigurations()) {
            if (id.equals(m2.getId())) {
                prv.setActiveConfiguration(m2);
            }
        }
        //save action mappings for configurations..
        for (ModelHandle2.Configuration c : handle.getConfigurations()) {
            if (ACCESSOR2.isModified(handle,handle.getActionMappings(c))) {
                writeNbActionsModel(project, handle.getActionMappings(c), M2Configuration.getFileNameExt(c.getId()));
            }
        }
        if (performConfigsInvokedReload && handle.getPOMOperations().isEmpty()) { //#only do the reload if no change to po file was done. can be actually figured now with operations
            //#174637
            NbMavenProject.fireMavenProjectReload(project);
        }
   }

    public static void writeNbActionsModel(final FileObject pomDir, final ActionToGoalMapping mapping, final String path) throws IOException {
        writeNbActionsModel(null, pomDir, mapping, path);
    }

    public static void writeNbActionsModel(final Project project, final ActionToGoalMapping mapping, final String path) throws IOException {
        writeNbActionsModel(project, project.getProjectDirectory(), mapping, path);
    }
    
    @Messages({
        "TXT_Problem_Broken_Actions=Broken nbactions.xml file.", 
        "# {0} - exception text",
        "DESC_Problem_Broken_Actions=Cannot parse the $project_basedir/nbactions.xml file. The information contained in the file will be ignored until fixed. This affects several features in the IDE that will not work properly as a result.\n\n Parsing exception:\n{0}"})
    private static void writeNbActionsModel(final Project project, final FileObject pomDir, final ActionToGoalMapping mapping, final String path) throws IOException {
        pomDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                JDOMFactory factory = new DefaultJDOMFactory();
                try {
                    Document doc;
                    if (mapping.getActions().isEmpty()) { //#224450 don't write empty nbactions.xml files
                        FileObject fo = pomDir.getFileObject(path);
                        if (fo != null) {
                            fo.delete();
                        }
                        return;
                    }
                    FileObject fo = pomDir.getFileObject(path);
                    if (fo == null) {
                        fo = pomDir.createData(path);
                        doc = factory.document(factory.element("actions")); //NOI18N
                    } else {
                        //TODO..
                        try (InputStream inStr = fo.getInputStream()) {
                            SAXBuilder builder = new SAXBuilder();
                            doc = builder.build(inStr);
                        }
                    }
                    String encoding = mapping.getModelEncoding() != null ? mapping.getModelEncoding() : "UTF-8"; //NOI18N
                    try (FileLock lock = fo.lock();
                            OutputStreamWriter outStr = new OutputStreamWriter(fo.getOutputStream(lock), encoding);) {
                        NetbeansBuildActionJDOMWriter writer = new NetbeansBuildActionJDOMWriter();
                        Format form = Format.getRawFormat().setEncoding(encoding);
                        form = form.setLineSeparator(System.getProperty("line.separator")); //NOI18N
                        writer.write(mapping, doc, outStr, form);
                    }
                } catch (JDOMException exc){
                    //throw (IOException) new IOException("Cannot parse the nbactions.xml by JDOM.").initCause(exc); //NOI18N
                    //TODO this would need it's own problem provider, but how to access it in project lookup if all are merged into one?
                    NbMavenProjectImpl prj = project != null ? project.getLookup().lookup(NbMavenProjectImpl.class) : null;
                    ProblemReporterImpl impl = prj != null ? prj.getProblemReporter() : null;
                    if (impl != null && !impl.hasReportWithId(BROKEN_NBACTIONS)) {
                        ProblemReport rep = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                                TXT_Problem_Broken_Actions(),
                                DESC_Problem_Broken_Actions(exc.getMessage()),
                                ProblemReporterImpl.createOpenFileAction(pomDir.getFileObject(path)));
                        rep.setId(BROKEN_NBACTIONS);
                        impl.addReport(rep);
                    }
                    Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.INFO, exc.getMessage(), exc);
                }
            }
        });
    }
    
}
