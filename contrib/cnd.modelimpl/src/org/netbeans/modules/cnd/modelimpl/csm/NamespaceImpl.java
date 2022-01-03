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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.modelimpl.content.project.ProjectComponent;
import org.netbeans.modules.cnd.modelimpl.content.project.DeclarationContainerNamespace;
import org.netbeans.modules.cnd.modelimpl.content.project.DeclarationContainer;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * CsmNamespace implementation
 */
public class NamespaceImpl implements CsmNamespace, MutableDeclarationsContainer,
        Persistent, SelfPersistent, Disposable, CsmIdentifiable {
    
    private static final CharSequence GLOBAL = CharSequences.create("$Global$"); // NOI18N
    // only one of project/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private Object projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    
    // only one of parent/parentUID must be used (based on USE_UID_TO_CONTAINER)
    private /*final*/ CsmNamespace parentRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmNamespace> parentUID;
    
    private final CharSequence name;
    private final CharSequence qualifiedName;
    
    /** maps namespaces FQN to namespaces */
    private final Map<CharSequence, CsmUID<CsmNamespace>> nestedNamespaces;
    
    private final Key declarationsSorageKey;

    private final Set<CsmUID<CsmOffsetableDeclaration>> unnamedDeclarations;
    private final Set<CsmUID<CsmUsingDirective>> usingDirectives;
    
    private final TreeMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> nsDefinitions;
    private final ReadWriteLock nsDefinitionsLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock projectLock = new ReentrantReadWriteLock();
    
    private final boolean global;
    
    private int inlineDefinitionsCounter = 0;
    
    private final AtomicInteger inlineNamespacesCounter;
    
    /** Constructor used for global namespace */
    private NamespaceImpl(ProjectBase project, boolean fake) {
        this.name = GLOBAL;
        this.qualifiedName = CharSequences.empty(); // NOI18N
        this.parentUID = null;
        this.parentRef = null;
        this.global = true;
        this.inlineNamespacesCounter = new AtomicInteger(0);
        assert project != null;
        
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert this.projectUID != null;
        unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>());
        usingDirectives = Collections.synchronizedSet(new HashSet<CsmUID<CsmUsingDirective>>());
        nestedNamespaces = new ConcurrentHashMap<>();
        nsDefinitions = new TreeMap<>(defenitionComparator);

        this.projectRef = new WeakReference<>(project);
        this.declarationsSorageKey = fake ? null : new DeclarationContainerNamespace(this).getKey();
    }

    public static NamespaceImpl create(ProjectBase project, boolean fake) {
        NamespaceImpl namespaceImpl = new NamespaceImpl(project, fake);
        if (!fake) {
            project.registerNamespace(namespaceImpl);
        }
        return namespaceImpl;
    }
    
    private static final boolean CHECK_PARENT = false;
    
    protected NamespaceImpl(ProjectBase project, NamespaceImpl parent, CharSequence name, CharSequence qualifiedName) {
        this.name = NameCache.getManager().getString(name);
        this.global = false;
        this.inlineNamespacesCounter = new AtomicInteger(0);
        assert project != null;
        
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert this.projectUID != null;
        unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>());
        usingDirectives = Collections.synchronizedSet(new HashSet<CsmUID<CsmUsingDirective>>());
        nestedNamespaces = new ConcurrentHashMap<>();
        nsDefinitions = new TreeMap<>(defenitionComparator);

        this.projectRef = new WeakReference<>(project);
        this.qualifiedName = QualifiedNameCache.getManager().getString(qualifiedName);
        // TODO: rethink once more
        // now all classes do have namespaces
//        // TODO: this makes parent-child relationships assymetric, that's bad;
//        // on the other hand I dont like an idea of top-level namespaces' getParent() returning non-null
//        // Probably the CsmProject should have 2 methods:
//        // getGlobalNamespace() and getTopLevelNamespaces()
//        this.parent = (parent == null || parent.isGlobal()) ? null : parent;
        assert !CHECK_PARENT || parent != null;

        this.parentUID = UIDCsmConverter.namespaceToUID(parent);
        assert parentUID != null || parent == null;

        this.parentRef = null;
        declarationsSorageKey = new DeclarationContainerNamespace(this).getKey();
        
    }

    public static NamespaceImpl create(ProjectBase project, NamespaceImpl parent, CharSequence name, CharSequence qualifiedName) {
        assert project.holdsNamespaceLock() : "Logical namespace can be created only via ProjectBase!";
        NamespaceImpl namespaceImpl = new NamespaceImpl(project, parent, name, qualifiedName);
        project.registerNamespace(namespaceImpl);
        if( parent != null ) {
            // nb: this.parent should be set first, since getQualidfiedName request parent's fqn
            parent.addNestedNamespace(namespaceImpl);
        }
        namespaceImpl.notify(namespaceImpl, NotifyEvent.NAMESPACE_ADDED);
        return namespaceImpl;
    }

    protected enum NotifyEvent {
        DECLARATION_ADDED,
        DECLARATION_REMOVED,
        NAMESPACE_ADDED,
        NAMESPACE_REMOVED,
    }

    protected void notify(CsmObject obj, NotifyEvent kind) {
        switch (kind) {
            case DECLARATION_ADDED:
                assert obj instanceof CsmOffsetableDeclaration;
                if (!ForwardClass.isForwardClass((CsmOffsetableDeclaration)obj)) {
                    // no need to notify about fake classes
                    Notificator.instance().registerNewDeclaration((CsmOffsetableDeclaration)obj);
                }
                break;
            case DECLARATION_REMOVED:
                assert obj instanceof CsmOffsetableDeclaration;
                if (!ForwardClass.isForwardClass((CsmOffsetableDeclaration)obj)) {
                    // no need to notify about fake classes
                    Notificator.instance().registerRemovedDeclaration((CsmOffsetableDeclaration)obj);
                }
                break;
            case NAMESPACE_ADDED:
                assert obj instanceof CsmNamespace;
                assert !((CsmNamespace)obj).isGlobal();
                Notificator.instance().registerNewNamespace((CsmNamespace)obj);
                break;
            case NAMESPACE_REMOVED:
                assert obj instanceof CsmNamespace;
                assert !((CsmNamespace)obj).isGlobal();
                Notificator.instance().registerRemovedNamespace((CsmNamespace)obj);
                break;
            default:
                throw new IllegalArgumentException("unexpected kind " + kind); // NOI18N
        }
    }
    
    @Override
    public void dispose() {
        onDispose();
        notify(this, NotifyEvent.NAMESPACE_REMOVED);
    }    
    
    private void onDispose() {
        projectLock.writeLock().lock();
        try {
            if (projectRef == null) {
                // restore container from it's UID
                this.projectRef = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                assert this.projectRef != null || this.projectUID == null : "no object for UID " + this.projectUID;
            }
            if (parentRef == null) {
                // restore container from it's UID
                this.parentRef = UIDCsmConverter.UIDtoNamespace(this.parentUID);
                assert this.parentRef != null || this.parentUID == null : "no object for UID " + this.parentUID;
            }
        } finally {
            projectLock.writeLock().unlock();
        }
        weakDeclarationContainer = null;
    }
    
    private static final String UNNAMED_PREFIX = "<unnamed>";  // NOI18N
    private Set<Integer> unnamedNrs = new HashSet<>();
    public String getNameForUnnamedElement() {
        String out = UNNAMED_PREFIX;
        int minVal = getMinUnnamedValue();
        if (minVal != 0) {
            out = out + minVal;
        }
        unnamedNrs.add(Integer.valueOf(minVal));
        return out;
    }
    
    private int getMinUnnamedValue() {
        for (int i = 0; i < unnamedNrs.size(); i++) {
            if (!unnamedNrs.contains(Integer.valueOf(i))) {
                return i;
            }
        }
        return unnamedNrs.size();
    }
    
    @Override
    public CsmNamespace getParent() {
        return _getParentNamespace();
    }
    
    @Override
    public Collection<CsmNamespace> getNestedNamespaces() {
        Collection<CsmNamespace> out = UIDCsmConverter.UIDsToNamespaces(new ArrayList<>(nestedNamespaces.values()));
        return out;
    }

    @Override
    public Collection<CsmNamespace> getInlinedNamespaces() {
        if (hasInlined()) {
            List<CsmNamespace> result = new ArrayList<>(inlineNamespacesCounter.get());
            for (CsmNamespace nested : getNestedNamespaces()) {
                if (nested.isInline()) {
                    result.add(nested);
                }
            }
            return Collections.unmodifiableCollection(result);
        }
        return Collections.emptyList();
    }

    private volatile WeakReference<DeclarationContainerNamespace> weakDeclarationContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ?  new WeakReference<DeclarationContainerNamespace>(null) : null;
    private int preventMultiplyDiagnosticExceptions = 0;
    private DeclarationContainerNamespace getDeclarationsSorage() {
        if (declarationsSorageKey == null) {
            return DeclarationContainerNamespace.empty();
        }
        DeclarationContainerNamespace dc = null;
        WeakReference<DeclarationContainerNamespace> weak = null;
        if (TraceFlags.USE_WEAK_MEMORY_CACHE) {
            weak = weakDeclarationContainer;
            if (weak != null) {
                dc = weak.get();
                if (dc != null) {
                    return dc;
                }
            }
        }
        dc = (DeclarationContainerNamespace) RepositoryUtils.get(declarationsSorageKey);
        if (dc == null && preventMultiplyDiagnosticExceptions < DiagnosticExceptoins.LimitMultiplyDiagnosticExceptions) {
            DiagnosticExceptoins.registerIllegalRepositoryStateException("Failed to get DeclarationsSorage by key ", declarationsSorageKey); // NOI18N
            preventMultiplyDiagnosticExceptions++;
        }
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && dc != null && weakDeclarationContainer != null) {
            weakDeclarationContainer = new WeakReference<>(dc);
        }
        return dc != null ? dc : DeclarationContainerNamespace.empty();
    }
    
    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        DeclarationContainer declStorage = getDeclarationsSorage();
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = declStorage.getDeclarationsUIDs();
        // add all unnamed declarations
        synchronized (unnamedDeclarations) {
            uids.addAll(unnamedDeclarations);
        }
        // convert to objects
        Collection<CsmOffsetableDeclaration> decls = UIDCsmConverter.UIDsToDeclarations(uids);
        return decls;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        DeclarationContainer declStorage = getDeclarationsSorage();
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = declStorage.getDeclarationsUIDs();
        // add all unnamed declarations
        synchronized (unnamedDeclarations) {
            uids.addAll(unnamedDeclarations);
        }
        return UIDCsmConverter.UIDsToDeclarations(uids, filter);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> findUidsByPrefix(String prefix) {
        // To improve performance use char(255) instead real Character.MAX_VALUE
        char maxChar = 255; //Character.MAX_VALUE;
        return findUidsRange(prefix, prefix+maxChar);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> findUidsRange(String from, String to) {
        DeclarationContainer declStorage = getDeclarationsSorage();
        return declStorage.getUIDsRange(from, to);
    }

    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(CharSequence fqn, Kind[] kinds) {
        DeclarationContainer declStorage = getDeclarationsSorage();
        return declStorage.getDeclarationsRange(fqn, kinds);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getUnnamedUids() {
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> uids;
        // add all unnamed declarations
        synchronized (unnamedDeclarations) {
            uids = new ArrayList<>(unnamedDeclarations);
        }
        return uids;
    }

    @Override
    public boolean isGlobal() {
        return global;
    }

    @Override
    public boolean isInline() {
        return inlineDefinitionsCounter > 0;
    }

    @Override
    public CharSequence getQualifiedName() {
        return qualifiedName;
    }
    
    @Override
    public CharSequence getName() {
        return name;
    }
    
    private boolean hasInlined() {
        return inlineNamespacesCounter.get() > 0;
    }    
    
    private void addNestedNamespace(NamespaceImpl nsp) {
        assert nsp != null;
        CsmUID<CsmNamespace> nestedNsUid = RepositoryUtils.put((CsmNamespace)nsp);
        assert nestedNsUid != null;
        nestedNamespaces.put(nsp.getQualifiedName(), nestedNsUid);
        if (nsp.isInline()) {
            incInlinedNamespacesCounter();
        }
        RepositoryUtils.put(this);
    }
    
    private void removeNestedNamespace(NamespaceImpl nsp) {
        assert nsp != null;
        CsmUID<CsmNamespace> nestedNsUid = nestedNamespaces.remove(nsp.getQualifiedName());
        if (nestedNsUid == null) {
            CndUtils.assertTrueInConsole(false, "can not remove " + nsp.getQualifiedName() + " from " + this.getQualifiedName(), " see https://netbeans.org/bugzilla/show_bug.cgi?id=257674"); // NOI18N
        }
        // handle unnamed namespace index
        if (nsp.getName().length() == 0) {
            String fqn = nsp.getQualifiedName().toString();
            int greaterInd = fqn.lastIndexOf('>');
            assert greaterInd >= 0;
            if (greaterInd + 1 < fqn.length()) {
                try {
                    Integer index = Integer.parseInt(fqn.substring(greaterInd+1));
                    unnamedNrs.remove(index);
                } catch (NumberFormatException ex) {
                    DiagnosticExceptoins.register(ex);
                }
            } else {
                unnamedNrs.remove(Integer.valueOf(0));
            }
        }
        if (nsp.isInline()) {
            decInlinedNamespacesCounter();
        }
        RepositoryUtils.put(this);
    }

    /**
     * Determines whether a variable has namespace or global scope
     *
     * @param v variable to check.
     * NB: should be file- or namespace- level,
     * don't pass a field, a parameter or a local var!
     *
     * @param isFileLevel true if it's defined on file level,
     * otherwise (if it's defined in namespace definition) false
     *
     * @return true if the variable has namesapce scope or global scope,
     * or false if it is file-local scope (i.e. no external linkage)
     */
    public static boolean isNamespaceScope(VariableImpl<?> var, boolean isFileLevel) {
        if( ((FileImpl) var.getContainingFile()).isHeaderFile() && ! CsmKindUtilities.isVariableDefinition(var)) {
            return true;
        } else if( var.isStatic() ) {
	    return false;
	}
	else if( isFileLevel && var.isConst()) {
            // all const variables have external linkage visibility
            // so, can return true,
            // but to keep less side effects, let's leave old behavior for
            // variable definitions (they are scope-named)
            return !CsmKindUtilities.isVariableDefinition(var);
	}
	return true;
    }

    /**
     * Determines whether a function has namesace scope
     *
     * @param func function to check.
     *
     * @return true if the function has namesapce scope or global scope,
     * or false if it is file-local scope (i.e. no external linkage)
     */
    public static boolean isNamespaceScope(FunctionImpl<?> func) {
        if( ((FileImpl) func.getContainingFile()).isHeaderFile() && ! func.isPureDefinition() ) {
            return true;
        } else if (func.isCStyleStatic()) {
//        } else if (func.isStatic()) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether a function has namesace scope
     *
     * @param func function to check.
     *
     * @return true if the function has namesapce scope or global scope,
     * or false if it is file-local scope (i.e. no external linkage)
     */
    public static boolean isNamespaceScope(CsmFile file, boolean pureDefinition, boolean _static) {
        if( ((FileImpl) file).isHeaderFile() && ! pureDefinition ) {
            return true;
        } else if (_static) {
            return false;
        }
        return true;
    }
    
    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, int end, CharSequence name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, CharSequence name, CsmDeclaration.Kind kind) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addDeclaration(CsmOffsetableDeclaration declaration) {
        boolean unnamed = !Utils.canRegisterDeclaration(declaration);
        // allow to register any enum
        if(unnamed && !CsmKindUtilities.isEnum(declaration) ) {
            return;
        }
        
        // TODO: remove this dirty hack!
        if( (declaration instanceof VariableImpl<?>) ) {
            VariableImpl<?> v = (VariableImpl<?>) declaration;
            if( isNamespaceScope(v, isGlobal()) ) {
                v.setScope(this);
            } else {
                return;
            }
        }


        if (unnamed) {
            unnamedDeclarations.add(UIDCsmConverter.declarationToUID(declaration));
        } else {
            getDeclarationsSorage().putDeclaration(declaration);
        }
        
        // update repository
        RepositoryUtils.put(this);

        notify(declaration, NotifyEvent.DECLARATION_ADDED);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> declarationUid;
        if (declaration.getName().length() == 0) {
            declarationUid = UIDs.get(declaration);
            unnamedDeclarations.remove(declarationUid);
        } else {
            getDeclarationsSorage().removeDeclaration(declaration);
        }
        // do not clean repository, it must be done from physical container of declaration
        if (false) { RepositoryUtils.remove(declarationUid, declaration); }
        // update repository
        RepositoryUtils.put(this);
        notify(declaration, NotifyEvent.DECLARATION_REMOVED);
    }
    
    @Override
    public Collection<CsmNamespaceDefinition> getDefinitions()  {
        List<CsmUID<CsmNamespaceDefinition>> uids = new ArrayList<>();
        try {
            nsDefinitionsLock.readLock().lock();
            uids.addAll(nsDefinitions.values());
        } finally {
            nsDefinitionsLock.readLock().unlock();
        }
        Collection<CsmNamespaceDefinition> defs = UIDCsmConverter.UIDsToDeclarations(uids);
        return defs;
    }
    
    public void addNamespaceDefinition(CsmNamespaceDefinition def) {
        CsmUID<CsmNamespaceDefinition> definitionUid = UIDCsmConverter.objectToUID(def);
        boolean add = false;
        try {
            nsDefinitionsLock.writeLock().lock();
            add = nsDefinitions.isEmpty();
            nsDefinitions.put(getSortKey(def), definitionUid);
            inlineDefinitionsCounter += def.isInline() ? 1 : 0;
            boolean becameInline = (def.isInline() && inlineDefinitionsCounter == 1);
            CsmNamespace parentNs = getParent();
            if (parentNs != null && becameInline) {
                ((NamespaceImpl) parentNs).incInlinedNamespacesCounter();
            }
        } finally {
            nsDefinitionsLock.writeLock().unlock();
        }
        // update repository
        RepositoryUtils.put(this);
        if (add){
            addRemoveInParentNamespace(true);
        }
    }
    
    private void addRemoveInParentNamespace(boolean add){
        ProjectBase proj = _getProject();
        assert proj.holdsNamespaceLock() : "Logical namespace can be modified only via ProjectBase!" + //NOI18N
                " project=" + proj + " (platform=" + proj.getPlatformProject() + "); namespace=" + this; //NOI18N
        if (add){
            // add this namespace in the parent namespace
            NamespaceImpl parent = (NamespaceImpl) _getParentNamespace();
            if (parent != null) {
                parent.addNestedNamespace(this);
            }
            proj.registerNamespace(this);
        } else {
            // remove this namespace from the parent namespace
            try {
                nsDefinitionsLock.readLock().lock();
                if (!nsDefinitions.isEmpty()) {
                    // someone already registered in definitions
                    // do not unregister
                    return;
                }
            } finally {
                nsDefinitionsLock.readLock().unlock();
            }
            NamespaceImpl parent = (NamespaceImpl) _getParentNamespace();
            if (parent != null) {
                parent.removeNestedNamespace(this);
            }
            projectRef = proj;
            ((ProjectBase)projectRef).unregisterNamesace(this);
            dispose();            
        }
    }

    public void removeNamespaceDefinition(CsmNamespaceDefinition def) {
        assert !this.isGlobal();
        boolean remove = false;
        CsmUID<CsmNamespaceDefinition> definitionUid = null;
        try {
            nsDefinitionsLock.writeLock().lock();
            definitionUid = nsDefinitions.remove(getSortKey(def));
            remove =  nsDefinitions.isEmpty();
            inlineDefinitionsCounter -= def.isInline() ? 1 : 0;
            boolean becameNotInline = (def.isInline() && inlineDefinitionsCounter == 0);
            CsmNamespace parentNs = getParent();
            if (parentNs != null && becameNotInline) {
                ((NamespaceImpl) parentNs).decInlinedNamespacesCounter();
            }            
        } finally {
            nsDefinitionsLock.writeLock().unlock();
        }
        // does not remove unregistered declaration from repository, it's responsibility of physical container
        if (false) { RepositoryUtils.remove(definitionUid, def); }
        // update repository about itself
        RepositoryUtils.put(this);
        if (remove) {
            addRemoveInParentNamespace(false);
        }
    }
    
    public void addUsingDirective(CsmUsingDirective usingDirective) {
        synchronized (usingDirectives) {
            usingDirectives.add(UIDs.get(usingDirective));
        }
    }
    
    public void removeUsingDirective(CsmUsingDirective usingDirective) {
        synchronized (usingDirectives) {
            usingDirectives.remove(UIDs.get(usingDirective));
        }
    }
    
    public Collection<CsmUID<CsmUsingDirective>> getUsingDirectives() {
        // add all declarations
        Collection<CsmUID<CsmUsingDirective>> uids;
        // add all unnamed declarations
        synchronized (usingDirectives) {
            uids = new ArrayList<>(usingDirectives);
        }
        return uids;
    }    
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return (List) getDeclarations();
    }
    
    @Override
    public CsmProject getProject() {
        return _getProject();
    }
    
    private volatile CsmUID<CsmNamespace> uid = null;
    @Override
    public final CsmUID<CsmNamespace> getUID() {
        CsmUID<CsmNamespace> out = uid;
        if (out == null) {
            synchronized (this) {
                if (uid == null) {
                    uid = createUID();
                }
            }
            return uid;
        }
        return out;
    }   
    
    protected CsmUID<CsmNamespace> createUID() {
	return UIDUtilities.createNamespaceUID(this);
    }

    private ProjectBase _getProject() {
        Object o = projectRef;
        if (o instanceof ProjectBase) {
            return (ProjectBase) o;
        } else if (o instanceof Reference<?>) {
            ProjectBase prj = (ProjectBase)((Reference<?>) o).get();
            if (prj != null) {
                return prj;
            }
        }
        projectLock.readLock().lock();
        try {
            ProjectBase prj = null;
            if (projectRef instanceof ProjectBase) {
                prj = (ProjectBase)projectRef;
            } else if (projectRef instanceof Reference<?>) {
                @SuppressWarnings("unchecked")
                Reference<ProjectBase> ref = (Reference<ProjectBase>) projectRef;
                prj = ref.get();
            }
            if (prj == null) {
                prj = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
                projectRef = new WeakReference<>(prj);
            }
            return prj;
        } finally {
            projectLock.readLock().unlock();
        }
    }
    
    private CsmNamespace _getParentNamespace() {
        projectLock.readLock().lock();
        try {
            CsmNamespace ns = this.parentRef;
            if (ns == null) {
                ns = UIDCsmConverter.UIDtoNamespace(this.parentUID);
                assert (ns != null || this.parentUID == null) : "null object for UID " + this.parentUID;   
            }
            return ns;
        } finally {
            projectLock.readLock().unlock();
        }
    }
    
    private void incInlinedNamespacesCounter() {
        inlineNamespacesCounter.incrementAndGet();
    }
    
    private void decInlinedNamespacesCounter() {
        inlineNamespacesCounter.decrementAndGet();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(' ');
        sb.append(getQualifiedName());
        sb.append(" NamespaceImpl @"); // NOI18N
        sb.append(hashCode());
        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        output.writeBoolean(this.global);
        
        UIDObjectFactory theFactory = UIDObjectFactory.getDefaultFactory();      
        // not null UID
        assert this.projectUID != null;
        theFactory.writeUID(this.projectUID, output);
        // can be null for global ns
        assert !CHECK_PARENT || this.parentUID != null || isGlobal();
        theFactory.writeUID(this.parentUID, output);
        
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        assert this.qualifiedName != null;
        PersistentUtils.writeUTF(qualifiedName, output);

        theFactory.writeStringToUIDMap(this.nestedNamespaces, output, false);
        ProjectComponent.writeKey(this.declarationsSorageKey, output);
        try {
            nsDefinitionsLock.readLock().lock();
            theFactory.writeNameSortedToUIDMap2(this.nsDefinitions, output, false);
        } finally {
            nsDefinitionsLock.readLock().unlock();
        }
        theFactory.writeUIDCollection(this.unnamedDeclarations, output, true);
        theFactory.writeUIDCollection(this.usingDirectives, output, true);
        output.writeInt(inlineDefinitionsCounter);
        output.writeInt(inlineNamespacesCounter.get());
    }
    
    public NamespaceImpl(RepositoryDataInput input) throws IOException {
        this.global = input.readBoolean();
        
        UIDObjectFactory theFactory = UIDObjectFactory.getDefaultFactory();
        
        this.projectUID = theFactory.readUID(input);
        this.parentUID = theFactory.readUID(input);
        // not null UID
        assert this.projectUID != null;
        assert !CHECK_PARENT || this.parentUID != null || this.global;
        this.projectRef = null;
        this.parentRef = null;
       

        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.qualifiedName = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.qualifiedName != null;

        int collSize = input.readInt();
        if (collSize <= 0) {
            nestedNamespaces = new ConcurrentHashMap<>(0);
        } else {
            nestedNamespaces = new ConcurrentHashMap<>(collSize);
        }
        theFactory.readStringToUIDMap(this.nestedNamespaces, input, QualifiedNameCache.getManager(), collSize);
        declarationsSorageKey = ProjectComponent.readKey(input);
        assert declarationsSorageKey != null : "declarationsSorageKey can not be null";

        this.nsDefinitions = theFactory.readNameSortedToUIDMap2(input, null);

        collSize = input.readInt();
        if (collSize < 0) {
            unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>(0));
        } else {
            unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>(collSize));
        }
        theFactory.readUIDCollection(this.unnamedDeclarations, input, collSize);
        collSize = input.readInt();
        if (collSize < 0) {
            usingDirectives = Collections.synchronizedSet(new HashSet<CsmUID<CsmUsingDirective>>(0));
        } else {
            usingDirectives = Collections.synchronizedSet(new HashSet<CsmUID<CsmUsingDirective>>(collSize));
        }        
        theFactory.readUIDCollection(this.usingDirectives, input, collSize);
        inlineDefinitionsCounter = input.readInt();
        inlineNamespacesCounter = new AtomicInteger(input.readInt());
    }

    private static FileNameSortedKey getSortKey(CsmNamespaceDefinition def) {
        return new FileNameSortedKey(def);
    }

    public static final Comparator<FileNameSortedKey> defenitionComparator = new Comparator<FileNameSortedKey>() {
        @Override
        public int compare(FileNameSortedKey o1, FileNameSortedKey o2) {
            return o1.compareTo(o2);
        }
    };

    public static class FileNameSortedKey implements Comparable<FileNameSortedKey>, Persistent, SelfPersistent {
        private final int start;
        private final int fileIndex;
        private FileNameSortedKey(CsmNamespaceDefinition def) {
            this(UIDUtilities.getFileID(((FileImpl)def.getContainingFile()).getUID()), def.getStartOffset());
        }
        private FileNameSortedKey(int fileIndex, int start) {
            this.start = start;
            this.fileIndex = fileIndex;
        }
        @Override
        public int compareTo(FileNameSortedKey o) {
            int res = fileIndex - o.fileIndex;
            if (res == 0) {
                res = start - o.start;
            }
            return res;
        }
        @Override public boolean equals(Object obj) {
            if (obj instanceof FileNameSortedKey) {
                FileNameSortedKey key = (FileNameSortedKey) obj;
                return compareTo(key)==0;
            }
            return false;
        }
        @Override public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.start;
            hash = 37 * hash + this.fileIndex;
            return hash;
        }
        @Override public String toString() {
            return "FileNameSortedKey: " + this.fileIndex + "[" + this.start; // NOI18N
        }
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            output.writeInt(start);
            output.writeInt(fileIndex);
        }
        public FileNameSortedKey(RepositoryDataInput input) throws IOException {
            start = input.readInt();
            fileIndex = input.readInt();
        }
    }
}
