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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.viewmodel.CheckNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="SourcesView/netbeans-JPDASession/Current",
                             types={ TreeModel.class,
                                     CheckNodeModelFilter.class,
                                     NodeActionsProvider.class })
public class SourcesCurrentModel implements TreeModel, CheckNodeModelFilter,
NodeActionsProvider {

    private static final Logger logger = Logger.getLogger(SourcesCurrentModel.class.getName());

    private Vector<ModelListener>   listeners = new Vector<ModelListener>();
    // set of filters
    //private Set<String>             enabledSourceRoots = new HashSet<String>();
    private Set<String>             disabledSourceRoots = new HashSet<String>();
    private List<String>            additionalSourceRoots = Collections.emptyList();
    private String[]                unorderedOriginalSourceRoots;
    private String[]                sortedOriginalSourceRoots;
    private int[]                   sourcePathPermutation;
    private Properties              sourcesProperties = Properties.
        getDefault ().getProperties ("debugger").getProperties ("sources");
    //private final Set<String>       sourceRootsSet = new HashSet<String>();
    private String                  projectRoot;
    private PropertyChangeListener  mainProjectListener;
    private DebuggerManagerListener debuggerListener;
    private SourcePathProviderImpl  currentSourcePathProvider;


    public SourcesCurrentModel () {
        bindWithSourcePathProvider();
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
        MOVE_UP_ACTION.putValue("DisabledWhenInSortedTable", Boolean.TRUE);     // NOI18N
        MOVE_DOWN_ACTION.putValue("DisabledWhenInSortedTable", Boolean.TRUE);   // NOI18N
        RESET_ORDER_ACTION.putValue("DisabledWhenInSortedTable", Boolean.TRUE); // NOI18N
        
    }

    private void bindWithSourcePathProvider() {
        debuggerListener = new DebuggerManagerAdapter() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                    Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
                    SourcePathProviderImpl spImpl = null;
                    if (s != null) {
                        List<? extends SourcePathProvider> sourcePathProviders = s.lookup(null, SourcePathProvider.class);
                        for (SourcePathProvider sp : sourcePathProviders) {
                            if (sp instanceof SourcePathProviderImpl) {
                                spImpl = (SourcePathProviderImpl) sp;
                                setSources(spImpl);
                                break;
                            }
                        }
                    }
                    synchronized (SourcesCurrentModel.this) {
                        currentSourcePathProvider = spImpl;
                    }
                    fireTreeChanged();
                }
            }
        };
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                WeakListeners.create(DebuggerManagerListener.class, debuggerListener,
                                             DebuggerManager.getDebuggerManager()));
    }

    private synchronized void setSources(SourcePathProviderImpl sp) {
        //Find the correct project root and take the disabled and additional sources for that project root.
        /*
        File projectBase = sp.baseDir;
        String rootPath = null;
        if (projectBase != null) {
            try {
                rootPath = projectBase.toURI().toURL().toExternalForm();
                System.err.println("\n\nrootPath = '"+rootPath+"'\n\n");
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
         */
        currentSourcePathProvider = sp;
        //Unset disabled source roots from sp.
        //Add additional source roots to sp.
    }


    // TreeModel ...............................................................

    /**
     *
     * @return threads contained in this group of threads
     */
    @Override
    public Object getRoot () {
        return ROOT;
    }

    /**
     *
     * @return threads contained in this group of threads
     */
    @Override
    public Object[] getChildren (Object parent, int from, int to)
    throws UnknownTypeException {
        if (parent == ROOT) {

            //Display source roots from currentSourcePathProvider, if set.
            synchronized (this) {
                if (currentSourcePathProvider != null) {
                    this.sortedOriginalSourceRoots = currentSourcePathProvider.getSourceRoots();
                    return this.sortedOriginalSourceRoots;
                }
            }

            // 1) get source roots
            if (mainProjectListener == null) {
                mainProjectListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        fireTreeChanged();
                    }
                };
                MainProjectManager.getDefault().addPropertyChangeListener(
                        WeakListeners.propertyChange(mainProjectListener, MainProjectManager.getDefault()));
            }
            Project p = MainProjectManager.getDefault().getMainProject();
            String[] sourceRoots;
            String root = null;
            if (p != null) {
                List<FileObject> projectSources = getProjectSources(p);
                //System.err.println("\nProject sources = "+projectSources);
                sourceRoots = new String[projectSources.size()];
                for (int i = 0; i < sourceRoots.length; i++) {
                    sourceRoots[i] = SourcePathProviderImpl.getRoot(projectSources.get(i));
                }
                root = p.getProjectDirectory().toURL().toExternalForm();
            } else {
                sourceRoots = new String[] {};
            }
            //String[] sourceRoots = sourcePath.getOriginalSourceRoots ();

            // 3) find additional and disabled source roots
            List<String> addSrcRoots = null;
            synchronized (this) {
                if (root != null) {
                    addSrcRoots = loadAdditionalSourceRoots(root);
                }
                if (addSrcRoots == null) {
                    addSrcRoots = new ArrayList<String>();
                } else {
                    addSrcRoots = new ArrayList<String>(addSrcRoots);
                }
                additionalSourceRoots = addSrcRoots;
                disabledSourceRoots = loadDisabledSourceRoots(root);
                if (disabledSourceRoots == null) {
                    disabledSourceRoots = new HashSet<String>();
                } else {
                    disabledSourceRoots = new HashSet<String>(disabledSourceRoots);
                }
                projectRoot = root;
            }

            // 3) join them
            String[] os = new String [sourceRoots.length + addSrcRoots.size()];
            System.arraycopy (sourceRoots, 0, os, 0, sourceRoots.length);
            System.arraycopy (addSrcRoots.toArray(), 0, os, sourceRoots.length, addSrcRoots.size());

            // 4) sort them
            Map<String, Integer> orderIndexes = SourcePathProviderImpl.getSourceRootsOrder(root);
            String[] sortedOriginalRoots = new String[os.length];
            int[] sourcePathPermutation = SourcePathProviderImpl.createPermutation(
                    os,
                    orderIndexes,
                    sortedOriginalRoots);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("getChildren(): orderIndexes = "+orderIndexes+", sourcePathPermutation = "+Arrays.toString(sourcePathPermutation));
                logger.fine("    sorted roots = "+Arrays.toString(sortedOriginalRoots));
                logger.fine("  sourcePathPermutation = "+Arrays.toString(sourcePathPermutation));
            }

            to = Math.min(sortedOriginalRoots.length, to);
            from = Math.min(sortedOriginalRoots.length, from);
            Object[] fos = new Object [to - from];
            System.arraycopy (sortedOriginalRoots, from, fos, 0, to - from);
            synchronized (this) {
                this.unorderedOriginalSourceRoots = os;
                this.sortedOriginalSourceRoots = sortedOriginalRoots;
                this.sourcePathPermutation = sourcePathPermutation;
            }
            return fos;
        } else {
            throw new UnknownTypeException (parent);
        }
    }

    private List<String> loadAdditionalSourceRoots(String projectRoot) {
        if (projectRoot == null) {
            return null;
        }
        return (List<String>) sourcesProperties.getProperties("additional_source_roots").
                getMap("project", Collections.emptyMap()).
                get(projectRoot);
    }

    private Set<String> loadDisabledSourceRoots(String projectRoot) {
        if (projectRoot == null) {
            return null;
        }
        return (Set<String>) sourcesProperties.getProperties("source_roots").
                getMap("project_disabled", Collections.emptyMap()).
                get(projectRoot);
    }

    private static List<FileObject> getProjectSources(Project p) {
        List<FileObject> allSourceRoots = new ArrayList<FileObject>();
        Set<FileObject> addedBinaryRoots = new HashSet<FileObject>();
        Set<FileObject> preferredRoots = new HashSet<FileObject>();
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] sgs = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sg : sgs) {
            ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT);
            if (ecp != null) {
                addSourceRoots(ecp, allSourceRoots, addedBinaryRoots, preferredRoots);
            }
        }
        for (SourceGroup sg : sgs) {
            ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE);
            if (ecp != null) {
                addSourceRoots(ecp, allSourceRoots, addedBinaryRoots, preferredRoots);
            }
        }
        for (SourceGroup sg : sgs) {
            ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
            if (ecp != null) {
                addSourceRoots(ecp, allSourceRoots, preferredRoots);
            }
        }
        
        return allSourceRoots;
    }

    private static void addSourceRoots(ClassPath ecp,
                                       List<FileObject> allSourceRoots,
                                       Set<FileObject> addedBinaryRoots,
                                       Set<FileObject> preferredRoots) {
        FileObject[] binaryRoots = ecp.getRoots();
        for (FileObject fo : binaryRoots) {
            if (addedBinaryRoots.contains(fo)) {
                continue;
            }
            addedBinaryRoots.add(fo);
            FileObject[] roots = SourceForBinaryQuery.findSourceRoots(fo.toURL()).getRoots();
            for (FileObject fr : roots) {
                if (!preferredRoots.contains(fr)) {
                    allSourceRoots.add(fr);
                    preferredRoots.add(fr);
                }
            }
        }
    }

    private static void addSourceRoots(ClassPath ecp,
                                       List<FileObject> allSourceRoots,
                                       Set<FileObject> preferredRoots) {
        FileObject[] sourceRoots = ecp.getRoots();
        for (FileObject fr : sourceRoots) {
            if (!preferredRoots.contains(fr) && !fr.isVirtual()) {
                allSourceRoots.add(fr);
                preferredRoots.add(fr);
            }
        }
    }

    /**
     * Returns number of children for given node.
     *
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    @Override
    public int getChildrenCount (Object node) throws UnknownTypeException {
        if (node == ROOT) {
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            //return sourcePath.getOriginalSourceRoots ().length +
            //    filters.size ();
        } else {
            throw new UnknownTypeException (node);
        }
    }

    @Override
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        }
        if (node instanceof String) {
            return true;
        }
        throw new UnknownTypeException (node);
    }

    @Override
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    @Override
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }

    public void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get (i)).modelChanged (null);
        }
    }

    private void fireSelectedNodes(Object[] nodes) {
        ModelEvent event = new ModelEvent.SelectionChanged(this, nodes);
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get (i)).modelChanged (event);
        }
    }


    // NodeActionsProvider .....................................................

    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node instanceof String) {
            if (additionalSourceRoots.contains((String) node)) {
                return new Action[] {
                    NEW_SOURCE_ROOT_ACTION,
                    DELETE_ACTION,
                    null,
                    MOVE_UP_ACTION,
                    MOVE_DOWN_ACTION,
                    null,
                    RESET_ORDER_ACTION,
                };
            } else {
                return new Action[] {
                    NEW_SOURCE_ROOT_ACTION,
                    null,
                    MOVE_UP_ACTION,
                    MOVE_DOWN_ACTION,
                    null,
                    RESET_ORDER_ACTION,
                };
            }
        } else {
            throw new UnknownTypeException (node);
        }
    }

    @Override
    public void performDefaultAction (Object node)
    throws UnknownTypeException {
        if (!(node instanceof String)) {
            throw new UnknownTypeException (node);
        }
    }

    // other methods ...........................................................

    private boolean isEnabled (String root) {
        synchronized(this) {
            return !disabledSourceRoots.contains(root);
        }
    }

    private void setEnabled (String root, boolean enabled) {
        synchronized (this) {
            if (enabled) {
                disabledSourceRoots.remove (root);
            } else {
                disabledSourceRoots.add (root);
            }
            saveDisabledSourceRoots ();
        }
    }

    /*
    private void loadFilters () {
        enabledSourceRoots = new HashSet (
            sourcesProperties.getProperties ("source_roots").getCollection (
                "enabled",
                Collections.EMPTY_SET
            )
        );
        disabledSourceRoots = new HashSet (
            sourcesProperties.getProperties ("source_roots").getCollection (
                "disabled",
                Collections.EMPTY_SET
            )
        );
        additionalSourceRoots = new ArrayList(
            sourcesProperties.getProperties("additional_source_roots").getCollection(
                "src_roots",
                Collections.EMPTY_LIST)
        );
    }
     */

    private synchronized void saveDisabledSourceRoots () {
        Map map = sourcesProperties.getProperties("source_roots").
                getMap("project_disabled", new HashMap());
        map.put(projectRoot, disabledSourceRoots);
        sourcesProperties.getProperties("source_roots").
                setMap("project_disabled", map);
    }

    private synchronized void saveAdditionalSourceRoots () {
        Map map = sourcesProperties.getProperties("additional_source_roots").
                getMap("project", new HashMap());
        map.put(projectRoot, additionalSourceRoots);
        sourcesProperties.getProperties("additional_source_roots").
                setMap("project", map);
    }

    // CheckNodeModelFilter
    
    @Override
    public boolean isCheckable(NodeModel original, Object node) throws UnknownTypeException {
        return true;
    }

    @Override
    public boolean isCheckEnabled(NodeModel original, Object node) throws UnknownTypeException {
        return true;
    }

    @Override
    public Boolean isSelected(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof String) {
            return isEnabled ((String) node);
        } else {
            throw new UnknownTypeException (node);
        }
    }

    @Override
    public void setSelected(NodeModel original, Object node, Boolean selected) throws UnknownTypeException {
        if (node instanceof String) {
            setEnabled ((String) node, selected.booleanValue ());
            return;
        }
        throw new UnknownTypeException (node);
    }

    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        return original.getDisplayName(node);
    }

    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        return original.getIconBase(node);
    }

    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        return original.getShortDescription(node);
    }

    public synchronized void reorderOriginalSourceRoots(int[] permutation) {
        String[] srcRoots = sortedOriginalSourceRoots;
        if (permutation == null) {
            // Restting the order to the original
            for (int i = 0; i < sourcePathPermutation.length; i++) {
                sourcePathPermutation[i] = i;
            }
            sortedOriginalSourceRoots = unorderedOriginalSourceRoots;
            srcRoots = unorderedOriginalSourceRoots;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("reorderOriginalSourceRoots("+Arrays.toString(permutation));
            }
            if (srcRoots.length != permutation.length) {
                throw new IllegalArgumentException("Bad length of permutation: "+permutation.length+", have "+srcRoots.length+" source roots.");
            }
            int n = permutation.length;
            String[] unorderedOriginalRoots = unorderedOriginalSourceRoots;
            String[] sortedOriginalRoots = new String[n];
            // Adding the permutation
            for (int i = 0; i < n; i++) {
                permutation[i] = sourcePathPermutation[permutation[i]];
                sortedOriginalRoots[i] = unorderedOriginalRoots[permutation[i]];
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("  sourcePathPermutation = "+Arrays.toString(sourcePathPermutation));
            }
            for (int i = 0; i < n; i++) {
                sourcePathPermutation[i] = permutation[i];
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("  => sourcePathPermutation = "+Arrays.toString(sourcePathPermutation));
                logger.fine("  => sorted roots = "+Arrays.toString(sortedOriginalRoots));
            }
            sortedOriginalSourceRoots = sortedOriginalRoots;
            srcRoots = unorderedOriginalRoots;
        }
        SourcePathProviderImpl.storeSourceRootsOrder(projectRoot, srcRoots, sourcePathPermutation);
    }

    private static String[] resize(String[] array, int by) {
        int n = array.length + by;
        String[] newArray = new String[n];
        n = Math.min(n, array.length);
        System.arraycopy(array, 0, newArray, 0, n);
        return newArray;
    }

    private static int[] resize(int[] array, int by) {
        int n = array.length + by;
        int[] newArray = new int[n];
        n = Math.min(n, array.length);
        System.arraycopy(array, 0, newArray, 0, n);
        return newArray;
    }

    // innerclasses ............................................................

    private JFileChooser newSourceFileChooser;

    static File getCurrentSourceRoot() {
        FileObject fo = EditorContextDispatcher.getDefault().getMostRecentFile();
        if (fo == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        fo = cp.findOwnerRoot(fo);
        if (fo == null) {
            return null;
        }
        return FileUtil.toFile(fo);
    }

    private final Action NEW_SOURCE_ROOT_ACTION = new AbstractAction(
            NbBundle.getMessage(SourcesCurrentModel.class, "CTL_SourcesModel_Action_AddSrc")) {
        @Override
        public void actionPerformed (ActionEvent e) {
            if (newSourceFileChooser == null) {
                newSourceFileChooser = new JFileChooser();
                newSourceFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                newSourceFileChooser.setFileFilter(new FileFilter() {

                    @Override
                    public String getDescription() {
                        return NbBundle.getMessage(SourcesCurrentModel.class, "CTL_SourcesModel_AddSrc_Chooser_Filter_Description");
                    }

                    @Override
                    public boolean accept(File file) {
                        if (file.isDirectory()) {
                            return true;
                        }
                        try {
                            return FileUtil.isArchiveFile(file.toURI().toURL());
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                            return false;
                        }
                    }

                });
            }
            File currentSourceRoot = getCurrentSourceRoot();
            if (currentSourceRoot != null) {
                newSourceFileChooser.setSelectedFile(currentSourceRoot);
            }
            int state = newSourceFileChooser.showDialog(org.openide.windows.WindowManager.getDefault().getMainWindow(),
                                      NbBundle.getMessage(SourcesCurrentModel.class, "CTL_SourcesModel_AddSrc_Btn"));
            if (state == JFileChooser.APPROVE_OPTION) {
                File zipOrDir = newSourceFileChooser.getSelectedFile();
                try {
                    if (!zipOrDir.isDirectory() && !FileUtil.isArchiveFile(zipOrDir.toURI().toURL())) {
                        return ;
                    }
                    String d = FileUtil.normalizePath(zipOrDir.getAbsolutePath());
                    synchronized (SourcesCurrentModel.this) {
                        additionalSourceRoots.add(d);

                        unorderedOriginalSourceRoots = resize(unorderedOriginalSourceRoots, +1);
                        sortedOriginalSourceRoots = resize(sortedOriginalSourceRoots, +1);
                        sourcePathPermutation = resize(sourcePathPermutation, +1);

                        unorderedOriginalSourceRoots[unorderedOriginalSourceRoots.length - 1] = d;
                        sortedOriginalSourceRoots[sortedOriginalSourceRoots.length - 1] = d;
                        sourcePathPermutation[sourcePathPermutation.length - 1] = sourcePathPermutation.length - 1;

                        SourcePathProviderImpl.storeSourceRootsOrder(projectRoot, unorderedOriginalSourceRoots, sourcePathPermutation);
                        //enabledSourceRoots.add(d);
                    }
                    // Set the new source roots:
                    /*
                    String[] sourceRoots = sourcePath.getSourceRoots();
                    int l = sourceRoots.length;
                    String[] newSourceRoots = new String[l + 1];
                    System.arraycopy(sourceRoots, 0, newSourceRoots, 0, l);
                    newSourceRoots[l] = d;
                    sourcePath.setSourceRoots(newSourceRoots);
                    */
                    saveAdditionalSourceRoots();
                    fireTreeChanged ();
                } catch (java.io.IOException ioex) {
                    ErrorManager.getDefault().notify(ioex);
                }
            }
        }
    };

    private final Action DELETE_ACTION = Models.createAction (
        NbBundle.getMessage
            (SourcesCurrentModel.class, "CTL_SourcesModel_Action_Delete"),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return true;
            }
            @Override
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                synchronized (SourcesCurrentModel.this) {
                    for (i = 0; i < k; i++) {
                        String node = (String) nodes [i];
                        additionalSourceRoots.remove(node);
                        //enabledSourceRoots.remove(node);
                        disabledSourceRoots.remove(node);

                        List<String> unorderedSR = new ArrayList<String>(Arrays.asList(unorderedOriginalSourceRoots));
                        int index = unorderedSR.indexOf(node);
                        if (index >= 0) {
                            unorderedSR.remove(index);
                            unorderedOriginalSourceRoots = unorderedSR.toArray(new String[] {});
                            int pi = sourcePathPermutation[index];
                            for (int j = 0; j < sourcePathPermutation.length; j++) {
                                if (sourcePathPermutation[j] > pi) {
                                    sourcePathPermutation[j]--;
                                }
                            }
                            for (int j = index; j < (sourcePathPermutation.length - 1); j++) {
                                sourcePathPermutation[j] = sourcePathPermutation[j+1];
                            }
                        }
                        List<String> sortedSR = new ArrayList<String>(Arrays.asList(sortedOriginalSourceRoots));
                        index = sortedSR.indexOf(node);
                        if (index >= 0) {
                            sortedSR.remove(index);
                            sortedOriginalSourceRoots = sortedSR.toArray(new String[] {});
                        }
                    }
                    
                    sourcePathPermutation = resize(sourcePathPermutation, -k);

                    saveAdditionalSourceRoots();
                    saveDisabledSourceRoots();
                    SourcePathProviderImpl.storeSourceRootsOrder(projectRoot, unorderedOriginalSourceRoots, sourcePathPermutation);
                    // Set the new source roots:
                    /*
                    String[] sourceRoots = sourcePath.getSourceRoots();
                    int l = sourceRoots.length;
                    String[] newSourceRoots = new String[l - 1];
                    int index = -1;
                    for (int ii = 0; ii < l; ii++) {
                        if (node.equals(sourceRoots[ii])) {
                            index = ii;
                            break;
                        }
                    }
                    if (index >= 0) {
                        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, index);
                        System.arraycopy(sourceRoots, index + 1, newSourceRoots, index, l - (index + 1));
                        sourcePath.setSourceRoots(newSourceRoots);
                    }
                     */
                }
                fireTreeChanged ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

    private final Action MOVE_UP_ACTION = Models.createAction (
        NbBundle.getMessage
            (SourcesCurrentModel.class, "CTL_SourcesModel_MoveUpSrc"),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                if (ROOT.equals(node)) {
                    return false;
                }
                synchronized (SourcesCurrentModel.this) {
                    return sortedOriginalSourceRoots.length > 0 && !sortedOriginalSourceRoots[0].equals(node);
                }
            }
            @Override
            public void perform (Object[] nodes) {
                int k = nodes.length;
                synchronized (SourcesCurrentModel.this) {
                    String[] roots = sortedOriginalSourceRoots;
                    int n = roots.length;
                    int[] permutation = new int[n];
                    for (int i = 0; i < n; i++) {
                        int j;
                        for (j = 0; j < k; j++) {
                            if (roots[i].equals(nodes[j])) {
                                break;
                            }
                        }
                        if (j < k) {
                            // Move up the node
                            if (i > 0) {
                                permutation[i] = permutation[i-1];
                                permutation[i-1] = i;
                            }
                        } else {
                            permutation[i] = i;
                        }
                    }
                    if (currentSourcePathProvider != null) {
                        currentSourcePathProvider.reorderOriginalSourceRoots(permutation);
                    } else {
                        reorderOriginalSourceRoots(permutation);
                    }
                }
                //saveFilters ();
                fireTreeChanged ();
                fireSelectedNodes(nodes);
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

    private final Action MOVE_DOWN_ACTION = Models.createAction (
        NbBundle.getMessage
            (SourcesCurrentModel.class, "CTL_SourcesModel_MoveDownSrc"),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                if (ROOT.equals(node)) {
                    return false;
                }
                synchronized (SourcesCurrentModel.this) {
                    return sortedOriginalSourceRoots.length > 0 &&
                           !sortedOriginalSourceRoots[sortedOriginalSourceRoots.length - 1].equals(node);
                }
            }
            @Override
            public void perform (Object[] nodes) {
                int k = nodes.length;
                synchronized (SourcesCurrentModel.this) {
                    String[] roots = sortedOriginalSourceRoots;
                    int n = roots.length;
                    int[] permutation = new int[n];
                    for (int i = n - 1; i >= 0; i--) {
                        int j;
                        for (j = 0; j < k; j++) {
                            if (roots[i].equals(nodes[j])) {
                                break;
                            }
                        }
                        if (j < k) {
                            // Move down the node
                            if (i < (n - 1)) {
                                permutation[i] = permutation[i+1];
                                permutation[i+1] = i;
                            }
                        } else {
                            permutation[i] = i;
                        }
                    }
                    if (currentSourcePathProvider != null) {
                        currentSourcePathProvider.reorderOriginalSourceRoots(permutation);
                    } else {
                        reorderOriginalSourceRoots(permutation);
                    }
                }
                //saveFilters ();
                fireTreeChanged ();
                fireSelectedNodes(nodes);
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

    private final Action RESET_ORDER_ACTION = Models.createAction (
        NbBundle.getMessage
            (SourcesCurrentModel.class, "CTL_SourcesModel_ResetOrderSrc"),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return true;
            }
            @Override
            public void perform (Object[] nodes) {
                if (currentSourcePathProvider != null) {
                    currentSourcePathProvider.reorderOriginalSourceRoots(null);
                } else {
                    reorderOriginalSourceRoots(null);
                }
                //saveFilters ();
                fireTreeChanged ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

}
