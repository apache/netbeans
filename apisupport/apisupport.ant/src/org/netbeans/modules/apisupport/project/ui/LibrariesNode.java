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
package org.netbeans.modules.apisupport.project.ui;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.ui.ModulesNodeFactory.AddNewLibraryWrapperAction;
import org.netbeans.modules.apisupport.project.ui.customizer.AddModulePanel;
import org.netbeans.modules.apisupport.project.ui.customizer.EditDependencyPanel;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.FindAction;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;

/**
 * @author Martin Krauskopf
 */
final class LibrariesNode extends AbstractNode {

    private static final Logger LOG = Logger.getLogger(LibrariesNode.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(LibrariesNode.class);

    static final String LIBRARIES_NAME = "libraries"; // NOI18N
    private static final String DISPLAY_NAME = getMessage("LBL_libraries");
    private final Action[] actions;

    public LibrariesNode(final NbModuleProject project) {
        super(new LibrariesChildren(project), org.openide.util.lookup.Lookups.fixed(project));
        setName(LIBRARIES_NAME);
        setDisplayName(DISPLAY_NAME);
        if (project.getModuleType() == NbModuleType.SUITE_COMPONENT) {
            actions = new Action[]{
                        new AddModuleDependencyAction(project),
                        new AddNewLibraryWrapperAction(project, project)
                    };
        } else {
            actions = new Action[]{
                        new AddModuleDependencyAction(project),
                    };
        }
    }

    public Image getIcon(int type) {
        return getIcon(false);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(true);
    }

    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/apisupport/project/ui/resources/libraries-badge.png", true);
        return ImageUtilities.mergeImages(ApisupportAntUIUtils.getTreeFolderIcon(opened), badge, 8, 8);
    }

    public Action[] getActions(boolean context) {
        return actions;
    }

    private static String createHtmlDescription(final ModuleDependency dep) {
        // assemble an html short description (tooltip actually)
        StringBuilder shortDesc = new StringBuilder("<html><u>" + dep.getModuleEntry().getCodeNameBase() + "</u><br>"); // NOI18N
        if (dep.hasImplementationDependency()) {
            shortDesc.append("<br><font color=\"red\">" + getMessage("CTL_ImplementationDependency") + "</font>");
        }
        if (dep.hasCompileDependency()) {
            shortDesc.append("<br>").append(getMessage("CTL_NeededToCompile"));
        }
        if (dep.getReleaseVersion() != null) {
            shortDesc.append("<br>").append(NbBundle.getMessage(LibrariesNode.class, "CTL_MajorReleaseVersion",
                    dep.getReleaseVersion()));
        }
        if (dep.getSpecificationVersion() != null) {
            shortDesc.append("<br>").append(NbBundle.getMessage(LibrariesNode.class, "CTL_SpecificationVersion",
                    dep.getSpecificationVersion()));
        }
        shortDesc.append("</html>"); // NOI18N
        return shortDesc.toString();
    }

    private static String getMessage(String bundleKey) {
        return NbBundle.getMessage(LibrariesNode.class, bundleKey);
    }

    private static final class LibrariesChildren extends Children.Keys<Object/*JDK_PLATFORM_NAME|ModuleDependency*/> implements AntProjectListener {

        private static final String JDK_PLATFORM_NAME = "jdkPlatform"; // NOI18N
        private final NbModuleProject project;
        private ImageIcon librariesIcon;

        LibrariesChildren(final NbModuleProject project) {
            this.project = project;
        }

        protected void addNotify() {
            super.addNotify();
            project.getHelper().addAntProjectListener(this);
            refreshKeys();
        }

        protected void removeNotify() {
            setKeys(Collections.emptySet());
            project.getHelper().removeAntProjectListener(this);
            super.removeNotify();
        }

        private void refreshKeys() {
            // Since miscellaneous operations may be run upon the project.xml
            // of individual projects we could be called from the same thread
            // with already acquired ProjectManager.mutex. This could lead to
            // refreshing during the misconfigurated suite/suite_component
            // relationship.
            ImportantFilesNodeFactory.getNodesSyncRP().post(new Runnable() {

                public void run() {
                    try {
                        ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {

                            public Void run() throws IOException {
                                ProjectXMLManager pxm = new ProjectXMLManager(project);
                                final List<Object> keys = new ArrayList<Object>();
                                keys.add(JDK_PLATFORM_NAME);
                                
                                SortedSet<String> binOrigs = new TreeSet<String>();
                                binOrigs.addAll(Arrays.asList(pxm.getBinaryOrigins()));
                                keys.addAll(binOrigs);
  
                                SortedSet<ModuleDependency> deps = new TreeSet<ModuleDependency>(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
                                deps.addAll(pxm.getDirectDependencies());
                                keys.addAll(deps);
                                // XXX still not good when dependency was just edited, since Children use
                                // hashCode/equals (probably HashMap) to find lastly selected node (so neither
                                // SortedSet would help here). Use probably wrapper instead to keep selection.
                                ImportantFilesNodeFactory.getNodesSyncRP().post(new Runnable() {

                                    public void run() {
                                        setKeys(Collections.unmodifiableList(keys));
                                    }
                                });
                                return null;
                            }
                        });
                    } catch (MutexException e) {
                        LOG.log(Level.FINE, null, e);
                    }
                 }
            });
        }

        protected Node[] createNodes(Object key) {
            List<Node> nodes = new ArrayList<Node>(2);
            if (key == JDK_PLATFORM_NAME) {
                nodes.add(PlatformNode.create(project, project.evaluator(), "nbjdk.home")); // NOI18N
            } else if (key instanceof ModuleDependency) {
                ModuleDependency dep = (ModuleDependency) key;
                ModuleEntry me = dep.getModuleEntry();
                File srcF = me.getSourceLocation();
                if (srcF == null) {
                    File jarF = me.getJarLocation();
                    URL jarRootURL = FileUtil.urlForArchiveOrDir(jarF);
                    assert jarRootURL != null;
                    FileObject root = URLMapper.findFileObject(jarRootURL);
                    if (root != null) {
                    String name = me.getLocalizedName() + " - " + me.getCodeNameBase(); // NOI18N
                    Icon icon = getLibrariesIcon();
                    Node pvNode = ActionFilterNode.create(
                            PackageView.createPackageView(new LibrariesSourceGroup(root, name, icon, icon)));
                    nodes.add(new LibraryDependencyNode(dep, project, pvNode));
                    } else {
                        Node n = new AbstractNode(Children.LEAF);
                        n.setName(me.getCodeNameBase());
                        nodes.add(n);
                    }
                    for (String cpext : me.getClassPathExtensions().split(File.pathSeparator)) {
                        if (cpext.length() > 0) {
                            FileObject jar = FileUtil.toFileObject(new File(cpext));
                            if (jar != null) {
                                nodes.add(createLibraryPackageViewNode(jar));
                            }
                        }
                    }
                } else {
                    nodes.add(new ProjectDependencyNode(dep, project));
                }
            } else {
                // binary origin path
                FileObject jar = project.getHelper().resolveFileObject((String) key);
                if (jar != null) {
                    nodes.add(createLibraryPackageViewNode(jar));
                }
            }
            return nodes.toArray(new Node[0]);
        }

        private Node createLibraryPackageViewNode(FileObject jfo) {
            Icon icon = getLibrariesIcon();
            FileObject root = FileUtil.getArchiveRoot(jfo);
            if (root == null) {
                return Node.EMPTY;
            }
            String name = String.format(getMessage("LBL_WrappedLibraryFmt"), FileUtil.toFile(jfo).getName());
            return ActionFilterNode.create(PackageView.createPackageView(new LibrariesSourceGroup(root, name, icon, icon)));
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            // XXX this is a little strange but happens during project move. Bad ordering.
            // Probably bug in moving implementation (our or in general Project API).
            if (! project.isRunInAtomicAction() && project.getHelper().resolveFileObject(AntProjectHelper.PROJECT_XML_PATH) != null) {
                refreshKeys();
            }
        }

        public void propertiesChanged(AntProjectEvent ev) {
            // do not need
        }

        /*
        private Node getNodeDelegate(final File jarF) {
        Node n = null;
        assert jarF != null;
        FileObject jar = FileUtil.toFileObject(jarF);
        if (jarF != null) {
        DataObject dobj;
        try {
        dobj = DataObject.find(jar);
        if (dobj != null) {
        n = dobj.getNodeDelegate();
        }
        } catch (DataObjectNotFoundException e) {
        assert false : e;
        }
        }
        return n;
        }
         */
        private Icon getLibrariesIcon() {
            if (librariesIcon == null) {
                librariesIcon = ImageUtilities.loadImageIcon(UIUtil.LIBRARIES_ICON, true);
            }
            return librariesIcon;
        }
    }

    private static final class ProjectDependencyNode extends AbstractNode {

        private final ModuleDependency dep;
        private final NbModuleProject project;

        ProjectDependencyNode(final ModuleDependency dep, final NbModuleProject project) {
            super(Children.LEAF, Lookups.fixed(dep, project, dep.getModuleEntry()));
            this.dep = dep;
            this.project = project;
            ModuleEntry me = dep.getModuleEntry();
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            setDisplayName(me.getLocalizedName());
            setShortDescription(LibrariesNode.createHtmlDescription(dep));
        }

        public @Override String getHtmlDisplayName() {
            if (dep.getModuleEntry().isDeprecated()) {
                try {
                    return "<s>" + XMLUtil.toElementContent(getDisplayName()) + "</s>"; // NOI18N
                } catch (CharConversionException x) {
                    // ignore
                }
            }
            return null;
        }

        public Action[] getActions(boolean context) {
            return new Action[]{
                        SystemAction.get(OpenProjectAction.class),
                        new EditDependencyAction(dep.getModuleEntry().getCodeNameBase(), project),
                        new ShowJavadocAction(dep, project),
                        SystemAction.get(RemoveAction.class),
                    };
        }

        public Action getPreferredAction() {
            return getActions(false)[0]; // open
        }

        public boolean canDestroy() {
            return true;
        }

        public void destroy() throws IOException {
            removeDependency(project, dep);
        }
    }

    private static final class LibraryDependencyNode extends FilterNode {

        private final ModuleDependency dep;
        private final NbModuleProject project;

        LibraryDependencyNode(final ModuleDependency dep,
                final NbModuleProject project, final Node original) {
            super(original, null, new ProxyLookup(original.getLookup(), Lookups.fixed(dep, project)));
            this.dep = dep;
            this.project = project;
            setShortDescription(LibrariesNode.createHtmlDescription(dep));
        }

        public @Override String getHtmlDisplayName() {
            if (dep.getModuleEntry().isDeprecated()) {
                try {
                    return "<s>" + XMLUtil.toElementContent(getDisplayName()) + "</s>"; // NOI18N
                } catch (CharConversionException x) {
                    // ignore
                }
            }
            return null;
        }

        public Action[] getActions(boolean context) {
            return new Action[]{
                        new EditDependencyAction(dep.getModuleEntry().getCodeNameBase(), project),
                        SystemAction.get(FindAction.class),
                        new ShowJavadocAction(dep, project),
                        SystemAction.get(RemoveAction.class),
                    };
        }

        public Action getPreferredAction() {
            return new EditDependencyAction(dep.getModuleEntry().getCodeNameBase(), project);
        }

        public boolean canDestroy() {
            return true;
        }

        public void destroy() throws IOException {
            removeDependency(project, dep);
        }
    }

    private static void removeDependency(NbModuleProject project, ModuleDependency dep) throws IOException {
        new ProjectXMLManager(project).removeDependencies(Collections.singleton(dep));
        ProjectManager.getDefault().saveProject(project);
    }

    private static final class AddModuleDependencyAction extends AbstractAction {

        private final NbModuleProject project;

        AddModuleDependencyAction(final NbModuleProject project) {
            super(getMessage("CTL_AddModuleDependency"));
            this.project = project;
        }

        public void actionPerformed(ActionEvent ev) {
            SingleModuleProperties props = SingleModuleProperties.getInstance(project);
            final ModuleDependency[] newDeps = AddModulePanel.selectDependencies(props);
            final AtomicBoolean cancel = new AtomicBoolean();
            final Set<ModuleDependency> dependencies = new HashSet<ModuleDependency>(Arrays.asList(newDeps));
            if(project.getModuleType() == NbModuleType.SUITE_COMPONENT)
            {
                   File suiteDirectory = project.getLookup().lookup(SuiteProvider.class).getSuiteDirectory();
                   if(suiteDirectory!=null)
                   {
                        FileObject suiteDirectoryFO = FileUtil.toFileObject(suiteDirectory);
                        if(suiteDirectoryFO != null)
                        {
                            try {
                                final Project suiteProject = ProjectManager.getDefault().findProject(suiteDirectoryFO);
                                if(suiteProject!=null)
                                {
                                    Set<NbModuleProject> subModules = SuiteUtils.getSubProjects(suiteProject);
                                    final SuiteProperties suiteProps = new SuiteProperties((SuiteProject) suiteProject, ((SuiteProject) suiteProject).getHelper(),
                                        ((SuiteProject) suiteProject).getEvaluator(), subModules);

                                    TreeSet<String> includedClusters = null;
                                    Set<ClusterInfo> clusterInfoSet = suiteProps.getClusterPath();
                                    if(clusterInfoSet!=null)
                                    {
                                        includedClusters = new TreeSet<String>();
                                        for(ClusterInfo infoIter:clusterInfoSet)
                                        {
                                            File clusterDirectory = infoIter.getClusterDir();
                                            if(clusterDirectory!=null)
                                            {
                                                includedClusters.add(clusterDirectory.getName());
                                            }
                                        }
                                    }
                                    
                                    Set<String> disabledModules = new HashSet<String>(Arrays.asList(suiteProps.getDisabledModules()));
                                    Set<ModuleDependency> dependenciesToIter = new HashSet<ModuleDependency>(Arrays.asList(newDeps));
                                    List<ClusterInfo> updatedClusterPath = null;
                                    boolean changed = false;
                                    for(ModuleDependency moduleDepIter:dependenciesToIter)
                                    {
                                        if(disabledModules.contains(moduleDepIter.getModuleEntry().getCodeNameBase()))
                                        {
                                            NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(NbBundle.getMessage(LibrariesNode.class, "MSG_AddModuelToTargetPlatform", moduleDepIter.getModuleEntry().getLocalizedName()), NbBundle.getMessage(LibrariesNode.class, "MSG_AddModuelToTargetPlatformTitle"), NotifyDescriptor.YES_NO_OPTION);
                                            DialogDisplayer.getDefault().notify(confirmation);
                                            if (confirmation.getValue() == NotifyDescriptor.YES_OPTION) {
                                                disabledModules.remove(moduleDepIter.getModuleEntry().getCodeNameBase());
                                                changed = true;
                                            }
                                            else
                                            {
                                                dependencies.remove(moduleDepIter);
                                            }
                                         }
                                         else if(includedClusters != null && !includedClusters.
                                                 contains(moduleDepIter.getModuleEntry().getClusterDirectory().getName()))
                                         {
                                             if(suiteProps.getActivePlatform() != null)
                                             {
                                                NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(NbBundle.getMessage(LibrariesNode.class, "MSG_AddModuelToTargetPlatformWithItsCluster", moduleDepIter.getModuleEntry().getLocalizedName(), moduleDepIter.getModuleEntry().getClusterDirectory().getName()), NbBundle.getMessage(LibrariesNode.class, "MSG_AddModuelToTargetPlatformTitle"), NotifyDescriptor.YES_NO_OPTION);
                                                DialogDisplayer.getDefault().notify(confirmation);
                                                if (confirmation.getValue() == NotifyDescriptor.YES_OPTION) { 

                                                    ClusterInfo newClusterInfo = ClusterInfo.create(moduleDepIter.getModuleEntry().getClusterDirectory(), 
                                                            true, true);
                                                    if(updatedClusterPath == null) {
                                                        updatedClusterPath = new ArrayList<ClusterInfo>();
                                                        updatedClusterPath.addAll(suiteProps.getClusterPath());
                                                    }
                                                    updatedClusterPath.add(newClusterInfo);

                                                    Set<ModuleEntry> moduleList = suiteProps.getActivePlatform().getModules();
                                                    TreeSet<String> disabledClusterDependencies = new TreeSet<String>();
                                                    for(ModuleEntry entryIter:moduleList)
                                                    {
                                                        if(entryIter.getClusterDirectory().equals(moduleDepIter.getModuleEntry().getClusterDirectory()))
                                                        {
                                                            disabledClusterDependencies.add(entryIter.getCodeNameBase());
                                                        }
                                                    }
                                                    disabledClusterDependencies.remove(moduleDepIter.getModuleEntry().getCodeNameBase());
                                                    disabledModules.addAll(disabledClusterDependencies);
                                                    changed = true;
                                                }
                                                else
                                                {
                                                    dependencies.remove(moduleDepIter);
                                                }
                                            }
                                            else
                                            {
                                                dependencies.remove(moduleDepIter);
                                            }
                                         }
                                      }
                                      if(changed)
                                      {
                                            String [] updatedDiasabledModules = new String[disabledModules.size()];
                                            disabledModules.toArray(updatedDiasabledModules);
                                            suiteProps.setDisabledModules(updatedDiasabledModules);
                                            if(updatedClusterPath != null && updatedClusterPath.size() > 0) {
                                                suiteProps.setClusterPath(updatedClusterPath);
                                            }
                                            ProjectManager.mutex().writeAccess(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        suiteProps.storeProperties();
                                                        ProjectManager.getDefault().saveProject(suiteProject);
                                                        } catch (IOException ex) {
                                                             Exceptions.printStackTrace(ex);
                                                        }
                                                }
                                            });
                                      }
                                }
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IllegalArgumentException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
            }
            if(dependencies.size() > 0)
            {
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public @Override void run() {
                        ProjectXMLManager pxm = new ProjectXMLManager(project);
                        try {
                            pxm.addDependencies(dependencies); // XXX cannot cancel
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            LOG.log(Level.INFO, "Cannot add selected dependencies: " + Arrays.asList(newDeps), e);
                        } catch (ProjectXMLManager.CyclicDependencyException ex) {
                            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(msg);
                        }
                    }
                }, NbBundle.getMessage(LibrariesNode.class, "LibrariesNode.update_deps"), cancel, false);
            }
        }
    }

    private static final class EditDependencyAction extends AbstractAction {

        private final NbModuleProject project;
        private final String codeNameBase;

        EditDependencyAction(final String codeNameBase, final NbModuleProject project) {
            super(getMessage("CTL_EditDependency"));
            this.codeNameBase = codeNameBase;
            this.project = project;
        }

        public void actionPerformed(ActionEvent ev) {
            SuiteProvider sp = project.getLookup().lookup(SuiteProvider.class);
            if (sp != null) {
                ModuleList.refreshModuleListForRoot(sp.getSuiteDirectory());
            }
            final ProjectXMLManager pxm = new ProjectXMLManager(project);
            final ModuleDependency dep;
            try {
                dep = pxm.getModuleDependency(codeNameBase);
            } catch (IOException e) {
                LOG.log(Level.INFO, "Cannot get dependencies for module: " + codeNameBase, e);
                return;
            }

            // XXX duplicated from CustomizerLibraries --> Refactor
            NbPlatform plaf = project.getPlatform(true);
            EditDependencyPanel editPanel = new EditDependencyPanel(dep, plaf);
            DialogDescriptor descriptor = new DialogDescriptor(editPanel,
                    NbBundle.getMessage(LibrariesNode.class, "CTL_EditModuleDependencyTitle",
                    dep.getModuleEntry().getLocalizedName()));
            descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.EditDependencyPanel"));
            Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
            try {
                d.setVisible(true);
                if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                    final ModuleDependency editedDep = editPanel.getEditedDependency();
                    final AtomicBoolean cancel = new AtomicBoolean();
                    ProgressUtils.runOffEventDispatchThread(new Runnable() {
                        public @Override void run() {
                            try {
                                SortedSet<ModuleDependency> deps = new TreeSet<ModuleDependency>(pxm.getDirectDependencies());
                                deps.remove(dep);
                                deps.add(editedDep);
                                if (cancel.get()) {
                                    return;
                                }
                                pxm.replaceDependencies(deps); // XXX cannot cancel
                                ProjectManager.getDefault().saveProject(project);
                            } catch (IOException e) {
                                LOG.log(Level.INFO, "Cannot store dependency: " + editedDep, e);
                            } catch (ProjectXMLManager.CyclicDependencyException ex) {
                                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                                DialogDisplayer.getDefault().notify(msg);
                            }
                        }
                    }, NbBundle.getMessage(LibrariesNode.class, "LibrariesNode.update_deps"), cancel, false);
                }
            } finally {
                d.dispose();
            }
        }
    }

    private static final class ShowJavadocAction extends AbstractAction {

        private final ModuleDependency dep;
        private final NbModuleProject project;
        private URL currectJavadoc;

        ShowJavadocAction(final ModuleDependency dep, final NbModuleProject project) {
            super(getMessage("CTL_ShowJavadoc"));
            this.dep = dep;
            this.project = project;
        }

        public void actionPerformed(ActionEvent ev) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(currectJavadoc);
        }

        public boolean isEnabled() {
            currectJavadoc = dep.getModuleEntry().getJavadoc(project.getPlatform(true));
            return currectJavadoc != null;
        }
    }

    static final class OpenProjectAction extends CookieAction {

        protected void performAction(Node[] activatedNodes) {
            try {
                final Project[] projects = new Project[activatedNodes.length];
                for (int i = 0; i < activatedNodes.length; i++) {
                    ModuleEntry me = activatedNodes[i].getLookup().lookup(ModuleEntry.class);
                    assert me != null;
                    File prjDir = me.getSourceLocation();
                    assert prjDir != null;
                    Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDir));
                    assert project != null;
                    projects[i] = project;
                }
                RP.post(new Runnable() {
                    public @Override void run() {
                        StatusDisplayer.getDefault().setStatusText(
                                getMessage("MSG_OpeningProjects"));
                        OpenProjects.getDefault().open(projects, false);
                    }
                });
            } catch (IOException e) {
                assert false : e;
            }
        }

        public boolean isEnabled() {
            return true;
        }

        public String getName() {
            return getMessage("CTL_Open");
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected boolean asynchronous() {
            return false;
        }

        protected int mode() {
            return CookieAction.MODE_ALL;
        }

        protected Class[] cookieClasses() {
            return new Class[]{ModuleDependency.class, TestModuleDependency.class};
        }
    }

    private static final class RemoveAction extends DeleteAction {

        public String getName() {
            return getMessage("CTL_RemoveDependency");
        }

        protected void initialize() {
            super.initialize();
            putValue(Action.ACCELERATOR_KEY, SystemAction.get(DeleteAction.class).getValue(Action.ACCELERATOR_KEY));
        }
    }
}
