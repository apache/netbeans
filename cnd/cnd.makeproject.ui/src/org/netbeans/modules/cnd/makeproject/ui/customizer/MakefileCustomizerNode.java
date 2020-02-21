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
package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

class MakefileCustomizerNode extends CustomizerNode {
    private static final RequestProcessor RP = new RequestProcessor("MakeConfiguration", 1); // NOI18N

    public MakefileCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        Sheet sheet = getSheet(((MakeConfiguration) configuration).getMakefileConfiguration());
        return new Sheet[]{sheet};
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsMake"); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getBundle(MakefileCustomizerNode.class).getString(s);
    }
    
    private Sheet getSheet(MakefileConfiguration conf) {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Makefile"); // NOI18N
        set.setDisplayName(getString("MakefileTxt"));
        set.setShortDescription(getString("MakefileHint"));
        set.put(new DirStringNodeProp(conf.getBuildCommandWorkingDir(), "WorkingDirectory", getString("WorkingDirectory_LBL"), getString("WorkingDirectory_TT"), conf)); // NOI18N
        set.put(new StringNodeProp(conf.getBuildCommand(), "BuildCommandLine", getString("BuildCommandLine_LBL"), getString("BuildCommandLine_TT"))); // NOI18N
        set.put(new StringNodeProp(conf.getCleanCommand(),  "CleanCommandLine", getString("CleanCommandLine_LBL"), getString("CleanCommandLine_TT"))); // NOI18N
        set.put(new OutputStringNodeProp(conf.getOutput(), "BuildResult", getString("BuildResult_LBL"), getString("BuildResult_TT"), conf)); // NOI18N
        sheet.put(set);
        
        return sheet;
    }

    private static ExecutionEnvironment getSourceExecutionEnvironment(MakefileConfiguration conf) {
        ExecutionEnvironment env = null;
        MakeConfiguration mc = conf.getMakeConfiguration();
        if (mc != null) {
            return FileSystemProvider.getExecutionEnvironment(mc.getBaseFSPath().getFileSystem());
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        return env;
    }
    
    private static class DirStringNodeProp extends StringNodeProp {
        private final MakefileConfiguration conf;
        public DirStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3, MakefileConfiguration conf) {
            super(stringConfiguration, txt1, txt2, txt3);
            this.conf = conf;
        }
        
        @Override
        public void setValue(String v) {
            String path = CndPathUtilities.toRelativePath(conf.getMakeConfiguration().getBaseDir(), v); // FIXUP: not always relative path
            path = CndPathUtilities.normalizeSlashes(path);
            super.setValue(path);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new DirEditor(conf.getAbsBuildCommandWorkingDir(), conf);
        }
    }
    
    private static class OutputStringNodeProp extends StringNodeProp {
        private final MakefileConfiguration conf;
        public OutputStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3, MakefileConfiguration conf) {
            super(stringConfiguration, txt1, txt2, txt3);
            this.conf = conf;
        }
        
        @Override
        public void setValue(String v) {
            String path = CndPathUtilities.toRelativePath(conf.getMakeConfiguration().getBaseDir(), v); // FIXUP: not always relative path
            path = CndPathUtilities.normalizeSlashes(path);
            super.setValue(path);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            String seed = conf.getAbsOutput();
            if (seed.length() == 0) {
                seed = conf.getMakeConfiguration().getBaseDir();
            }
            return new ElfEditor(seed, conf);
        }
    }
    
    private static class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private final String seed;
        private final MakefileConfiguration conf;
        
        public DirEditor(String seed, MakefileConfiguration conf) {
            this.seed = seed;
            this.conf = conf;
        }
        
        @Override
        public void setAsText(String text) {
            conf.getBuildCommandWorkingDir().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return conf.getBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public Object getValue() {
            return conf.getBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            conf.getBuildCommandWorkingDir().setValue((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return createDirPanel(seed, this, propenv);
        }
        
        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }

        private JFileChooser createDirPanel(String seed, final PropertyEditorSupport editor, PropertyEnv propenv) {
            String titleText = NbBundle.getMessage(MakefileCustomizerNode.class, "Run_Directory");
            String buttonText = NbBundle.getMessage(MakefileCustomizerNode.class, "SelectLabel");
            final JFileChooser chooser = RemoteFileChooserUtil.createFileChooser(getSourceExecutionEnvironment(conf), titleText, buttonText,
                    JFileChooser.DIRECTORIES_ONLY, null, seed, true);
            chooser.putClientProperty("title", chooser.getDialogTitle()); // NOI18N
            chooser.setControlButtonsAreShown(false);
            propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propenv.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
                    File selectedFile= chooser.getSelectedFile();
                    String path = CndPathUtilities.toRelativePath(conf.getMakeConfiguration().getBaseDir(), selectedFile.getPath()); // FIXUP: not always relative path
                    path = CndPathUtilities.normalizeSlashes(path);
                    editor.setValue(path);
                }
            });
            return chooser;
        }
    }
   
    private static final class ElfEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private final String seed;
        private final MakefileConfiguration conf;
        
        public ElfEditor(String seed, MakefileConfiguration conf) {
            this.seed = seed;
            this.conf = conf;
        }
        
        @Override
        public void setAsText(String text) {
            conf.getOutput().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return conf.getOutput().getValue();
        }
        
        @Override
        public Object getValue() {
            return conf.getOutput().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            conf.getOutput().setValue((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return createElfPanel(seed, this, propenv);
        }
        
        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }

        private JFileChooser createElfPanel(String seed, final PropertyEditorSupport editor, PropertyEnv propenv) {
            MakeConfiguration mc = conf.getMakeConfiguration();
            final RemoteSyncFactory syncFactory = (mc == null) ? null : mc.getRemoteSyncFactory();
            final ExecutionEnvironment execEnv =
                    // mc != null is redundant, only to prevent false "null pointer dereference" shown byNetBeans
                    (mc != null && syncFactory != null && syncFactory.isCopying()) ?
                    mc.getDevelopmentHost().getExecutionEnvironment() :
                    getSourceExecutionEnvironment(conf);
            final PathMap pathMap =
                    (syncFactory != null && syncFactory.isCopying()) ?
                    syncFactory.getPathMap(execEnv) : null;
            final CountDownLatch latch = new CountDownLatch(1);
            final List<FileFilter> filters = Collections.synchronizedList(new ArrayList<FileFilter>());
            final JFileChooser chooser = RemoteFileChooserUtil.createFileChooser(execEnv,
                    "", "", JFileChooser.FILES_ONLY, null,  //NOI18N
                    new ElfChooserInitializer(execEnv, pathMap, seed, filters, latch),
                    true);
            chooser.setControlButtonsAreShown(false);
            chooser.putClientProperty("title", chooser.getDialogTitle()); // NOI18N
            setElfFilters(chooser, filters, latch);
            propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propenv.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                File selectedFile = chooser.getSelectedFile();
                if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID && selectedFile != null) {
                    String path = selectedFile.getPath();
                    if (pathMap != null) {
                        String newPath = pathMap.getTrueLocalPath(path);
                        path = (newPath == null) ? ("//" + path) : newPath;
                    }
                    path = CndPathUtilities.toRelativePath(conf.getMakeConfiguration().getBaseDir(), path); // FIXUP: not always relative path
                    path = CndPathUtilities.normalizeSlashes(path);
                    editor.setValue(path);
                }
            });
            return chooser;
        }
        
        private void setElfFilters(final JFileChooser chooser, final List<FileFilter> filters, final CountDownLatch latch) {
            // to be run in EDT
            final Runnable setFiltersRunner = () -> {
                filters.forEach((f) -> {
                    chooser.addChoosableFileFilter(f);
                });
                if (!filters.isEmpty()) {
                    chooser.setFileFilter(filters.get(0));
                    //chooser.setFileFilter(chooser.getAcceptAllFileFilter());
                }
            };
            Runnable waiter = () -> {
                try {
                    latch.await();
                    SwingUtilities.invokeLater(setFiltersRunner);
                } catch (InterruptedException ex) {
                    // don't report interrupted exception
                }
            };
            RP.post(waiter);
        }
    }

    private static class ElfChooserInitializer implements Callable<String> {

        private final ExecutionEnvironment execEnv;
        private final PathMap pathMap;
        private final String seed;
        private final List<FileFilter> filters;
        private final CountDownLatch latch;

        public ElfChooserInitializer(ExecutionEnvironment execEnv, PathMap pathMap, String seed, List<FileFilter> filters, CountDownLatch latch) {
            this.execEnv = execEnv;
            this.pathMap = pathMap;
            this.seed = seed;
            this.filters = filters;
            this.latch = latch;
        }


        @Override
        public String call() throws Exception {

            String realSeed = this.seed;

            try {

                HostInfo hostInfo = null;
                try {
                    ConnectionManager.getInstance().connectTo(execEnv);
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                } catch (ConnectionManager.CancellationException e) {
                    // never report CancellationException
                }
                // even if connectTo failed or was cancelled, host info may present
                if (HostInfoUtils.isHostInfoAvailable(execEnv)) {
                    hostInfo = HostInfoUtils.getHostInfo(execEnv);
                }

                if (pathMap != null && seed != null) {
                    realSeed = pathMap.getRemotePath(seed, true);
                }
                // check file existence
                FileObject seedFo = FileSystemProvider.getFileObject(execEnv, realSeed);
                // if it does not exist, let's set the closest existing parent
                while (seedFo == null && realSeed.length() > 1) {
                    realSeed = PathUtilities.getDirName(realSeed);
                    seedFo = FileSystemProvider.getFileObject(execEnv, realSeed);
                }
                filters.add(FileFilterFactory.getAllBinaryFileFilter());
                if (hostInfo != null) {
                    if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                        filters.add(FileFilterFactory.getPeExecutableFileFilter());
                        filters.add(FileFilterFactory.getPeStaticLibraryFileFilter());
                        filters.add(FileFilterFactory.getPeDynamicLibraryFileFilter());
                    } else if (hostInfo.getOSFamily() == HostInfo.OSFamily.MACOSX) {
                        filters.add(FileFilterFactory.getMacOSXExecutableFileFilter());
                        filters.add(FileFilterFactory.getElfStaticLibraryFileFilter());
                        filters.add(FileFilterFactory.getMacOSXDynamicLibraryFileFilter());
                    } else {
                        filters.add(FileFilterFactory.getElfExecutableFileFilter());
                        filters.add(FileFilterFactory.getElfStaticLibraryFileFilter());
                        filters.add(FileFilterFactory.getElfDynamicLibraryFileFilter());
                    }
                }
            } finally {
                latch.countDown();
            }
            return realSeed;
        }
    }
}
