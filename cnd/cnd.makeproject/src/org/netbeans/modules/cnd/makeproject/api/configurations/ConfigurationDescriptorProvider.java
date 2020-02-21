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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.MakeProjectImpl;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLReader;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ComponentType;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public abstract class ConfigurationDescriptorProvider {
    public static final boolean VCS_WRITE = true; // Boolean.getBoolean("cnd.make.vcs.write");//org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec.VCS_WRITE;
    
    public static final String PROP_CONFIGURATIONS_LOADED = "loadedConfigurations"; // NOI18N
     
    public static final String USG_PROJECT_CONFIG_CND = "USG_PROJECT_CONFIG_CND"; // NOI18N
    public static final String USG_PROJECT_OPEN_CND = "USG_PROJECT_OPEN_CND"; // NOI18N
    public static final String USG_PROJECT_CREATE_CND = "USG_PROJECT_CREATE_CND"; // NOI18N
    private static final String USG_CND_PROJECT_ACTION = "USG_CND_PROJECT_ACTION"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private final static RequestProcessor RP = new RequestProcessor("Configuration Updater", 1); // NOI18N
    private final FileObject projectDirectory;
    private final Project project;
    private final Object readLock = new Object();
    private final AtomicBoolean isOpened = new AtomicBoolean();
    private final FileChangeListener configFilesListener = new ConfigurationXMLChangeListener();
    private final List<FileObject> trackedConfigFiles = new ArrayList(2);
    
    private final MakeConfigurationDescriptor projectDescriptor;
    private boolean hasTried;
    private String relativeOffset;
    private boolean needReload = true;
    private volatile Interrupter interrupter;

    protected ConfigurationDescriptorProvider(Project project, FileObject projectDirectory) {
        this.project = project;
        this.projectDirectory = projectDirectory;
        isOpened.set(true);
        projectDescriptor = new MakeConfigurationDescriptor(project, projectDirectory, projectDirectory);
    }
    
    public void setRelativeOffset(String relativeOffset) {
        this.relativeOffset = relativeOffset;
    }
    
    public MakeConfigurationDescriptor getConfigurationDescriptor() {
        return getConfigurationDescriptor(false);
    }

    protected MakeConfigurationDescriptor getConfigurationDescriptorImpl() {
        return projectDescriptor;
    }
    
    abstract protected void fireConfigurationDescriptorLoaded();
    
    private boolean shouldBeLoaded() {
        synchronized(isOpened) {
            return ((needReload) && !hasTried);
        }
    }

    private MakeConfigurationDescriptor getConfigurationDescriptor(boolean reload) {
        synchronized(isOpened) {
            if (!isOpened.get()) {
                return null;
            }
        }
        if (shouldBeLoaded()) {
            // attempt to read configuration descriptor
            // do this only once
            synchronized (readLock) {
                // check again that someone already havn't read
                if (shouldBeLoaded()) {
                    LOGGER.log(Level.FINE, "Start reading project descriptor for project {0} in ConfigurationDescriptorProvider@{1}", new Object[]{projectDirectory.getNameExt(), System.identityHashCode(this)}); // NOI18N
                    // It's important to set needReload=false before calling
                    // projectDescriptor.assign(), otherwise there will be
                    // infinite recursion.
                    synchronized(isOpened) {
                        needReload = false;
                    }

                    ConfigurationXMLReader reader = new ConfigurationXMLReader(project, projectDirectory);
                    try {
                        SnapShot delta = startModifications();
                        if (reload) {
                            projectDescriptor.clean();
                        }
                        reader.read(projectDescriptor, relativeOffset, interrupter);
                        projectDescriptor.waitInitTask();
                        endModifications(delta, true, LOGGER);
                        RP.post(this::fireConfigurationDescriptorLoaded);

                        LOGGER.log(Level.FINE, "End of reading project descriptor for project {0} in ConfigurationDescriptorProvider@{1}", // NOI18N
                                new Object[]{projectDirectory.getNameExt(), System.identityHashCode(this)});
                    } catch (java.io.IOException x) {
                        x.printStackTrace(System.err);
                        // most likely open failed
                    }

                    synchronized(isOpened) {
                        hasTried = true;
                    }
                }
            }
        }
        projectDescriptor.waitInitTask();
        return projectDescriptor;
    }

    public SnapShot startModifications() {
        return new Delta(projectDescriptor);
    }

    public void endModifications(SnapShot snapShot, boolean sendChangeEvent, Logger logger) {
        if (snapShot instanceof Delta) {
            Delta delta = (Delta) snapShot;
            if (sendChangeEvent) {
                delta.computeDelta(projectDescriptor);
                if (logger != null) {
                    delta.printStatistic(logger);
                }
                projectDescriptor.checkForChangedItems(delta);
            }
        }
    }
    
    public boolean gotDescriptor() {
        return isOpened.get() && projectDescriptor.getState() != State.READING;
    }

    public static ConfigurationAuxObjectProvider[] getAuxObjectProviders() {
        HashSet<ConfigurationAuxObjectProvider> auxObjectProviders = new HashSet<>();
        Collection<? extends ConfigurationAuxObjectProvider> collection =
                Lookup.getDefault().lookupAll(ConfigurationAuxObjectProvider.class);
//      System.err.println("-------------------------------collection " + collection);
        Iterator<? extends ConfigurationAuxObjectProvider> iterator = collection.iterator();
        while (iterator.hasNext()) {
            auxObjectProviders.add(iterator.next());
        }
//      System.err.println("-------------------------------auxObjectProviders " + auxObjectProviders);
        return auxObjectProviders.toArray(new ConfigurationAuxObjectProvider[auxObjectProviders.size()]);
    }

    public static void recordMetrics(String msg, MakeConfigurationDescriptor descr) {
        recordMetricsImpl(msg, null, descr, null);
    }

    public static void recordCreatedProjectMetrics(MakeConfiguration[] confs) {
        if (confs != null && confs.length > 0) {
            recordMetricsImpl(USG_PROJECT_CREATE_CND, confs[0], null, null);
        }
    }

    public static void recordActionMetrics(String action, MakeConfigurationDescriptor descr) {
        recordMetricsImpl(USG_CND_PROJECT_ACTION, null, descr, action);
    }

    private static void recordMetricsImpl(String msg,
            MakeConfiguration makeConfiguration,
            MakeConfigurationDescriptor descr,
            String action) {
        if (CndUtils.isUnitTestMode()) {
            // we don't want to count own tests
            return;
        }
        if (descr == null && makeConfiguration == null) {
            return;
        }
        Item[] projectItems = null;
        if (makeConfiguration == null) {
            if (descr.getConfs() == null || descr.getConfs().getActive() == null) {
                return;
            }
            if (makeConfiguration == null) {
                makeConfiguration = descr.getActiveConfiguration();
            }
            projectItems = (descr).getProjectItems();
            if (!USG_PROJECT_CREATE_CND.equals(msg) && (projectItems == null || projectItems.length == 0)) {
                // do not track empty applications
                return;
            }
        }
        String type;
        switch (makeConfiguration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_MAKEFILE:
                type = "MAKEFILE"; // NOI18N
                break;
            case MakeConfiguration.TYPE_APPLICATION:
                type = "APPLICATION"; // NOI18N
                break;
            case MakeConfiguration.TYPE_DB_APPLICATION:
                type = "DB_APPLICATION"; // NOI18N
                break;
            case MakeConfiguration.TYPE_DYNAMIC_LIB:
                type = "DYNAMIC_LIB"; // NOI18N
                break;
            case MakeConfiguration.TYPE_STATIC_LIB:
                type = "STATIC_LIB"; // NOI18N
                break;
            case MakeConfiguration.TYPE_QT_APPLICATION:
                type = "QT_APPLICATION"; // NOI18N
                break;
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                type = "QT_DYNAMIC_LIB"; // NOI18N
                break;
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                type = "QT_STATIC_LIB"; // NOI18N
                break;
            default:
                type = "UNKNOWN"; // NOI18N
        }
        String host;
        CompilerSet compilerSet;
        if (makeConfiguration.getDevelopmentHost().isLocalhost()) {
            host = "LOCAL"; // NOI18N
            compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        } else {
            host = "REMOTE"; // NOI18N
            // do not force creation of compiler sets
            compilerSet = null;
        }
        String flavor;
        String[] families;
        if (compilerSet != null) {
            families = compilerSet.getCompilerFlavor().getToolchainDescriptor().getFamily();
            flavor = compilerSet.getCompilerFlavor().getToolchainDescriptor().getName();
        } else {
            families = new String[0];
            if (makeConfiguration.getCompilerSet() != null) {
                families = new String[]{makeConfiguration.getCompilerSet().getName()};
            }
            flavor = makeConfiguration.getCompilerSet().getFlavor();
        }
        String family;
        if (families.length == 0) {
            family = flavor; // NOI18N
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < families.length; i++) {
                if (families[i] != null) {
                    buffer.append(families[i]);
                    if (i < families.length - 1) {
                        buffer.append(","); // NOI18N
                    }
                }
            }
            family = buffer.toString();
        }
        String platform;
        int platformID = makeConfiguration.getDevelopmentHost().getBuildPlatform();
        if (Platforms.getPlatform(platformID) != null) {
            platform = Platforms.getPlatform(platformID).getName();
        } else {
            platform = "UNKNOWN_PLATFORM"; // NOI18N
        }

        String ideType = ComponentType.OSS_IDE.getTag();
        if (USG_PROJECT_CREATE_CND.equals(msg)) {
            // stop here
            UIGesturesSupport.submit(msg, type, flavor, family, host, platform, "USER_PROJECT", ideType); //NOI18N
        } else if (USG_CND_PROJECT_ACTION.equals(msg)) {
            UIGesturesSupport.submit(msg, action, type, flavor, family, host, platform, ideType); //NOI18N
        } else if (projectItems != null) {
            makeConfiguration.reCountLanguages(descr);
            int size = 0;
            int allItems = projectItems.length;
            boolean cLang = false;
            boolean ccLang = false;
            boolean fLang = false;
            boolean aLang = false;
            for (Item item : projectItems) {
                ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
                if (itemConfiguration != null && !itemConfiguration.getExcluded().getValue()) {
                    size++;
                    switch (itemConfiguration.getTool()) {
                        case CCompiler:
                            cLang = true;
                            break;
                        case CCCompiler:
                            ccLang = true;
                            break;
                        case FortranCompiler:
                            fLang = true;
                            break;
                        case Assembler:
                            aLang = true;
                            break;
                    }
                }
            }
            String ccUsage = ccLang ? "USE_CPP" : "NO_CPP"; // NOI18N
            String cUsage = cLang ? "USE_C" : "NO_C"; // NOI18N
            String fUsage = fLang ? "USE_FORTRAN" : "NO_FORTRAN"; // NOI18N
            String aUsage = aLang ? "USE_ASM" : "NO_ASM"; // NOI18N
            UIGesturesSupport.submit(msg, type, flavor, family, host, platform, toSizeString(allItems), toSizeString(size), ccUsage, cUsage, fUsage, aUsage, ideType);
        }
    }

    private static String toSizeString(int size) {
        String strSize;
        if (size < 25) {
            strSize = "25"; // NOI18N
        } else if (size < 100) {
            strSize = "100"; // NOI18N
        } else if (size < 500) {
            strSize = "500"; // NOI18N
        } else if (size < 1000) {
            strSize = "1000"; // NOI18N
        } else if (size < 2000) {
            strSize = "2000"; // NOI18N
        } else if (size < 5000) {
            strSize = "5000"; // NOI18N
        } else if (size < 10000) {
            strSize = "10000"; // NOI18N
        } else if (size < 20000) {
            strSize = "20000"; // NOI18N
        } else if (size < 50000) {
            strSize = "50000"; // NOI18N
        } else {
            strSize = "99999"; // NOI18N
        }
        return strSize;
    }

    public void closed() {
        detachConfigurationFilesListener();
        MakeConfigurationDescriptor descr = getConfigurationDescriptor();
        if (descr != null) {
            descr.closed();
        }
        
        // clean up
        synchronized(isOpened) {
            isOpened.set(false);
            projectDescriptor.clean();
            hasTried = false;
            relativeOffset = null;
            needReload = false;
        }
    }

    protected void opening(Interrupter interrupter) {
        synchronized(isOpened) {
            isOpened.set(true);
            needReload = true;
            hasTried = false;
            projectDescriptor.setState(State.READING);
            this.interrupter = interrupter;
        }
    }
    
    public void opened() {
        MakeConfigurationDescriptor descr = getConfigurationDescriptor(true);
        if (descr != null) {
            descr.opened(interrupter);
        }
        if (interrupter != null && interrupter.cancelled()) {
            return;
        }
        attachConfigurationFilesListener();
    }

    private void attachConfigurationFilesListener() {
        synchronized (trackedConfigFiles) {
            initTrackedConfigFiles();
            if (trackedConfigFiles.size() == 2) {
                for (FileObject fileObject : trackedConfigFiles) {
                    fileObject.addFileChangeListener(configFilesListener);
                    LOGGER.log(Level.FINE, "attached config file {2} listener for project {0} in ConfigurationDescriptorProvider@{1}", new Object[]{projectDirectory, System.identityHashCode(this), fileObject}); // NOI18N
                }
            }
        }
    }
    
    private void detachConfigurationFilesListener() {
        synchronized (trackedConfigFiles) {
            for (FileObject fileObject : trackedConfigFiles) {
                fileObject.removeFileChangeListener(configFilesListener);
                LOGGER.log(Level.FINE, "detached config file {2} listener for project {0} in ConfigurationDescriptorProvider@{1}", new Object[]{projectDirectory, System.identityHashCode(this), fileObject}); // NOI18N
            }
        }
    }

    private void initTrackedConfigFiles() {
        assert Thread.holdsLock(trackedConfigFiles);
        if (trackedConfigFiles.size() != 2) {
            LOGGER.log(Level.FINE, "(re)initializing config files {2} for project {0} in ConfigurationDescriptorProvider@{1}", new Object[]{projectDirectory, System.identityHashCode(this), trackedConfigFiles}); // NOI18N
            trackedConfigFiles.clear();
            boolean first = true;
            for (String path : new String[]{
                        MakeConfiguration.NBPROJECT_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML, //NOI18N
                        MakeConfiguration.NBPROJECT_PRIVATE_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML}) { //NOI18N
                FileObject fo = projectDirectory.getFileObject(path);
                if (fo != null) {
                    // We have to store tracked files somewhere.
                    // Otherwise they will be GCed, and we won't get notifications.
                    trackedConfigFiles.add(fo);
                } else {
                    if (first) {
                        // prevent reading configurations before project cration
                        CndUtils.threadsDump();
                        new Exception("Attempt to read project before creation. Not found file " + projectDirectory.getPath() + "/" + path).printStackTrace(System.err); // NOI18N
                    }
                }
                first = false;
            }
            LOGGER.log(Level.FINE, "initialized config files {2} for project {0} in ConfigurationDescriptorProvider@{1}", new Object[]{projectDirectory, System.identityHashCode(this), trackedConfigFiles}); // NOI18N
        }
    }

    /**
     * Method also is called from org.netbeans.modules.cnd.discovery.projectimport.ImportProject.updateRemoteProjectImpl()
     */
    private void resetConfiguration() {
        AtomicBoolean writeLock = MakeConfigurationDescriptor.getWriteLock(project);
        if (writeLock.get()) {
            // configuration is being saved right now
            return;
        }
        if (projectDescriptor.isModified()) {
            if (interrupter.cancelled()) {
                return;
            }
            if (project instanceof MakeProjectImpl) {
                if (((MakeProjectImpl)project).isDeleted()) {
                    return;
                }
            }
            // Ask user if descriptor is modified in memory.
            String title = NbBundle.getMessage(ConfigurationDescriptorProvider.class, "MakeConfigurationDescriptor.UpdateConfigurationTitle"); // NOI18N
            String txt = NbBundle.getMessage(ConfigurationDescriptorProvider.class, "MakeConfigurationDescriptor.UpdateConfigurationText", project.getProjectDirectory().getPath()); //NOI18N
            String autoConfirm = NbBundle.getMessage(ConfigurationDescriptorProvider.class, "MakeConfigurationDescriptor.UpdateConfigurationText.auto");
            if (CndUtils.isStandalone()) {
                System.err.print(txt);
                System.err.println(autoConfirm); //NOI18N
            } else {
                ConfirmSupport.AutoConfirm confirm = ConfirmSupport.getAutoConfirmFactory().create(title, txt, autoConfirm);
                if (confirm == null) {
                    return;
                }
            }
        }
        synchronized (readLock) {
            LOGGER.log(Level.FINE, "Mark to reload project descriptor MakeConfigurationDescriptor@{0} for project {1} in ConfigurationDescriptorProvider@{2}", new Object[]{System.identityHashCode(projectDescriptor), projectDirectory.getNameExt(), System.identityHashCode(this)}); // NOI18N
            synchronized(isOpened) {
                if (isOpened.get()) {
                    needReload = true;
                    hasTried = false;
                }
            }
            RP.post(() -> {
                getConfigurationDescriptor(true);
            });
        }
    }
            
    /**
     * This listener will be notified about updates of files
     * <code>nbproject/configurations.xml</code> and
     * <code>nbproject/private/configurations.xml</code>.
     * These files should be reloaded when changed externally.
     * See IZ#146701: can't update project through subversion, or any other
     */
    private class ConfigurationXMLChangeListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // Don't reset configuration on file attribute change.
        }
    }

    public interface SnapShot {
        boolean isViewChanged();
    }
    
    static final class Delta implements SnapShot {

        private final Map<String, Pair> oldState = new HashMap<>();
        private final List<NativeFileItem> included = new ArrayList<>();
        private final List<NativeFileItem> added = new ArrayList<>(); 
        private final List<NativeFileItem> excluded = new ArrayList<>(); 
        private final List<NativeFileItem> deleted = new ArrayList<>(); 
        private final List<NativeFileItem> changed = new ArrayList<>(); 
        private final List<NativeFileItem> replaced = new ArrayList<>(); 

        private Delta(MakeConfigurationDescriptor oldDescriptor) {
            if (oldDescriptor != null) {
                for(Item item : oldDescriptor.getProjectItems()) {
                    oldState.put(item.getAbsolutePath(), new Pair(item, getCRC(item), item.isExcluded()));
                }
                NativeProjectProvider np = oldDescriptor.getProject().getLookup().lookup(NativeProjectProvider.class);
                if (np != null) {
                    for(NativeFileItem item : np.getStandardHeadersIndexers()) {
                        if (item instanceof NativeProjectProvider.NativeFileIndexer) {
                            NativeProjectProvider.NativeFileIndexer indexer = (NativeProjectProvider.NativeFileIndexer)item;
                            oldState.put(item.getAbsolutePath(), new Pair(indexer, getCRC(indexer), indexer.isExcluded()));
                        }
                    }
                }
            }
        }
        
        private void computeDelta(MakeConfigurationDescriptor newDescriptor) {
            Set<NativeFileItem> oldSet = new HashSet<>();
            oldState.entrySet().forEach((entry) -> {
                oldSet.add(entry.getValue().item);
            });
            Item[] newItems = newDescriptor.getProjectItems();
            for (Item item : newItems) {
                checkItem(item, oldSet);
            }
            NativeProjectProvider np = newDescriptor.getProject().getLookup().lookup(NativeProjectProvider.class);
            if (np != null) {
                for(NativeFileItem indexer : np.getStandardHeadersIndexers()) {
                    checkItem(indexer, oldSet);
                }
            }
            oldSet.forEach((item) -> {
                deleted.add(item);
            });
            oldState.clear();
        }

        private void checkItem(NativeFileItem item, Set<NativeFileItem> oldSet) {
            Delta.Pair pair = oldState.get(item.getAbsolutePath());
            if (pair == null) {
                added.add(item);
            } else {
                oldSet.remove(pair.item);
                if (item.isExcluded() && pair.excluded) {
                    // no changes
                    replaced.add(item);
                } else if (item.isExcluded() && !pair.excluded) {
                    excluded.add(item);
                } else if (!item.isExcluded() && pair.excluded) {
                    included.add(item);
                } else {
                    // compare item properties
                    if (getCRC(item) != pair.crc) {
                        changed.add(item);
                    } else {
                        if (pair.item != item) {
                            replaced.add(item);
                        }
                    }
                }
            }
        }
        
        public void printStatistic(Logger logger) {
            if (logger.isLoggable(CndUtils.isUnitTestMode()? Level.FINE : Level.INFO)) {
                logger.log(Level.INFO, "Configuration updated:\n\t{0} deleted items\n\t{1} added items\n\t{2} changed items",
                        new Object[]{deleted.size()+excluded.size(), added.size()+included.size(), changed.size()});
            }
        }
        
        public boolean isEmpty() {
            return included.isEmpty() && added.isEmpty() && excluded.isEmpty() && deleted.isEmpty() && changed.isEmpty();
        }

        @Override
        public boolean isViewChanged() {
            if (getAdded().isEmpty() &&
                getDeleted().isEmpty() &&
                getExcluded().isEmpty() &&
                getIncluded().isEmpty() &&
                getReplaced().isEmpty()) {
                // only changed properties => no changes in project tree
                return false;
            }
            return true;
        }

        /**
         * marked as included items
         */
        public List<NativeFileItem> getIncluded() {
            return Collections.unmodifiableList(included);
        }

        /**
         * added in project items
         */
        public List<NativeFileItem> getAdded() {
            return Collections.unmodifiableList(added);
        }

        /**
         * marked as excluded items
         */
        public List<NativeFileItem> getExcluded() {
            return Collections.unmodifiableList(excluded);
        }

        /**
         * deleted from project items
         */
        public List<NativeFileItem> getDeleted() {
            return Collections.unmodifiableList(deleted);
        }

        /**
         * items with changed properties
         */
        public List<NativeFileItem> getChanged() {
            return Collections.unmodifiableList(changed);
        }

        /**
         * Items which properties were not changed (from code model point of view) but instances were replaced
         */
        public List<NativeFileItem> getReplaced() {
            return Collections.unmodifiableList(replaced);
        }

        private static int getCRC(NativeFileItem item) {
            int res = 0;
            for(IncludePath aPath : item.getUserIncludePaths()) {
                res += 37 * aPath.getFSPath().hashCode();
            }
            for(FSPath aPath : item.getIncludeFiles()) {
                res += 37 * aPath.hashCode();
            }
            for(String macro: item.getUserMacroDefinitions()) {
                res += 37 * macro.hashCode();
            }
            for(IncludePath aPath : item.getSystemIncludePaths()) {
                res += 37 * aPath.getFSPath().hashCode();
            }
            for(FSPath aPath : item.getSystemIncludeHeaders()) {
                res += 37 * aPath.getPath().hashCode();
            }
            for(String macro: item.getSystemMacroDefinitions()) {
                res += 37 * macro.hashCode();
            }
            res += 37 * item.getLanguage().hashCode();
            res += 37 * item.getLanguageFlavor().hashCode();
            if (item instanceof Item) {
                Item i = (Item)item;
                for(String macro: i.getUndefinedMacros()) {
                    res += 37 * macro.hashCode();
                }
            }
            return res;
        }
        
        private static final class Pair {
            final int crc;
            final boolean excluded;
            final NativeFileItem item;
            private Pair(NativeFileItem item, int crc, boolean excluded) {
                this.crc = crc;
                this.excluded = excluded;
                this.item = item;
            }
        }
    }
}
