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
package org.netbeans.modules.cnd.modelimpl.uid;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.api.model.CsmBuiltIn;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTypeAlias;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * utilities to create CsmUID for CsmObjects
 */
public class UIDUtilities {

    /** Creates a new instance of UIDUtilities */
    private UIDUtilities() {
    }

    public static CsmUID<CsmProject> createProjectUID(ProjectBase prj) {
        return getCachedUID(new ProjectUID(prj), prj);
    }

    public static CsmUID<CsmFile> createFileUID(FileImpl file) {
        return getCachedUID(new FileUID(file), file);
    }

    public static CsmUID<CsmNamespace> createNamespaceUID(CsmNamespace ns) {
        return getCachedUID(new NamespaceUID(ns), ns);
    }

    public static <T extends CsmOffsetableDeclaration> CsmUID<T> createDeclarationUID(T declaration) {
        assert (!(declaration instanceof CsmBuiltIn)) : "built-in have own UIDs";
        CsmUID<T> uid;
        //if (!ProjectBase.canRegisterDeclaration(declaration)) {
        if (!namedDeclaration(declaration)) {
            uid = handleUnnamedDeclaration(declaration);
        } else {
            if (declaration instanceof CsmTypedef) {
                uid = new TypedefUID<>(declaration);
            } else if (declaration instanceof CsmTypeAlias) {
                uid = new TypeAliasUID<>(declaration);
            } else if (ForwardClass.isForwardClass(declaration)) {
                uid = new ForwardClassUID<>(declaration);
            } else if (declaration instanceof CsmClassifier) {
                uid = new ClassifierUID<>(declaration);
            } else {
                uid = new DeclarationUID<>(declaration);
            }
        }
        return updateCachedUIDIfNeeded(uid, declaration);
    }

    public static <T extends CsmInstantiation> CsmUID<T> createInstantiationUID(T inst) {
        CsmUID<T> uid = new InstantiationUID<>(inst);
        return updateCachedUIDIfNeeded(uid, inst);
    }
    
    private static <T extends CsmOffsetableDeclaration> boolean namedDeclaration(T declaration) {
        assert declaration != null;
        assert declaration.getName() != null;
        return declaration.getName().length() > 0;
    }

    public static CsmUID<CsmMacro> createMacroUID(CsmMacro macro) {
        return getCachedUID(new MacroUID(macro), macro);
    }

    public static CsmUID<CsmInclude> createIncludeUID(CsmInclude incl) {
        return getCachedUID(new IncludeUID(incl), incl);
    }

    public static CsmUID<CsmInheritance> createInheritanceUID(CsmInheritance inh) {
        return getCachedUID(new InheritanceUID(inh), inh);
    }

    public static CsmUID<CsmClass> createUnresolvedClassUID(String name, CsmProject project) {
        CsmUID<CsmClass> sharedUID = UIDManager.instance().getSharedUID(new UnresolvedClassUID(name, project));
        assert !(sharedUID instanceof CachedUID);
        return sharedUID;
    }

    public static CsmUID<CsmFile> createUnresolvedFileUID(CsmProject project) {
        CsmUID<CsmFile> sharedUID = UIDManager.instance().getSharedUID(new UnresolvedFileUID(project));
        assert !(sharedUID instanceof CachedUID);
        return sharedUID;
    }

    public static CsmUID<CsmNamespace> createUnresolvedNamespaceUID(CsmProject project) {
        CsmUID<CsmNamespace> sharedUID = UIDManager.instance().getSharedUID(new UnresolvedNamespaceUID(project));
        assert !(sharedUID instanceof CachedUID);
        return sharedUID;
    }

    private static <T extends CsmObject> CsmUID<T> getCachedUID(CachedUID<T> uid, T obj) {
        return updateCachedUID(uid, obj);
    }

    public static int getProjectID(CsmUID<?> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            return KeyUtilities.getProjectIndex(((KeyBasedUID<?>) uid).getKey());
        }
        return -1;
    }

    public static boolean isProjectFile(CsmUID<CsmProject> uid1, CsmUID<CsmFile> uid2) {
        if (uid1 instanceof KeyBasedUID<?> && uid2 instanceof KeyBasedUID<?>) {
            int i1 = KeyUtilities.getProjectIndex(((KeyBasedUID<?>) uid1).getKey());
            int i2 = KeyUtilities.getProjectIndex(((KeyBasedUID<?>) uid2).getKey());
            if (i1 >= 0 && i2 >=0) {
                return i1 == i2;
            }
        }
        return false;
    }

    public static boolean isSameProject(CsmUID<CsmFile> uid1, CsmUID<CsmFile> uid2) {
        if (uid1 instanceof KeyBasedUID<?> && uid2 instanceof KeyBasedUID<?>) {
            int i1 = KeyUtilities.getProjectIndex(((KeyBasedUID<?>) uid1).getKey());
            int i2 = KeyUtilities.getProjectIndex(((KeyBasedUID<?>) uid2).getKey());
            if (i1 >= 0 && i2 >=0) {
                return i1 == i2;
            }
        }
        return false;
    }

    public static boolean isSameFile(CsmUID<CsmOffsetableDeclaration> uid1, CsmUID<CsmOffsetableDeclaration> uid2) {
        if (uid1 instanceof KeyBasedUID<?> && uid2 instanceof KeyBasedUID<?>) {
            int i1 = KeyUtilities.getProjectFileIndex(((KeyBasedUID<?>) uid1).getKey());
            int i2 = KeyUtilities.getProjectFileIndex(((KeyBasedUID<?>) uid2).getKey());
            if (i1 >= 0 && i2 >=0) {
                return i1 == i2;
            }
        }
        return isSameFile(uid1.getObject(), uid2.getObject());
    }

    public static boolean isForwardClass(CsmUID<?> uid) {
        // TODO: check usages and decide if replaced forward class should be removed from repository
        if (uid instanceof ForwardClassUID<?>) {
            return true;
        }
        return false;
    }

    public static int getFileID(CsmUID<?> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            return KeyUtilities.getProjectFileIndex(((KeyBasedUID<?>) uid).getKey());
        }
        return -1;
    }

    private static boolean isSameFile(CsmOffsetableDeclaration decl1, CsmOffsetableDeclaration decl2) {
        if (decl1 != null && decl2 != null) {
            CsmFile file1 = decl1.getContainingFile();
            CsmFile file2 = decl2.getContainingFile();
            if (file1 != null && file2 != null) {
                return file1.equals(file2);
            }
        }
        return false;
    }

    public static CsmDeclaration.Kind getKind(CsmUID<?> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            return KeyUtilities.getKeyKind(key);
        } else if (UIDProviderIml.isSelfUID(uid)) {
            CsmObject object = (CsmObject) uid.getObject();
            if (CsmKindUtilities.isDeclaration(object)) {
                return ((CsmDeclaration)object).getKind();
            }
        }
        return null;
    }

    public static CsmVisibility getVisibility(CsmUID<CsmInheritance> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            return KeyUtilities.getKeyVisibility(key);
        }
        return null;
    }

    public static char getKindChar(CsmUID<?> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            return KeyUtilities.getKeyChar(key);
        }
        return 0;
    }

    public static CharSequence getFileName(CsmUID<CsmFile> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            return KeyUtilities.getKeyName(key);
        }
        return null;
    }

    public static CharSequence getProjectName(CsmUID<CsmProject> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            return KeyUtilities.getKeyName(key);
        }
        return null;
    }
    
    public static CharSequence getName(CsmUID<?> uid) {
        return getName(uid, false);
    }

    public static CharSequence getName(CsmUID<?> uid, boolean internalName) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            return KeyUtilities.getKeyName(key, internalName);
        } else if (UIDProviderIml.isSelfUID(uid)) {
            Object object = uid.getObject();
            if (CsmKindUtilities.isNamedElement(object)) {
                return ((CsmNamedElement) object).getName();
            }
        }
        return null;
    }

    public static int getStartOffset(CsmUID<?> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            return KeyUtilities.getKeyStartOffset(key);
        } else if (UIDProviderIml.isSelfUID(uid)) {
            Object object = uid.getObject();
            if (CsmKindUtilities.isOffsetable(object)) {
                return ((CsmOffsetable) object).getStartOffset();
            }
        }
        return -1;
    }

    public static int getEndOffset(CsmUID<?> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key key = ((KeyBasedUID<?>) uid).getKey();
            int out = KeyUtilities.getKeyEndOffset(key);
            if (out == KeyUtilities.NON_INITIALIZED) {
                Object object = uid.getObject();
                if (CsmKindUtilities.isOffsetable(object)) {
                    out = ((CsmOffsetable) object).getEndOffset();
                    KeyUtilities.cacheKeyEndOffset(key, out);
                }
            }
            return out;
        } else if (UIDProviderIml.isSelfUID(uid)) {
            Object object = uid.getObject();
            if (CsmKindUtilities.isOffsetable(object)) {
                return ((CsmOffsetable) object).getEndOffset();
            }
        }
        return -1;
    }

    /**
     * Compares UIDs of the two declarationds within the same file
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater 
     *         than the second.
     */
    public static <T extends CsmOffsetable> int compareWithinFile(CsmUID<T> d1, CsmUID<T> d2) {

        // by start offset
        int offset1 = getStartOffset(d1);
        int offset2 = getStartOffset(d2);
        if (offset1 != offset2) {
            return offset1 - offset2;
        }
        // by end offset
        offset1 = getEndOffset(d1);
        offset2 = getEndOffset(d2);
        if (offset1 != offset2) {
            return offset1 - offset2;
        }
        // by name
        CharSequence name1 = getName(d1, true);
        CharSequence name2 = getName(d2, true);
        if (name1 instanceof Comparable<?>) {
            @SuppressWarnings("unchecked")
            Comparable<CharSequence> o1 = (Comparable<CharSequence>) name1;
            int i = o1.compareTo(name2);
            if (i == 0) {
                int i1 = getKindChar(d1);
                int i2 = getKindChar(d2);
                return i1-i2;
            }
            return i;
        }
        if (name1 != null) {
            return (name2 == null) ? 1 : 0;
        } else { // name1 == null
            return (name2 == null) ? 0 : -1;
        }
    }

    public static <T extends CsmOffsetableDeclaration> CsmUID<T> findExistingUIDInList(List<CsmUID<T>> list, int start, int end, CharSequence name) {
        CsmUID<T> out = null;
        // look for the object with the same start position and the same name
        // TODO: for now we are in O(n), but better to be O(ln n) speed
        for (int i = list.size() - 1; i >= 0; i--) {
            CsmUID<T> csmUID = list.get(i);
            int startOffset = UIDUtilities.getStartOffset(csmUID);
            if (startOffset == start && end == UIDUtilities.getEndOffset(csmUID) && name.equals(UIDUtilities.getName(csmUID))) {
                out = csmUID;
                break;
            } else if (startOffset < start) {
                break;
            }
        }
        return out;
    }
    
    public static <T extends CsmOffsetableDeclaration> CsmUID<T> findExistingUIDInList(List<CsmUID<T>> list, int start, CharSequence name, CsmDeclaration.Kind kind) {
        CsmUID<T> out = null;
        // look for the object with the same start position and the same name
        // TODO: for now we are in O(n), but better to be O(ln n) speed
        for (int i = list.size() - 1; i >= 0; i--) {
            CsmUID<T> csmUID = list.get(i);
            int startOffset = UIDUtilities.getStartOffset(csmUID);
            if (startOffset == start && name.equals(UIDUtilities.getName(csmUID)) && kind.equals(UIDUtilities.getKind(csmUID))) {
                out = csmUID;
                break;
            } else if (startOffset < start) {
                break;
            }
        }
        return out;
    }        

    public static <T extends CsmOffsetable> void insertIntoSortedUIDList(CsmUID<T> uid, List<CsmUID<T>> list) {
        int start = UIDUtilities.getStartOffset(uid);
        // start from the last, because most of the time we are in append, not insert mode
        boolean lessThanOthers = false;
        for (int pos = list.size() - 1; pos >= 0; pos--) {
            CsmUID<T> currUID = list.get(pos);
            int i = UIDUtilities.compareWithinFile(currUID, uid);
            if (i <= 0) {
                if (i == 0) {
                    list.set(pos, uid);
                } else {
                    list.add(pos + 1, uid);
                }
                return;
            } else if (UIDUtilities.getStartOffset(currUID) < start) {
                break;
            } else {
                lessThanOthers = true;
            }
        }
        if (!list.isEmpty() && lessThanOthers) {
            // insert as the first
            list.add(0, uid);
        } else {
            // add as the last
            list.add(uid);
        }
    }

    private static <T extends CsmOffsetableDeclaration> CsmUID<T> handleUnnamedDeclaration(T decl) {
        if (TraceFlags.TRACE_UNNAMED_DECLARATIONS) {
            System.err.print("\n\ndeclaration with empty name '" + decl.getUniqueName() + "'");
            new CsmTracer().dumpModel(decl);
        }
        if (decl instanceof CsmClassifier) {
            return new UnnamedClassifierUID<>(decl, UnnamedID.incrementAndGet());
        } else {
            return new UnnamedOffsetableDeclarationUID<>(decl, UnnamedID.incrementAndGet());
        }
    }
    
    private static final AtomicInteger UnnamedID = new AtomicInteger(0);
    //////////////////////////////////////////////////////////////////////////
    // impl details

    /**
     * Base UID for cached objects
     */
    /* package */ static class CachedUID<T> extends KeyBasedUID<T> {
        private static final SoftReference<Object> DUMMY = new SoftReference<>(null);
        private static final SoftReference<Object> EMPTY = new SoftReference<>(null);
        private volatile Reference<Object> weakT;

        protected CachedUID(Key key, T obj) {
            super(key);
            weakT = TraceFlags.USE_WEAK_MEMORY_CACHE && key.hasCache() ? new WeakReference<Object>(obj) : DUMMY;
        }

        protected CachedUID(Key key) {
            super(key);
            weakT = DUMMY;
        }
        
        CachedUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
            weakT = TraceFlags.USE_WEAK_MEMORY_CACHE && getKey().hasCache() ? EMPTY : DUMMY;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T getObject() {
            T out;
            Reference<T> weak = (Reference<T>) weakT;
            if (weak != DUMMY) {
                out = weak.get();
                if (out != null) {
                    return out;
                }
            }
            out = RepositoryUtils.get(this);
            if (out != null && weak != DUMMY) {
                weakT = (Reference<Object>) new WeakReference<>(out);
            }
            return out;
        }

        @Override
        public void dispose(T obj) {
            if (obj == null) {
                weakT = DUMMY;
            } else {
                weakT = new SoftReference<Object>(obj);
            }
        }

        public void update(T obj) {
            if (weakT.get() != obj) {
                weakT = new WeakReference<Object>(obj);
            }
        }

        public void clear() {
            if (TraceFlags.USE_WEAK_MEMORY_CACHE && getKey().hasCache()) {
                weakT = EMPTY;
            }
        }
    }

    /**
     * UID for CsmProject
     */
    /* package */ static final class ProjectUID extends CachedUID<CsmProject> { //KeyBasedUID<CsmProject> {

        public ProjectUID(ProjectBase project) {
            super(KeyUtilities.createProjectKey(project), project);
        }

        /* package */ ProjectUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmNamespace
     */
    /* package */ static final class NamespaceUID extends CachedUID<CsmNamespace> { //KeyBasedUID<CsmNamespace> {

        public NamespaceUID(CsmNamespace ns) {
            super(KeyUtilities.createNamespaceKey(ns), ns);
        }

        /* package */ NamespaceUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmFile
     */
    /* package */ static final class FileUID extends CachedUID<CsmFile> { //KeyBasedUID<CsmFile> {

        public FileUID(FileImpl file) {
            super(KeyUtilities.createFileKey(file), file);
        }

        /* package */ FileUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * base UID for CsmDeclaration
     */
    private static abstract class OffsetableDeclarationUIDBase<T extends CsmOffsetableDeclaration> extends CachedUID<T> { //KeyBasedUID<T> {

        public OffsetableDeclarationUIDBase(T declaration) {
            this(KeyUtilities.createOffsetableDeclarationKey((OffsetableDeclarationBase<?>) declaration), declaration);
        }

        protected OffsetableDeclarationUIDBase(Key key, T obj) {
            super(key, obj);
        }

        protected OffsetableDeclarationUIDBase(Key key) {
            super(key);
        }
        
        /* package */ OffsetableDeclarationUIDBase(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        public String toString() {
            String retValue = getToStringPrefix() + ":" + super.toString(); // NOI18N
            return retValue;
        }

        protected String getToStringPrefix() {
            return "UID for OffsDecl"; // NOI18N
        }
    }

    /**
     * base UID for cached CsmDeclaration
     */
    private static abstract class OffsetableDeclarationUIDBaseCached<T extends CsmOffsetableDeclaration> extends CachedUID<T> { //KeyBasedUID<T> {

        public OffsetableDeclarationUIDBaseCached(T declaration) {
            this(KeyUtilities.createOffsetableDeclarationKey((OffsetableDeclarationBase<?>) declaration), declaration);
        }

        protected OffsetableDeclarationUIDBaseCached(Key key, T obj) {
            super(key, obj);
        }

        protected OffsetableDeclarationUIDBaseCached(Key key) {
            super(key);
        }
        
        /* package */ OffsetableDeclarationUIDBaseCached(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        public String toString() {
            String retValue = getToStringPrefix() + ":" + super.toString(); // NOI18N
            return retValue;
        }

        protected String getToStringPrefix() {
            return "UID for OffsDecl"; // NOI18N
        }
    }

    /**
     * UID for CsmTypedef
     */
    /* package */ static final class TypedefUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public TypedefUID(T typedef) {
            super(typedef);
//            assert typedef instanceof RegistarableDeclaration;
//            if (!((RegistarableDeclaration)typedef).isRegistered()) {
//                System.err.print("\n\nunregistered declaration'" + typedef.getUniqueName() + "'");
//                new CsmTracer().dumpModel(typedef);
//            }
//            assert ((RegistarableDeclaration)typedef).isRegistered();            
        }

        public TypedefUID(Key key) {
            super(key);
        }
        
        /* package */ TypedefUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "TypedefUID"; // NOI18N
        }
    }
    
    /**
     * UID for CsmTypeAlias
     */
    /* package */ static final class TypeAliasUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public TypeAliasUID(T typeAlias) {
            super(typeAlias);
        }

        public TypeAliasUID(Key key) {
            super(key);
        }
        
        /* package */ TypeAliasUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "TypeAliasUID"; // NOI18N
        }
    }    

    /**
     * UID for CsmMacro
     */
    /* package */ static final class MacroUID extends CachedUID<CsmMacro> { //KeyBasedUID<CsmMacro> {

        public MacroUID(CsmMacro macro) {
            super(KeyUtilities.createMacroKey(macro), macro);
        }

        /* package */ MacroUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmInclude
     */
    /* package */ static final class IncludeUID extends CachedUID<CsmInclude> { //KeyBasedUID<CsmInclude> {

        public IncludeUID(CsmInclude incl) {
            super(KeyUtilities.createIncludeKey(incl), incl);
        }

        /* package */ IncludeUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmInclude
     */
    /* package */ static final class InheritanceUID extends CachedUID<CsmInheritance> { //KeyBasedUID<CsmInclude> {

        public InheritanceUID(CsmInheritance inh) {
            super(KeyUtilities.createInheritanceKey(inh), inh);
        }

        /* package */ InheritanceUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }

    /**
     * UID for CsmClassifier
     */
    /* package */ static final class DeclarationUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public DeclarationUID(T decl) {
            super(decl);
        }
        
        public DeclarationUID(Key key) {
            super(key);
        }        

        /* package */ DeclarationUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "DeclarationUID"; // NOI18N
        }
    }

    /**
     * UID for CsmClassifier
     */
    /* package */ static final class ClassifierUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBaseCached<T> {//OffsetableDeclarationUIDBase<T> {

        public ClassifierUID(T classifier) {
            super(classifier);
        }

        public ClassifierUID(Key key) {
            super(key);
        }        
        
        /* package */ ClassifierUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "ClassifierUID"; // NOI18N
        }
    }

    /**
     * UID for CsmClassifier
     */
    /* package */ static final class ForwardClassUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBaseCached<T> {//OffsetableDeclarationUIDBase<T> {

        public ForwardClassUID(T classifier) {
            super(classifier);
        }

        public ForwardClassUID(Key key) {
            super(key);
        }
        
        /* package */ ForwardClassUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "ForwardClassUID"; // NOI18N
        }
    }

    /**
     * UID for CsmClassifier with empty getName()
     */
    /* package */ static final class UnnamedClassifierUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public UnnamedClassifierUID(T classifier, int index) {
            super(KeyUtilities.createUnnamedOffsetableDeclarationKey((OffsetableDeclarationBase<?>) classifier, index), classifier);
        }

        public UnnamedClassifierUID(Key key) {
            super(key);
        }
        
        /* package */ UnnamedClassifierUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "<UNNAMED CLASSIFIER UID>"; // NOI18N
        }
    }

    /**
     * UID for CsmDeclaration with empty getName()
     */
    /* package */ static final class UnnamedOffsetableDeclarationUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {

        public UnnamedOffsetableDeclarationUID(T decl, int index) {
            super(KeyUtilities.createUnnamedOffsetableDeclarationKey((OffsetableDeclarationBase<?>) decl, index), decl);
        }

        public UnnamedOffsetableDeclarationUID(Key key) {
            super(key);
        }
        
        /* package */ UnnamedOffsetableDeclarationUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        protected String getToStringPrefix() {
            return "<UNNAMED OFFS-DECL UID>"; // NOI18N
        }
    }
    
    /* package */ static final class InstantiationUID<T extends CsmInstantiation> extends CachedUID<T> { //KeyBasedUID<T> {

        public InstantiationUID(T inst) {
            this(KeyUtilities.createInstantiationKey(inst), inst);
        }

        protected InstantiationUID(Key key, T obj) {
            super(key, obj);
        }

        /* package */ InstantiationUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }

        @Override
        public String toString() {
            String retValue = getToStringPrefix() + ":" + super.toString(); // NOI18N
            return retValue;
        }

        protected String getToStringPrefix() {
            return "UID for Instantiation"; // NOI18N
        }
    }    
    
    

    /**
     * Abstract base class for Unresolved* UIDs.
     */
    /* package */ static abstract class UnresolvedUIDBase<T> implements CsmUID<T>, SelfPersistent {

        private final CsmUID<CsmProject> projectUID;

        public UnresolvedUIDBase(CsmProject project) {
            assert project != null : "how to create UID without project?";
            projectUID = UIDs.get(project);
        }

        protected ProjectBase getProject() {
            return (ProjectBase) projectUID.getObject();
        }

        /* package */ UnresolvedUIDBase(RepositoryDataInput aStream) throws IOException {
            projectUID = UIDObjectFactory.getDefaultFactory().readUID(aStream);
        }

        @Override
        public abstract T getObject();

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(projectUID, output);
        }

        protected String getToStringPrefix() {
            return "<UNRESOLVED UID>"; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UnresolvedUIDBase<?> other = (UnresolvedUIDBase<?>) obj;
            if (this.projectUID != other.projectUID && (this.projectUID == null || !this.projectUID.equals(other.projectUID))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 43 * hash + (this.projectUID != null ? this.projectUID.hashCode() : 0);
            return hash;
        }



    }

    /* package */ static final class UnresolvedClassUID extends UnresolvedUIDBase<CsmClass> {

        private final CharSequence name;

        public UnresolvedClassUID(String name, CsmProject project) {
            super(project);
            this.name = NameCache.getManager().getString(name);
        }

        @Override
        public CsmClass getObject() {
            return getProject().getDummyForUnresolved(name);
        }

        public UnresolvedClassUID(RepositoryDataInput input) throws IOException {
            super(input);
            name = PersistentUtils.readUTF(input, NameCache.getManager());
        }

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            PersistentUtils.writeUTF(name, output);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final UnresolvedClassUID other = (UnresolvedClassUID) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

    }

    /* package */ static final class UnresolvedNamespaceUID extends UnresolvedUIDBase<CsmNamespace> {

        public UnresolvedNamespaceUID(CsmProject project) {
            super(project);
        }

        public UnresolvedNamespaceUID(RepositoryDataInput input) throws IOException {
            super(input);
        }

        @Override
        public CsmNamespace getObject() {
            return getProject().getUnresolvedNamespace();
        }
    }

    /* package */ static final class UnresolvedFileUID extends UnresolvedUIDBase<CsmFile> implements Disposable {
        private ProjectBase prjRef = null;
        public UnresolvedFileUID(CsmProject project) {
            super(project);
            prjRef = (ProjectBase)project;
        }

        public UnresolvedFileUID(RepositoryDataInput input) throws IOException {
            super(input);
        }

        @Override
        public CsmFile getObject() {
            return getProject().getUnresolvedFile();
        }

        @Override
        protected ProjectBase getProject() {
            ProjectBase prj = prjRef;
            if (prj == null) {
                prj = super.getProject();
            }
            return prj;
        }

        @Override
        public void dispose() {
            prjRef = getProject();
        }
    }

    public static void disposeUnresolved(CsmUID<?> uid) {
        if (uid instanceof UnresolvedFileUID) {
            UnresolvedFileUID fileUID = (UnresolvedFileUID) uid;
            fileUID.dispose();
        }
    }

    private static <T extends CsmObject> CsmUID<T> updateCachedUIDIfNeeded(CsmUID<T> uid, T declaration) {
        CsmUID<T> sharedUID = UIDManager.instance().getSharedUID(uid);
        if(sharedUID instanceof CachedUID) {
            ((CachedUID)sharedUID).update(declaration);
        }
        return sharedUID;
    }

    private static <T extends CsmObject> CsmUID<T> updateCachedUID(CachedUID<T> uid, T obj) {
        CachedUID<T> cachedUid = (CachedUID<T>)UIDManager.instance().getSharedUID(uid);
        cachedUid.update(obj);
        return cachedUid;
    }
    
}
