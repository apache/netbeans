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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.project.BrokenIncludes;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.MakeProjectCustomizerEx;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;

/**
 * Filter node contain additional features for the Make physical
 *
 */
final class MakeLogicalViewRootNode extends AnnotatedNode implements ChangeListener, LookupListener, PropertyChangeListener {


    private boolean brokenIncludes;
    private boolean brokenProject;
    private Folder folder;
    private final Lookup.Result<BrokenIncludes> brokenIncludesResult;
    private final MakeLogicalViewProvider provider;
    private final InstanceContent ic;
    private final RequestProcessor.Task stateChangedTask;
    private static final int WAIT_DELAY = 500;
    private boolean confProviderListenerAttached = false;
    
    public MakeLogicalViewRootNode(Folder folder, MakeLogicalViewProvider provider, InstanceContent ic) {
        this(new ProjectRootChildren(folder, provider),folder, provider, ic);
    }
    
    private MakeLogicalViewRootNode(ProjectRootChildren children, Folder folder, MakeLogicalViewProvider provider, InstanceContent ic) {
        super(children, new AbstractLookup(ic), provider.getAnnotationRP());
        children.setMakeLogicalViewRootNode(MakeLogicalViewRootNode.this);
        this.ic = ic;
        this.folder = folder;
        this.provider = provider;        
//        setChildren(new ProjectRootChildren(folder, provider));
        setIconBaseWithExtension(MakeConfigurationDescriptor.ICON);
        setName(ProjectUtils.getInformation(provider.getProject()).getDisplayName());

        brokenIncludesResult = Lookup.getDefault().lookup(new Lookup.Template<>(BrokenIncludes.class));
        brokenIncludesResult.addLookupListener(MakeLogicalViewRootNode.this);
        resultChanged(null);

        brokenIncludes = hasBrokenIncludes(provider.getProject());
        // Handle annotations
        setForceAnnotation(true);
        if (folder != null) {
            updateAnnotationFiles();
        }
        ProjectInformation pi = provider.getProject().getLookup().lookup(ProjectInformation.class);
        pi.addPropertyChangeListener(WeakListeners.propertyChange(MakeLogicalViewRootNode.this, pi));
        ToolsCacheManager.addChangeListener(WeakListeners.change(MakeLogicalViewRootNode.this, null));
        //if (gotMakeConfigurationDescriptor()) {
            ProjectConfigurationProvider confProvider = provider.getProject().getLookup().lookup(ProjectConfigurationProvider.class);
            if (confProvider != null){                
                confProvider.addPropertyChangeListener(WeakListeners.propertyChange(MakeLogicalViewRootNode.this, confProvider));
                confProviderListenerAttached = true;
            }
        //}

        stateChangedTask = provider.getAnnotationRP().create(new StateChangeRunnableImpl(), true);
    }

    @Override
    public String getHtmlDisplayName() {
        String ret = getHtmlDisplayName2();
        ExecutionEnvironment env = provider.getProject().getFileSystemHost();
        if (env != null && env.isRemote()) {
            if (ret == null) {
                ret = getName();
            }
            ret = ret + " <font color=\"!textInactiveText\">[" + env.getDisplayName() + "]"; // NOI18N
        }
        return ret;
    }
    
    private String getHtmlDisplayName2() {
        String ret = super.getHtmlDisplayName();
        if (brokenProject) {
            try {
                ret = XMLUtil.toElementContent(ret);
            } catch (CharConversionException ex) {
                return ret;
            }
            return "<font color=\"#"+Integer.toHexString(getErrorForeground().getRGB() & 0xffffff) +"\">" + ret + "</font>"; //NOI18N
        }
        return ret;
    }

    private static Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  //NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return result;
    }

    public void reInit(MakeConfigurationDescriptor configurationDescriptor) {
        // FIXME:
        // It looks like unmanaged project does not refresh children correctly
        // on the root node. It looks it ignores setChildren which is called
        // in this method. 
        // I've tried to put setChildren(Children.LEAF) 
        // before setChildren(new LogicalViewChildren(folder, provider));
        // and it cause that some of reopened project are "empty" in project view
        // although you can click on them and they will be expanded correctly.
        //
        // We call reInit for opening project in reopened IDE and tried to add after reloading
        // externally modified configuration.xml (but removed).
        // Unfortunately these situations are not distinguished now,
        // so to solve the issue with "Select In Project" I introduced 
        // fallback in findFolderNode & findPath to check by name and by id
        
        Folder logicalFolders = configurationDescriptor.getLogicalFolders();
        if (folder != null) {
            ic.remove(folder);
        }
        folder = logicalFolders;
        ic.add(logicalFolders);
        setChildren(new LogicalViewChildren(folder, provider));
        if (!confProviderListenerAttached) {
            ProjectConfigurationProvider confProvider = provider.getProject().getLookup().lookup(ProjectConfigurationProvider.class);
            if (confProvider != null) {
                confProvider.addPropertyChangeListener(WeakListeners.propertyChange(MakeLogicalViewRootNode.this, confProvider));
                confProviderListenerAttached = true;
            }
        }
        stateChanged(null);
    }
    
    void reInitWithRemovedPrivate() {
        final MakeConfigurationDescriptor mcd = provider.getMakeConfigurationDescriptor();
        Configuration[] confs = mcd.getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration conf = (MakeConfiguration) confs[i];
            if (conf.getDevelopmentHost().isLocalhost()) {
                final int platform1 = CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform();
                final int platform2 = conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue();
                if (platform1 != platform2) {
                    conf.getDevelopmentHost().getBuildPlatformConfiguration().setValue(platform1);
                }
            }
        }
        reInit(provider.getMakeConfigurationDescriptor());
    }
    
    void reInitWithUnsupportedVersion() {
        //checkVersion = false;
        reInit(provider.getMakeConfigurationDescriptor());
    }
    
    private void setRealProjectFolder(Folder folder) {
        assert folder != null;
        if (this.folder != null) {
            ic.remove(this.folder);
        }
        this.folder = folder;
        ic.add(folder);
        stateChanged(null);
        Runnable runnable = () -> {
            try {
                // to reinvalidate Run/Debug and other toolbar buttons, we use the workaround with selection
                // remember selection
                Node[] selectedNodes = ProjectTabBridge.getInstance().getExplorerManager().getSelectedNodes();
                // clear
                ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(new Node[0]);
                // restore
                ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(selectedNodes);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public Folder getFolder() {
        return folder;
    }

    private Project getProject() {
        return provider.getProject();
    }

    private boolean gotMakeConfigurationDescriptor() {
        return provider.gotMakeConfigurationDescriptor();
    }

    MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        return provider.getMakeConfigurationDescriptor();
    }

    private void updateAnnotationFiles() {
        // Add project directory
        FileObject fo = getProject().getProjectDirectory();
        if (fo == null || !fo.isValid()) {
            // See IZ 125880
            Logger.getLogger("cnd.makeproject").log(Level.WARNING, "project.getProjectDirectory() == null - {0}", getProject());
        }
        if (!gotMakeConfigurationDescriptor()) {
            return;
        }
        // Add buildfolder from makefile projects to sources. See IZ 90190.
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return;
        }
        Configurations confs = makeConfigurationDescriptor.getConfs();
        if (confs == null) {
            return;
        }
        Set<FileObject> set = new LinkedHashSet<>();
        for (Configuration conf : confs.toArray()) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
            if (makeConfiguration.isMakefileConfiguration()) {
                MakefileConfiguration makefileConfiguration = makeConfiguration.getMakefileConfiguration();
                FileObject buildCommandFO = makefileConfiguration.getAbsBuildCommandFileObject();
                if (buildCommandFO != null && buildCommandFO.isValid()) {
                    try {
                        FileObject fileObject = CndFileUtils.getCanonicalFileObject(buildCommandFO);
                        if (fileObject != null /*paranoia*/ && fileObject.isValid()) {
                            set.add(fileObject);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace(System.err);
                    }
                }
            }
        }
        set.add(getProject().getProjectDirectory());
        setFiles(set);
        Folder aFolder = folder;
        if (aFolder != null) {
            List<Folder> allFolders = new ArrayList<>();
            allFolders.add(aFolder);
            allFolders.addAll(aFolder.getAllFolders(true));
            Iterator<Folder> iter = allFolders.iterator();
            while (iter.hasNext()) {
                iter.next().addChangeListener(this);
            }
        }
    }

    @Override
    public String getShortDescription() {
        return MakeLogicalViewProvider.getShortDescription(provider.getProject());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setName(ProjectUtils.getInformation(provider.getProject()).getDisplayName());
        String prop = evt.getPropertyName();
        if (ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
            fireDisplayNameChange(null, null);
        } else if (ProjectInformation.PROP_NAME.equals(prop)) {
            fireNameChange(null, null);
        } else if (ProjectInformation.PROP_ICON.equals(prop)) {
            fireIconChange();
        } else if (ConfigurationDescriptorProvider.PROP_CONFIGURATIONS_LOADED.equals(prop)) {
            reInit(getMakeConfigurationDescriptor());
        } else if (ProjectConfigurationProvider.PROP_CONFIGURATIONS.equals(prop)) {
            stateChanged(null) ;
        }
    }

    private final class VisualUpdater implements Runnable {

        @Override
        public void run() {
            if (brokenProject) {
                MakeLogicalViewRootNode.this.setChildren(Children.LEAF);
            }
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
    }

    /*
     * Something in the folder has changed
     **/
    @Override
    public void stateChanged(ChangeEvent e) {
        if (stateChangedTask != null) {
            stateChangedTask.schedule(WAIT_DELAY);
        }
    }

    @Override
    public Object getValue(String valstring) {
        if (valstring == null) {
            return super.getValue(null);
        }
        if (valstring.equals("Folder")) // NOI18N
        {
            return folder;
        } else if (valstring.equals("Project")) // NOI18N
        {
            return getProject();
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        }
        return super.getValue(valstring);
    }

    @Override
    public Image getIcon(int type) {
        ProjectInformation pi = provider.getProject().getLookup().lookup(ProjectInformation.class);
        return mergeBadge(annotateIcon(ImageUtilities.icon2Image(pi.getIcon()), type));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    private Image mergeBadge(Image original) {
        ProjectProblemsProvider pp = getProject().getLookup().lookup(ProjectProblemsProvider.class);
        if (brokenProject) {
            return ImageUtilities.mergeImages(original, MakeLogicalViewProvider.brokenProjectBadge, 8, 0);
        }
        if (pp.getProblems().size()>0) {
            return ImageUtilities.mergeImages(original, MakeLogicalViewProvider.brokenProjectBadge, 8, 0);
        }
        if (brokenIncludes) {
            return ImageUtilities.mergeImages(original, MakeLogicalViewProvider.brokenIncludeBadge, 8, 0);
        }        
        return original;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>();
        if (!gotMakeConfigurationDescriptor()) {
            actions.add(CommonProjectActions.closeProjectAction());
            return actions.toArray(new Action[actions.size()]);        
        }
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();

        // TODO: not clear if we need to call the following method at all
        // but we need to remove remembering the output to prevent memory leak;
        // I think it could be removed
        if (descriptor != null) {
            descriptor.getLogicalFolders();
        }

        MakeConfiguration active = (descriptor == null) ? null : descriptor.getActiveConfiguration();
        String projectType = MakeProjectType.PROJECT_TYPE;
        Action[] projectActions = null;
        if (active != null && active.isCustomConfiguration()) {
            //TODO: fix it as all actions can use  HIDE_WHEN_DISABLE and be enabled in own context only
            projectActions = ((MakeProjectCustomizerEx)active.getProjectCustomizer()).getActions(getProject(), Arrays.asList(CommonProjectActions.forType(projectType)));            
            projectType = active.getProjectCustomizer().getCustomizerId();                        
        }        
        projectActions = projectActions == null ? CommonProjectActions.forType(projectType) : projectActions;
        if (brokenProject) {
            actions.add(CommonProjectActions.closeProjectAction());
            Action resolveAction = Actions.forID("Project", "org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory"); //NOI18N
            for(Action action : projectActions) {
                if (action == resolveAction) {
                    actions.add(action);
                }
            }
            return actions.toArray(new Action[actions.size()]);        
        }
        actions.addAll(Arrays.asList(projectActions));
        actions.add(null);
        return actions.toArray(new Action[actions.size()]);        
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action, int index) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                return super.getDropType(transferable, action, index);
            }
        }
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                try {
                    ViewItemNode viewItemNode = (ViewItemNode) transferable.getTransferData(flavors[i]);
                    int type = Integer.parseInt(flavors[i].getParameter(MakeLogicalViewProvider.MASK));
                    list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type, provider));
                } catch (Exception e) {
                }
            }
        }
        super.createPasteTypes(transferable, list);
    }
   

    @Override
    public void resultChanged(LookupEvent ev) {
        brokenIncludesResult.allInstances().forEach((elem) -> {
            elem.addChangeListener(this);
        });
    }

    private boolean hasBrokenIncludes(Project project) {
        BrokenIncludes biProvider = Lookup.getDefault().lookup(BrokenIncludes.class);
        if (biProvider != null) {
            NativeProject id = project.getLookup().lookup(NativeProject.class);
            if (id != null) {
                return biProvider.isBroken(id);
            }
        }
        return false;
    }

    private final static class ProjectRootChildren extends LogicalViewChildren {
        private MakeLogicalViewRootNode parent;
        private ProjectRootChildren(Folder folder, MakeLogicalViewProvider provider) {
            super(folder, provider);
        }

        @Override
        protected void onFolderChange(Folder folder) {
            assert parent != null;
            this.parent.setRealProjectFolder(folder);
        }
        
        private void setMakeLogicalViewRootNode(MakeLogicalViewRootNode parent) {
            assert this.parent == null;
            this.parent = parent;
        }

        @Override
        protected boolean isRoot() {
            return true;
        }
    }

    private final class StateChangeRunnableImpl implements Runnable {

        @Override
        public void run() {
            brokenIncludes = hasBrokenIncludes(getProject());
            if (provider.gotMakeConfigurationDescriptor()) {
                MakeConfigurationDescriptor makeConfigurationDescriptor = provider.getMakeConfigurationDescriptor();
                if (makeConfigurationDescriptor != null) {
                    brokenProject = makeConfigurationDescriptor.getState() == State.BROKEN;
                    if (makeConfigurationDescriptor.getConfs().size() == 0) {
                        brokenProject = true;
                    }
                    if (provider.isIncorectVersion()) {
                        brokenProject = true;
                    }
                }
            }
            updateAnnotationFiles();
            EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
        }
    }
}
