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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.TypeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.ClassifierContainerKey;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * Storage for project classifiers. Class was extracted from ProjectBase.
 */
public class ClassifierContainer extends ProjectComponent {

    private final Map<CharSequence, CsmUID<CsmClassifier>> classifiers;
    // storage for classifiers defined as inner classfiers in C structures
    private final Map<CharSequence, CsmUID<CsmClassifier>> shortClassifiers;
    private final Map<CharSequence, CsmUID<CsmClassifier>> typedefs;
    private final Map<CharSequence, Set<CsmUID<CsmInheritance>>> inheritances;
    private final ReadWriteLock declarationsLock = new ReentrantReadWriteLock();

    // empty stub
    private static final ClassifierContainer EMPTY = new ClassifierContainer() {
        @Override
        public boolean putClassifier(CsmClassifier decl) {
            return false;
        }

        @Override
        public void put() {
        }
    };

    public static ClassifierContainer empty() {
        return EMPTY;
    }

    /** Creates a new instance of ClassifierContainer */
    public ClassifierContainer(ProjectBase project) {
        super(new ClassifierContainerKey(project.getUnitId()));
        classifiers = new HashMap<>();
        shortClassifiers = new HashMap<>();
        typedefs = new HashMap<>();
        inheritances = new HashMap<>();
        put();
    }

    public ClassifierContainer(RepositoryDataInput input) throws IOException {
        super(input);
        int collSize = input.readInt();
        classifiers = new HashMap<>(collSize);
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.classifiers, input, QualifiedNameCache.getManager(), collSize);
        collSize = input.readInt();
        shortClassifiers = new HashMap<>(collSize);
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.shortClassifiers, input, QualifiedNameCache.getManager(), collSize);
        collSize = input.readInt();
        typedefs = new HashMap<>(collSize);
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.typedefs, input, QualifiedNameCache.getManager(), collSize);
        collSize = input.readInt();
        inheritances = new HashMap<>();
        UIDObjectFactory.getDefaultFactory().readStringToUIDMapSet(this.inheritances, input, NameCache.getManager(), collSize);
    }

    // only for EMPTY static field
    private ClassifierContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null);
        classifiers = new HashMap<>();
        shortClassifiers = new HashMap<>();
        typedefs = new HashMap<>();
        inheritances = new HashMap<>();
    }

    public CsmClassifier getClassifier(CharSequence qualifiedName) {
        CsmClassifier result;
        CsmUID<CsmClassifier> uid;
        qualifiedName = CharSequences.create(qualifiedName);
        try {
            declarationsLock.readLock().lock();
            uid = classifiers.get(qualifiedName);
            if (uid == null) {
                uid = shortClassifiers.get(qualifiedName);
            }
            if (uid == null) {
                uid = typedefs.get(qualifiedName);
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        result = UIDCsmConverter.UIDtoDeclaration(uid);
        return result;
    }

    public Collection<CsmInheritance> getInheritances(CharSequence name) {
        Collection<CsmUID<CsmInheritance>> inh;
        int i = CharSequenceUtils.lastIndexOf(name, "::"); //NOI18N
        if (i >= 0) {
            name = name.subSequence(i + 2, name.length());
        }
        name = CharSequences.create(name);
        try {
            declarationsLock.readLock().lock();
            inh = inheritances.get(name);
            if (inh != null) {
                inh = new ArrayList<>(inh);
            } else {
                return Collections.<CsmInheritance>emptyList();
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return UIDCsmConverter.<CsmInheritance>UIDsToInheritances(inh);
    }


    // for unit teast
    public Map<CharSequence, CsmClassifier> getTestClassifiers(){
        return convertTestMap(classifiers);
    }

    // for unit teast
    public Map<CharSequence, CsmClassifier> getTestShortClassifiers() {
        return convertTestMap(shortClassifiers);
    }

    // for unit teast
    public Map<CharSequence, CsmClassifier> getTestTypedefs(){
        return convertTestMap(typedefs);
    }

    private Map<CharSequence, CsmClassifier> convertTestMap(Map<CharSequence, CsmUID<CsmClassifier>> map) {
        Map<CharSequence, CsmClassifier> res = new TreeMap<>();
        try {
            declarationsLock.readLock().lock();
            for (Map.Entry<CharSequence, CsmUID<CsmClassifier>> entry : map.entrySet()) {
                res.put(entry.getKey(), UIDCsmConverter.UIDtoDeclaration(entry.getValue()));
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return res;
    }

    public boolean putClassifier(CsmClassifier decl) {
        boolean changed = false;
        CsmUID<CsmClassifier> uid = UIDCsmConverter.declarationToUID(decl);
        Map<CharSequence, CsmUID<CsmClassifier>> map;
        Map<CharSequence, CsmUID<CsmClassifier>> shortNamesMap;
        if (isTypedef(decl) || isTypeAlias(decl)) {
            map = typedefs;
            shortNamesMap = null;
        } else {
            map = classifiers;
            shortNamesMap = shortClassifiers;
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                Collection<CsmInheritance> base = cls.getBaseClasses();
                if (!base.isEmpty()) {
                    try {
                        declarationsLock.writeLock().lock();
                        for(CsmInheritance inh : base) {
                            CharSequence id = inheritanceName(inh);
                            Set<CsmUID<CsmInheritance>> set = inheritances.get(id);
                            if (set == null) {
                                set = new HashSet<>();
                                inheritances.put(id, set);
                            }
                            set.add(UIDCsmConverter.inheritanceToUID(inh));
                            changed = true;
                        }
                    } finally {
                        declarationsLock.writeLock().unlock();
                    }
                }
            }
        }
        CharSequence qn = decl.getQualifiedName();
        changed |= putClassifier(map, qn, uid);

        // Special case for nested structs in C
        if (shortNamesMap != null) {
            // See Bug 144535 - wrong error highlighting for inner structure
            CharSequence qn2 = getQualifiedNameWithoutScopeStructNameForC(decl);
            if (qn2 != null && qn.length() != qn2.length()) {
                // TODO: think about multiple objects per name in classifier as well
                changed |= putClassifier(shortNamesMap, qn2, uid);
            }
        }
        if (changed) {
            put();
        }
        return changed;
    }

    private CharSequence inheritanceName(CsmInheritance inh) {
        CharSequence id;
        if (inh instanceof TypeImpl) {
            id = ((TypeImpl) inh.getAncestorType()).getOwnText();
        } else {
            id = inh.getAncestorType().getClassifierText();
        }
        int i = CharSequenceUtils.lastIndexOf(id, "::"); //NOI18N
        if (i >= 0) {
            id = id.subSequence(i+2, id.length());
        }
        return NameCache.getManager().getString(id);
    }

    private boolean putClassifier(Map<CharSequence, CsmUID<CsmClassifier>> map, CharSequence qn, CsmUID<CsmClassifier> uid) {
        boolean changed = false;
        try {
            declarationsLock.writeLock().lock();
            CsmUID<CsmClassifier> old = map.get(qn);
            if (old == null || (!UIDUtilities.isForwardClass(uid) && UIDUtilities.isForwardClass(old))) {
                assert uid != null;
                map.put(qn, uid);
                assert (UIDCsmConverter.UIDtoDeclaration(uid) != null);
                changed = true;
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        return changed;
    }

    public void removeClassifier(CsmDeclaration decl) {
        Map<CharSequence, CsmUID<CsmClassifier>> map;
        Map<CharSequence, CsmUID<CsmClassifier>> shortNamesMap;
        CsmUID<?> uid = UIDs.get(decl);
        boolean changed = false;
        if (isTypedef(decl) || isTypeAlias(decl)) {
            map = typedefs;
            shortNamesMap = null;
        } else {
            map = classifiers;
            shortNamesMap = shortClassifiers;
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                Collection<CsmInheritance> base = cls.getBaseClasses();
                if (!base.isEmpty()) {
                    try {
                        declarationsLock.writeLock().lock();
                        for(CsmInheritance inh : base) {
                            CharSequence id = inheritanceName(inh);
                            Set<CsmUID<CsmInheritance>> set = inheritances.get(id);
                            if (set != null) {
                                set.remove(UIDCsmConverter.inheritanceToUID(inh));
                                changed = true;
                            }
                        }
                    } finally {
                        declarationsLock.writeLock().unlock();
                    }
                }
            }
        }
        CharSequence qn = decl.getQualifiedName();
        changed |= removeClassifier(map, qn, uid);

        // Special case for nested structs in C
        if (shortNamesMap != null) {
            // See Bug 144535 - wrong error highlighting for inner structure
            CharSequence qn2 = getQualifiedNameWithoutScopeStructNameForC(decl);
            if (qn2 != null && qn.length() != qn2.length()) {
                // TODO: think about multiple objects per name in classifier as well
                changed |= removeClassifier(shortNamesMap, qn2, uid);
            }
        }
        if (changed) {
            put();
        }
    }

    private boolean removeClassifier(Map<CharSequence, CsmUID<CsmClassifier>> map, CharSequence qn, CsmUID<?> uid) {
        CsmUID<CsmClassifier> removed;
        try {
            declarationsLock.writeLock().lock();
            removed = map.get(qn);
            if (removed != null && removed.equals(uid)) {
                map.remove(qn);
            } else {
                removed = null;
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        assert (removed == null) || (UIDCsmConverter.UIDtoCsmObject(removed) != null) : " no object for UID " + removed;
        return removed != null;
    }

    //public void clearClassifiers() {
    //    classifiers.clear();
    //    typedefs.clear();
    //}

    private CharSequence getQualifiedNameWithoutScopeStructNameForC(CsmDeclaration decl) {
        // #192897 -  unstable LiteSQL accuracy test
        // skip not C env
        Kind kind = decl.getKind();
        if ((kind != CsmDeclaration.Kind.STRUCT && kind != CsmDeclaration.Kind.UNION) || CsmKindUtilities.isTemplate(decl)) {
            // not valid in C env
            return null;
        }

        CharSequence qualifiedNamePostfix;
        if(decl instanceof OffsetableDeclarationBase) {
            qualifiedNamePostfix = ((OffsetableDeclarationBase)decl).getQualifiedNamePostfix();
        } else {
            qualifiedNamePostfix = decl.getName();
        }
        CsmScope scope = decl.getScope();
        while (CsmKindUtilities.isClass(scope)) {
            CsmClass cls = (CsmClass) scope;
            kind = cls.getKind();
            if ((kind != CsmDeclaration.Kind.STRUCT && kind != CsmDeclaration.Kind.UNION) || CsmKindUtilities.isTemplate(decl)) {
                // not valid in C env
                return null;
            }
            scope = cls.getScope();
        }
        if (CsmKindUtilities.isNamespace(scope) && !((CsmNamespace)scope).isGlobal()) {
            // not valid in C env
            return null;
        }
        CharSequence qualifiedName = QualifiedNameCache.getManager().getString(qualifiedNamePostfix);
        return qualifiedName;
    }

    private boolean isTypedef(CsmDeclaration decl){
        return CsmKindUtilities.isTypedef(decl);
    }

    private boolean isTypeAlias(CsmDeclaration decl){
        return CsmKindUtilities.isTypeAlias(decl);
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        try {
            declarationsLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.classifiers, output, false);
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.shortClassifiers, output, false);
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.typedefs, output, false);
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMapSet(this.inheritances, output);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }
}
