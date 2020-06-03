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
package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLDocReader;
import org.netbeans.modules.cnd.makeproject.MakeProjectImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;
import org.xml.sax.Attributes;

/**
 * was: ConfigurationDescriptorHelper
 */
public class ConfigurationXMLReader extends XMLDocReader {

    private static final int DEPRECATED_VERSIONS = 26;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private final FileObject projectDirectory;
    private final Project project;
    private final static RequestProcessor REQUEST_PROCESSOR = new RequestProcessor("ConfigurationXMLReader", 10);//NOI18N
    private static final boolean TRACE_LONG_LOADING = false;

    public ConfigurationXMLReader(Project project, FileObject projectDirectory) {
        this.project = project;
        this.projectDirectory = projectDirectory;
        // LATER configurationDescriptor = new
    }

    /*
     * was: readFromDisk
     */
    public void read(final MakeConfigurationDescriptor configurationDescriptor, final String relativeOffset, final Interrupter interrupter) throws IOException {
        final String tag;
        final FileObject xml;
        // Try first new style file
        FileObject fo = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML); // NOI18N
        if (fo == null) {
            // then try old style file....
            tag = CommonConfigurationXMLCodec.PROJECT_DESCRIPTOR_ELEMENT;
            xml = projectDirectory.getFileObject("nbproject/projectDescriptor.xml"); // NOI18N
        } else {
            tag = CommonConfigurationXMLCodec.CONFIGURATION_DESCRIPTOR_ELEMENT;
            xml = fo;
        }

        if (xml == null) {
            displayErrorDialog();
            configurationDescriptor.setState(State.BROKEN);
            return;
        }
        configurationDescriptor.setState(State.READING);
        Task task = REQUEST_PROCESSOR.post(new NamedRunnable("Reading project configuraion") { //NOI18N

            protected 
            @Override
            void runImpl() {
                try {
                    if (TRACE_LONG_LOADING) {
                        try {
                            Thread.sleep(10000); // to emulate long reading for testing purpose
                        } catch (InterruptedException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                    if (_read(relativeOffset, interrupter, tag, xml, configurationDescriptor) == null) {
                        // TODO configurationDescriptor is broken
                        configurationDescriptor.setState(State.BROKEN);
                        return;
                    }
                    try {
                        _postRead(relativeOffset, interrupter, configurationDescriptor);
                        configurationDescriptor.setState(State.READY);
                    } catch (Throwable e) {
                        configurationDescriptor.setState(State.BROKEN);
                    }
                } catch (IOException ex) {
                    configurationDescriptor.setState(State.BROKEN);
                }
            }
        });
        configurationDescriptor.setInitTask(task);
    }

    private ConfigurationDescriptor _read(final String relativeOffset, Interrupter interrupter,
            String tag, FileObject xml, final MakeConfigurationDescriptor configurationDescriptor) throws IOException {

        boolean success;

        XMLDecoder decoder = new ConfigurationXMLCodec(tag, projectDirectory, configurationDescriptor, relativeOffset);
        registerXMLDecoder(decoder);
        InputStream inputStream = null;
        try {
            inputStream = xml.getInputStream();
            success = read(inputStream, xml.getPath(), interrupter);
            if (getMasterComment() != null && project instanceof MakeProjectImpl) {
                ((MakeProjectImpl) project).setConfigurationXMLComment(getMasterComment());
            }
        } finally {
            deregisterXMLDecoder(decoder);
            if (inputStream != null) {
                inputStream.close();
            }
        }

        if (!success) {
            displayErrorDialog();
            return null;
        }


        //
        // Now for the auxiliary/private entry
        //

        xml = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML); // NOI18N
        if (xml != null) {
            // Don't post an error.
            // It's OK to sometimes not have a private config
            XMLDecoder auxDecoder = new AuxConfigurationXMLCodec(tag, configurationDescriptor);
            registerXMLDecoder(auxDecoder);
            inputStream = null;
            try {
                inputStream = xml.getInputStream();
                success = read(inputStream, projectDirectory.getName(), interrupter);
            } finally {
                deregisterXMLDecoder(auxDecoder);
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            if (!success) {
                return null;
            }
        }

        // Read things from private/project.xml
        if (project != null) {
            int activeIndex = ((MakeProjectImpl) project).getActiveConfigurationIndexFromPrivateXML();
            if (activeIndex >= 0) {
                configurationDescriptor.getConfs().setActive(activeIndex);
            } else {
                PlatformInfo aDefault = PlatformInfo.getDefault(ExecutionEnvironmentFactory.getLocal());
                int localPlatform = aDefault.getPlatform();
                int i = 0;
                for(Configuration conf : configurationDescriptor.getConfs().getConfigurations()) {
                    MakeConfiguration mk = (MakeConfiguration)conf;
                    if (mk.getPlatformSpecific().getValue()) {
                        if (localPlatform == mk.getDevelopmentHost().getBuildPlatform()) {
                            configurationDescriptor.getConfs().setActive(i);
                            break;
                        }
                    } else {
                        configurationDescriptor.getConfs().setActive(i);
                        break;
                    }
                    i++;
                }
            }
        }

        // Ensure all item configurations have been created (default are not stored in V >= 57)
        Item[] projectItems = configurationDescriptor.getProjectItems();
        configurationDescriptor.getConfs().getConfigurations().forEach((configuration) -> {
            for (Item item : projectItems) {
                if (item.getItemConfiguration(configuration) == null) {
                    ItemConfiguration itemConfiguration = new ItemConfiguration(configuration, item);
                    configuration.addAuxObject(itemConfiguration);
                    // in version with inverted serialization all items not seen
                    // during deserialization of current 'configuration' are 
                    // considered as excluded by default => set exclude state to 'true'
                    if (configurationDescriptor.getVersion() >= CommonConfigurationXMLCodec.VERSION_WITH_INVERTED_SERIALIZATION) {
                        itemConfiguration.getExcluded().setValue(true);
                    }
                }
            }
        });

        return configurationDescriptor;
    }

    private void _postRead(final String relativeOffset, Interrupter interrupter, MakeConfigurationDescriptor configurationDescriptor) {
        boolean schemeWithExcludedItems = false;
        if (configurationDescriptor.getVersion() >= 0 && configurationDescriptor.getVersion() < CommonConfigurationXMLCodec.VERSION_WITH_INVERTED_SERIALIZATION) {
            schemeWithExcludedItems = true;
        }

        // Some samples are generated without generated makefile. Don't mark these 'not modified'. Then
        // the makefiles will be generated before the project is being built
        boolean isMakefileProject = false;
        for (Configuration configuration : configurationDescriptor.getConfs().getConfigurations()) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) configuration;
            if (makeConfiguration.isMakefileConfiguration()) {
                isMakefileProject = true;
                break;
            }
        }
        FileObject makeImpl = projectDirectory.getFileObject("nbproject/Makefile-impl.mk"); // NOI18N
        configurationDescriptor.setModified((!isMakefileProject && makeImpl == null) || relativeOffset != null);

        // Check version and display deprecation warning if too old
        if (configurationDescriptor.getVersion() >= 0 && configurationDescriptor.getVersion() <= DEPRECATED_VERSIONS) {
            final String message = NbBundle.getMessage(ConfigurationXMLReader.class, "OLD_VERSION_WARNING", projectDirectory.getPath()); // NOI18N
            final String autoMessage = NbBundle.getMessage(ConfigurationXMLReader.class, "OLD_VERSION_WARNING_AUTO");
            if (CndUtils.isStandalone()) {
                System.err.print(message);
                System.err.println(autoMessage);
                configurationDescriptor.setModified();
            } else {
                String title = NbBundle.getMessage(ConfigurationXMLReader.class, "CONVERT_DIALOG_TITLE");
                ConfirmSupport.getConfirmVersionFactory().create(title, message, autoMessage, configurationDescriptor::setModified);
            }
        }

        if (configurationDescriptor.isModified()) {
            // Project is modified and will be saved with current version. This includes samples.
            configurationDescriptor.setVersion(CommonConfigurationXMLCodec.CURRENT_VERSION);
        }

        ConfigurationDescriptorProvider.recordMetrics(ConfigurationDescriptorProvider.USG_PROJECT_OPEN_CND, configurationDescriptor);        
        
        String customizerId = configurationDescriptor.getActiveConfiguration() == null ? null : 
                configurationDescriptor.getActiveConfiguration().getCustomizerId();
        Lookups.forPath(MakeProjectTypeImpl.projectMetadataFactoryPath(customizerId)).lookupAll(ProjectMetadataFactory.class).forEach((f) -> {
            f.read(projectDirectory);
        });        
        prepareFoldersTask(configurationDescriptor, schemeWithExcludedItems, interrupter);
    }
    
    private void displayErrorDialog() {
        //String errormsg = NbBundle.getMessage(ConfigurationXMLReader.class, "CANTREADDESCRIPTOR", projectDirectory.getName());
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
    }

    // Attach listeners to all disk folders
    private void prepareFoldersTask(final MakeConfigurationDescriptor configurationDescriptor, final boolean oldSchemeWasRestored, final Interrupter interrupter) {
        final List<Folder> firstLevelFolders = configurationDescriptor.getLogicalFolders().getFolders();
        REQUEST_PROCESSOR.post(new Runnable() {
            // retstore in only scheme only once, then switch to new scheme
            private boolean restoreInOldScheme = oldSchemeWasRestored;
            @Override
            public void run() {
                String postfix = configurationDescriptor.getBaseDir();
                String threadName = "Attach listeners and refresh content of all disk folders " + postfix; // NOI18N
                LOGGER.log(Level.FINE, "Start {0}", threadName);
                long time = System.currentTimeMillis();
                String oldName = Thread.currentThread().getName();
                try {
                    //boolean currentState = configurationDescriptor.getModified();
                    Thread.currentThread().setName(threadName); // NOI18N
                    for (Folder f : firstLevelFolders) {
                        if (f.isDiskFolder()) {
                            if (restoreInOldScheme) {
                                LOGGER.log(Level.FINE, "Restore based on old scheme {0}", f);
                                restoreInOldScheme = false;
                                f.refreshDiskFolderAfterRestoringOldScheme(interrupter);
                            } else {
                                LOGGER.log(Level.FINE, "Restore based on new scheme {0}", f);
                                f.refreshDiskFolder(interrupter);
                            }
                            f.attachListeners(interrupter);
                        }
                    }
                    //configurationDescriptor.setModified(currentState);
                    LOGGER.log(Level.FINE, "End attach listeners and refresh content of all disk folders, time {0}ms. {1}", 
                            new Object[] {(System.currentTimeMillis() - time), postfix});
                } finally {
                    // restore thread name - it might belong to the pool
                    Thread.currentThread().setName(oldName);
                }
            }
        });
        // Refresh disk folders in background process
        // revert changes bacause opening project time is increased.
        //task.waitFinished(); // See IZ https://netbeans.org/bugzilla/show_bug.cgi?id=184260
        //configurationDescriptor.setFoldersTask(task);
    }

    // interface XMLDecoder
    @Override
    protected String tag() {
        return null;
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) {
    }

    // interface XMLDecoder
    @Override
    public void end() {
    }

    // interface XMLDecoder
    @Override
    public void startElement(String name, Attributes atts) {
    }

    // interface XMLDecoder
    @Override
    public void endElement(String name, String currentText) {
    }
}
