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

/*
 * CustomScopePanel.java
 *
 * Created on Jun 13, 2011, 9:51:30 AM
 */
package org.netbeans.modules.refactoring.java.ui.scope;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.CheckableNode;
import org.openide.explorer.view.OutlineView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Ralph Ruijs
 */
@NbBundle.Messages({"DLG_CustomScope=Custom Scope"})
public class CustomScopePanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    private static final Image PACKAGEBADGE = ImageUtilities.loadImage("org/netbeans/spi/java/project/support/ui/packageBadge.gif"); // NOI18N
    private static final String PACKAGE = "org/netbeans/spi/java/project/support/ui/package.gif"; // NOI18N
    private static final String CLASS = "org/netbeans/modules/java/source/resources/icons/class.png"; // NOI18N

    private abstract static class Data {

        private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
        public static final String PROP_SELECTED = "selected"; //NOI18N
        private String name;
        private Data parent;

        public Data(String name, Data parent) {
            this.name = name;
            this.parent = parent;
        }

        public Boolean isSelected() {
            Boolean selected;
            if(isFullySelected()) {
                selected = true;
            } else if(isPartiallySelected()) {
                selected = null;
            } else {
                selected = false;
            }
            return selected;
        }
        
        protected abstract boolean isFullySelected();
        protected abstract boolean isPartiallySelected();

        public void setSelected(boolean selected) {
            propertyChangeSupport.firePropertyChange(PROP_SELECTED, !Boolean.FALSE.equals(isSelected()), selected);
            setSelected(selected, true);
        }
        
        protected abstract void setSelected(boolean selected, boolean event);

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        public String getName() {
            return name;
        }

        public Data getParent() {
            return parent;
        }
    }

    private static class ProjectData extends Data {

        private Icon icon;
        private List<SourceData> sources;

        public ProjectData(String name, Data parent) {
            super(name, parent);
            sources = new LinkedList<SourceData>();
        }

        private void setIcon(Icon icon) {
            this.icon = icon;
        }

        private Icon getIcon() {
            return this.icon;
        }

        public List<SourceData> getSources() {
            return sources;
        }

        @Override
        protected boolean isFullySelected() {
            boolean selected = true;
            for (SourceData sourceData : sources) {
                if(!sourceData.isFullySelected()) {
                    selected = false;
                }
            }
            return selected;
        }

        @Override
        protected boolean isPartiallySelected() {
            boolean selected = false;
            for (SourceData sourceData : sources) {
                if(sourceData.isFullySelected() || sourceData.isPartiallySelected()) {
                    selected = true;
                }
            }
            return selected;
        }

        @Override
        public void setSelected(boolean selected, boolean event) {
            for (SourceData sourceData : sources) {
                sourceData.setSelected(selected);
            }
        }
    }

    private static class SourceData extends Data {

        private FileObject sourceRoot;
        private Image icon, openedIcon;
        private Map<String, PackageData> packages;
        private final Set<FileObject> sourceRoots;

        public SourceData(String name, Data parent, FileObject sourceRoot, Set<FileObject> sourceRoots) {
            super(name, parent);
            packages = new TreeMap<String, PackageData>();
            this.sourceRoot = sourceRoot;
            this.sourceRoots = sourceRoots;
        }

        public Image getIcon() {
            return icon;
        }

        public void setIcon(Image icon) {
            this.icon = icon;
        }

        public Image getOpenedIcon() {
            return openedIcon;
        }

        public void setOpenedIcon(Image openedIcon) {
            this.openedIcon = openedIcon;
        }

        public Map<String, PackageData> getPackages() {
            return packages;
        }

        public FileObject getSourceRoot() {
            return sourceRoot;
        }

        @Override
        public void setSelected(boolean selected, boolean event) {
            if(selected) {
                sourceRoots.add(sourceRoot);
            } else {
                if(sourceRoots.contains(sourceRoot)) {
                    sourceRoots.remove(sourceRoot);
                } else if(isPartiallySelected()) {
                    for (Map.Entry<String, PackageData> entry : getPackages().entrySet()) {
                        entry.getValue().setSelected(false);
                    }
                }
            }
        }

        @Override
        protected boolean isFullySelected() {
            return sourceRoots.contains(sourceRoot);
        }

        @Override
        protected boolean isPartiallySelected() {
            boolean selected = false;
            for (Map.Entry<String, PackageData> entry : packages.entrySet()) {
                if(entry.getValue().isFullySelected() || entry.getValue().isPartiallySelected()) {
                    selected = true;
                }
            }
            return selected;
        }
    }

    private static class PackageData extends Data {

        private SourceData source;
        private List<ClassData> classes;
        private NonRecursiveFolder folder;
        private final Map<String, NonRecursiveFolder> folders;

        public PackageData(SourceData source, String name, Data parent, final FileObject folder, Map<String, NonRecursiveFolder> folders) {
            super(name, parent);
            classes = new LinkedList<ClassData>();
            this.source = source;
            this.folder = new NonRecursiveFolder() {

                @Override
                public FileObject getFolder() {
                    return folder;
                }
            };
            this.folders = folders;
        }

        public List<ClassData> getClasses() {
            return classes;
        }

        public SourceData getSource() {
            return source;
        }

        @Override
        protected boolean isFullySelected() {
            return folders.containsKey(folder.getFolder().getPath())
                    || getParent().isFullySelected();
        }

        @Override
        protected boolean isPartiallySelected() {
            boolean selected = false;
            for (ClassData classData : classes) {
                if (classData.isFullySelected() || classData.isPartiallySelected()) {
                    selected = true;
                    break;
                }
            }
            return selected;
        }

        @Override
        public void setSelected(boolean selected, boolean recursive) {
            if(selected) {
                folders.put(folder.getFolder().getPath(), folder);
            } else {
                if(folders.containsKey(folder.getFolder().getPath())) {
                    folders.remove(folder.getFolder().getPath());
                } else if(isFullySelected()) {
                    Data parent = getParent();
                    parent.setSelected(false);
                    for (Map.Entry<String, PackageData> entry : source.getPackages().entrySet()) {
                        PackageData packageData = entry.getValue();
                        if(packageData != this) {
                            packageData.setSelected(true);
                        }
                    }
                } else if(isPartiallySelected()) {
                    for (ClassData classData : classes) {
                        classData.setSelected(false);
                    }
                }
            }
        }
    }

    private static class ClassData extends Data {

        private SourceData source;
        private FileObject file;
        private final Set<FileObject> files;

        public ClassData(SourceData source, String name, FileObject file, Data parent, Set<FileObject> files) {
            super(name, parent);
            this.source = source;
            this.file = file;
            this.files = files;
        }

        public SourceData getSource() {
            return source;
        }

        public FileObject getFile() {
            return file;
        }

        @Override
        protected boolean isFullySelected() {
            return files.contains(file) || getParent().isFullySelected();
        }

        @Override
        protected boolean isPartiallySelected() {
            return false;
        }

        @Override
        protected void setSelected(boolean selected, boolean event) {
            if(selected) {
                files.add(file);
            } else {
                if(files.contains(file)) {
                    files.remove(file);
                } else if(isFullySelected()) {
                    PackageData parent = (PackageData) getParent();
                    parent.setSelected(false);
                    List<ClassData> classes = parent.getClasses();
                    for (ClassData classData : classes) {
                        if(classData != this) {
                            classData.setSelected(true);
                        }
                    }
                }
            }
        }
    }

    private abstract static class CustomNode extends AbstractNode implements PropertyChangeListener {

        protected final CustomScopePanel panel;
        private final Data data;

        public CustomNode(Children children, Lookup lookup, CustomScopePanel panel, final Data data) {
            super(children, new ProxyLookup(lookup, Lookups.fixed(new CheckableNode() {

                /**
                 * Tell the view to display a check-box for this node.
                 *
                 * @return <code>true</code> if the check-box should be displayed, <code>false</code> otherwise.
                 */
                @Override
                public boolean isCheckable() {
                    return true;
                }

                /**
                 * Provide the enabled state of the check-box.
                 *
                 * @return <code>true</code> if the check-box should be enabled, <code>false</code> otherwise.
                 */
                @Override
                public boolean isCheckEnabled() {
                    return true;
                }

                /**
                 * Provide the selected state of the check-box.
                 *
                 * @return <code>true</code> if the check-box should be selected,
                 *         <code>false</code> if it should be unselected and
                 *         <code>null</code> if the state is unknown.
                 */
                @Override
                public Boolean isSelected() {
                    return data.isSelected();
                }

                /**
                 * Called by the view when the check-box gets selected/unselected
                 *
                 * @param selected <code>true</code> if the check-box was selected,
                 *                 <code>false</code> if the check-box was unselected.
                 */
                @Override
                public void setSelected(Boolean selected) {
                    data.setSelected(selected);
                }
            })));
            data.addPropertyChangeListener(WeakListeners.propertyChange(this, data));
            this.panel = panel;
            this.data = data;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

        @Override
        public String getHtmlDisplayName() {
            String htmlDisplayName = super.getHtmlDisplayName() == null ? getDisplayName() : super.getHtmlDisplayName();
            if (data.isSelected() == null) {
                // TODO: Get colors from settings
                htmlDisplayName = String.format("<font color=\"#0000E6\">%s</font>", htmlDisplayName); //NOI18N
            } else if(!data.isSelected()) {
                htmlDisplayName = String.format("<font color=\"#969696\">%s</font>", htmlDisplayName); //NOI18N
            }
            return htmlDisplayName;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Data.PROP_SELECTED)) {
                if(getParentNode() instanceof CustomNode) {
                    CustomNode customNode = (CustomNode) getParentNode();
                    fireParentsIconChanged(customNode);
                }
                fireChildsIconChanged(getChildren());
            }
        }

        private void fireParentsIconChanged(CustomNode parentNode) {
            parentNode.fireIconChange();
            if(parentNode.getParentNode() instanceof CustomNode) {
                CustomNode customNode = (CustomNode) parentNode.getParentNode();
                fireParentsIconChanged(customNode);
            }
        }

        private void fireChildsIconChanged(Children children) {
            for (Node node : children.getNodes()) {
                ((CustomNode)node).fireIconChange();
                fireChildsIconChanged(node.getChildren());
            }
        }
    }

    private static class ProjectNode extends CustomNode {

        private final ProjectData data;

        public ProjectNode(ProjectData data, CustomScopePanel panel) {
            super(createChildren(data, panel), Lookups.fixed(data), panel, data);
            this.data = data;
            setDisplayName(data.getName());
        }

        private static Children createChildren(ProjectData data, CustomScopePanel panel) {
            Children childs = new Children.Array();
            for (SourceData sourceData : data.getSources()) {
                childs.add(new Node[]{new SourceGroupNode(sourceData, panel)});
            }
            return childs;
        }

        @Override
        public Image getIcon(int type) {
            Icon icon = data.getIcon();
            if (icon instanceof ImageIcon) {
                return ((ImageIcon) icon).getImage();
            } else {
                int w = icon.getIconWidth();
                int h = icon.getIconHeight();
                GraphicsEnvironment ge =
                        GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gd.getDefaultConfiguration();
                BufferedImage image = gc.createCompatibleImage(w, h);
                Graphics2D g = image.createGraphics();
                icon.paintIcon(null, g, 0, 0);
                g.dispose();
                return image;
            }
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }

    private static class SourceGroupNode extends CustomNode {

        private final SourceData data;

        public SourceGroupNode(SourceData data, CustomScopePanel panel) {
            super(new Childs(data, panel), Lookups.fixed(data), panel, data);
            this.data = data;
            setDisplayName(data.getName());
        }

        private static class Childs extends Children.Keys<String> {

            private final SourceData data;
            private final CustomScopePanel panel;

            public Childs(SourceData data, CustomScopePanel panel) {
                super(true);
                this.data = data;
                setKeys(data.getPackages().keySet());
                this.panel = panel;
            }

            @Override
            protected Node[] createNodes(String key) {
                return new Node[]{new PackageNode(data.getPackages().get(key), panel)};
            }
        }

        @Override
        public Image getIcon(int type) {
            return data.getIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return data.getOpenedIcon();
        }
    }

    private static class PackageNode extends CustomNode {

        private final PackageData data;

        public PackageNode(PackageData data, CustomScopePanel panel) {
            super(createChildren(data, panel), Lookups.fixed(data), panel, data);
            setDisplayName(data.getName().isEmpty()? "<default package>" : data.getName()); //NOI18N
            setIconBaseWithExtension(PACKAGE);
            this.data = data;
        }

        private static Children createChildren(final PackageData data, CustomScopePanel panel) {
            Children childs = new Children.Array();
            for (ClassData classData : data.getClasses()) {
                childs.add(new Node[]{new ClassNode(classData, panel)});
            }
            return childs;
        }

        @Override
        public String getHtmlDisplayName() {
            String htmlDisplayName = data.getName().isEmpty()? "&lt;default package&gt;" : data.getName(); //NOI18N
            if (data.isSelected() == null) {
                // TODO: Get colors from settings
                htmlDisplayName = String.format("<font color=\"#0000E6\">%s</font>", htmlDisplayName); //NOI18N
            } else if(!data.isSelected()) {
                htmlDisplayName = String.format("<font color=\"#969696\">%s</font>", htmlDisplayName); //NOI18N
            }
            return htmlDisplayName;
        }
    }

    private static class ClassNode extends CustomNode {

        public ClassNode(ClassData data, CustomScopePanel panel) {
            super(Children.LEAF, Lookups.fixed(data), panel, data);
            setDisplayName(data.getName());
            setIconBaseWithExtension(CLASS);
        }
    }
    
    private static class WaitNode extends AbstractNode {

        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/refactoring/java/resources/wait.gif"); // NOI18N

        WaitNode() {
            super(Children.LEAF);
        }

        @Override
        public Image getIcon(int type) {
            return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(CustomScopePanel.class, "LBL_WaitNode"); // NOI18N
        }
    }    
    
    private ExplorerManager manager;
    private boolean initialized;
    private List<ProjectData> projectList;
    private Set<FileObject> sourceRoots;
    private Set<FileObject> files;
    private Map<String, NonRecursiveFolder> folders;

    /** Creates new form CustomScopePanel */
    public CustomScopePanel() {
        projectList = new LinkedList<ProjectData>();
        sourceRoots = new HashSet<FileObject>();
        files = new HashSet<FileObject>();
        folders = new HashMap<String, NonRecursiveFolder>();
        manager = new ExplorerManager();
        initComponents();
        manager.setRootContext(new WaitNode());
        outlineView1.getOutline().setColumnHidingAllowed(false);
    }

    public Scope getCustomScope() {
        return Scope.create(sourceRoots, folders.values(), files);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public void initialize(Scope customScope) {
        if (!initialized) {
            if(customScope != null) {
                sourceRoots.addAll(customScope.getSourceRoots());
                
                for (NonRecursiveFolder folder : customScope.getFolders()) {
                    folders.put(folder.getFolder().getPath(), folder);
                }
                
                files.addAll(customScope.getFiles());
            }
            for (Project project : OpenProjects.getDefault().getOpenProjects()) {
                ProjectInformation information = ProjectUtils.getInformation(project);
                ProjectData pData = new ProjectData(information.getDisplayName(), null);
                pData.setIcon(information.getIcon());

                SourceGroup[] sources = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (SourceGroup sg : sources) {
                    SourceData sourceData = new SourceData(sg.getDisplayName(), pData, sg.getRootFolder(), sourceRoots);

                    try {
                        Image icon = DataObject.find(sg.getRootFolder()).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_32x32);
                        icon = ImageUtilities.mergeImages(icon, PACKAGEBADGE, 7, 7);
                        sourceData.setIcon(icon);
                    } catch (DataObjectNotFoundException ex) {
                        // Not important, is only for the icon
                    }

                    try {
                        Image icon = DataObject.find(sg.getRootFolder()).getNodeDelegate().getOpenedIcon(BeanInfo.ICON_COLOR_32x32);
                        icon = ImageUtilities.mergeImages(icon, PACKAGEBADGE, 7, 7);
                        sourceData.setOpenedIcon(icon);
                    } catch (DataObjectNotFoundException ex) {
                        // Not important, is only for the icon
                    }

                    ClassPath rcp = ClassPathSupport.createClassPath(sg.getRootFolder());
                    ClasspathInfo cpInfo = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, rcp);
                    ClassIndex index = cpInfo.getClassIndex();
                    Set<String> packageNames = index.getPackageNames("", false, EnumSet.of(ClassIndex.SearchScope.SOURCE)); // NOI18N
                    for (String packageName : packageNames) {
                        String pathname = packageName.replace(".", "/"); // NOI18N
                        final FileObject folder = sg.getRootFolder().getFileObject(pathname);
                        if(folder != null) {
                            PackageData data = new PackageData(sourceData, packageName, sourceData, folder, folders); // NOI18N
                            sourceData.getPackages().put(packageName, data);
                        }
                    }
                    
                    Set<ElementHandle<TypeElement>> declaredTypes = index.getDeclaredTypes("", ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.SOURCE));
                    for (ElementHandle<TypeElement> elementHandle : declaredTypes) {

                        String qualifiedName = elementHandle.getQualifiedName();
                        String packageName = ""; // NOI18N
                        String className = qualifiedName;

                        int delimiter = qualifiedName.lastIndexOf("."); // NOI18N
                        if (delimiter > 0) {
                            packageName = qualifiedName.substring(0, delimiter);
                            className = qualifiedName.substring(delimiter + 1);
                        }
                        PackageData data = sourceData.getPackages().get(packageName);
                        FileObject file = resolveFile(sg.getRootFolder(), elementHandle.getBinaryName());
                        if (data != null && file != null) {
                            ClassData classData = new ClassData(sourceData, className, file, data, files);
                            data.getClasses().add(classData);
                        }
                    }
                    pData.getSources().add(sourceData);
                }
                if(pData.getSources().isEmpty()) {
                    LOG.log(Level.INFO, "Poject {0} does not have any source roots", pData.getName());
                } else {
                    projectList.add(pData);
                }
            }
            if(customScope == null) {
                for (ProjectData projectData : projectList) {
                    projectData.setSelected(true);
                }
            }
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final AbstractNode rootNode = new AbstractNode(new Children.Array() {

                               @Override
                               protected Collection<Node> initCollection() {
                                   List<Node> collection = new LinkedList<Node>();
                                   for (ProjectData projectData : projectList) {
                                       collection.add(new ProjectNode(projectData, CustomScopePanel.this));
                                   }
                                   return collection;
                               }
                           });
                    manager.setRootContext(rootNode);
                    outlineView1.getOutline().setRootVisible(false);
                }
            });
        }
        initialized = true;
    }
    private static final Logger LOG = Logger.getLogger(CustomScopePanel.class.getName());
    
    private static FileObject resolveFile(final FileObject root, String classBinaryName) {
        assert classBinaryName != null;
        classBinaryName = classBinaryName.replace('.', '/');    //NOI18N
        int index = classBinaryName.lastIndexOf('/');           //NOI18N
        FileObject folder;
        String name;
        if (index < 0) {
            folder = root;
            name = classBinaryName;
        } else {
            assert index > 0 : classBinaryName;
            assert index < classBinaryName.length() - 1 : classBinaryName;
            folder = root.getFileObject(classBinaryName.substring(0, index));
            name = classBinaryName.substring(index + 1);
        }
        if (folder == null) {
            return null;
        }
        index = name.indexOf('$');                              //NOI18N
        if (index > 0) {
            name = name.substring(0, index);
        }
        for (FileObject child : folder.getChildren()) {
            if ("java".equalsIgnoreCase(child.getExt()) && name.equals(child.getName())) { // NOI18N
                return child;
            }
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outlineView1 = new OutlineView(NbBundle.getMessage(CustomScopePanel.class, "DLG_CustomScope"));

        outlineView1.setDefaultActionAllowed(false);
        outlineView1.setDoubleBuffered(true);
        outlineView1.setDragSource(false);
        outlineView1.setDropTarget(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(outlineView1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(outlineView1, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.OutlineView outlineView1;
    // End of variables declaration//GEN-END:variables
}
