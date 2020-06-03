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
package org.netbeans.modules.cnd.completion.cplusplus;

import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.completion.csm.CsmProjectContentResolver;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.Queue;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmLabelResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletion;

/**
 *
 * based on MDRFinder
 */
public class CsmFinderImpl implements CsmFinder {

    private final boolean caseSensitive;
    private final CsmFile csmFile;
    private final String mimeType;

    // ..........................................................................
    public CsmFinderImpl(FileObject fo, String mimeType) {
        this.csmFile = null;
        this.mimeType = mimeType;
        this.caseSensitive = _getCaseSensitive();
    }

    public CsmFinderImpl(CsmFile csmFile, String mimeType) {
        this.csmFile = csmFile;
        this.mimeType = mimeType;
        this.caseSensitive = _getCaseSensitive();
    }

    public CsmFinderImpl(CsmFile csmFile, String mimeType, boolean caseSensitive) {
        this.csmFile = csmFile;
        this.mimeType = mimeType;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public CsmFile getCsmFile() {
        return this.csmFile;
    }

    private boolean getCaseSensitive() {
        return caseSensitive;
    }

    private boolean _getCaseSensitive() {
        return CsmCompletionUtils.isCaseSensitive(mimeType);
    }

    private CsmNamespace resolveNamespace(String namespaceName, boolean caseSensitive) {
        Queue<CsmProject> queue = new LinkedList<CsmProject>();
        Set<CsmProject> seen = new HashSet<CsmProject>();
        queue.add(csmFile.getProject());
        CsmNamespace namespace = resolveNamespaceBfs(queue, seen, namespaceName);
        if (namespace != null) {
            return namespace;
        }
        for (CsmProject project : CsmModelAccessor.getModel().projects()) {
            if (!seen.contains(project)) {
                queue.add(project);
            }
        }
        return resolveNamespaceBfs(queue, seen, namespaceName);
    }

    private CsmNamespace resolveNamespaceBfs(Queue<CsmProject> queue, Set<CsmProject> seen, String namespace) {
        // breadth-first search in project dependency graph
        while (!queue.isEmpty()) {
            CsmProject project = queue.poll();
            CsmNamespace ns = project.findNamespace(namespace);
            if (ns != null) {
                return ns;
            }
            seen.add(project);
            for (CsmProject lib : project.getLibraries()) {
                if (!seen.contains(lib)) {
                    queue.offer(lib);
                }
            }
        }
        return null;
    }

    @Override
    public CsmNamespace getExactNamespace(String namespaceName) {

        // System.out.println ("getExactNamespace: " + packageName); //NOI18N

//        repository.beginTrans (false);
        try {
//            ((JMManager) JMManager.getManager()).setSafeTrans(true);
            CsmNamespace nmsp = resolveNamespace(namespaceName, true);
            return nmsp;
        } finally {
//            repository.endTrans (false);
        }

//        return null;
    }

    @Override
    public CsmClassifier getExactClassifier(String classFullName) {
        // System.out.println ("getExactClassifier: " + classFullName); //NOI18N
//        CsmClassifier cls = csmFile.getProject().findClassifier(classFullName);
        CsmClassifier cls = CsmClassifierResolver.getDefault().findClassifierUsedInFile(classFullName, csmFile, false);
        return cls;
    }

    public List<CsmNamespace> findNamespaces(String name, boolean exactMatch, boolean subNamespaces) {
        // System.out.println("findNamespaces: " + name); //NOI18N

        List<CsmNamespace> ret = new ArrayList<CsmNamespace>();
        if (true) {
            // this methods should not be called
            return ret;
        }

//        repository.beginTrans (false);
        try {
//            ((JMManager) JMManager.getManager()).setSafeTrans(true);
            if (exactMatch) {
                CsmNamespace nmsp = getExactNamespace(name);
                if (nmsp != null) {
                    ret.add(nmsp);
                }
            } else {
                int index = name.lastIndexOf(CsmCompletion.SCOPE);
                String prefix = index > 0 ? name.substring(0, index) : ""; //NOI18N
                CsmNamespace nmsp = resolveNamespace(prefix, caseSensitive);
                if (nmsp != null) {
                    Collection<CsmNamespace> subpackages = nmsp.getNestedNamespaces();
                    List<CsmNamespace> list = new ArrayList<CsmNamespace>();
                    for (Iterator<CsmNamespace> it = subpackages.iterator(); it.hasNext();) {
                        CsmNamespace subPackage = it.next();
                        String spName = caseSensitive ? subPackage.getName().toString() : subPackage.getName().toString().toUpperCase();
                        String csName = caseSensitive ? name : name.toUpperCase();
                        if (spName.startsWith(csName)) {
                            list.add(subPackage);
                        }
                    }
                    for (Iterator<CsmNamespace> iter = list.iterator(); iter.hasNext();) {
                        CsmNamespace nestedNmsp = iter.next();
                        ret.add(nestedNmsp);
                    }
                }
            } // else

            if (subNamespaces) {
                int size = ret.size();
                for (int x = 0; x < size; x++) {
                    CsmNamespace nestedNmsp = ret.get(x);
                    addNestedNamespaces(ret, nestedNmsp);
                }
            }

        } finally {
//            repository.endTrans (false);
        }
        return ret;
    }

    @Override
    public List<CsmNamespace> findNestedNamespaces(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested) {

        // System.out.println("findNamespaces: " + name); //NOI18N

        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        return contResolver.getNestedNamespaces(nmsp, name, exactMatch, searchNested);
    }

    /** Find elements (classes, variables, enumerators) by name and possibly in some namespace
     * @param nmsp namespace where the elements should be searched for. It can be null
     * @param begining of the name of the element. The namespace name must be omitted.
     * @param exactMatch whether the given name is the exact requested name
     *   of the element or not.
     * @param searchNested whether elements must be searched in unnamed nested namespaces as well
     * @return list of the matching elements
     */
    @Override
    public List<CsmObject> findNamespaceElements(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested, boolean searchFirst) {
        List<CsmObject> ret = new ArrayList<CsmObject>();

        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        HashSet<CsmNamespace> vasitedNamespaces = new HashSet<CsmNamespace>();

        if (csmFile != null && csmFile.getProject() != null) {
            CsmProject prj = csmFile.getProject();
            CsmNamespace ns = nmsp == null ? prj.getGlobalNamespace() : nmsp;
            if (checkStopAfterAppendAllNamespaceElements(ns, name, exactMatch, searchNested, searchFirst, true, csmFile, contResolver, ret, false, new HashSet<CharSequence>(), vasitedNamespaces)) {
                return ret;
            }
            final Collection<CsmProject> projets = new ArrayList<CsmProject>();
            projets.add(prj);
            projets.addAll(getProjectsWithLibrary(prj));
            for (CsmProject csmProject : projets) {
                final Collection<CsmProject> libraries = csmProject.getLibraries();
                if (!libraries.isEmpty()) {
                    // TODO: it seems that all libraries should be collected at the beginning of this method
                    // and there is no need to create contResolver more than once (no need for libContResolver).
                    CsmProjectContentResolver libContResolver = new CsmProjectContentResolver(null, null, getCaseSensitive(), false, false, libraries);
                    HashSet<CharSequence> set = new HashSet<CharSequence>();
                    for (Object o : ret) {
                        if (CsmKindUtilities.isQualified((CsmObject) o)) {
                            set.add(((CsmQualifiedNamedElement) o).getQualifiedName());
                        }
                    }
                    for (CsmProject lib : libraries) {
                        CsmNamespace libNmsp;
                        if (ns.isGlobal()) {
                            libNmsp = lib.getGlobalNamespace();
                        } else {
                            libNmsp = lib.findNamespace(ns.getQualifiedName());
                        }
                        if (libNmsp != null) {
                            if (checkStopAfterAppendAllNamespaceElements(libNmsp, name, exactMatch, searchNested, searchFirst, false, null, libContResolver, ret, true, set, vasitedNamespaces)) {
                                return ret;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public Collection<CsmProject> getProjectsWithLibrary(CsmProject lib) {
        Collection<CsmProject> res = new ArrayList<CsmProject>();
        Collection<CsmProject> projects = CsmModelAccessor.getModel().projects();
        boolean changed = true;
        while(changed) {
            changed = false;
            loop : for (CsmProject project : projects) {
                if(!res.contains(project)) {
                    if(project.getLibraries().contains(lib)) {
                        res.add(project);
                        changed = true;
                        continue loop;
                    }
                    for (CsmProject resProject : res) {
                        if(project.getLibraries().contains(resProject)) {
                            res.add(project);
                            changed = true;
                            continue loop;
                        }
                    }
                }
            }
        }
        return res;
    }

    @Override
    public List<CsmObject> findStaticNamespaceElements(CsmNamespace nmsp, String name, boolean exactMatch) {
        List<CsmObject> ret = new ArrayList<CsmObject>();
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        ret.addAll(contResolver.getFileLocalNamespaceFunctions(nmsp, csmFile, name, exactMatch));
        ret.addAll(contResolver.getFileLocalNamespaceVariables(nmsp, csmFile, name, exactMatch));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private boolean checkStopAfterAppendAllNamespaceElements(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested, boolean searchFirst,
            boolean needFileLocal, CsmFile file,
            CsmProjectContentResolver contResolver, List ret,
            boolean merge, Set<CharSequence> set, HashSet<CsmNamespace> vasitedNamespaces) {
        if (vasitedNamespaces.contains(nmsp)) {
            return false;
        }
        vasitedNamespaces.add(nmsp);

        Collection elements = contResolver.getNamespaceClassesEnums(nmsp, name, exactMatch, searchNested);
        if (checkStopAfterAppendElements(ret, elements, set, merge, searchFirst)) {
            return true;
        }
        elements = contResolver.getNamespaceEnumerators(nmsp, name, exactMatch, searchNested);
        if (checkStopAfterAppendElements(ret, elements, set, merge, searchFirst)) {
            return true;
        }
        elements = contResolver.getNamespaceVariables(nmsp, name, exactMatch, searchNested);
        if (checkStopAfterAppendElements(ret, elements, set, merge, searchFirst)) {
            return true;
        }
        elements = contResolver.getNamespaceFunctions(nmsp, name, exactMatch, searchNested);
        if (checkStopAfterAppendElements(ret, elements, set, merge, searchFirst)) {
            return true;
        }
        elements = contResolver.getNamespaceAliases(nmsp, name, exactMatch, searchNested);
        if (checkStopAfterAppendElements(ret, elements, set, merge, searchFirst)) {
            return true;
        }
        if (needFileLocal) {
            assert file != null : "file must be passed if needFileLocal is true";
            boolean needLocalMerge = merge;
            if (searchNested && !merge) {
                // if searchNested is set, then some file local elements already
                // can be added. To avoid adding duplicates, lets initialize set of added objects
                for (Object added : ret) {
                    addFoundElement(set, added);
                }
                if (!set.isEmpty()) {
                    needLocalMerge = true;
                }
            }
            elements = contResolver.getFileLocalNamespaceVariables(nmsp, file, name, exactMatch);
            if (checkStopAfterAppendElements(ret, elements, set, needLocalMerge, searchFirst)) {
                return true;
            }
            elements = contResolver.getFileLocalNamespaceFunctions(nmsp, file, name, exactMatch);
            if (checkStopAfterAppendElements(ret, elements, set, needLocalMerge, searchFirst)) {
                return true;
            }
        }
        for (CsmNamespace ns : CsmUsingResolver.getDefault().findVisibleNamespaces(nmsp, file == null ? nmsp.getProject() : file.getProject())) {
            if (checkStopAfterAppendAllNamespaceElements(ns, name, exactMatch, searchNested, searchFirst, needFileLocal, file, contResolver, ret, merge, set, vasitedNamespaces)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkStopAfterAppendElements(List<CsmObject> ret, Collection<CsmObject> elements, Set<CharSequence> set, boolean merge, boolean searchFirst) {
        if (merge) {
            merge(set, ret, elements);
        } else {
            ret.addAll(elements);
        }
        if (searchFirst && ret.size() > 0) {
            return true;
        }
        return false;
    }

    private void merge(Set<CharSequence> set, List<CsmObject> ret, Collection<CsmObject> classes) {
        if (classes != null) {
            for (CsmObject o : classes) {
                if (addFoundElement(set, o)) {
                    ret.add(o);
                }
            }
        }
    }

    private boolean addFoundElement(Set<CharSequence> set, Object obj) {
        if (CsmKindUtilities.isCsmObject(obj)) {
            if (CsmKindUtilities.isFunction((CsmObject) obj)) {
                return set.add(((CsmFunction) obj).getUniqueName());
            } else if (CsmKindUtilities.isQualified((CsmObject) obj)) {
                return set.add(((CsmQualifiedNamedElement) obj).getQualifiedName());
            }
        }
        return false;
    }

    /** Find classes by name and possibly in some namespace
     * @param nmsp namespace where the classes should be searched for. It can be null
     * @param begining of the name of the class. The namespace name must be omitted.
     * @param exactMatch whether the given name is the exact requested name
     *   of the class or not.
     * @return list of the matching classes
     */
    @Override
    public List<CsmClassifier> findClasses(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested) {
        // System.out.println("findClasses: " + (nmsp == null ? "null" : nmsp.getName ()) + " " + name); //NOI18N

        List<CsmClassifier> ret = new ArrayList<CsmClassifier>();

        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());

        if (csmFile != null && csmFile.getProject() != null) {
            CsmProject prj = csmFile.getProject();
            CsmNamespace ns = nmsp == null ? prj.getGlobalNamespace() : nmsp;
            Collection<CsmClassifier> classes = contResolver.getNamespaceClassesEnums(ns, name, exactMatch, searchNested);
            if (classes != null) {
                ret.addAll(classes);
            }
            classes = prj.getGlobalNamespace() == ns ? null : contResolver.getLibClassesEnums(name, exactMatch);
            if (classes != null) {
                ret.addAll(classes);
            }
        }
        return ret;
    }

    private void addNestedNamespaces(List<CsmNamespace> list, CsmNamespace nmsp) {
        Iterator<CsmNamespace> iter = nmsp.getNestedNamespaces().iterator();
        while (iter.hasNext()) {
            CsmNamespace n = iter.next();
            list.add(n);
            addNestedNamespaces(list, n);
        }
    }

    /** Find fields by name in a given class.
     * @param contextDeclaration declaration which defines context (class or function)
     * @param c class which is searched for the fields.
     * @param name start of the name of the field
     * @param exactMatch whether the given name of the field is exact
     * @param staticOnly whether search for the static fields only
     * @param inspectOuterClasses if the given class is inner class of some
     *   outer class, whether the fields of the outer class should be possibly
     *   added or not. This should be false when searching for 'this.'
     * @return list of the matching fields
     */
    @Override
    public List<CsmField> findFields(CsmOffsetableDeclaration contextDeclaration, CsmClass classifier, String name, boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses, boolean scopeAccessedClassifier, boolean sort) {
        // get class variables visible in this method
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        List<CsmField> classFields = contResolver.getFields(classifier, contextDeclaration, name, staticOnly, exactMatch, inspectParentClasses, inspectOuterClasses, scopeAccessedClassifier);
        return classFields;
    }

    /** Find enumerators by name in a given class.
     * @param contextDeclaration declaration which defines context (class or function)
     * @param c class which is searched for the enumerators.
     * @param name start of the name of the field
     * @param exactMatch whether the given name of the enumerators is exact
     * @param inspectOuterClasses if the given class is inner class of some
     *   outer class, whether the enumerators of the outer class should be possibly
     *   added or not. This should be false when searching for 'this.'
     * @return list of the matching fields
     */
    @Override
    public List<CsmEnumerator> findEnumerators(CsmOffsetableDeclaration contextDeclaration, CsmClass classifier, String name, boolean exactMatch, boolean inspectOuterClasses, boolean inspectParentClasses, boolean scopeAccessedClassifier, boolean sort) {
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        List<CsmEnumerator> classEnumerators = contResolver.getEnumerators(classifier, contextDeclaration, name, exactMatch, inspectParentClasses, inspectOuterClasses, scopeAccessedClassifier);
        return classEnumerators;
    }

    /** Find methods by name in a given class.
     * @param contextDeclaration declaration which defines context (class or function)
     * @param c class which is searched for the methods.
     * @param name start of the name of the method
     * @param exactMatch whether the given name of the method is exact
     * @param staticOnly whether search for the static methods only
     * @param inspectOuterClasses if the given class is inner class of some
     *   outer class, whether the methods of the outer class should be possibly
     *   added or not. This should be false when searching for 'this.'
     * @return list of the matching methods
     */
    @Override
    public List<CsmMethod> findMethods(CsmOffsetableDeclaration contextDeclaration, CsmClass classifier, String name, boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses, boolean scopeAccessedClassifier, boolean sort) {
        CsmClass clazz = classifier;
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        if (contextDeclaration == null) {
            // in global context get all
            contextDeclaration = clazz;
        }
        List<CsmMethod> classMethods = contResolver.getMethods(clazz, contextDeclaration, name, staticOnly, exactMatch, inspectParentClasses, inspectOuterClasses, scopeAccessedClassifier);
        return classMethods;
    }

    @Override
    public List<CsmClassifier> findNestedClassifiers(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch, boolean inspectParentClasses, boolean sort) {
        CsmClass clazz = c;
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        List<CsmClassifier> classClassifiers = contResolver.getNestedClassifiers(clazz, contextDeclaration, name, exactMatch, inspectParentClasses, true, false);
        return classClassifiers;
    }

    @Override
    public List<CsmLabel> findLabel(CsmOffsetableDeclaration contextDeclaration, String name, boolean exactMatch, boolean sort) {
        List<CsmLabel> out = new ArrayList<CsmLabel>();
        if (CsmKindUtilities.isFunctionDefinition(contextDeclaration)) {
            Collection<CsmReference> res = CsmLabelResolver.getDefault().getLabels((CsmFunctionDefinition) contextDeclaration, null, EnumSet.of(CsmLabelResolver.LabelKind.Definiton));
            for (CsmReference ref : res) {
                if (CsmKindUtilities.isLabel(ref.getReferencedObject())) {
                    CsmLabel label = (CsmLabel) ref.getReferencedObject();
                    if (CsmSortUtilities.matchName(label.getLabel(), name, exactMatch, caseSensitive)) {
                        out.add(label);
                    }
                }
            }
        }
        return out;
    }

    @Override
    public List<CsmClass> findBaseClasses(CsmOffsetableDeclaration contextDeclaration, CsmClassifier c, String name, boolean exactMatch, boolean sort) {
        CsmFile contextFile = getCsmFile();
        if (contextFile == null && contextDeclaration != null) {
            contextFile = contextDeclaration.getContainingFile();
        }
        c = CsmBaseUtilities.getOriginalClassifier(c, contextFile);
        if (CsmKindUtilities.isClass(c)) {
            CsmClass clazz = (CsmClass) c;
            CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
            List<CsmClass> classClassifiers = contResolver.getBaseClasses(clazz, contextDeclaration, name, exactMatch);
            return classClassifiers;
        } else {
            return new ArrayList<CsmClass>();
        }
    }
}
