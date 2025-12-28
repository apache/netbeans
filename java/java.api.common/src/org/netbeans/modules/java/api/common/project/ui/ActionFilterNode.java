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
package org.netbeans.modules.java.api.common.project.ui;


import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.libraries.Library;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.impl.ClassPathPackageAccessor;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.project.support.ui.EditJarSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.actions.EditAction;
import org.openide.actions.FindAction;
import org.openide.actions.OpenAction;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * This class decorates package nodes and file nodes under the Libraries Nodes.
 * It removes all actions from these nodes except of file node's {@link OpenAction}
 * and package node's {@link FindAction} It also adds the {@link ShowJavadocAction}
 * to both file and package nodes. It also adds {@link RemoveClassPathRootAction} to
 * class path roots.
 */
final class ActionFilterNode extends FilterNode implements NodeListener {

    private static enum Mode {
        ROOT {
            @Override
            public boolean isFolder() {
                return true;
            }
            @Override
            public boolean isRoot() {
                return true;
            }
        },
        EDITABLE_ROOT {
            @Override
            public boolean isFolder() {
                return true;
            }
            @Override
            public boolean isRoot() {
                return true;
            }
        },
        PACKAGE {
            @Override
            public boolean isFolder() {
                return true;
            }
            @Override
            public boolean isRoot() {
                return false;
            }
        },
        FILE {
            @Override
            public boolean isFolder() {
                return false;
            }
            @Override
            public boolean isRoot() {
                return false;
            }
        },
        FILE_CONTENT {
            @Override
            public boolean isFolder() {
                return false;
            }
            @Override
            public boolean isRoot() {
                return false;
            }
        };

        public abstract boolean isFolder();

        public abstract boolean isRoot();
    }

    private static final RequestProcessor RP = new RequestProcessor(ActionFilterNode.class);

    private final Mode mode;
    private final Children children;
    private Action[] actionCache;

    /**
     * Creates new ActionFilterNode for class path root
     * @param original the original node
     * @param helper used for implementing {@link RemoveClassPathRootAction.Removable}
     * @param classPathId ant property name of classpath to which these classpath root belongs 
     * @param entryId ant property name of this classpath root
     * @return ActionFilterNode
     */
    @CheckForNull
    static FilterNode forRoot (
            final @NonNull Node original,
            final @NonNull UpdateHelper helper,
            final @NonNull String classPathId,
            final @NonNull String entryId,
            final @NullAllowed String webModuleElementName,     //xxx: remove
            final @NonNull ClassPathSupport cs,
            final @NonNull ReferenceHelper rh,
            @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
            @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
            boolean removeFromProject) {
        Parameters.notNull("original", original);   //NOI18N
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("classPathId", classPathId); //NOI18N
        Parameters.notNull("entryId", entryId);     //NOI18N
        Parameters.notNull("cs", cs);       //NOI18N
        Parameters.notNull("rh", rh);       //NOI18N

        final FileObject root =  getFolder(original);
        return root == null ?
            null :
            new ActionFilterNode (original, Mode.ROOT, root, createLookup(original,
                new Removable (helper, classPathId, entryId, webModuleElementName, cs, rh, preRemoveAction, postRemoveAction, removeFromProject),
                new JavadocProvider(root,root)));
    }

    @CheckForNull
    static FilterNode forLibrary(
            final @NonNull Node original,
            final @NonNull UpdateHelper helper,
            final @NonNull String classPathId,
            final @NonNull String entryId,
            final @NullAllowed String webModuleElementName,     //xxx: remove
            final @NonNull ClassPathSupport cs,
            final @NonNull ReferenceHelper rh,
            @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
            @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
            final boolean removeShared) {
        Parameters.notNull("original", original);   //NOI18N
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("classPathId", classPathId); //NOI18N
        Parameters.notNull("entryId", entryId);     //NOI18N
        Parameters.notNull("cs", cs);       //NOI18N
        Parameters.notNull("rh", rh);       //NOI18N

        final FileObject root =  getFolder(original);
        return root == null ?
            null :
            new ActionFilterNode (original, Mode.EDITABLE_ROOT, root, createLookup(original,
                new Removable (helper, classPathId, entryId, webModuleElementName, cs, rh, preRemoveAction, postRemoveAction, removeShared),
                new LibraryEditable(entryId, rh),
                new JavadocProvider(root,root)));
    }

    @CheckForNull
    static FilterNode forArchive(
            final @NonNull Node original,
            final @NonNull UpdateHelper helper,
            final @NonNull PropertyEvaluator eval,
            final @NonNull String classPathId,
            final @NonNull String entryId,
            final @NullAllowed String webModuleElementName,     //xxx: remove
            final @NonNull ClassPathSupport cs,
            final @NonNull ReferenceHelper rh,
            @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
            @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
            boolean removeFromProject) {
        Parameters.notNull("original", original);   //NOI18N
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("eval", eval);           //NOI18N
        Parameters.notNull("classPathId", classPathId); //NOI18N
        Parameters.notNull("entryId", entryId);     //NOI18N
        Parameters.notNull("cs", cs);       //NOI18N
        Parameters.notNull("rh", rh);       //NOI18N

        final FileObject root =  getFolder(original);
        return root == null ?
            null :
            new ActionFilterNode (original, Mode.EDITABLE_ROOT, root, createLookup(original,
                new Removable (helper, classPathId, entryId, webModuleElementName, cs, rh, preRemoveAction, postRemoveAction, removeFromProject),
                new ArchiveEditable(entryId, helper, eval, rh),
                new JavadocProvider(root,root)));
    }

    @CheckForNull
    static FilterNode forPackage(final @NonNull Node original) {
        Parameters.notNull("original", original);   //NOI18N
        final FileObject root = getFolder(original);
        return root == null ?
            null :
            new ActionFilterNode (original, Mode.PACKAGE, root, createLookup(original,
                new JavadocProvider(root,root)));
    }

    @CheckForNull
    private static FileObject getFolder(final Node original) {
        final DataObject dobj = original.getLookup().lookup(DataObject.class);        
        return dobj == null ? null : dobj.getPrimaryFile();
    }

    private static Lookup createLookup(final Node original, Object... toAdd) {
        final Lookup lookup = original.getLookup();
        final org.netbeans.spi.project.ui.PathFinder pathFinder =
                lookup.lookup(org.netbeans.spi.project.ui.PathFinder.class);
        final Lookup lkp = new ProxyLookup(
                Lookups.exclude(lookup, org.netbeans.spi.project.ui.PathFinder.class),
                Lookups.fixed (toAdd),
                Lookups.singleton(new PathFinder(pathFinder)));
        return lkp;
    }



    private ActionFilterNode (Node original, Mode mode, FileObject cpRoot, FileObject resource) {
        this (original, mode, cpRoot,
            new ProxyLookup(new Lookup[] {original.getLookup(),Lookups.singleton(new JavadocProvider(cpRoot,resource))}));
    }

    private ActionFilterNode (Node original, Mode mode) {
        super (original, Children.LEAF);
        this.mode = mode;
        this.children = new ActionFilterChildren (original, mode, null);
        initChildren();
    }

    private ActionFilterNode (Node original, Mode mode, FileObject root, Lookup lkp) {
        super (original, Children.LEAF, lkp);
        this.mode = mode;
        this.children = new ActionFilterChildren (original, mode,root);
        initChildren();
    }

    private void initChildren() {
        Node node = getOriginal();
        node.addNodeListener(WeakListeners.create(NodeListener.class, this, node));
        final boolean leaf = node.isLeaf();
        if (!leaf) {
            setChildren(children);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] result = initActions();        
        return result;
    }

    @Override
    public String getShortDescription() {
        final DataObject dobj = getLookup().lookup(DataObject.class);
        FileObject pf;
        if (dobj != null && (pf = dobj.getPrimaryFile()) != null) {
            return FileUtil.getFileDisplayName(pf);

        } else {
            return super.getShortDescription();
        }
    }

    @Override
    public Action getPreferredAction() {
        if (mode == Mode.FILE) {
            Action[] actions = initActions();
            if (actions.length > 0 && isOpenAction(actions[0])) {
                return actions[0];
            }
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Node.PROP_LEAF.equals(evt.getPropertyName()) && children != getChildren()) {
            setChildren(children);
        }
    }

    @Override
    public void childrenAdded(NodeMemberEvent ev) {
    }

    @Override
    public void childrenRemoved(NodeMemberEvent ev) {
    }

    @Override
    public void childrenReordered(NodeReorderEvent ev) {
    }

    @Override
    public void nodeDestroyed(NodeEvent ev) {
    }

    private Action[] initActions () {
        if (actionCache == null) {
            List<Action> result = new ArrayList<Action>(2);
            if (mode == Mode.FILE) {
                for (Action superAction : super.getActions(false)) {
                    if (isOpenAction(superAction)) {
                        result.add(superAction);
                    }
                }
                result.add (SystemAction.get(ShowJavadocAction.class));
            }
            else if (mode.isFolder()) {
                result.add (SystemAction.get(ShowJavadocAction.class));
                Action[] superActions = super.getActions(false);
                for (int i=0; i<superActions.length; i++) {
                    if (superActions[i] instanceof FindAction) {
                        result.add (superActions[i]);
                    }
                }                
                if (mode.isRoot()) {
                    result.add (SystemAction.get(RemoveClassPathRootAction.class));
                }
                if (mode == Mode.EDITABLE_ROOT) {
                    result.add (SystemAction.get(EditRootAction.class));
                }
            }            
            actionCache = result.toArray(new Action[0]);
        }
        return actionCache;
    }

    private static boolean isOpenAction(final Action action) {
        if (action == null) {
            return false;
        }
        if (action instanceof OpenAction || action instanceof EditAction) {
            return true;
        }
        if ("org.netbeans.api.actions.Openable".equals(action.getValue("type"))) { //NOI18N
            return true;
        }
        return false;
    }

    private static class ActionFilterChildren extends FilterNode.Children {

        private final Mode mode;
        private final FileObject cpRoot;

        ActionFilterChildren (@NonNull Node original, @NonNull Mode mode, @NonNull FileObject cpRooot) {
            super (original);
            this.mode = mode;
            this.cpRoot = cpRooot;
        }

        @Override
        protected Node[] createNodes(Node n) {
            if (mode.isFolder()) {
                final FileObject fobj = n.getLookup().lookup(FileObject.class);
                if (fobj == null) {
                    if (n.isLeaf() && n.getActions(false).length == 0) {
                        //"Please Wait..." node
                        return super.createNodes(n);
                    } else {
                        assert false : String.format(
                            "DataNode without FileObject in Lookup %s : %s",   //NOI18N
                            n,
                            n.getClass());
                        return new Node[0];
                    }
                }
                else if (fobj.isFolder()) {
                    return new Node[] {new ActionFilterNode (n, Mode.PACKAGE, cpRoot, fobj)};
                }
                else {
                    return new Node[] {new ActionFilterNode (n, Mode.FILE, cpRoot, fobj)};
                }
            } else {
                return new Node[] {new ActionFilterNode (n, Mode.FILE_CONTENT)};
            }
        }
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private static final AtomicBoolean initialized = new AtomicBoolean();

        private final FileObject cpRoot;
        private final FileObject resource;

        JavadocProvider (final @NonNull FileObject cpRoot, final @NullAllowed FileObject resource) {
            this.cpRoot = cpRoot;
            this.resource = resource;
            if (!initialized.getAndSet(true)) {
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        JavadocForBinaryQuery.findJavadoc(cpRoot.toURL());
                    }
                });
            }
        }

        @Override
        public boolean hasJavadoc() {
            return resource != null && JavadocForBinaryQuery.findJavadoc(cpRoot.toURL()).getRoots().length>0;
        }

        @Override
        public void showJavadoc() {
                String relativeName = FileUtil.getRelativePath(cpRoot,resource);
                URL[] urls = JavadocForBinaryQuery.findJavadoc(cpRoot.toURL()).getRoots();
                URL pageURL;
                if (relativeName.length()==0) {
                    pageURL = ShowJavadocAction.findJavadoc ("overview-summary.html",urls); //NOI18N
                    if (pageURL == null) {
                        pageURL = ShowJavadocAction.findJavadoc ("index.html",urls); //NOI18N
                    }                    
                }
                else if (resource.isFolder()) {
                    //XXX Are the names the same also in the localized javadoc?                    
                    pageURL = ShowJavadocAction.findJavadoc (relativeName+"/package-summary.html",urls); //NOI18N
                }
                else {
                    String javadocFileName = relativeName.substring(0,relativeName.lastIndexOf('.'))+".html"; //NOI18Ns
                    pageURL = ShowJavadocAction.findJavadoc (javadocFileName,urls);
                }
                ShowJavadocAction.showJavaDoc(pageURL,relativeName.replace('/','.'));  //NOI18N
        }
    }

    static class Removable implements RemoveClassPathRootAction.Removable {

       private final UpdateHelper helper;
       private final String classPathId;
       private final String entryId;
       private final String webModuleElementName;
       private final ClassPathSupport cs;
       private final ReferenceHelper rh;
       private final Consumer<Pair<String,String>> preRemoveAction;
       private final Consumer<Pair<String,String>> postRemoveAction;
       private final ThreadLocal<String> lastRef = new ThreadLocal<>();
       private final boolean removeFromProject;
       
       Removable (
               @NonNull final UpdateHelper helper,
               @NonNull final String classPathId,
               @NonNull final String entryId,
               @NullAllowed final String webModuleElementName,
               @NonNull final ClassPathSupport cs,
               @NonNull final ReferenceHelper rh,
               @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
               @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
               boolean removeFromProject) {
           this.helper = helper;
           this.classPathId = classPathId;
           this.entryId = entryId;
           this.webModuleElementName = webModuleElementName;
           this.cs = cs;
           this.rh = rh;
           this.preRemoveAction = preRemoveAction;
           this.postRemoveAction = postRemoveAction;
           this.removeFromProject = removeFromProject;
       }

        @Override
       public boolean canRemove () {
            if (!removeFromProject) {
                return false;
            }
            //Allow to remove only entries from PROJECT_PROPERTIES, same behaviour as the project customizer
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return props.getProperty (classPathId) != null;
        }

        @Override
       public Project remove() {
           if (!removeFromProject) {
               return null;
           }
           // The caller has write access to ProjectManager
           // and ensures the project will be saved.
            boolean found = false;
            final List<ClassPathSupport.Item> resources = getClassPathItems();
            for (Iterator<ClassPathSupport.Item> i = resources.iterator(); i.hasNext();) {
                ClassPathSupport.Item item = i.next();
                if (entryId.equals(CommonProjectUtils.getAntPropertyName(item.getReference()))) {
                    lastRef.set(item.getReference());
                    i.remove();
                    ClassPathPackageAccessor.getInstance().removeUnusedReference(item, classPathId, helper, rh);
                    found = true;
                }
            }
            if (found) {
                String[] itemRefs = cs.encodeToStrings(resources, webModuleElementName);
                final EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                props.setProperty (classPathId, itemRefs);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
               return FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
           } else {
               return null;
           }
       }

        @Override
        public void beforeRemove() {
            if (preRemoveAction != null) {
                getClassPathItems().stream()
                        .map((i) -> i.getReference())
                        .filter((r) -> entryId.equals(CommonProjectUtils.getAntPropertyName(r)))
                        .findAny()
                        .ifPresent((r) -> preRemoveAction.accept(Pair.of(classPathId, r)));
            }
        }

        @Override
        public void afterRemove() {
            try {
                if (postRemoveAction != null) {
                    final String ref = lastRef.get();
                    if (ref != null) {
                        postRemoveAction.accept(Pair.of(classPathId,ref));
                    }
                }
            } finally {
                lastRef.remove();
            }
        }
        
        @NonNull
        private List<ClassPathSupport.Item> getClassPathItems() {
            final EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
            final String raw = props.getProperty (classPathId);
            return cs.itemsList( raw, webModuleElementName );
        }
        
    }

    private static class LibraryEditable implements EditRootAction.Editable {

        private final ReferenceHelper refHelper;
        private final String entryId;

        private LibraryEditable(
               @NonNull final String entryId,
               @NonNull final ReferenceHelper refHelper) {
           Parameters.notNull("entryId", entryId);  //NOI18N
           Parameters.notNull("refHelper", refHelper);  //NOI18N
           if (!entryId.startsWith("libs.") || entryId.lastIndexOf('.')<=4) {   //NOI18N
               throw new IllegalArgumentException(entryId);
           }
           this.entryId = entryId;
           this.refHelper = refHelper;
        }

        @Override
        public boolean canEdit() {
            return getLibrary() != null;
        }

        @Override
        public void edit() {
            final Library lib = getLibrary();
            assert lib != null;
            LibrariesCustomizer.showSingleLibraryCustomizer(lib);
        }

        private Library getLibrary() {
            //Todo: Caching if needed
            final String libName = entryId.substring(5, entryId.lastIndexOf('.'));
            return refHelper.findLibrary(libName);
        }
    }

    private static class ArchiveEditable implements EditRootAction.Editable {

        private static final String FILE_REF = "file.reference.";   //NOI18N
        private static final String SRC_REF = "source.reference.";   //NOI18N
        private static final String JDOC_REF = "javadoc.reference.";  //NOI18N

        private final UpdateHelper updateHelper;
        private final PropertyEvaluator eval;
        private final ReferenceHelper refHelper;
        private final String entryId;

        private ArchiveEditable(
                final @NonNull String entryId,
                final @NonNull UpdateHelper updateHelper,
                final @NonNull PropertyEvaluator eval,
                final @NonNull ReferenceHelper refHelper) {
            Parameters.notNull("entryId", entryId); //NOI18N
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("refHelper", refHelper);   //NOI18N
            if (!entryId.startsWith(FILE_REF)) {
                throw new IllegalArgumentException(entryId);
            }
            this.entryId = entryId;
            this.updateHelper = updateHelper;
            this.eval = eval;
            this.refHelper = refHelper;
        }

        @Override
        public boolean canEdit() {
            final String propValue = eval.getProperty(entryId);
            return propValue != null;
        }

        @Override
        public void edit() {
            final String[] propValue = new String[1];
            final String[] oldSource = new String[1];
            final String[] oldJavadoc = new String[1];
            ProjectManager.mutex().readAccess(new Runnable(){
                @Override
                public void run () {
                    propValue[0] = eval.getProperty(entryId);
                    assert propValue[0] != null;
                    oldSource[0] = getSource();
                    oldJavadoc[0] = getJavadoc();
                }
            });
            final EditJarSupport.Item oldItem = new EditJarSupport.Item();
            oldItem.setJarFile(propValue[0]);
            oldItem.setSourceFile(oldSource[0]);
            oldItem.setJavadocFile(oldJavadoc[0]);
            final EditJarSupport.Item newItem = EditJarSupport.showEditDialog(updateHelper.getAntProjectHelper(), oldItem);
            if (newItem != null) {
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            @Override
                            public void run() {
                                store(getSourceProperty(), oldSource[0], newItem.getSourceFile());
                                store(getJavadocProperty(), oldJavadoc[0], newItem.getJavadocFile());
                            }
                        });
                    }
                });
            }
        }

        private String getSource() {
            return eval.getProperty(getSourceProperty());
        }

        private String getJavadoc() {
            return eval.getProperty(getJavadocProperty());
        }

        private String getSourceProperty() {
            return SRC_REF + entryId.substring(FILE_REF.length());
        }

        private String getJavadocProperty() {
            return JDOC_REF + entryId.substring(FILE_REF.length());
        }

        private void store (
                final @NonNull String property,
                final @NullAllowed String oldValue,
                final @NullAllowed String newValue) {
            Parameters.notNull("property", property);       //NOI18N
            if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) {                
                if (newValue != null) {
                    refHelper.createExtraForeignFileReferenceAsIs(newValue, property);
                } else {
                    final EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep.remove(property);
                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                }
                try {
                    final Project prj = FileOwnerQuery.getOwner(updateHelper.getAntProjectHelper().getProjectDirectory());
                    ProjectManager.getDefault().saveProject(prj);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static final class PathFinder implements org.netbeans.spi.project.ui.PathFinder {

        private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
        private static final String SUBST = ".class"; // NOI18N

        //@GuardBy("PathFinder.class")
        private static URI currentKey;
        //@GuardBy("PathFinder.class")
        private static Set<URI> currentValues;
        
        private final org.netbeans.spi.project.ui.PathFinder delegate;

        public PathFinder(@NullAllowed final org.netbeans.spi.project.ui.PathFinder delegate) {
            this.delegate = delegate;
        }

        @Override
        public Node findPath(Node root, Object target) {
            Node result = null;
            if (delegate != null && target instanceof FileObject) {
                FileObject binRoot = root.getLookup().lookup(FileObject.class);
                if (binRoot == null) {
                    final DataFolder dobj = root.getLookup().lookup(DataFolder.class);
                    binRoot = dobj == null ? null : dobj.getPrimaryFile();
                }
                if (binRoot != null) {
                    FileObject newTarget = rebase(binRoot, (FileObject) target);
                    if (newTarget != null) {
                        result = delegate.findPath(root, newTarget);
                    }
                }
            }
            return result;
        }

        @CheckForNull
        static FileObject rebase(
                @NonNull final FileObject binRoot,
                @NonNull final FileObject sourceTarget) {

            if (shouldIgnore(sourceTarget.toURI(), binRoot.toURI())) {
                return null;
            }
            final URL providedBinRootURL = (URL) sourceTarget.getAttribute("classfile-root");    //NOI18N
            final String providedBinaryName = (String) sourceTarget.getAttribute("classfile-binaryName");   //NOI18N
            if (providedBinRootURL != null && providedBinaryName != null) {
                final FileObject providedBinRoot = URLMapper.findFileObject(providedBinRootURL);
                if (binRoot.equals(providedBinRoot)) {
                    return binRoot.getFileObject(providedBinaryName + SUBST);
                }
            } else {
                for (FileObject srcRoot : SourceForBinaryQuery.findSourceRoots(binRoot.toURL()).getRoots()) {
                    if (FileUtil.isParentOf(srcRoot, sourceTarget)) {
                        final FileObject[] newTarget = ActionUtils.regexpMapFiles(
                            new FileObject[]{sourceTarget},
                            srcRoot,
                            SRCDIRJAVA,
                            binRoot,
                            SUBST,
                            true);
                        if (newTarget != null) {
                            return newTarget[0];
                        }
                    }
                }
            }
            if (FileUtil.isParentOf(binRoot, sourceTarget) || binRoot.equals(sourceTarget))  {
                return sourceTarget;
            }
            ignore(sourceTarget.toURI(), binRoot.toURI());
            return null;
        }

        private static synchronized boolean shouldIgnore (
                @NonNull final URI key,
                @NonNull final URI value) {
            if (!key.equals(currentKey)) {
                return false;
            }
            return currentValues.contains(value);
        }

        private static synchronized void ignore(
                @NonNull final URI key,
                @NonNull final URI value) {
            if (!key.equals(currentKey)) {
                currentKey = key;
                currentValues = new HashSet<URI>();
            }
            currentValues.add(value);
        }
        
    }
}
