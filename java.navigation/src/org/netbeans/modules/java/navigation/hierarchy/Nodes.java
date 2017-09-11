/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.hierarchy;

import org.netbeans.modules.java.navigation.actions.NameActions;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.java.navigation.actions.SortActions;
import org.netbeans.modules.java.navigation.base.Utils;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.InstanceContent.Convertor;

/**
 *
 * @author Tomas Zezula
 */
class Nodes {

    private static final String INSPECT_HIERARCHY_ACTION = "Actions/Edit/org-netbeans-modules-java-navigation-actions-ShowHierarchyAction.instance";    //NOI18N
    @StaticResource
    private static final String ICON = "org/netbeans/modules/java/navigation/resources/wait.gif";   //NOI18N
    private static final String ACTION_FOLDER = "Navigator/Actions/Hierarchy/text/x-java";  //NOI18N
    private static final WaitNode WAIT_NODE = new WaitNode();

    
    private Nodes() {
        throw new IllegalStateException();
    }

    static Node rootNode(
            @NonNull final Children cld,
            @NonNull final HierarchyFilters filters) {
        assert filters != null;
        return new RootNode(cld, globalActions(filters));
    }

    static Node waitNode() {
        return WAIT_NODE;
    }

    static Node superTypeHierarchy(
            @NonNull final DeclaredType type,
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final HierarchyFilters filters) {
        assert type != null;
        assert cpInfo != null;
        assert filters != null;
        return superTypeHierarchy(type, cpInfo, filters, 0);
    }

    private static Node superTypeHierarchy(
            @NonNull final DeclaredType type,
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final HierarchyFilters filters,
            final int order) {
        final TypeElement element = (TypeElement)type.asElement();
        final TypeMirror superClass = element.getSuperclass();
        final List<? extends TypeMirror> interfaces = element.getInterfaces();
        final List<Node> childNodes = new ArrayList<Node>(interfaces.size()+1);
        int childOrder = 0;
        if (superClass.getKind() != TypeKind.NONE) {
            childNodes.add(superTypeHierarchy((DeclaredType)superClass, cpInfo, filters, childOrder));
        }
        for (TypeMirror superInterface : interfaces) {
            childOrder++;
            childNodes.add(superTypeHierarchy((DeclaredType)superInterface, cpInfo, filters, childOrder));
        }
        final Children cld;
        if (childNodes.isEmpty()) {
            cld = Children.LEAF;
        } else {
            cld = new SuperTypeChildren(filters);
            cld.add(childNodes.toArray(new Node[childNodes.size()]));
        }
        return new TypeNode(
            cld,
            new Description(
                cpInfo,
                ElementHandle.create(element),
                order),
            filters,
            globalActions(filters));
        
    }

    private static Action[] globalActions(@NonNull final HierarchyFilters filters) {
        return new Action[] {
            NameActions.createFullyQualifiedNameAction(filters),
            SortActions.createSortByNameAction(filters),
            SortActions.createSortBySourceAction(filters)
        };
    }

    private static final class Description {

        private final ClasspathInfo cpInfo;
        private final ElementHandle<TypeElement> handle;
        private final int order;


        Description(
                @NonNull final ClasspathInfo cpInfo,
                @NonNull final ElementHandle<TypeElement> handle,
                final int order) {
            assert cpInfo != null;
            assert handle != null;
            this.cpInfo = cpInfo;
            this.handle = handle;
            this.order = order;
        }

        ClasspathInfo getClasspathInfo() {
            return cpInfo;
        }

        ElementHandle<TypeElement> getHandle() {
            return handle;
        }

        int getSourceOrder() {
            return order;
        }

    }

    private static class RootNode extends AbstractNode {

        private Action[] globalActions;

        RootNode(
            @NonNull final Children cld,
            @NonNull final Action[] globalActions) {
            super(cld);
            assert globalActions != null;
            this.globalActions = globalActions;
        }

        @Override
        public Action[] getActions(boolean context) {
            return globalActions;
        }
    }
    
    private static class WaitNode extends AbstractNode {
        @NbBundle.Messages({
            "LBL_PleaseWait=Please Wait..."
        })
        WaitNode() {
            super(Children.LEAF);
            setIconBaseWithExtension(ICON);
            setDisplayName(Bundle.LBL_PleaseWait());
        }
    }

    private static final class TypeNode extends AbstractNode implements PropertyChangeListener {

        private final Description description;
        private final HierarchyFilters filters;
        private final Action[] globalActions;
        //@GuardedBy("this")
        private Action openAction;

        TypeNode(
            @NonNull final Children cld,
            @NonNull final Description description,
            @NonNull final HierarchyFilters filters,
            @NonNull final Action[] globalActions) {
            super(cld, createLookup(description));
            assert description != null;
            assert filters != null;
            assert globalActions != null;
            this.description = description;
            this.filters = filters;
            this.globalActions = globalActions;
            this.filters.addPropertyChangeListener(this);
            updateDisplayName();
        }

        @Override
        public String getShortDescription() {
            if (filters.isFqn()) {
                return super.getShortDescription();
            } else {
                return description.getHandle().getQualifiedName();
            }
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(
                    ElementIcons.getElementIcon(
                    description.getHandle().getKind(),
                    EnumSet.noneOf(Modifier.class)));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (HierarchyFilters.PROP_FQN.equals(evt.getPropertyName())) {
                updateDisplayName();
            }
        }

        @Override
        public Action[] getActions(boolean context) {
            if (context) {
                return globalActions;
            } else {
                final List<? extends Action> additionalActions = Utilities.actionsForPath(ACTION_FOLDER);
                final int additionalActionSize = additionalActions.isEmpty() ? 0 : additionalActions.size() + 1;
                final List<Action> actions  = new ArrayList<Action>(4 + globalActions.length + additionalActionSize);
                actions.add(getOpenAction());
                actions.add(FileUtil.getConfigObject(INSPECT_HIERARCHY_ACTION, Action.class));
                actions.add(RefactoringActionsFactory.whereUsedAction());
                actions.add(null);
                if (additionalActionSize > 0) {
                    actions.addAll(additionalActions);
                    actions.add(null);
                }
                actions.addAll(Arrays.asList(globalActions));
                return actions.toArray(new Action[actions.size()]);
            }
        }

        @Override
        public Action getPreferredAction() {
            return getOpenAction();
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            return null;
        }

        @Override
        public Transferable drag() throws IOException {
            return null;
        }

        @Override
        protected void createPasteTypes(Transferable t, List<PasteType> s) {
            // Do nothing
        }        

        private synchronized Action getOpenAction() {
            if ( openAction == null) {
                openAction = new OpenAction();
            }
            return openAction;
        }

        private void updateDisplayName() {
            String name = description.handle.getQualifiedName();
            if (!filters.isFqn()) {
                name = getSimpleName(name);
            }
            setDisplayName(name);
        }

        @NonNull
        private static Lookup createLookup (@NonNull Description desc) {
            final InstanceContent ic = new InstanceContent();
            ic.add(desc);
            ic.add(desc, ConvertDescription2TreePathHandle);
            ic.add(desc, ConvertDescription2FileObject);
            ic.add(desc, ConvertDescription2DataObject);
            return new AbstractLookup(ic);
        }

        private class OpenAction extends AbstractAction {

            @NbBundle.Messages({"LBL_GoTo=Go to Source"})
            OpenAction() {
                putValue ( Action.NAME, Bundle.LBL_GoTo());
            }

            @NbBundle.Messages({"MSG_NoSource=Source not available for {0}"})
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ElementOpen.open(
                        description.getClasspathInfo(),
                        description.getHandle())) {
                    Toolkit.getDefaultToolkit().beep();
                    StatusDisplayer.getDefault().setStatusText(
                        Bundle.MSG_NoSource(description.getHandle().getQualifiedName()));
                }
            }
        }


        private static final Convertor<Description, TreePathHandle> ConvertDescription2TreePathHandle =
                new InstanceContent.Convertor<Description, TreePathHandle>() {
                    @Override
                    public TreePathHandle convert(Description desc) {
                        return TreePathHandle.from(desc.getHandle(), desc.getClasspathInfo());
                    }
                    @Override
                    public Class<? extends TreePathHandle> type(Description desc) {
                        return TreePathHandle.class;
                    }
                    @Override
                    public String id(Description desc) {
                        return "IL[" + desc.toString();
                    }
                    @Override
                    public String displayName(Description desc) {
                        return id(desc);
                    }
            };

        private static final Convertor<Description, FileObject> ConvertDescription2FileObject =
                new InstanceContent.Convertor<Description, FileObject>() {
                    @Override
                    public FileObject convert(Description desc) {
                        return Utils.getFile(
                            desc.getHandle(),
                            desc.getClasspathInfo());
                    }
                    @Override
                    public Class<? extends FileObject> type(Description desc) {
                        return FileObject.class;
                    }
                    @Override
                    public String id(Description desc) {
                        return "IL[" + desc.toString();
                    }
                    @Override
                    public String displayName(Description desc) {
                        return id(desc);
                    }
            };

        private static final Convertor<Description, DataObject> ConvertDescription2DataObject =
                new InstanceContent.Convertor<Description, DataObject>(){
                    @Override
                    public DataObject convert(Description desc) {
                        try {
                            final FileObject file = Utils.getFile(
                                desc.getHandle(),
                                desc.getClasspathInfo());
                            return file == null ? null : DataObject.find(file);
                        } catch (DataObjectNotFoundException ex) {
                            return null;
                        }
                    }
                    @Override
                    public Class<? extends DataObject> type(Description desc) {
                        return DataObject.class;
                    }
                    @Override
                    public String id(Description desc) {
                        return "IL[" + desc.toString();
                    }
                    @Override
                    public String displayName(Description desc) {
                        return id(desc);
                    }
            };

    }

    private static class SuperTypeChildren extends Children.SortedArray implements PropertyChangeListener {

        private final HierarchyFilters hierarchy;

        SuperTypeChildren(@NonNull final HierarchyFilters filters) {
            assert filters != null;
            this.hierarchy = filters;
            this.hierarchy.addPropertyChangeListener(this);
            updateComparator();
        }


        private void updateComparator() {
            if (hierarchy.isNaturalSort()) {
                setComparator(new OrderComparator());
            } else {
                setComparator(new LexicographicComparator());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (HierarchyFilters.PROP_NATURAL_SORT.equals(evt.getPropertyName())) {
                updateComparator();
            } else if (!hierarchy.isNaturalSort() && HierarchyFilters.PROP_FQN.equals(evt.getPropertyName())) {
                updateComparator();
            }
        }

    }

    private static final class LexicographicComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            return n1.getDisplayName().compareTo(n2.getDisplayName());
        }
    }

    private static final class OrderComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            final int o1 = n1.getLookup().lookup(Description.class).getSourceOrder();
            final int o2 = n2.getLookup().lookup(Description.class).getSourceOrder();
            return o1 < o2 ? -1 : o1 == o2 ? 0 : 1;
        }
    }

    static Node subTypeHierarchy(
            @NonNull final TypeElement element,
            @NonNull final CompilationInfo info,
            @NonNull final HierarchyFilters filters,
            @NonNull final AtomicBoolean cancel) {
        try {
            boolean isSourceRoot = true;
            FileObject thisRoot = findSourceRoot(SourceUtils.getFile(element, info.getClasspathInfo()));
            if (thisRoot == null) {
                thisRoot = findBinaryRoot(element, info);
                isSourceRoot = false;
            }
            if (thisRoot == null) {
                return null;
            }
            ElementHandle<TypeElement> elementHandle = ElementHandle.create(element);
            TypeDescription td = new TypeDescription(info.getClasspathInfo(), elementHandle);
            
            Map<TypeDescription, Set<TypeDescription>> subclassesJoined = new ComputeSubClasses(cancel).computeUsers(info, thisRoot, Collections.singleton(td), new long[1], false, isSourceRoot);
            
            if (subclassesJoined == null) return null;
            
            List<TypeDescription> inOrder = Utilities.topologicalSort(subclassesJoined.keySet(), subclassesJoined);
            
            Collections.reverse(inOrder);
            
            Map<TypeDescription, Node> type2Node = new HashMap<TypeDescription, Node>();
            
            for (TypeDescription toProcess : inOrder) {
                Set<TypeDescription> subclasses = subclassesJoined.get(toProcess);
                List<Node> childNodes = new ArrayList<Node>(subclasses.size());
                
                for (TypeDescription subclass : subclasses) {
                    Node subNode = new FilterNode(type2Node.get(subclass));
                    
                    assert subNode != null;
                    
                    childNodes.add(subNode);
                }
                
                final Children cld;
                if (childNodes.isEmpty()) {
                    cld = Children.LEAF;
                } else {
                    cld = new SuperTypeChildren(filters);
                    cld.add(childNodes.toArray(new Node[childNodes.size()]));
                }
                type2Node.put(toProcess, new TypeNode(
                    cld,
                    new Description(
                        toProcess.cpInfo,
                        toProcess.element,
                        /*XXX:*/0),
                    filters,
                    new Action[0]));
            }

            return type2Node.get(td);
        } catch (TopologicalSortException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }


    private static FileObject findSourceRoot(FileObject file) {
        final ClassPath cp = file != null ? ClassPath.getClassPath(file, ClassPath.SOURCE) : null;
        //Null is a valid value for files which have no source path (default filesystem).
        return cp != null ? cp.findOwnerRoot(file) : null;
    }

    @CheckForNull
    private static FileObject findBinaryRoot(
            @NonNull final TypeElement element,
            @NonNull final CompilationInfo info) {
        final FileObject res = findBinaryInCp(
                info.getElements(),
                element,
                info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT));
        if (res != null) {
            return res;
        }
        return findBinaryInCp(
                info.getElements(),
                element,
                info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE));
    }

    @CheckForNull
    private static FileObject findBinaryInCp(
            @NonNull final Elements elements,
            @NonNull final TypeElement element,
            @NonNull final ClassPath cp) {
        final FileObject file = cp.findResource(String.format(
                "%s.class", //NOI18N
                elements.getBinaryName(element).toString().replace('.', '/'))); //NOI18N
        return file == null ? null : cp.findOwnerRoot(file);
    }

    private static String getSimpleName(@NonNull final String fqn) {
        int sepIndex = fqn.lastIndexOf('$');   //NOI18N
        if (sepIndex == -1) {
            sepIndex = fqn.lastIndexOf('.');   //NOI18N
        }
        return sepIndex >= 0?
            fqn.substring(sepIndex+1):
            fqn;
    }

    static final class ComputeSubClasses {
        private final AtomicBoolean cancel;

        public ComputeSubClasses(AtomicBoolean cancel) {
            this.cancel = cancel;
        }
        
        Map<TypeDescription, Set<TypeDescription>> computeUsers(CompilationInfo info, FileObject thisRoot, Set<TypeDescription> baseHandles, long[] classIndexCumulative, boolean interactive, boolean isSourceRoot) {
            Map<URL, List<URL>> sourceDeps = getDependencies(false);
            Map<URL, List<URL>> binaryDeps = getDependencies(true);

            if (sourceDeps == null || binaryDeps == null) {
    //            if (interactive) {
    //                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GoToImplementation.class, "ERR_NoDependencies"), NotifyDescriptor.ERROR_MESSAGE);
    //
    //                DialogDisplayer.getDefault().notifyLater(nd);
    //            } else {
                    LOG.log(Level.FINE, "No dependencies");
    //            }

                return null;
            }

            final URL thisRootURL = thisRoot.toURL();

            Map<URL, List<URL>> rootPeers = getRootPeers();
            List<URL> sourceRoots = reverseSourceRootsInOrder(info, thisRootURL, thisRoot, sourceDeps, binaryDeps, rootPeers, interactive, isSourceRoot);

            if (sourceRoots == null) {
                return null;
            }

            baseHandles = new HashSet<TypeDescription>(baseHandles);

            for (Iterator<TypeDescription> it = baseHandles.iterator(); it.hasNext(); ) {
                if (cancel.get()) return null;
                if (it.next().element.getBinaryName().contentEquals("java.lang.Object")) {
                    it.remove();
                    break;
                }
            }

            Map<TypeDescription, Set<TypeDescription>> result = new HashMap<TypeDescription, Set<TypeDescription>>();
            Map<TypeDescription, Set<TypeDescription>> auxHandles = new HashMap<TypeDescription, Set<TypeDescription>>();

            if (!sourceDeps.containsKey(thisRootURL)) {
                Set<URL> binaryRoots = new HashSet<URL>();

                for (URL sr : sourceRoots) {
                    List<URL> deps = sourceDeps.get(sr);

                    if (deps != null) {
                        binaryRoots.addAll(deps);
                    }
                }

                binaryRoots.retainAll(binaryDeps.keySet());

                for (TypeDescription handle : baseHandles) {
                    Set<TypeDescription> types = computeUsers(ClasspathInfo.create(ClassPath.EMPTY, ClassPathSupport.createClassPath(binaryRoots.toArray(new URL[0])), ClassPath.EMPTY), SearchScope.DEPENDENCIES, Collections.singleton(handle), classIndexCumulative, result);

                    if (types == null/*canceled*/ || cancel.get()) {
                        return null;
                    }

                    auxHandles.put(handle, types);
                }
            }

            Map<URL, Map<TypeDescription, Set<TypeDescription>>> root2SubClasses = new LinkedHashMap<URL, Map<TypeDescription, Set<TypeDescription>>>();

            for (URL file : sourceRoots) {
                for (TypeDescription base : baseHandles) {
                    if (cancel.get()) return null;

                    Set<TypeDescription> baseTypes = new HashSet<TypeDescription>();

                    baseTypes.add(base);

                    Set<TypeDescription> aux = auxHandles.get(base);

                    if (aux != null) {
                        baseTypes.addAll(aux);
                    }

                    for (URL dep : sourceDeps.get(file)) {
                        Map<TypeDescription, Set<TypeDescription>> depTypesMulti = root2SubClasses.get(dep);
                        Set<TypeDescription> depTypes = depTypesMulti != null ? depTypesMulti.get(base) : null;

                        if (depTypes != null) {
                            baseTypes.addAll(depTypes);
                        }
                    }

                    Set<TypeDescription> types = computeUsers(file, baseTypes, classIndexCumulative, result);

                    if (types == null/*canceled*/ || cancel.get()) {
                        return null;
                    }

                    types.removeAll(baseTypes);

                    Map<TypeDescription, Set<TypeDescription>> currentUsers = root2SubClasses.get(file);

                    if (currentUsers == null) {
                        root2SubClasses.put(file, currentUsers = new LinkedHashMap<TypeDescription, Set<TypeDescription>>());
                    }

                    currentUsers.put(base, types);
                }
            }

            return result;
        }
        private static final Logger LOG = Logger.getLogger(Nodes.class.getName());

        static Map<URL, List<URL>> dependenciesOverride;

        private static Map<URL, List<URL>> getDependencies(boolean binary) {
            if (dependenciesOverride != null) {
                return dependenciesOverride;
            }

            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

            if (l == null) {
                return null;
            }

            Class clazz = null;
            String method = null;

            try {
                clazz = l.loadClass("org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController");
                method = binary ? "getBinaryRootDependencies" : "getRootDependencies";
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.FINE, null, ex);
                try {
                    clazz = l.loadClass("org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater");
                    method = binary ? "getDependencies" : "doesnotexist";
                } catch (ClassNotFoundException inner) {
                    LOG.log(Level.FINE, null, inner);
                    return null;
                }
            }

            try {
                Method getDefault = clazz.getDeclaredMethod("getDefault");
                Object instance = getDefault.invoke(null);
                Method dependenciesMethod = clazz.getDeclaredMethod(method);

                return (Map<URL, List<URL>>) dependenciesMethod.invoke(instance);
            } catch (IllegalAccessException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (InvocationTargetException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (NoSuchMethodException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (SecurityException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (ClassCastException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            }
        }

        static Map<URL, List<URL>> rootPeers;

        private static Map<URL, List<URL>> getRootPeers() {
            if (rootPeers != null) {
                return rootPeers;
            }

            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

            if (l == null) {
                return null;
            }

            Class clazz = null;
            String method = null;

            try {
                clazz = l.loadClass("org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController");
                method = "getRootPeers";
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            }

            try {
                Method getDefault = clazz.getDeclaredMethod("getDefault");
                Object instance = getDefault.invoke(null);
                Method peersMethod = clazz.getDeclaredMethod(method);

                return (Map<URL, List<URL>>) peersMethod.invoke(instance);
            } catch (IllegalAccessException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (InvocationTargetException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (NoSuchMethodException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (SecurityException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            } catch (ClassCastException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            }
        }

        static List<URL> reverseSourceRootsInOrderOverride;

        private List<URL> reverseSourceRootsInOrder(CompilationInfo info, URL thisRoot, FileObject thisRootFO, Map<URL, List<URL>> sourceDeps, Map<URL, List<URL>> binaryDeps, Map<URL, List<URL>> rootPeers, boolean interactive, boolean isSourceRoot) {
            if (reverseSourceRootsInOrderOverride != null) {
                return reverseSourceRootsInOrderOverride;
            }

            Set<URL> sourceRootsSet;

            if (sourceDeps.containsKey(thisRoot)) {
                sourceRootsSet = findReverseSourceRoots(thisRoot, sourceDeps, rootPeers, info.getFileObject());
            } else {
                sourceRootsSet = new HashSet<URL>();

                for (URL binary : findBinaryRootsForSourceRoot(thisRootFO, binaryDeps, isSourceRoot)) {
                    List<URL> deps = binaryDeps.get(binary);

                    if (deps != null) {
                        sourceRootsSet.addAll(deps);
                    }
                }
            }

            List<URL> sourceRoots;
            try {
                sourceRoots = new LinkedList<URL>(Utilities.topologicalSort(sourceDeps.keySet(), sourceDeps));
            } catch (TopologicalSortException ex) {
                if (interactive) {
    //                Exceptions.attachLocalizedMessage(ex,NbBundle.getMessage(GoToImplementation.class, "ERR_CycleInDependencies"));
                    Exceptions.printStackTrace(ex);
                } else {
                    LOG.log(Level.FINE, null, ex);
                }
                return null;
            }

            sourceRoots.retainAll(sourceRootsSet);

            Collections.reverse(sourceRoots);

            return sourceRoots;
        }

        private static Set<URL> findReverseSourceRoots(final URL thisSourceRoot, Map<URL, List<URL>> sourceDeps, Map<URL, List<URL>> rootPeers, final FileObject thisFile) {
            long startTime = System.currentTimeMillis();

            try {
                //TODO: from SourceUtils (which filters out source roots that do not belong to open projects):
                //Create inverse dependencies
                final Map<URL, List<URL>> inverseDeps = new HashMap<URL, List<URL>> ();
                for (Map.Entry<URL,List<URL>> entry : sourceDeps.entrySet()) {
                    final URL u1 = entry.getKey();
                    final List<URL> l1 = entry.getValue();
                    for (URL u2 : l1) {
                        List<URL> l2 = inverseDeps.get(u2);
                        if (l2 == null) {
                            l2 = new ArrayList<URL>();
                            inverseDeps.put (u2,l2);
                        }
                        l2.add (u1);
                    }
                }
                //Collect dependencies
                final Set<URL> result = new HashSet<URL>();
                final LinkedList<URL> todo = new LinkedList<URL> ();
                todo.add (thisSourceRoot);
                List<URL> peers = rootPeers != null ? rootPeers.get(thisSourceRoot) : null;
                if (peers != null)
                    todo.addAll(peers);
                while (!todo.isEmpty()) {
                    final URL u = todo.removeFirst();
                    if (!result.contains(u)) {
                        result.add (u);
                        final List<URL> ideps = inverseDeps.get(u);
                        if (ideps != null) {
                            todo.addAll (ideps);
                        }
                    }
                }
                return result;
            } finally {
                long endTime = System.currentTimeMillis();

                Logger.getLogger("TIMER").log(Level.FINE, "Find Reverse Source Roots", //NOI18N
                        new Object[]{thisFile, endTime - startTime});
            }
        }

        private Set<URL> findBinaryRootsForSourceRoot(FileObject root, Map<URL, List<URL>> binaryDeps, boolean isSourceRoot) {
    //      BinaryForSourceQuery.findBinaryRoots(thisSourceRoot).getRoots();
            Set<URL> result = new HashSet<URL>();

            if (isSourceRoot) {
                for (URL bin : binaryDeps.keySet()) {
                    if (cancel.get()) return Collections.emptySet();
                    for (FileObject s : SourceForBinaryQuery.findSourceRoots(bin).getRoots()) {
                        if (s == root) {
                            result.add(bin);
                        }
                    }
                }
            } else if (binaryDeps.containsKey(root.toURL())) {
                result.add(root.toURL());
            }
            return result;
        }

        private Set<TypeDescription> computeUsers(URL source, Set<TypeDescription> base, long[] classIndexCumulative, Map<TypeDescription, Set<TypeDescription>> output) {
            ClasspathInfo cpinfo = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPathSupport.createClassPath(source));

            return computeUsers(cpinfo, ClassIndex.SearchScope.SOURCE, base, classIndexCumulative, output);
        }

        private Set<TypeDescription> computeUsers(ClasspathInfo cpinfo, SearchScope scope, Set<TypeDescription> base, long[] classIndexCumulative, Map<TypeDescription, Set<TypeDescription>> output) {
            long startTime = System.currentTimeMillis();

            try {
                List<TypeDescription> l = new LinkedList<TypeDescription>(base);
                Set<TypeDescription> result = new HashSet<TypeDescription>();

                while (!l.isEmpty()) {
                    if (cancel.get()) return null;

                    TypeDescription eh = l.remove(0);

                    result.add(eh);
                    Set<ElementHandle<TypeElement>> typeElements = cpinfo.getClassIndex().getElements(eh.element, Collections.singleton(SearchKind.IMPLEMENTORS), EnumSet.of(scope));


                    //XXX: Canceling
                    if (typeElements != null) {
                        Set<TypeDescription> outputElements = output.get(eh);

                        if (outputElements == null) {
                            output.put(eh, outputElements = new HashSet<TypeDescription>());
                        }

                        for (ElementHandle<TypeElement> te : typeElements) {
                            TypeDescription currentTD = new TypeDescription(cpinfo, te);
                            outputElements.add(currentTD);
                            l.add(currentTD);
                        }
                    }
                }
                return result;
            } finally {
                classIndexCumulative[0] += (System.currentTimeMillis() - startTime);
            }
        }

    }
    
    private static final class TypeDescription {
        private final ClasspathInfo cpInfo;
        private final ElementHandle<TypeElement> element;

        public TypeDescription(ClasspathInfo cpInfo, ElementHandle<TypeElement> element) {
            this.cpInfo = cpInfo;
            this.element = element;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.cpInfo != null ? this.cpInfo.hashCode() : 0);
            hash = 79 * hash + (this.element != null ? this.element.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypeDescription other = (TypeDescription) obj;
            if (this.cpInfo != other.cpInfo && (this.cpInfo == null || !this.cpInfo.equals(other.cpInfo))) {
                return false;
            }
            if (this.element != other.element && (this.element == null || !this.element.equals(other.element))) {
                return false;
            }
            return true;
        }

    }
}
