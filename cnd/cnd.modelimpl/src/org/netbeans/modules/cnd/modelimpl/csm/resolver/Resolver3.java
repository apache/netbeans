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

package org.netbeans.modules.cnd.modelimpl.csm.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmCacheMap;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import static org.netbeans.modules.cnd.apt.utils.APTUtils.SCOPE;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardEnum;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.impl.services.BaseUtilitiesProviderImpl;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MutableObject;
import org.openide.util.CharSequences;

/**
 */
public final class Resolver3 implements Resolver {
    static final Logger LOGGER = Logger.getLogger("Resolver3"); // NOI18N
    
    private final ProjectBase project;
    private final CsmFile file;
    private final CsmFile startFile;
    private final int origOffset;
    private Resolver parentResolver;

    private final Map<CharSequence, CsmClassifier> currUsedClassifiers = new HashMap<>();

    private CsmClassifier currLocalClassifier;
    private boolean currDone = false;

    private CharSequence[] names;
    private int currNamIdx;
    private int interestedKind;
    private boolean resolveInBaseClass;
    private final boolean SUPRESS_RECURSION_EXCEPTION = Boolean.getBoolean("cnd.modelimpl.resolver3.hide.exception"); // NOI18N
    private final boolean SHOW_EMPTY_NAME_WARNING = Boolean.getBoolean("cnd.modelimpl.resolver3.show.empty_name_warning"); // NOI18N

    private CharSequence currName() {
        return (names != null && currNamIdx < names.length) ? names[currNamIdx] : CharSequences.empty();
    }

    private final Context context;
    private final FileMapsCollector fileMaps;

    //private CsmNamespace currentNamespace;

    /**
     * should be created by ResolverFactory only
     * @param file file where object to be resolved is located
     * @param offset offset where object to be resolved is located 
     * @param parent parent resolver (can be null)
     * @param startFile start file where resolving started, it affects which objects considered as visible or not while resolving name at (file, offset)
     */
    /*package*/ Resolver3(CsmFile file, int offset, Resolver parent, CsmFile startFile) {
        this.file = file;
        this.origOffset = offset;
        parentResolver = parent;
        this.project = (ProjectBase) file.getProject();
        this.startFile = startFile;
        context = new Context(file, origOffset, this);
        fileMaps = new FileMapsCollector(file, startFile, origOffset);
    }

    private Resolver3(CsmFile file, int offset, Resolver parent) {
        this(file, offset, parent, (parent == null) ? file : parent.getStartFile());
    }

    private void initFileMaps() {
        FindCurrLocalClassifier cb = new FindCurrLocalClassifier();
        fileMaps.initFileMaps(needClassifiers(), cb);
    }
    
    private CsmClassifier findClassifier(CsmNamespace ns, CharSequence qualifiedNamePart, AtomicBoolean outVisibility) {
        outVisibility.set(false);
        CsmClassifier backupResult = null;
        while ( ns != null) {
            String fqn = ns.getQualifiedName() + "::" + qualifiedNamePart; // NOI18N
            CsmClassifier aCls = findClassifierUsedInFile(fqn, outVisibility);
            if (aCls != null) {
                if (!ForwardClass.isForwardClass(aCls) || needForwardClassesOnly()) {
                    if (outVisibility.get()) {
                        return aCls;
                    }
                }
                if (backupResult == null) {
                    backupResult = aCls;
                }
            }
            ns = ns.getParent();
        }
        return backupResult;
    }

    private CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName, AtomicBoolean resultIsVisible) {
        resultIsVisible.set(false);
        // try to find visible classifier
        CsmClassifier result = null;
        final CharSequence id = CharSequences.create(qualifiedName);
        CsmClassifier globalResult = CsmClassifierResolver.getDefault().findClassifierUsedInFile(id, getStartFile(), needClasses());
        // first of all - check local context
        if (!currDone) {
            currLocalClassifier = null;
            fileMaps.initMapsFromCurrentFileOnly(needClassifiers(), origOffset, new FindCurrLocalClassifier());
            currDone = true;
        }
        if (currLocalClassifier != null && needClassifiers()) {
            result = currLocalClassifier;
            resultIsVisible.set(true);
        }
        if (result == null) {
            if (currUsedClassifiers.containsKey(id)) {
                result = currUsedClassifiers.get(id);
            } else {
                result = globalResult;
                if (isObjectVisibleFromFile(result, startFile)) {
                    resultIsVisible.set(true);
                    currUsedClassifiers.put(id, result);
                }
            }
        }
        return result;
    }

    @Override
    public CsmFile getStartFile() {
        return startFile;
    }
    
    private CsmNamespace findNamespace(CsmNamespace ns, CharSequence qualifiedNamePart) {
        CsmNamespace result = null;
        if (ns == null) {
            result = findNamespace(qualifiedNamePart);
        } else {
            CsmNamespace containingNs = ns;
            while (containingNs != null && result == null) {
                String fqn = (containingNs.isGlobal() ? "" : (containingNs.getQualifiedName() + "::")) + qualifiedNamePart; // NOI18N
                result = findNamespace(fqn);
                if (result == null) {
                    result = findNamespaceInInlined(containingNs, qualifiedNamePart);
                }
                containingNs = containingNs.getParent();
            }
        }
        return result;
    }
    
    private CsmNamespace findNamespaceInInlined(CsmNamespace ns, CharSequence qualifiedNamePart) {
        CsmNamespace result = null;
        if (ns != null) {
            for (CsmNamespace inlinedNs : ns.getInlinedNamespaces()) {
                String fqn = (inlinedNs.isGlobal() ? "" : (inlinedNs.getQualifiedName() + "::")) + qualifiedNamePart; // NOI18N
                result = findNamespace(fqn);
                if (result == null) {
                    result = findNamespaceInInlined(inlinedNs, qualifiedNamePart);
                }
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    private CsmNamespace findNamespace(CharSequence qualifiedName) {
        CsmNamespace result = project.findNamespace(qualifiedName);
        if( result == null ) {
            for (Iterator<CsmProject> iter = getLibraries().iterator(); iter.hasNext() && result == null;) {
                CsmProject lib = iter.next();
                result = lib.findNamespace(qualifiedName);
            }
        }
        return result;
    }

    @Override
    public Collection<CsmProject> getLibraries() {
        return getSearchLibraries(this.startFile.getProject());
    }

    public static Collection<CsmProject> getSearchLibraries(CsmProject prj) {
        if (prj.isArtificial() && prj instanceof ProjectBase) {
            Set<CsmProject> libs = new HashSet<>();
            for (ProjectBase projectBase : ((ProjectBase)prj).getDependentProjects()) {
                if (!projectBase.isArtificial()) {
                    libs.addAll(projectBase.getLibraries());
                }
            }
            return libs;
        } else {
            return prj.getLibraries();
        }
    }

    @Override
    public CsmClassifier getOriginalClassifier(CsmClassifier orig) {
        Object cacheKey = new OrigClassifierCacheKey(this.origOffset, this.file, this.startFile, orig);
        CsmCacheMap origClassifiersCache = CsmCacheManager.getClientCache(OrigClassifierCacheKey.class, ORIG_CLASSIFIER_CACHE_INITIALIZER);
        CsmClassifier out = (CsmClassifier) getFromCache(origClassifiersCache, cacheKey);
        if (out == null) {
            long time = System.currentTimeMillis();
            out = getOriginalClassifierImpl(orig);
            time = System.currentTimeMillis() - time;
            if (origClassifiersCache != null) {
                origClassifiersCache.put(cacheKey, CsmCacheMap.toValue(out, time));
            }
        }
        return out;
    }
    
    private CsmClassifier getOriginalClassifierImpl(CsmClassifier orig) {
        if (isRecursionOnResolving(INFINITE_RECURSION)) {
            return null;
        }
        ClassifiersAntiLoop set = new ClassifiersAntiLoop(100);
        while (true) {
            set.add(orig);
            CsmClassifier resovedClassifier;
            if (CsmKindUtilities.isClassForwardDeclaration(orig)){
                CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) orig;
                resovedClassifier = fd.getCsmClass();
                if (resovedClassifier == null){
                    break;
                }
            } else if (CsmKindUtilities.isEnumForwardDeclaration(orig)) {
                CsmEnumForwardDeclaration fd = (CsmEnumForwardDeclaration) orig;
                resovedClassifier = fd.getCsmEnum();
                if (resovedClassifier == null) {
                    break;
                } 
            } else if (CsmKindUtilities.isTypedef(orig) || CsmKindUtilities.isTypeAlias(orig)) {
                CsmType t = ((CsmTypedef)orig).getType();
                resovedClassifier = t.getClassifier();
                if (CsmBaseUtilities.isUnresolved(resovedClassifier) && CsmExpressionResolver.shouldResolveAsMacroType(t, orig.getScope())) {
                    CsmType resolvedMacroType = CsmExpressionResolver.resolveMacroType(
                        t, 
                        orig.getScope(), 
                        Instantiation.getInstantiatedTypeInstantiations(t),
                        null
                    );
                    if (resolvedMacroType != null) {
                        resovedClassifier = resolvedMacroType.getClassifier();
                    }
                }
                if (resovedClassifier == null) {
                    // have to stop with current 'orig' value
                    break;
                }
            } else if (ForwardClass.isForwardClass(orig) || ForwardEnum.isForwardEnum(orig)) {
                // try to find another classifier
                AtomicBoolean resultIsVisible = new AtomicBoolean(false);
                resovedClassifier = findClassifierUsedInFile(orig.getQualifiedName(), resultIsVisible);
            } else {
                break;
            }
            if (set.contains(resovedClassifier)) {
                // try to recover from this error
                resovedClassifier = findOtherClassifier(orig);
                if (resovedClassifier == null || set.contains(resovedClassifier)) {
                    // have to stop with current 'orig' value
                    break;
                }
            }
            orig = resovedClassifier;
        }
        return orig;

    }

    private CsmClassifier findOtherClassifier(CsmClassifier out) {
        CsmNamespace ns = BaseUtilitiesProviderImpl.getImpl()._getClassNamespace(out);
        CsmClassifier cls = null;
        if (ns != null) {
            CsmUID<?> uid = UIDs.get(out);
            CharSequence fqn = out.getQualifiedName();
            Collection<CsmOffsetableDeclaration> col;
            if (ns instanceof NamespaceImpl) {
                col = ((NamespaceImpl)ns).getDeclarationsRange(fqn,
                        new Kind[]{Kind.CLASS, Kind.UNION, Kind.STRUCT, Kind.ENUM, Kind.TYPEDEF, Kind.TYPEALIAS,
                            Kind.TEMPLATE_DECLARATION, Kind.TEMPLATE_SPECIALIZATION,
                            Kind.CLASS_FORWARD_DECLARATION, Kind.ENUM_FORWARD_DECLARATION});

            } else {
                col = ns.getDeclarations();
            }
            for (CsmDeclaration decl : col) {
                if (CsmKindUtilities.isClassifier(decl) && decl.getQualifiedName().equals(fqn)) {
                    if (!UIDs.get(decl).equals(uid)) {
                        cls = (CsmClassifier)decl;
                        if (!ForwardClass.isForwardClass(cls)) {
                            break;
                        }
                    }
                }
            }
        }
        return cls;
    }

    @Override
    public boolean isRecursionOnResolving(int maxRecursion) {
        Resolver3 parent = (Resolver3)parentResolver;
        int count = 0;
        int similarCount = 0;
        while(parent != null) {
            if (parent.origOffset == origOffset && parent.file.equals(file)) {
                similarCount++;
                if (similarCount > LIMITED_RECURSION) {
                    if (TRACE_RECURSION) { traceRecursion(); }
                    return true;
                }
            }
            parent = (Resolver3) parent.parentResolver;
            count++;
            if (count > maxRecursion) {
                if (TRACE_RECURSION) { traceRecursion(); }
                return true;
            }
        }
        return false;
    }

    private CsmObject resolveInUsingDirectives(CsmNamespace containingNS, CharSequence nameToken, AtomicBoolean outVisibility) {
        CsmObject result = null;
        Set<CharSequence> checked = new HashSet<>(10);
        for (CsmUsingDirective udir : CsmUsingResolver.getDefault().findUsingDirectives(containingNS)) {
            final CharSequence name = udir.getName();
            if (checked.add(name)) {
                String fqn = name + "::" + nameToken; // NOI18N
                if(fqn.startsWith("::")) { // NOI18N
                    fqn = fqn.substring(2);
                }
                result = findClassifierUsedInFile(fqn, outVisibility);
                if (result != null && outVisibility.get()) {
                    break;
                } else {
                    CsmNamespace refNs = udir.getReferencedNamespace();
                    if (refNs != null) {
                        fqn = new StringBuilder(refNs.getQualifiedName())
                            .append(SCOPE)
                            .append(nameToken).toString();
                        result = findClassifierUsedInFile(fqn, outVisibility);
                        if (result != null && outVisibility.get()) {
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private CsmObject resolveInUsingDeclarations(CsmObject result, CsmNamespace containingNS, CharSequence nameToken, AtomicBoolean outVisibility) {
        if (result == null || !outVisibility.get()) {
            MutableObject<CsmObject> usedDecl = new MutableObject<>();
            while (containingNS != null) { 
                Collection<CsmDeclaration> decls = CsmUsingResolver.getDefault().findUsedDeclarations(containingNS, nameToken);//, nameToken);
                if (resolveInUsingDeclarations(decls, nameToken, usedDecl)) {
                    return usedDecl.value;
                }
                for (CsmProject library : project.getLibraries()) {
                    CsmNamespace libNs = library.findNamespace(containingNS.getQualifiedName());
                    if (libNs != null) {
                        decls = CsmUsingResolver.getDefault().findUsedDeclarations(libNs, nameToken);
                        if (resolveInUsingDeclarations(decls, nameToken, usedDecl)) {
                            return usedDecl.value;
                        }
                    }
                }
                containingNS = containingNS.getParent();
            }
        }
        return result;
    }
    
    private boolean resolveInUsingDeclarations(Collection<CsmDeclaration> decls, CharSequence nameToken, MutableObject<CsmObject> usedDecl) {
        for (CsmDeclaration decl : decls) {
            if (CharSequences.comparator().compare(nameToken, decl.getName()) == 0) {
                if (CsmKindUtilities.isClassifier(decl) && needClassifiers()) {
                    usedDecl.value = decl;
                    return true;
                } else if (CsmKindUtilities.isClass(decl) && needClasses()) {
                    usedDecl.value = decl;
                    return true;
                }
            }
        }
        return false;
    }
    
    private CsmObject resolveInInlinedNamespaces(CsmObject result, CsmNamespace namespace, CharSequence nameToken, AtomicBoolean outVisibility) {
        if (result == null || !outVisibility.get()) {
            Collection<CsmNamespace> inlinedNamespaces = CsmBaseUtilities.getInlinedNamespaces(namespace, getLibraries());
            for (CsmNamespace ns : inlinedNamespaces) {
                CsmObject fromInlined = resolveInInlinedNamespace(ns, nameToken, outVisibility);
                if (fromInlined != null && outVisibility.get()) {
                    return fromInlined;
                }
            }
        }
        return result;
    }   
    
    private CsmObject resolveInInlinedNamespace(CsmNamespace ns, CharSequence nameToken, AtomicBoolean outVisibility) {
        final CharSequence name = ns.getQualifiedName();
        String fqn = name + "::" + nameToken; // NOI18N
        if (fqn.startsWith("::")) { // NOI18N
            fqn = fqn.substring(2);
        }
        CsmObject result = findClassifierUsedInFile(fqn, outVisibility);
        result = resolveInInlinedNamespaces(result, ns, nameToken, outVisibility);
        return result;
    }

    void traceRecursion(){
        System.out.println("Detected recursion in resolver:"); // NOI18N
        System.out.println("\t"+this); // NOI18Nv
        Resolver3 parent = (Resolver3)parentResolver;
        while(parent != null) {
            System.out.println("\t"+parent); // NOI18N
            parent = (Resolver3) parent.parentResolver;
        }
        new Exception().printStackTrace(System.err);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(file.getAbsolutePath()).append(":").append(origOffset); // NOI18N
        buf.append(":Looking for "); // NOI18N
        if (needClassifiers()) {
            if (needClasses()) {
                buf.append("c"); // NOI18N
            } else {
                buf.append("C"); // NOI18N
            }
        }
        if (needNamespaces()) {
            buf.append("N"); // NOI18N
        }
        buf.append(":").append(currName()); // NOI18N
        if (names != null) {
            for(int i = 0; i < names.length; i++){
                if (i == 0) {
                    buf.append("?"); // NOI18N
                } else {
                    buf.append("::"); // NOI18N
                }
                buf.append(names[i]); // NOI18N
            }
        }

        if (context.getContainingClass() != null) {
            buf.append(":Class=").append(context.getContainingClass().getName()); // NOI18N
        }
        if (context.getContainingNamespace() != null) {
            buf.append(":NS=").append(context.getContainingNamespace().getName()); // NOI18N
        }
        return buf.toString();
    }
    
    private CsmClassifier findNestedClassifier(CsmClassifier clazz) {
        if (CsmKindUtilities.isClass(clazz)) {
            Iterator<CsmMember> it = CsmSelect.getClassMembers((CsmClass)clazz,
                    CsmSelect.getFilterBuilder().createNameFilter(currName(), true, true, false));
            while(it.hasNext()) {
                CsmMember member = it.next();
                if( CharSequences.comparator().compare(currName(),member.getName())==0 ) {
                    if(CsmKindUtilities.isClassifier(member)) {
                        return (CsmClassifier) member;
                    }
                }
            }
        }
        return null;
    }

    private boolean isInContext(CsmScope scope) {
        if (!CsmKindUtilities.isClass(scope) && !CsmKindUtilities.isNamespace(scope)) {
            return false;
        }
        CsmQualifiedNamedElement el = (CsmQualifiedNamedElement)scope;
        CsmNamespace ns = context.getContainingNamespace();
        if (ns != null && startsWith(ns.getQualifiedName(), el.getQualifiedName())) {
            return true;
        }
        CsmClass cls = context.getContainingClass();
        if (cls != null && startsWith(cls.getQualifiedName(), el.getQualifiedName())) {
            return true;
        }
        return false;
    }

    private boolean startsWith(CharSequence qname, CharSequence prefix) {
        if (qname.length() < prefix.length()) {
            return false;
        }
        for (int i = 0; i < prefix.length(); ++i) {
            if (qname.charAt(i) != prefix.charAt(i)) {
                return false;
            }
        }
        return qname.length() == prefix.length()
                || qname.charAt(prefix.length()) == ':'; // NOI18N
    }
    /**
     * Resolver class or namespace name.
     * Why class or namespace? Because in usage of kind org::vk::test
     * you don't know which is class and which is namespace name
     *
     * @param nameTokens tokenized name to resolve
     * (for example, for std::vector it is new CharSequence[] { "std", "vector" })
     *
     * @param context declaration within which the name found
     *
     * @return object of the following class:
     *  CsmClass
     *  CsmEnum
     *  CsmNamespace
     */
    @Override
    public CsmObject resolve(CharSequence[] nameTokens, int interestedKind) {
        if (nameTokens == null || nameTokens.length == 0) {
            String position = file.getAbsolutePath().toString();
            if (file instanceof FileImpl) {
                int[] lineColumn = ((FileImpl)file).getLineColumn(origOffset);
                if (lineColumn != null) {
                    position = "line=" + lineColumn[0] + ":" + lineColumn[1] + position; // NOI18N
                }
            }
            if (SHOW_EMPTY_NAME_WARNING) {
                CndUtils.assertTrueInConsole(false, "no names are passed to resolve at " + position); // NOI18N
            }
            return null;
        }
        long time = System.currentTimeMillis();
        names = nameTokens;
        currNamIdx = 0;
        this.interestedKind = interestedKind;
        String fullName = fullName(nameTokens);        
        Object cacheKey = new NameResolveCacheKey(fullName, this.origOffset, this.startFile, this.file, interestedKind);
        CsmCacheMap nameResolverCache = CsmCacheManager.getClientCache(NameResolveCacheKey.class, NAME_CACHE_INITIALIZER);
        CsmObject result = (CsmObject) getFromCache(nameResolverCache, cacheKey);
        if (result == null) {
            if( nameTokens.length == 1 ) {
                result = resolveSimpleName(result, nameTokens[0], interestedKind);
            } else if( nameTokens.length > 1 ) {
                result = resolveCompoundName(nameTokens, result, interestedKind);
            }
            time = System.currentTimeMillis() - time;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "RESOLVE {0} ({1}) at {2} Took {3}ms\n", new Object[]{fullName, interestedKind, origOffset, time});
            }
            if (nameResolverCache != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "KEEP NEW RESOLVED {0} ({1}) at {2}[{4}] Took {3}ms=>{5}\n", new Object[]{fullName, interestedKind, origOffset, time, file.getName(), result});
                }
                nameResolverCache.put(cacheKey, CsmCacheMap.toValue(result, time));
            }
        }
        return result;
    }

    private static Object getFromCache(CsmCacheMap cache, Object cacheKey) {
        Object result = null;
        CsmCacheMap.Value cacheValue = null;
        if (cache != null) {
            cacheValue = cache.get(cacheKey);
        }
        if (cacheValue != null) {
            result = cacheValue.getResult();
        }
        return result;
    }
    
    private CsmObject resolveSimpleName(CsmObject result, CharSequence name, int interestedKind) {
        CsmNamespace containingNS = null;
        if (result == null && needClassifiers()) {
            CsmClass cls = context.getContainingClass();
            result = resolveInClass(cls, name);
            if (result == null) {
                if (parentResolver == null || !((Resolver3) parentResolver).resolveInBaseClass) {
                    result = resolveInBaseClasses(cls, name);
                    if(needTemplateClassesOnly() && !CsmKindUtilities.isTemplate(result)) {
                        result = null;
                    }
                }
            }
        }
        CsmObject[] backupResult = new CsmObject[] {null};
        AtomicBoolean resultIsVisible = new AtomicBoolean(true);
        if (result == null && needClassifiers()) {
            containingNS = context.getContainingNamespace();
            result = findClassifier(containingNS, name, resultIsVisible);
            if (!canStop(result, resultIsVisible, backupResult) && containingNS != null) {
                result = resolveInUsingDirectives(containingNS, name, resultIsVisible);
                result = resolveInInlinedNamespaces(result, containingNS, name, resultIsVisible);
                result = resolveInUsingDeclarations(result, containingNS, name, resultIsVisible);
            }
        }
        if (result == null && needNamespaces()) {
            resultIsVisible.set(true);
            containingNS = context.getContainingNamespace();
            result = findNamespace(containingNS, name);
        }
        if (needClassifiers() && !canStop(result, resultIsVisible, backupResult)) {
            CsmObject oldResult = result;
            result = findClassifierUsedInFile(name, resultIsVisible);
            if(needTemplateClassesOnly() && !CsmKindUtilities.isTemplate(result)) {
                result = null;
            }
            if (result == null) {
                result = oldResult;
            }
        }
        if (!canStop(result, resultIsVisible, backupResult)) {
            // try to find visible class or not forward
            result = null;
        }
        if (result == null) {
            initFileMaps();
            if (currLocalClassifier != null && needClassifiers()) {
                resultIsVisible.set(isObjectVisibleFromFile(currLocalClassifier, startFile));
                result = currLocalClassifier;
            }
            if (result == null) {
                CsmDeclaration decl = fileMaps.getUsingDeclaration(name);
                resultIsVisible.set(isObjectVisibleFromFile(decl, startFile));
                if (canStop(decl, resultIsVisible, backupResult)) {
                    result = decl;
                }
            }
            if (result == null && needClassifiers()) {
                for (Map.Entry<CharSequence, CsmObject> entry : fileMaps.getUsedNamespaces().entrySet()) {
                    String nsp = entry.getKey().toString();
                    String fqn = nsp + "::" + name; // NOI18N
                    result = findClassifierUsedInFile(fqn, resultIsVisible);
                    if (!canStop(result, resultIsVisible, backupResult)) {
                        result = findClassifier(containingNS, fqn, resultIsVisible);
                    }
                    if (!canStop(result, resultIsVisible, backupResult)) {
                        CsmObject val = entry.getValue();
                        if (CsmKindUtilities.isUsingDirective(val)) {
                            // replace using namespace by referenced namespace
                            val = ((CsmUsingDirective)val).getReferencedNamespace();
                            entry.setValue((CsmNamespace) val);
                            fileMaps.rememberResolvedUsing(entry.getKey(), (CsmNamespace) val);
                        }
                        if (val == null) {
                            val = findNamespace(nsp);
                            entry.setValue((CsmNamespace) val);
                            fileMaps.rememberResolvedUsing(entry.getKey(), (CsmNamespace) val);
                        }
                        if (CsmKindUtilities.isNamespace(val)) {
                            CsmNamespace ns = (CsmNamespace)val;
                            if (!nsp.contains(ns.getQualifiedName())) {
                                fqn = ns.getQualifiedName().toString() + "::" + name; // NOI18N
                                result = findClassifierUsedInFile(fqn, resultIsVisible);
                            }
                            if (!canStop(result, resultIsVisible, backupResult)) {
                                result = resolveInUsingDirectives(ns, name, resultIsVisible);
                                result = resolveInInlinedNamespaces(result, ns, name, resultIsVisible);
                                result = resolveInUsingDeclarations(result, ns, name, resultIsVisible);
                            }
                        }
                    }
                    if (canStop(result, resultIsVisible, backupResult)) {
                        break;
                    }
                }
            }
            if (result == null && needNamespaces()) {
                Object o = fileMaps.getNamespaceAlias(name);
                if (CsmKindUtilities.isNamespace(o)) {
                    result = (CsmNamespace) o;
                }
            }
            if (result == null && needNamespaces()) {
                for (Map.Entry<CharSequence, CsmObject> entry : fileMaps.getUsedNamespaces().entrySet()) {
                    String nsp = entry.getKey().toString();
                    String fqn = nsp + "::" + name; // NOI18N
                    result = findNamespace(fqn);
                    if (result != null) {
                        break;
                    } else {
                        CsmObject val = entry.getValue();
                        if (CsmKindUtilities.isUsingDirective(val)) {
                            // replace using namespace by referenced namespace
                            val = ((CsmUsingDirective) val).getReferencedNamespace();
                            entry.setValue((CsmNamespace) val);
                            fileMaps.rememberResolvedUsing(entry.getKey(), (CsmNamespace) val);
                            if (val != null) {
                                Collection<CsmNamespaceAlias> aliases = CsmUsingResolver.getDefault().findNamespaceAliases((CsmNamespace)val);
                                for (CsmNamespaceAlias alias : aliases) {
                                    if (alias.getAlias().toString().equals(name.toString())) {
                                        result = alias.getReferencedNamespace();
                                        break;
                                    }
                                }
                            }
                            if (result != null) {
                                break;
                            }
                        }
                    }
                }
            }
        }   
        if (result == null) {
            if (!needForwardClassesOnly() && 
                (CsmKindUtilities.isClassForwardDeclaration(backupResult[0])
                 || ForwardClass.isForwardClass(backupResult[0]))) {
                result = findClassifierUsedInFile(((CsmClassifier) backupResult[0]).getQualifiedName(), resultIsVisible);
            }
            if (result == null) {
                result = backupResult[0];
            }
        }
        if (result == null) {
            if (TemplateUtils.isTemplateQualifiedName(name.toString())) {
                Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
                try {
                    result = aResolver.resolve(Utils.splitQualifiedName(TemplateUtils.getTemplateQualifiedNameWithoutSiffix(name.toString())), TEMPLATE_CLASS);
                } finally {
                    ResolverFactory.releaseResolver(aResolver);
                }
            }
        }
        if(needTemplateClassesOnly() && !CsmKindUtilities.isTemplate(result)) {
            result = null;
        }
        if (result == null && needClassifiers() && !needForwardClassesOnly()) {
            result = resolve(Utils.splitQualifiedName(name.toString()), CLASS_FORWARD);
        }
        if(needForwardClassesOnly() && !CsmKindUtilities.isClassForwardDeclaration(result)) {
            result = null;
        }
        return result;
    }

    private String fullName(CharSequence[] nameTokens) {
        StringBuilder sb = new StringBuilder(nameTokens[0]);
        for (int i = 1; i < nameTokens.length; i++) {
            sb.append("::"); // NOI18N
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }

    private CsmObject resolveCompoundName(CharSequence[] nameTokens, CsmObject result, int interestedKind) {
        CsmNamespace containingNS;
        String fullName = fullName(nameTokens);
        CsmObject[] backupResult = new CsmObject[] {null};
        AtomicBoolean resultIsVisible = new AtomicBoolean(true);
        if (needClassifiers()) {
            result = findClassifierUsedInFile(fullName, resultIsVisible);
        }
        if (!canStop(result, resultIsVisible, backupResult) && needClassifiers()) {
            containingNS = context.getContainingNamespace();
            result = findClassifier(containingNS, fullName, resultIsVisible);
        }
        if (!canStop(result, resultIsVisible, backupResult) && needNamespaces()) {
            containingNS = context.getContainingNamespace();
            result = findNamespace(containingNS, fullName);
        }
        if (!canStop(result, resultIsVisible, backupResult) && needClassifiers()) {
            initFileMaps();
            if (currLocalClassifier != null && (CsmKindUtilities.isTypedef(currLocalClassifier) || CsmKindUtilities.isTypeAlias(currLocalClassifier))) {
                CsmType type = ((CsmTypedef)currLocalClassifier).getType();
                if (type != null) {
                    CsmClassifier currentClassifier = getTypeClassifier(type);
                    while (currNamIdx < names.length - 1 && currentClassifier != null) {
                        currNamIdx++;
                        currentClassifier = findNestedClassifier(currentClassifier);
                        if (CsmKindUtilities.isTypedef(currentClassifier) || CsmKindUtilities.isTypeAlias(currentClassifier)) {
                            CsmType curType = ((CsmTypedef) currentClassifier).getType();
                            currentClassifier = curType == null ? null : getTypeClassifier(curType);
                        }
                    }
                    if (currNamIdx == names.length - 1) {
                        result = currentClassifier;
                        resultIsVisible.set(isObjectVisibleFromFile(result, startFile));
                    }
                }
            }
            if (!canStop(result, resultIsVisible, backupResult)) {
                for (CharSequence nsp : fileMaps.getUsedNamespaces().keySet()) {
                    String fqn = nsp + "::" + fullName; // NOI18N
                    result = findClassifierUsedInFile(fqn, resultIsVisible);
                    if (canStop(result, resultIsVisible, backupResult)) {
                        break;
                    }
                }
            }
            if (!canStop(result, resultIsVisible, backupResult)) {
                CsmNamespace ns = null;
                String nsName = nameTokens[0].toString(); // NOI18N
                int i;
                for (i = 1; i < nameTokens.length; i++) {
                    CsmObject nsObj = null;
                    Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
                    try {
                        nsObj = aResolver.resolve(Utils.splitQualifiedName(nsName), NAMESPACE);
                    } finally {
                        ResolverFactory.releaseResolver(aResolver);
                    }                    
                    if (CsmKindUtilities.isNamespace(nsObj)) {
                        ns = (CsmNamespace)nsObj;                                            
                        CharSequence token = nameTokens[i];
                        nsName = ns.getQualifiedName() + "::" + token; // NOI18N
                    } else {
                        break;
                    }
                }
                i--;
                if (ns != null) {
                    StringBuilder sb = new StringBuilder(ns.getQualifiedName());
                    for (int j = i; j < nameTokens.length; j++) {
                        sb.append("::"); // NOI18N
                        sb.append(nameTokens[j]);
                    }
                    result = findClassifierUsedInFile(sb.toString(), resultIsVisible);
                    if (!canStop(result, resultIsVisible, backupResult)) {
                        sb = new StringBuilder(nameTokens[i]);
                        for (int j = i + 1; j < nameTokens.length; j++) {
                            sb.append("::"); // NOI18N
                            sb.append(nameTokens[j]);
                        }
                        result = resolveInUsingDirectives(ns, sb, resultIsVisible);
                        result = resolveInInlinedNamespaces(result, ns, sb, resultIsVisible);
                        result = resolveInUsingDeclarations(result, ns, sb, resultIsVisible);
                    }
                }
            }
            if (result == null) {
                result = backupResult[0];
            }
        }
        if (!canStop(result, resultIsVisible, backupResult) && needNamespaces()) {
            CsmObject obj = null;
            Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
            try {
                obj = aResolver.resolve(Utils.splitQualifiedName(nameTokens[0].toString()), NAMESPACE);
            } finally {
                ResolverFactory.releaseResolver(aResolver);
            }
            if (CsmKindUtilities.isNamespace(obj)) {
                CsmNamespace ns = (CsmNamespace) obj;
                for (int i = 1; i < nameTokens.length; i++) {
                    CsmNamespace newNs = null;
                    CharSequence name = nameTokens[i];
                    Collection<CsmNamespaceAlias> aliases = CsmUsingResolver.getDefault().findNamespaceAliases(ns);
                    for (CsmNamespaceAlias alias : aliases) {
                        if (alias.getAlias().toString().equals(name.toString())) {
                            newNs = alias.getReferencedNamespace();
                            break;
                        }
                    }
                    if (newNs == null) {
                        Collection<CsmNamespace> namespaces = ns.getNestedNamespaces();
                        for (CsmNamespace namespace : namespaces) {
                            if (namespace.getName().toString().equals(name.toString())) {
                                newNs = namespace;
                                break;
                            }
                        }
                    }
                    ns = newNs;
                    if (ns == null) {
                        break;
                    }
                }
                result = ns;
            }
        }
        if (result == null) {
            if (TemplateUtils.isTemplateQualifiedName(fullName)) {
                StringBuilder sb2 = new StringBuilder(TemplateUtils.getTemplateQualifiedNameWithoutSiffix(nameTokens[0].toString()));
                for (int i = 1; i < nameTokens.length; i++) {
                    sb2.append("::"); // NOI18N
                    sb2.append(TemplateUtils.getTemplateQualifiedNameWithoutSiffix(nameTokens[i].toString()));
                }
                Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
                try {
                    result = aResolver.resolve(Utils.splitQualifiedName(sb2.toString()), interestedKind);
                } finally {
                    ResolverFactory.releaseResolver(aResolver);
                }
            }
        }
        if (result == null && needClassifiers() && !needForwardClassesOnly()) {
            result = resolve(nameTokens, CLASS_FORWARD);
        }
        if(needForwardClassesOnly() && !CsmKindUtilities.isClassForwardDeclaration(result)) {
            result = null;
        }        
        return result;
    }

    private CsmClassifier getTypeClassifier(CsmType type){
        if (isRecursionOnResolving(INFINITE_RECURSION)) {
            return null;
        }
        return type.getClassifier();
    }

    private CsmObject resolveInBaseClasses(CsmClass cls, CharSequence name) {
        resolveInBaseClass = true;
        CsmObject res = _resolveInBaseClasses(cls, name, new HashSet<CharSequence>(), 0);
        resolveInBaseClass = false;
        return res;
    }

    private CsmObject _resolveInBaseClasses(CsmClass cls, CharSequence name, Set<CharSequence> antiLoop, int depth) {
        if (depth == 50) {
            String msg = "Recursion in resolver3:resolveInBaseClasses[" + name + "]" + this.file.getAbsolutePath() + ":" + this.origOffset; // NOI18N
            if (SUPRESS_RECURSION_EXCEPTION) {
                Utils.LOG.warning(msg);
            } else {
                new Exception(msg).printStackTrace(System.err);
            }
            return null;
        }
        if(isNotNullNotUnresolved(cls)) {
            List<CsmClass> toAnalyze = getClassesContainers(cls);
            for (CsmClass csmClass : toAnalyze) {
                for (CsmInheritance inh : csmClass.getBaseClasses()) {
                    CsmClass base = getInheritanceClass(inh);
                    if (base != null && !antiLoop.contains(base.getQualifiedName())) {
                        antiLoop.add(base.getQualifiedName());
                        CsmObject result = resolveInClass(base, name);
                        if (result != null) {
                            return result;
                        }
                        result = _resolveInBaseClasses(base, name, antiLoop, depth + 1);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private CsmClass getInheritanceClass(CsmInheritance inh){
        if (inh instanceof InheritanceImpl) {
            if (isRecursionOnResolving(INFINITE_RECURSION)) {
                return null;
            }
            CsmClassifier out = inh.getClassifier();
            out = getOriginalClassifier(out);
            if (CsmKindUtilities.isClass(out)) {
                return (CsmClass) out;
            }
        }
        return getCsmClass(inh);
    }

    private CsmClass getCsmClass(CsmInheritance inh) {
        CsmClassifier classifier = inh.getClassifier();
        classifier = getOriginalClassifier(classifier);
        if (CsmKindUtilities.isClass(classifier)) {
            return (CsmClass)classifier;
        }
        return null;
    }

    private boolean isNotNullNotUnresolved(Object obj) {
        return obj != null && !Unresolved.isUnresolved(obj);
    }

    private CsmObject resolveInClass(CsmClass cls, CharSequence name) {
        if(isNotNullNotUnresolved(cls)){
            List<CsmClass> classesContainers = getClassesContainers(cls);
            for (CsmClass csmClass : classesContainers) {
                CsmClassifier classifier = null;
                if (csmClass.getName().equals(name)) {
                    return csmClass;
                }
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(name, true, true, false);
                Iterator<CsmMember> it = CsmSelect.getClassMembers(csmClass, filter);
                while (it.hasNext()) {
                    CsmMember member = it.next();
                    if (CsmKindUtilities.isClassifier(member)) {
                        classifier = (CsmClassifier) member;
                        if (!CsmKindUtilities.isClassForwardDeclaration(classifier)) {
                            return classifier;
                        }
                    }
                }
                if (classifier != null) {
                    return classifier;
                }
            }
        }
        return null;
    }

    private List<CsmClass> getClassesContainers(CsmClass cls) {
        List<CsmClass> out = new ArrayList<>();
        CsmScope container = cls;
        while (CsmKindUtilities.isClass(container)) {
            out.add((CsmClass)container);
            container = ((CsmClass)container).getScope();
        }
        return out;
    }

    private boolean needClassifiers() {
        return ((interestedKind & CLASSIFIER) == CLASSIFIER) || needClasses() || needTemplateClasses() || needForwardClasses();
    }

    private boolean needNamespaces() {
        return (interestedKind & NAMESPACE) == NAMESPACE;
    }

    private boolean needClasses() {
        return (interestedKind & CLASS) == CLASS || needTemplateClasses() || needForwardClasses();
    }

    private boolean needTemplateClasses() {
        return (interestedKind & TEMPLATE_CLASS) == TEMPLATE_CLASS;
    }
    
    private boolean needTemplateClassesOnly() {
        return interestedKind == TEMPLATE_CLASS;
    }

    private boolean needForwardClasses() {
        return (interestedKind & CLASS_FORWARD) == CLASS_FORWARD;
    }
    
    private boolean needForwardClassesOnly() {
        return interestedKind == CLASS_FORWARD;
    }

    private boolean isObjectVisibleFromFile(CsmObject toCheck, CsmFile startFile) {
        if (toCheck == null) {
            return false;
        }
        CsmIncludeResolver resolver = CsmIncludeResolver.getDefault();
        return resolver.isObjectVisible(startFile, toCheck);
    }

    private boolean canStop(CsmObject result, AtomicBoolean resultIsVisible, CsmObject[] backupResult) {
        if (result != null) {
            if (backupResult[0] == null) {
                backupResult[0] = result;
            } else if (!needForwardClassesOnly()) {
                if (ForwardClass.isForwardClass(backupResult[0]) &&
                    !ForwardClass.isForwardClass(result)) {
                    backupResult[0] = result;
                }
            }
            if (resultIsVisible.get()) {
                return !ForwardClass.isForwardClass(result) || needForwardClassesOnly();
            }
        }
        return false;
    }

    private static final class OrigClassifierCacheKey {
        private final int origOffset;
        private final CsmFile file;
        private final CsmFile startFile;
        private final CsmClassifier orig;
        private int hashCode = 0;

        public OrigClassifierCacheKey(int origOffset, CsmFile file, CsmFile startFile, CsmClassifier orig) {
            this.origOffset = origOffset;
            this.file = file;
            this.startFile = startFile;
            this.orig = orig;
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 5;
                hash = 79 * hash + this.origOffset;
                hash = 79 * hash + Objects.hashCode(this.file);
                hash = 79 * hash + Objects.hashCode(this.startFile);
                hash = 79 * hash + Objects.hashCode(this.orig);
                hashCode = hash;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final OrigClassifierCacheKey other = (OrigClassifierCacheKey) obj;
            if (this.hashCode != other.hashCode && (this.hashCode != 0 && other.hashCode != 0)) {
                return false;
            }            
            if (this.origOffset != other.origOffset) {
                return false;
            }
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            if (!Objects.equals(this.startFile, other.startFile)) {
                return false;
            }
            if (!Objects.equals(this.orig, other.orig)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "OrigClassifierCacheKey{" + "origOffset=" + origOffset + ", file=" + file.getAbsolutePath() + ", startFile=" + startFile.getAbsolutePath() + ", orig=" + orig + '}'; // NOI18N
        }
    }
    
    private static final Callable<CsmCacheMap> ORIG_CLASSIFIER_CACHE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("OrigClassifier Cache", 1); // NOI18N
        }
    };
    
    private static final class NameResolveCacheKey {
        private final String fullName;
        private final int origOffset;
        private final CsmFile startFile;
        private final CsmFile file;
        private final int interestedKind;
        private int hashCode = 0;

        public NameResolveCacheKey(String fullName, int origOffset, CsmFile startFile, CsmFile file, int interestedKind) {
            this.fullName = fullName;
            this.origOffset = origOffset;
            this.startFile = startFile;
            this.file = file;
            this.interestedKind = interestedKind;
        }

        @Override
        public String toString() {
            return "NameResolveCacheKey{" + "name=" + fullName + // NOI18N
                    ", origOffset=" + origOffset + // NOI18N
                    ", startFile=" + startFile.getAbsolutePath() + // NOI18N
                    ", file=" + file.getAbsolutePath() + // NOI18N
                    ", interestedKind=" + interestedKind + // NOI18N
                    ", hashCode=" + hashCode + '}';// NOI18N
        }
    
        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 7;
                hash = 41 * hash + Objects.hashCode(this.fullName);
                hash = 41 * hash + this.origOffset;
                hash = 41 * hash + Objects.hashCode(this.startFile);
                hash = 41 * hash + Objects.hashCode(this.file);
                hash = 41 * hash + this.interestedKind;
                hashCode = hash;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NameResolveCacheKey other = (NameResolveCacheKey) obj;
            if (this.hashCode != other.hashCode && (this.hashCode != 0 && other.hashCode != 0)) {
                return false;
            }
            if (this.origOffset != other.origOffset) {
                return false;
            }
            if (this.interestedKind != other.interestedKind) {
                return false;
            }
            if (!Objects.equals(this.fullName, other.fullName)) {
                return false;
            }
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            if (!Objects.equals(this.startFile, other.startFile)) {
                return false;
            }
            return true;
        }
    }
    
    private static final Callable<CsmCacheMap> NAME_CACHE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("Resolver3 Cache"); // NOI18N
        }

    };
    
    private class FindCurrLocalClassifier implements FileMapsCollector.Callback {

        @Override
        public boolean needToTraverseDeeper(CsmScope scope) {
            if (CsmKindUtilities.isNamespace(scope) || CsmKindUtilities.isNamespaceDefinition(scope)) {
                return ((CsmNamedElement) scope).getName().length() == 0;
            }            
            return isInContext(scope);
        }

        @Override
        public void onVisibleClassifier(CsmClassifier cls) {
            if (CharSequences.comparator().compare(currName(), cls.getName()) == 0) {
                currLocalClassifier = cls;
            }
        }
    }   
}
