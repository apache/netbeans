/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.form.palette;

import java.awt.EventQueue;
import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;
import java.io.IOException;
import javax.swing.Action;

import org.netbeans.spi.palette.*;
import org.openide.ErrorManager;
import org.openide.nodes.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.form.FormServices;
import org.netbeans.modules.form.FormUtils;

import org.netbeans.modules.form.project.ClassSource;

/**
 * Class providing various useful methods for palette classes.
 *
 * @author Tomas Pavek, Jan Stola
 */

public final class PaletteUtils {
    
    private static FileObject paletteFolder;
    private static DataFolder paletteDataFolder;

    private static FileObject context;
    private static Map<Project,ProjectPaletteInfo> palettes = new WeakHashMap<Project,ProjectPaletteInfo>();

    private static class ProjectPaletteInfo {
        PaletteLookup paletteLookup;
        ClassPathFilter paletteFilter;
        List<PropertyChangeListener> paletteListeners;

        PaletteController getPalette() {
            return paletteLookup.lookup(PaletteController.class);
        }
    }

    private PaletteUtils() {
    }
    
    static String getItemComponentDescription(PaletteItem item) {
        ClassSource classSource = item.getComponentClassSource();
        
        if (classSource == null || !classSource.hasEntries()) {
            String className = classSource.getClassName();
            if (className != null) {
                if (className.startsWith("javax.") // NOI18N
                        || className.startsWith("java.")) // NOI18N
                    return getBundleString("MSG_StandardJDKComponent"); // NOI18N
                if (className.startsWith("org.netbeans.")) // NOI18N
                    return getBundleString("MSG_NetBeansComponent"); // NOI18N
            }
            return getBundleString("MSG_UnspecifiedComponent"); // NOI18N
        }
        else {
            return NbBundle.getMessage(PaletteUtils.class, "FMT_ComponentFrom", classSource.getEntries().iterator().next().getDisplayName());
        }
    }
    
    public static FileObject getPaletteFolder() {
        if (paletteFolder != null)
            return paletteFolder;
        
        try {
            paletteFolder = FileUtil.getConfigFile("FormDesignerPalette"); // NOI18N
            if (paletteFolder == null) // not found, create new folder
                paletteFolder = FileUtil.getConfigRoot().createFolder("FormDesignerPalette"); // NOI18N
        }
        catch (java.io.IOException ex) {
            throw new IllegalStateException("Palette folder not found and cannot be created."); // NOI18N
        }
        return paletteFolder;
    }
    
    public static Node getPaletteNode() {
        return getPaletteDataFolder().getNodeDelegate();
    }

    public static void showPaletteManager() {
        try {
            PaletteFactory.createPalette("FormDesignerPalette", // NOI18N
                                         createPaletteActions(),
                                         new ClassPathFilter(null), // filters out only invisible Layouts category
                                         null)
                    .showCustomizer();
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private static PaletteActions createPaletteActions() {
        FormServices services = Lookup.getDefault().lookup(FormServices.class);
        return services.createPaletteActions();
    }

    public static void setContext(FileObject fileInProject) {
        context = fileInProject;
    }

    public static synchronized void addPaletteListener(PropertyChangeListener listener,
                                                       FileObject context)
    {
        ProjectPaletteInfo pInfo = preparePalette(context);
        if (pInfo != null) {
            if (pInfo.paletteListeners == null) {
                pInfo.paletteListeners = new LinkedList<PropertyChangeListener>();
            }
            pInfo.paletteListeners.add(listener);
            pInfo.getPalette().addPropertyChangeListener(listener);
        }
    }

    public static synchronized void removePaletteListener(PropertyChangeListener listener,
                                                          FileObject context)
    {
        Project project = FileOwnerQuery.getOwner(context);
        ProjectPaletteInfo pInfo = palettes.get(project);
        if (pInfo != null && pInfo.paletteListeners != null) {
            pInfo.paletteListeners.remove(listener);
            pInfo.getPalette().removePropertyChangeListener(listener);
        }
    }

    public static Lookup getPaletteLookup(FileObject context) {
        ProjectPaletteInfo pInfo = preparePalette(context);
        return pInfo != null ? pInfo.paletteLookup : Lookup.EMPTY;
    }

    private static PaletteController getPalette() {
        ProjectPaletteInfo pInfo = preparePalette(context);
        return pInfo != null ? pInfo.getPalette() : null;
    }

    private static ClassPathFilter getPaletteFilter() {
        if (context != null) {
            Project project = FileOwnerQuery.getOwner(context);
            if (project != null) {
                ProjectPaletteInfo pInfo = palettes.get(project);
                if (pInfo != null)
                    return pInfo.paletteFilter;
            }
        }
        return null;
    }

    /**
     * Gets the registered palette and related data for given context (project
     * of given file). Creates new palette if does not exist yet.
     */
    private static ProjectPaletteInfo preparePalette(FileObject context) {
        if (context == null)
            return null;

        final Project project = FileOwnerQuery.getOwner(context);

        ProjectPaletteInfo pInfo = palettes.get(project);
        if (pInfo == null) {
            final ClassPathFilter filter;
            if (project != null) {
                ClassPath classPath = ClassPath.getClassPath(context, ClassPath.BOOT);
                classPath.addPropertyChangeListener(new ClassPathListener(classPath, project));
                filter = new ClassPathFilter(classPath);
            } else {
                filter = null;
            }

            PaletteLookup lookup = new PaletteLookup();
            lookup.setPalette(EventQueue.isDispatchThread() ? createDummyPalette() : createPalette(filter));
            pInfo = new ProjectPaletteInfo();
            pInfo.paletteLookup = lookup;
            pInfo.paletteFilter = filter;
            palettes.put(project, pInfo);
            if (EventQueue.isDispatchThread()) {
                // Init real palette
                FormUtils.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        PaletteController palette = createPalette(filter);

                        // 184551: Init all display names and icons
                        Lookup rootLookup = palette.getRoot();
                        Node root = rootLookup.lookup(Node.class);
                        for (Node category : root.getChildren().getNodes(true)) {
                            category.getDisplayName();
                            category.getIcon(BeanInfo.ICON_COLOR_16x16);
                            category.getIcon(BeanInfo.ICON_COLOR_32x32);
                            for (Node item : category.getChildren().getNodes(true)) {
                                item.getDisplayName();
                                item.getIcon(BeanInfo.ICON_COLOR_16x16);
                                item.getIcon(BeanInfo.ICON_COLOR_32x32);
                            }
                        }

                        // Replace the dummy palette
                        ProjectPaletteInfo pInfo = palettes.get(project);
                        if (pInfo != null) {
                            PaletteLookup lookup = pInfo.paletteLookup;
                            PaletteController oldPalette = pInfo.getPalette();
                            if (pInfo.paletteListeners != null) {
                                for (PropertyChangeListener l : pInfo.paletteListeners) {
                                    oldPalette.removePropertyChangeListener(l);
                                    palette.addPropertyChangeListener(l);
                                }
                            }
                            lookup.setPalette(palette);
                        }
                    }
                });
            }
        }
        return pInfo;
    }

    private static PaletteController createDummyPalette() {
        Node loadingNode = new AbstractNode(Children.LEAF);
        loadingNode.setDisplayName(getBundleString("MSG_DummyPaletteLoading")); // NOI18N
        Children.Array rootChildren = new Children.Array();
        rootChildren.add(new Node[] {loadingNode});
        Node root = new AbstractNode(rootChildren);
        return PaletteFactory.createPalette(root, new PaletteActions() {
            @Override
            public Action[] getImportActions() {
                return new Action[0];
            }
            @Override
            public Action[] getCustomPaletteActions() {
                return new Action[0];
            }
            @Override
            public Action[] getCustomCategoryActions(Lookup category) {
                return new Action[0];
            }
            @Override
            public Action[] getCustomItemActions(Lookup item) {
                return new Action[0];
            }
            @Override
            public Action getPreferredAction(Lookup item) {
                return null;
            }
        });
    }

    /**
     * Creates a new palette with filter for given ClassPath.
     */
    private static PaletteController createPalette(ClassPathFilter filter) {
        try {
            return PaletteFactory.createPalette("FormDesignerPalette", // NOI18N
                                                createPaletteActions(),
                                                filter,
                                                null);
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    /**
     * Called when the project's boot classpath changes (typically means that
     * the project's platform has changed). This method creates a new palette
     * with a filter based on the new classpath, updating the lookup providing
     * the palette. Palette listeners are transferred automatically.
     */
    private static synchronized void bootClassPathChanged(Project p, ClassPath cp) {
        ProjectPaletteInfo pInfo = palettes.get(p);
        if (pInfo != null) {
            PaletteLookup lookup = pInfo.paletteLookup;
            PaletteController oldPalette = pInfo.getPalette();
            oldPalette.clearSelection();
            ClassPathFilter newFilter = new ClassPathFilter(cp);
            PaletteController newPalette = createPalette(newFilter);
            if (pInfo.paletteListeners != null) {
                for (PropertyChangeListener l : pInfo.paletteListeners) {
                    oldPalette.removePropertyChangeListener(l);
                    newPalette.addPropertyChangeListener(l);
                }
            }
            lookup.setPalette(newPalette);
            pInfo.paletteFilter = newFilter;
        }
    }

    static DataFolder getPaletteDataFolder() {
        if (paletteDataFolder == null)
            paletteDataFolder = DataFolder.findFolder(getPaletteFolder());
        return paletteDataFolder;
    }

    public static void clearPaletteSelection() {
        PaletteController palette = getPalette();
        if (palette != null) {
            palette.clearSelection();
        }
    }
    
    public static PaletteItem getSelectedItem() {
        PaletteController palette = getPalette();
        if (palette == null) {
            return null;
        }
        Lookup lkp = palette.getSelectedItem();
        
        return lkp.lookup(PaletteItem.class);
    }
    
    public static void selectItem( PaletteItem item ) {
        if( null == item ) {
            clearPaletteSelection();
        } else {
            // This is not the node returned by getPaletteNode()!
            Node paletteNode = getPalette().getRoot().lookup(Node.class);
            Node[] categories = getCategoryNodes(paletteNode, true, true, true, true);
            for( int i=0; i<categories.length; i++ ) {
                Node[] items = getItemNodes( categories[i], true );
                for( int j=0; j<items.length; j++ ) {
                    PaletteItem formItem = items[j].getLookup().lookup( PaletteItem.class );
                    if( item.equals( formItem ) ) {
                        getPalette().setSelectedItem( categories[i].getLookup(), items[j].getLookup() );
                    }
                }
            }
        }
    }
    
    public static Image getIconForClass(String className, String classDetails, int type, boolean optimalResult) {
        Image img = null;
        for (PaletteItem item : getAllItems(optimalResult)) {
            if (PaletteItem.TYPE_CHOOSE_BEAN.equals(item.getExplicitComponentType())) {
                continue;
            }
            if (className.equals(item.getComponentClassName())) {
                Node node = item.getNode();
                if (node != null) {
                    img = node.getIcon(type);
                } else {
                    img = item.getIcon(type);
                }
                if ((classDetails == null) || (classDetails.equals(item.getComponentInitializerId()))) {
                    break;
                }
            }
        }
        return img;
    }
    
    public static PaletteItem[] getAllItems() {
        return getAllItems(true);
    }
    
    public static PaletteItem[] getAllItems(boolean optimalResult) {
        HashSet<PaletteItem> uniqueItems = null;
        // collect valid items from all categories (including invisible)
        Node[] categories = getCategoryNodes(getPaletteNode(), false, true, false, optimalResult);
        for( int i=0; i<categories.length; i++ ) {
            Node[] items = getItemNodes(categories[i], true, optimalResult);
            for( int j=0; j<items.length; j++ ) {
                PaletteItem formItem = items[j].getLookup().lookup( PaletteItem.class );
                if( null != formItem ) {
                    if( null == uniqueItems ) {
                        uniqueItems = new HashSet<PaletteItem>();
                    }
                    if (!PaletteItem.TYPE_CHOOSE_BEAN.equals(formItem.getExplicitComponentType())) {
                        uniqueItems.add(formItem);
                    }
                }
            }
        }
        PaletteItem[] res;
        if( null != uniqueItems ) {
            res = uniqueItems.toArray( new PaletteItem[uniqueItems.size()] );
        } else {
            res = new PaletteItem[0];
        }
        return res;
    }
    
    public static String getBundleString(String key) {
        return NbBundle.getBundle(PaletteUtils.class).getString(key);
    }
    
    public static Node[] getItemNodes(Node categoryNode, boolean mustBeValid) {
        return getItemNodes(categoryNode, mustBeValid, true);
    }
    
    /**
     * Get an array of Node for the given category.
     *
     * @param categoryNode Category node.
     * @param mustBeValid True if all the nodes returned must be valid palette items.
     * @return An array of Nodes for the given category.
     */
    private static Node[] getItemNodes(Node categoryNode, boolean mustBeValid, boolean optimalResult) {
        Node[] nodes = categoryNode.getChildren().getNodes(optimalResult);
        if (!mustBeValid)
            return nodes;

        ClassPathFilter filter = getPaletteFilter();
        if (filter == null)
            return nodes;

        List<Node> validList = null;
        for (int i=0; i < nodes.length; i++) {
            PaletteItem item = nodes[i].getCookie(PaletteItem.class);
            if (filter.isValidItem(item)) {
                if (validList != null)
                    validList.add(nodes[i]);
            }
            else if (validList == null) {
                validList = new ArrayList<Node>(nodes.length);
                for (int j=0; j < i; j++) {
                    validList.add(nodes[j]);
                }
            }
        }
        if (validList != null)
            nodes = validList.toArray(new Node[validList.size()]);

        return nodes;
    }
    
    /**
     * Get an array of all categories in the given palette.
     *
     * @param paletteNode Palette's root node.
     * @param mustBeVisible True to return only visible categories, false to return also
     * categories with Hidden flag.
     * @return An array of categories in the given palette.
     */
    public static Node[] getCategoryNodes(Node paletteNode, boolean mustBeVisible) {
        return getCategoryNodes(paletteNode, mustBeVisible, mustBeVisible, true, true);
    }
    
    /**
     * Get an array of all categories in the given palette.
     *
     * @param paletteNode Palette's root node.
     * @param mustBeVisible True to return only visible categories, false to return also
     *        categories with Hidden flag (user can setup what's visibile in palette manager).
     * @param mustBeValid True to return only categories containing some
     *        classpath-valid items, false to don't care about platform classpath.
     * @param mustBePaletteCategory True to return only categories not tagged as
     *        'isNoPaletteCategory' (marks a never visible category like Layouts)
     * @return An array of category nodes in the given palette.
     */
    private static Node[] getCategoryNodes(Node paletteNode,
                                           boolean mustBeVisible,
                                           boolean mustBeValid,
                                           boolean mustBePaletteCategory,
                                           boolean optimalResult)
    {
        if (mustBeVisible)
            mustBeValid = mustBePaletteCategory = true;

        Node[] nodes = paletteNode.getChildren().getNodes(optimalResult);

        ClassPathFilter filter = mustBeValid ? getPaletteFilter() : null;
        java.util.List<Node> list = null; // don't create until needed
        for( int i=0; i<nodes.length; i++ ) {
            if ((!mustBeVisible || isVisibleCategoryNode(nodes[i]))
                && (!mustBeValid || filter == null || filter.isValidCategory(nodes[i]))
                && (!mustBePaletteCategory || representsShowableCategory(nodes[i])))
            {   // this is a relevant category
                if( list != null ) {
                    list.add(nodes[i]);
                }
            } else if( list == null ) {
                list = new ArrayList<Node>(nodes.length);
                for( int j=0; j < i; j++ ) {
                    list.add(nodes[j]);
                }
            }
        }
        if( list != null ) {
            nodes = new Node[list.size()];
            list.toArray(nodes);
        }
        return nodes;
    }
    
    /**
     * @return True if the given node is a DataFolder and does not have Hidden flag set.
     */
    private static boolean isVisibleCategoryNode(Node node) {
        DataFolder df = node.getCookie(DataFolder.class);
        if (df != null) {
            Object value = node.getValue("psa_" + PaletteController.ATTR_IS_VISIBLE); // NOI18N
            if (null == value || "null".equals(value)) { // NOI18N
                value = df.getPrimaryFile().getAttribute(PaletteController.ATTR_IS_VISIBLE);
            }
            if (value == null) {
                value = Boolean.TRUE;
            }
            return Boolean.valueOf(value.toString()).booleanValue();
        }
        return false;
    }

    private static boolean representsShowableCategory(Node node) {
        DataFolder df = node.getCookie(DataFolder.class);
        return (df != null) && !Boolean.TRUE.equals(df.getPrimaryFile().getAttribute("isNoPaletteCategory")); // NOI18N
    }
    
    // -----

    /**
     * Filter for PaletteController. Filters items from platform (i.e. not user
     * beans) based on given classpath. If classpath is null, all items pass.
     * Also filters out categories containing only unavailable items.
     * Always filters out permanently invisible categories (e.g. Layouts).
     */
    private static class ClassPathFilter extends PaletteFilter {
        private ClassPath classPath;
        private Set<PaletteItem> validItems;
        private Set<PaletteItem> invalidItems;

        ClassPathFilter(ClassPath cp) {
            if (cp != null) {
                validItems = new WeakSet<PaletteItem>();
                invalidItems = new WeakSet<PaletteItem>();
            }
            classPath = cp;
        }

        @Override
        public boolean isValidCategory(Lookup lkp) {
            Node categoryNode = lkp.lookup(Node.class);
            if (!representsShowableCategory(categoryNode))
                return false; // filter out categories that should never be visible (e.g. Layouts)

            return isValidCategory(categoryNode);
        }

        boolean isValidCategory(Node node) {
            if (classPath == null)
                return true;

            // check if there is some valid item in this category
            // [ideally we should listen on the category for adding/removing items,
            //  practically we just need to hide Swing categories on some mobile platforms]
            DataFolder folder = node.getCookie(DataFolder.class);
            if (folder == null)
                return false;

            DataObject[] dobjs = folder.getChildren();
            for (int i=0; i < dobjs.length; i++) {
                PaletteItem item = dobjs[i].getCookie(PaletteItem.class);
                if (item == null || isValidItem(item))
                    return true;
            }
            return dobjs.length == 0;
        }

        @Override
        public boolean isValidItem(Lookup lkp) {
            return isValidItem(lkp.lookup(PaletteItem.class));
        }

        boolean isValidItem(PaletteItem item) {
            if (classPath == null)
                return true;
            
            if (item == null) // Issue 81506
                return false;

            if (item.getComponentClassSource().hasEntries()
                || PaletteItem.TYPE_CHOOSE_BEAN.equals(item.getExplicitComponentType())
                || "org.netbeans.modules.form.layoutsupport.delegates.NullLayoutSupport".equals(item.getComponentClassName())) // NOI18N
                return true; // this is not a platform component

           if (validItems.contains(item)) {
                return true;
            }
            else if (invalidItems.contains(item)) {
                return false;
            }

            // check if the class is available on platform classpath
            String resName = item.getComponentClassName().replace('.', '/').concat(".class"); // NOI18N
            if (classPath.findResource(resName) != null) {
                validItems.add(item);
                return true;
            }
            else {
                invalidItems.add(item);
                return false;
            }
        }
    }

    /**
     * Reacts on classpath changes and updates the palette for given project
     * accordingly (in lookup).
     */
    private static class ClassPathListener implements PropertyChangeListener {
        private ClassPath classPath;
        private WeakReference<Project> projRef;

        ClassPathListener(ClassPath cp, Project p) {
            classPath = cp;
            projRef = new WeakReference<Project>(p);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                final Project p = projRef.get();
                if (p != null) {
                    EventQueue.invokeLater(new Runnable() { // Issue 141326
                        @Override
                        public void run() {
                            PaletteUtils.bootClassPathChanged(p, classPath);
                        }
                    });
                } else {
                    classPath.removePropertyChangeListener(this);
                }
            }
        }
    }

    /**
     * Lookup providing a PaletteController. Can be updated with a new instance.
     */
    private static class PaletteLookup extends AbstractLookup {
        private InstanceContent content;

        PaletteLookup() {
            this(new InstanceContent());
        }

        private PaletteLookup(InstanceContent content) {
            super(content);
            this.content = content;
        }

        void setPalette(PaletteController palette) {
            content.set(Arrays.asList(new PaletteController[] { palette }), null);
        }
    }
}
