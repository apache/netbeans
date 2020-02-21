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

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.BrokenLinks.BrokenLink;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup.Template;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.SnapShot;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeLogicalViewModel;

/**
 * Support for creating logical views.
 */
public class MakeLogicalViewProvider implements LogicalViewProvider, MakeLogicalViewModel {

    @ServiceProvider(service = MakeProjectLookupProvider.class)
    public static class MakeLogicalViewProviderFactory implements MakeProjectLookupProvider {

        @Override
        public void addLookup(MakeProject owner, ArrayList<Object> ic) {
            ic.add(new MakeLogicalViewProvider(owner));
        }
    }
    // this is hard reference to template operations
    private static MakeTemplateListener templateListener;

    private static final String brokenLinkBadgePath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
    private static final String brokenProjectBadgePath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
    private static final String brokenFolderBadgePath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
    private static final String brokenIncludeImgPath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenIncludeBadge.png"; // NOI18N
    static final Image brokenLinkBadge = loadToolTipImage(brokenLinkBadgePath, "BrokenLinkTxt"); // NOI18N
    static final Image brokenProjectBadge = loadToolTipImage(brokenProjectBadgePath, "BrokenProjectTxt"); // NOI18N
    static final Image brokenFolderBadge = loadToolTipImage(brokenFolderBadgePath, "BrokenFolderTxt"); // NOI18N
    static final Image brokenIncludeBadge = loadToolTipImage(brokenIncludeImgPath, "BrokenIncludeTxt"); // NOI18N
    static final String SUBTYPE = "x-org-netbeans-modules-cnd-makeproject-uidnd"; // NOI18N
    static final String SUBTYPE_FOLDER = "x-org-netbeans-modules-cnd-makeproject-uidnd-folder"; // NOI18N
    static final String MASK = "mask"; // NOI18N
    private final RequestProcessor annotationRP;
    private final MakeProject project;
    private MakeLogicalViewRootNode projectRootNode;
    private final Object lock = new Object();
    private boolean checkVersion = true;

    public MakeLogicalViewProvider(MakeProject project) {
        this.project = project;
        // it is important to have RP with capacity "1", due to bug #223587 which races between
        // runners: BaseMakeViewChildren.refreshKeysTask & MakeLogicalViewProvider.setVisible
        annotationRP = new RequestProcessor("MakeLogicalViewProvider.AnnotationUpdater " + project, 1); // NOI18N
        assert project != null;
        synchronized(MakeLogicalViewProvider.class) {
            // it is created instance of template oerations and register in data pool
            if (templateListener == null) {
                templateListener = MakeTemplateListener.createInstance();
            }
        }
    }

    @Override
    public Node createLogicalView() {
        if (gotMakeConfigurationDescriptor()) {
            MakeConfigurationDescriptor configurationDescriptor = getMakeConfigurationDescriptor();
            if (configurationDescriptor == null) {
                return new MakeLogicalViewRootNodeBroken(project);
            } else {
                synchronized(lock) {
                    if (projectRootNode == null) {
                        createRoot(configurationDescriptor);
                    }
                    return projectRootNode;
                }
            }
        } else {
            synchronized(lock) {
                if (projectRootNode == null) {
                    createLoadingRoot();
                }
                return projectRootNode;
            }
        }
    }
    
    RequestProcessor getAnnotationRP() {
        return annotationRP;
    }

    private void createRoot(MakeConfigurationDescriptor configurationDescriptor) {
        InstanceContent ic = new InstanceContent();
        Folder logicalFolders = configurationDescriptor.getLogicalFolders();
        ic.add(logicalFolders);
        addLookup(ic);
        projectRootNode = new MakeLogicalViewRootNode(logicalFolders, this, ic);
    }

    private void createLoadingRoot() {
        InstanceContent ic = new InstanceContent();
        addLookup(ic);
        projectRootNode = new MakeLogicalViewRootNode(null, this, ic);
    }
    
    public void reInit(MakeConfigurationDescriptor configurationDescriptor, boolean ignoreFutureVersion) {
        if (ignoreFutureVersion) {
            checkVersion = false;
        }
        if (projectRootNode != null) {
            projectRootNode.reInit(configurationDescriptor);
        }
    }
    
    public boolean isIncorectVersion() {
        if (checkVersion) {
            if (gotMakeConfigurationDescriptor()) {
                int version = getMakeConfigurationDescriptor().getVersion();
                return version > ConfigurationDescriptor.CURRENT_VERSION;
            }
        }
        return false;
    }
    
    private void addLookup(InstanceContent ic) {
        ic.add(getProject());
        SearchInfoDefinition searchInfo = project.getLookup().lookup(SearchInfoDefinition.class);
        ic.add(searchInfo);
        ic.add(project, new InstanceContent.Convertor<MakeProject, FileObject>() {
            @Override
            public FileObject convert(MakeProject obj) {
                return obj.getProjectDirectory();
            }

            @Override
            public Class<? extends FileObject> type(MakeProject obj) {
                return FileObject.class;
            }

            @Override
            public String id(MakeProject obj) {
                final FileObject fo = obj.getProjectDirectory();
                return fo == null ? "" : fo.getPath();  //NOI18N
            }

            @Override
            public String displayName(MakeProject obj) {
                return obj.toString();
            }
        });
        ic.add(project, new InstanceContent.Convertor<MakeProject, DataObject>() {
            @Override
            public DataObject convert(MakeProject obj) {
                try {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? null : DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public Class<? extends DataObject> type(MakeProject obj) {
                return DataObject.class;
            }

            @Override
            public String id(MakeProject obj) {
                final FileObject fo = obj.getProjectDirectory();
                return fo == null ? "" : fo.getPath();  //NOI18N
            }

            @Override
            public String displayName(MakeProject obj) {
                return obj.toString();
            }
        });
    }
    
    private final AtomicBoolean findPathMode = new AtomicBoolean(false);

    boolean isFindPathMode() {
        return findPathMode.get();
    }

    @Override
    public Node findPath(Node root, Object target) {
        Node returnNode = null;
        Project rootProject = root.getLookup().lookup(Project.class);
        if (rootProject == null) {
            return null;
        }

        if (target instanceof DataObject) {
            target = ((DataObject) target).getPrimaryFile();
        }

        if (!(target instanceof FileObject)) {
            return null;
        }

        // FIXUP: this doesn't work with file groups (jl: is this still true?)
        FileObject fo = (FileObject) target;
        if (!gotMakeConfigurationDescriptor() || !fo.isValid()) {
            // IZ 111884 NPE while creating a web project
            return null;
        }
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        Item item = makeConfigurationDescriptor.findItemByFileObject(fo);

        if (item == null) {
            item = makeConfigurationDescriptor.findExternalItemByPath(fo.getPath());
            if (item == null) {
                // try to find any item
                item = makeConfigurationDescriptor.findItemByPathSlowly(fo.getPath());
                if (item == null) {
                    //not found:
                    return null;
                }
            }
        }

        // prevent double entering
        if (findPathMode.compareAndSet(false, true)) {
            try {
                // FIXUP: assume nde node is last node in current folder. Is this always true?
                // Find the node and return it
                Node folderNode = findFolderNode(root, item.getFolder());
                if (folderNode != null) {
                    Node[] nodes = folderNode.getChildren().getNodes(true);
                    int index = 0;
                    String name = item.getName();
                    for (index = 0; index < nodes.length; index++) {
                        Item nodeItem = (Item) nodes[index].getValue("Item"); // NOI18N
                        if (nodeItem == item) {
                            break;
                        } else if (nodeItem != null) {
                            // try fallback by name
                            String checkName = nodeItem.getName();
                            if (name.equals(checkName)) {
                                break;
                            }
                        }
                    }
                    if (nodes.length > 0 && index < nodes.length) {
                        returnNode = nodes[index];
                    }
                    /*
                     if (nodes.length > 0)
                     returnNode = nodes[nodes.length -1];
                     */
                }
            } finally {
                findPathMode.set(false);
            }
        }
        return returnNode;
    }

    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'folder'
     */
    private static Node findFolderNode(Node root, Folder folder) {
        if (root.getValue("Folder") == folder) { // NOI18N
            return root;
        }
        Folder parent = folder.getParent();

        if (parent == null) {
            return root;
        }

        Node parentNode = findFolderNode(root, parent);

        if (parentNode == null) {
            return null;
        }

        Node[] nodes = parentNode.getChildren().getNodes(true);
        String id = folder.getId();
        for (int i = 0; i < nodes.length; i++) {
            Object folderValue = nodes[i].getValue("Folder"); // NOI18N
            if (folderValue == folder) {
                return nodes[i];
            } else if (folderValue != null) {
                // fallback check by folder-ID
                if (id.equals(((Folder) folderValue).getId())) { 
                    return nodes[i];
                }
            }
        }
        return null;
    }

    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'item'
     */
    private static Node findItemNode(Node root, Item item) {
        Node parentNode = findFolderNode(root, item.getFolder());
        if (parentNode != null) {
            Node[] nodes = parentNode.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].getValue("Item") == item) { // NOI18N
                    return nodes[i];
                }
            }
        }
        return null;
    }

    /**
     * HACK: set the folder node visible in the project explorer See IZ7551
     */
    public static void setVisible(Project project, Folder folder) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        Node projectRoot = findProjectNode(rootNode, project);

        if (projectRoot == null) {
            return;
        }

        Node folderNode = findFolderNode(projectRoot, folder);
        try {
            ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(new Node[]{folderNode});
        } catch (Exception e) {
            // skip
        }
    }

    public static void setVisible(final Project project, final Item[] items) {
        final Runnable runnable = () -> {
            SwingUtilities.invokeLater(() -> {
                Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
                List<Node> nodes = new ArrayList<>();
                for (int i = 0; i < items.length; i++) {
                    Node root = findProjectNode(rootNode, project);
                    
                    if (root != null) {
                        nodes.add(findItemNode(root, items[i]));
                    }
                }
                try {
                    ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(nodes.toArray(new Node[0]));
                } catch (Exception e) {
                    // skip
                }
            });
        };
        // See IZ223587. The intention is to guarantee that the selection logic
        // is executed after update loop is performed.
        // Unfortunately this approach uses static metod (as there is no access
        // to the needed children refresher...
        MakeLogicalViewProvider provider = project.getLookup().lookup(MakeLogicalViewProvider.class);
        if (provider != null) {
            provider.getAnnotationRP().post(runnable, BaseMakeViewChildren.WAIT_DELAY);
        }
    }

    @Override
    public void checkForChangedName() {
        checkForChangedName(project);
    }

    private static void checkForChangedName(final Project project) {
        if (CndUtils.isStandalone()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
            Node root = findProjectNode(rootNode, project);
            if (root != null) {
                ProjectInformation pi = ProjectUtils.getInformation(project);
                if (pi != null) { // node will check whether it equals...
                    root.setDisplayName(pi.getDisplayName());
                }
            }
        });
    }

    @Override
    public void checkForChangedViewItemNodes(SnapShot delta) {
        checkForChangedViewItemNodes(project, delta);
    }

    private static void checkForChangedViewItemNodes(final Project project, final SnapShot delta) {
        if (CndUtils.isStandalone()) {
            return;
        }
        if (delta.isViewChanged()) {
            refreshProjectNodes(project);
        }
    }

    @Override
    public void checkForChangedViewItemNodes(Folder folder, Item item) {
        checkForChangedViewItemNodes(project, folder, item);
    }

    private static void checkForChangedViewItemNodes(final Project project, final Folder folder, final Item item) {
        if (CndUtils.isStandalone()) {
            return;
        }

        refreshProjectNodes(project);
    }

    private static void refreshProjectNodes(final Project project) {
        ProjectNodesRefreshSupport.refreshProjectNodes(project);
    }

    @Override
    public void refreshBrokenItems() {
        refreshBrokenItems(project);
    }

    /**
     * This is long operation, don't call from EDT.
     *
     * @param project
     */
    public static void refreshBrokenItems(final Project project) {
        BrokenViewItemRefreshSupport.refreshBrokenItems(project);
    }

    private static Node getRootNode() {
        // ProjectTabBridge.getExplorerManager() wants to be called from EDT
        if (SwingUtilities.isEventDispatchThread()) {
            return ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        } else {
            final Node[] root = new Node[1];
            try {
                SwingUtilities.invokeAndWait(() -> {
                    root[0] = getRootNode();
                });
            } catch (InterruptedException ex) {
                // skip
            } catch (InvocationTargetException ex) {
                // skip
            }
            return root[0];
        }
    }

    private static Node findProjectNode(Node root, Project p) {
        Node[] n = root.getChildren().getNodes(true);
        Template<Project> t = new Template<>(null, null, p);

        for (int cntr = 0; cntr < n.length; cntr++) {
            if (n[cntr].getLookup().lookupItem(t) != null) {
                return n[cntr];
            }
        }

        return null;
    }

    // Private innerclasses ----------------------------------------------------
    public boolean hasBrokenLinks() {
        List<BrokenLink> errs = BrokenLinks.getBrokenLinks(project);
        return !errs.isEmpty();
    }

    private static Image loadToolTipImage(String imgResouce, String textResource) {
        Image img = ImageUtilities.loadImage(imgResouce);
        img = ImageUtilities.assignToolTipToImage(img,
                "<img src=\"" + MakeLogicalViewRootNode.class.getClassLoader().getResource(imgResouce) + "\">&nbsp;" // NOI18N
                + NbBundle.getMessage(MakeLogicalViewRootNode.class, textResource));
        return img;
    }

    static String getShortDescription(MakeProject project) {
        String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
        ExecutionEnvironment devHost = project.getDevelopmentHost();
        if (devHost == null || devHost.isLocal()) {
            return NbBundle.getMessage(MakeLogicalViewProvider.class,
                    "HINT_project_root_node", prjDirDispName); // NOI18N
        } else {
            return NbBundle.getMessage(MakeLogicalViewProvider.class,
                    "HINT_project_root_node_on_host", prjDirDispName, devHost.getDisplayName()); // NOI18N
        }
    }

    MakeProject getProject() {
        return project;
    }

    MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        return makeConfigurationDescriptor;
    }

    boolean gotMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return pdp.gotDescriptor();
    }
}
