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

package org.netbeans.spi.java.project.support.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.project.ui.JavaProjectSettings;
import org.netbeans.modules.java.project.ui.PackageDisplayUtils;
import static org.netbeans.spi.java.project.support.ui.Bundle.*;
import org.netbeans.spi.project.ui.PathFinder;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Factory for package views.
 * @see org.netbeans.spi.project.ui.LogicalViewProvider
 * @author Jesse Glick
 */
public class PackageView {
    
    private static final Logger LOG = Logger.getLogger(PackageView.class.getName());
        
    private PackageView() {}
    
    /**
     * Create a node which will contain package-oriented view of a source group.
     * <p>
     * The precise structure of this node is <em>not</em> specified by the API
     * and is subject to arbitrary change (perhaps at user option).
     * Callers should not make assumptions about the nature of subnodes, the
     * code or display names of certain nodes, and so on. You may use cookies/lookup
     * to find if particular subnodes correspond to folders or files.
     * </p>
     * @param group a source group which should be represented
     * @return node which will display packages in given group
     */
    public static Node createPackageView( SourceGroup group ) {
        return new RootNode (group);                
    }
    
    /**
     * Finds the node representing given object, if any.
     * The current implementation works only for {@link org.openide.filesystems.FileObject}s
     * and {@link org.openide.loaders.DataObject}s.
     * @param rootNode a node some descendant of which should contain the object
     * @param object object to find
     * @return a node representing the given object, or null if no such node was found
     */
    public static Node findPath(Node rootNode, Object object) {
        
        final PathFinder pf = rootNode.getLookup().lookup(PathFinder.class);
        if ( pf != null ) {
            return pf.findPath( rootNode, object );
        }
        return null;
    }
    
    /**
     * Create a list or combo box model suitable for {@link javax.swing.JList} from a source group
     * showing all Java packages in the source group.
     * To display it you will also need {@link #listRenderer}.
     * <p>No particular guarantees are made as to the nature of the model objects themselves,
     * except that {@link Object#toString} will give the fully-qualified package name
     * (or <code>""</code> for the default package), regardless of what the renderer
     * actually displays.</p>
     * @param group a Java-like source group
     * @return a model of its packages
     * @since org.netbeans.modules.java.project/1 1.3 
     */
    
    public static ComboBoxModel createListView(SourceGroup group) {
        Parameters.notNull("group", group); //NOI18N
        SortedSet<PackageItem> data = new TreeSet<PackageItem>();
        findNonExcludedPackages(null, data, group.getRootFolder(), group, false);
        return new DefaultComboBoxModel(data.toArray(new PackageItem[0]));
    }

    /**
     * Creates actions for package root.
     * @return the array of {@link Action}s
     */
    @NonNull
    static Action[] createRootNodeActions() {
        return new Action[] {
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get( FindAction.class ),
            null,
            SystemAction.get( PasteAction.class ),
            null,
            SystemAction.get( FileSystemAction.class ),
            null,
            SystemAction.get( ToolsAction.class ),
        };
    }
    
    /** Fills given collection with flattened packages under given folder
     *@param target The collection to be filled
     *@param fo The folder to be scanned
     * @param group the group to scan
     * @param createPackageItems if false the collection will be filled with file objects; if
     *       true PackageItems will be created.
     * @param showProgress whether to show a progress handle or not
     */
    @Messages({"# {0} - root folder", "PackageView.find_packages_progress=Finding packages in {0}"})
    static void findNonExcludedPackages(PackageViewChildren children, Collection<PackageItem> target, FileObject fo, SourceGroup group, boolean showProgress) {
        if (showProgress) {
            ProgressHandle progress = ProgressHandleFactory.createHandle(PackageView_find_packages_progress(FileUtil.getFileDisplayName(fo)));
            progress.start(1000);
            findNonExcludedPackages(children, target, fo, group, progress, 0, 1000);
            progress.finish();
        } else {
            findNonExcludedPackages(children, target, fo, group, null, 0, 0);
        }
    }

    private static void findNonExcludedPackages(PackageViewChildren children, Collection<PackageItem> target, FileObject fo, SourceGroup group, ProgressHandle progress, int start, int end) {
        if (!fo.isValid() || fo.isVirtual()) {
            return;
        }
        if (!fo.isFolder()) {
            throw new IllegalArgumentException("Package view only accepts folders, given: " + FileUtil.getFileDisplayName(fo)); // NOI18N
        }
        
        if (progress != null) {
            String path = FileUtil.getRelativePath(children.getRoot(), fo);
            if (path == null) {
                if (!fo.isValid() || !children.getRoot().isValid()) {
                    return;
                } else {
                    throw new IllegalArgumentException(
                        MessageFormat.format(
                            "{0} in {1}", //NOI18N
                            FileUtil.getFileDisplayName(fo),
                            FileUtil.getFileDisplayName(children.getRoot())));
                }
            }
            progress.progress(path.replace('/', '.'), start);
        }
        if ( !VisibilityQuery.getDefault().isVisible( fo ) ) {
            return; // Don't show hidden packages
        }
        
        boolean hasSubfolders = false;
        boolean hasFiles = false;
        List<FileObject> folders = new ArrayList<FileObject>();
        for (FileObject kid : fo.getChildren()) {
            // XXX could use PackageDisplayUtils.isSignificant here
            if (kid.isValid() && VisibilityQuery.getDefault().isVisible(kid) && group.contains(kid)) {
                if (kid.isFolder()) {
                    folders.add(kid);
                    hasSubfolders = true;
                } 
                else {
                    hasFiles = true;
                }
            }
        }
        if (hasFiles || !hasSubfolders) {
            if (target != null) {
                target.add( new PackageItem(group, fo, !hasFiles ) );
            }
            else {
                if (fo.isValid()) {
                    children.add(fo, !hasFiles, false);
                }
            }
        }
        if (!folders.isEmpty()) {
            int diff = (end - start) / folders.size();
            int c = 0;
            for (FileObject kid : folders) {
                // Do this after adding the parent, so we get a pre-order traversal.
                // Also see PackageViewChildren.findChild: prefer to get root first.
                findNonExcludedPackages(children, target, kid, group, progress, start + c * diff, start + (c + 1) * diff);
                c++;
            }
        }
    }
         
//    public static ComboBoxModel createListView(SourceGroup group) {
//        DefaultListModel model = new DefaultListModel();
//        SortedSet/*<PackageItem>*/ items = new TreeSet();
//        FileObject root = group.getRootFolder();
//        if (PackageDisplayUtils.isSignificant(root)) {
//            items.add(new PackageItem(group, root));
//        }
//        Enumeration/*<FileObject>*/ files = root.getChildren(true);
//        while (files.hasMoreElements()) {
//            FileObject f = (FileObject) files.nextElement();
//            if (f.isFolder() && PackageDisplayUtils.isSignificant(f)) {
//                items.add(new PackageItem(group, f));
//            }
//        }
//        return new DefaultComboBoxModel(items.toArray(new PackageItem[items.size()]));
//    }
    
    
    /**
     * Create a renderer suited to rendering models created using {@link #createListView}.
     * The exact nature of the display is not specified.
     * Instances of String can also be rendered.
     * @return a suitable package renderer
     * @since org.netbeans.modules.java.project/1 1.3 
     */
    public static ListCellRenderer listRenderer() {
        return new PackageListCellRenderer();
    }
    
    /**
     * FilterNode which listens on the PackageViewSettings and changes the view to 
     * the package view or tree view
     *
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("DE_MIGHT_IGNORE") // ClassNotFoundException
    private static final class RootNode extends FilterNode implements PropertyChangeListener {
        
        private SourceGroup sourceGroup;
        
        private RootNode (SourceGroup group) {
            super(getOriginalNode(group));
            this.sourceGroup = group;
            JavaProjectSettings.addPropertyChangeListener(WeakListeners.propertyChange(this, JavaProjectSettings.class));
            group.addPropertyChangeListener(WeakListeners.propertyChange(this, group));
        }

        // XXX #98573: very crude, but what else to do? Want to call changeOriginal asynchronously.
        // But this could randomly screw up tests - not just PackageViewTest, but maybe others too.
        // (org.netbeans.modules.java.freeform.ui.ViewTest does not appear to be affected.)
        private static boolean IN_UNIT_TEST = Boolean.getBoolean("PackageView.unitTest");   //NOI18N

        public void propertyChange (PropertyChangeEvent event) {
            String prop = event.getPropertyName();
            if (JavaProjectSettings.PROP_PACKAGE_VIEW_TYPE.equals(prop) || SourceGroup.PROP_CONTAINERSHIP.equals(prop)) {
                if (IN_UNIT_TEST) {
                    changeOriginal(getOriginalNode(sourceGroup), true);
                } else {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            changeOriginal(getOriginalNode(sourceGroup), true);
                        }
                    });
                }
            }
        }
        
        private static Node getOriginalNode(SourceGroup group) {
            final FileObject root = group.getRootFolder();
            //Guard condition, if the project is (closed) and deleted but not yet gced
            // and the view is switched, the source group is not valid.
            if ( root == null || !root.isValid()) {
                return new AbstractNode (Children.LEAF);
            }
            if (!VisibilityQuery.getDefault().isVisible(root)) {
                LOG.log(
                    Level.WARNING,
                    "Ignoring source group: {0} with non visible root: {1}",    //NOI18N
                    new Object[]{
                        group,
                        FileUtil.getFileDisplayName(root)
                });
                return new AbstractNode (Children.LEAF);
            }
            try {
                switch (JavaProjectSettings.getPackageViewType()) {
                    case PACKAGES:
                        return new PackageRootNode(group);
                    case TREE:
                        return new TreeRootNode(group, false);
                    case REDUCED_TREE:
                        return new TreeRootNode(group, true);
                    default:
                        assert false : "Unknown PackageView Type"; //NOI18N
                        return new PackageRootNode(group);
                }
            } catch (IllegalArgumentException iae) {
                if (iae.getCause() instanceof DataObjectNotFoundException) {
                    LOG.log(Level.WARNING, "The root: {0} does not exist.", FileUtil.getFileDisplayName(root)); //NOI18N
                    return new AbstractNode (Children.LEAF);
                } else {
                    throw iae;
                }
            }
        }
    }
    
    /**
     * Model item representing one package.
     */
    static final class PackageItem implements Comparable<PackageItem> {
        
        private static Map<Image,Icon> image2icon = new IdentityHashMap<Image,Icon>();
        
        private final boolean empty;
        private final FileObject pkg;
        private final String pkgname;
        private Icon icon;
        
        public PackageItem(SourceGroup group, FileObject pkg, boolean empty) {
            this.pkg = pkg;
            this.empty = empty;
            String path = FileUtil.getRelativePath(group.getRootFolder(), pkg);
            assert path != null : "No " + pkg + " in " + group;
            pkgname = path.replace('/', '.');
        }
        
        public String toString() {
            return pkgname;
        }
        
        public String getLabel() {
            return PackageDisplayUtils.getDisplayLabel(pkgname);
        }
        
        public Icon getIcon() {
            if ( icon == null ) {
                Image image = PackageDisplayUtils.getIcon(pkg, empty);
                icon = image2icon.get(image);
                if ( icon == null ) {            
                    icon = new ImageIcon( image );
                    image2icon.put( image, icon );
                }
            }
            return icon;
        }

        public int compareTo(PackageItem p) {
            return pkgname.compareTo(p.pkgname);
        }
        
    }
    
    /**
     * The renderer which just displays {@link PackageItem#getLabel} and {@link PackageItem#getIcon}.
     */
    private static final class PackageListCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public PackageListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if (value instanceof PackageItem) {
                PackageItem pkgitem = (PackageItem) value;
                setText(pkgitem.getLabel());
                setIcon(pkgitem.getIcon());
            } else {
                // #49954: render a specially inserted package somehow.
                String pkgitem = (String) value;
                setText(pkgitem);
                setIcon(null);
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
    }
    
    }
    
    
}
