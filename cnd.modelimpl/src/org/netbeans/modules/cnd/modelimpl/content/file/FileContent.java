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
package org.netbeans.modules.cnd.modelimpl.content.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelimpl.cache.impl.WeakContainer;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MutableDeclarationsContainer;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.repository.FileDeclarationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileIncludesKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileInstantiationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileMacrosKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileReferencesKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Pair;
import org.openide.util.Union2;

/**
 * storage for file content. 
 * This object is passed to parser hooks to fill with objects created during parse phase.
 * Then it can be compared with other content to decide if signature of file
 * was changed significantly or not. If not then full reparse is not needed and 
 * this content can be used as new file content
 */
public final class FileContent implements MutableDeclarationsContainer {

    private final FileImpl fileImpl;
    private final boolean persistent;
    private final List<FakeIncludePair> fakeIncludeRegistrations;
    private final List<CsmUID<FunctionImplEx<?>>> fakeFunctionRegistrations;
    private int parserErrorsCount;    
    private final Set<ErrorDirectiveImpl> errors;    
    private final Union2<FileComponentDeclarations, WeakContainer<FileComponentDeclarations>> fileComponentDeclarations;
    /*FileComponentMacros or WeakContainer<FileComponentMacros>*/
    private final Union2<FileComponentMacros, WeakContainer<FileComponentMacros>> fileComponentMacros;
    /*FileComponentIncludes or WeakContainer<FileComponentIncludes>*/
    private final Union2<FileComponentIncludes, WeakContainer<FileComponentIncludes>> fileComponentIncludes;
    private final AtomicBoolean hasBrokenIncludes;
    /*FileComponentInstantiations or WeakContainer<FileComponentInstantiations>*/
    private final Union2<FileComponentInstantiations, WeakContainer<FileComponentInstantiations>> fileComponentInstantiations;
    /*FileComponentReferences or WeakContainer<FileComponentReferences>*/
    private final Union2<FileComponentReferences, WeakContainer<FileComponentReferences>> fileComponentReferences;
    private final Collection<CsmParserProvider.ParserError> parserErrors;
    
    public static FileContent createFileContent(FileImpl fileImpl, CsmValidable owner) {
        return new FileContent(fileImpl, owner, true,
                new FileComponentDeclarations(fileImpl),
                new FileComponentMacros(fileImpl),
                new FileComponentIncludes(fileImpl),
                false,
                new FileComponentInstantiations(fileImpl),
                new FileComponentReferences(fileImpl),
                createFakeIncludes(Collections.<FakeIncludePair>emptyList()),
                createFakeFunctions(Collections.<CsmUID<FunctionImplEx<?>>>emptyList()),
                createErrors(Collections.<ErrorDirectiveImpl>emptySet()), 0,
                createParserErrors(Collections.<CsmParserProvider.ParserError>emptyList()));
    }
    
    private FileContent(FileImpl fileImpl, CsmValidable owner, boolean persistent,
            FileComponentDeclarations fcd, FileComponentMacros fcm,
            FileComponentIncludes fcinc, boolean hasBrokenIncludes,
            FileComponentInstantiations fcinst, FileComponentReferences fcr,
            List<FakeIncludePair> fakeIncludeRegistrations, 
            List<CsmUID<FunctionImplEx<?>>> fakeFunctionRegistrations,
            Set<ErrorDirectiveImpl> errors, int parserErrorsCount, 
            Collection<CsmParserProvider.ParserError> parserErrors) {
        this.persistent = persistent;
        this.fileImpl = fileImpl;
        this.fileComponentDeclarations = asUnion(owner, fcd, persistent);
        this.fileComponentMacros = asUnion(owner, fcm, persistent);
        this.fileComponentIncludes = asUnion(owner, fcinc, persistent);
        this.hasBrokenIncludes = new AtomicBoolean(hasBrokenIncludes);
        this.fileComponentInstantiations = asUnion(owner, fcinst, persistent);
        this.fileComponentReferences = asUnion(owner, fcr, persistent);
        this.fakeIncludeRegistrations = fakeIncludeRegistrations;
        this.fakeFunctionRegistrations = fakeFunctionRegistrations;
        this.errors = errors;
        this.parserErrorsCount = parserErrorsCount;
        this.parserErrors = parserErrors;
        if (persistent) {
            fcd.put();
            fcm.put();
            fcinc.put();
            fcinst.put();
            fcr.put();
        }
        checkValid();
    }
    
    public Collection<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<>();
        l.addAll(getFileDeclarations().getStaticVariableDeclarations());
        l.addAll(getFileDeclarations().getStaticFunctionDeclarations());
        return l;
    }
    
    /**
     * returns copy with hard referenced collections. It can not be put into repository.
     * toWeakReferenceBasedCopy method should be used to release hard references.
     * @return hard reference based non-persistent copy of content
     */
    public static FileContent getHardReferenceBasedCopy(FileContent other, boolean emptyContent) {
        other.checkValid();
        return new FileContent(other.fileImpl, other.fileImpl, false,
                new FileComponentDeclarations(other.getFileDeclarations(), emptyContent),
                new FileComponentMacros(other.getFileMacros(), emptyContent),
                new FileComponentIncludes(other.getFileIncludes(), emptyContent),
                (emptyContent ? false : other.hasBrokenIncludes()),
                new FileComponentInstantiations(other.getFileInstantiations(), emptyContent),
                new FileComponentReferences(other.getFileReferences(), emptyContent),
                createFakeIncludes(emptyContent || other.fakeIncludeRegistrations.isEmpty()? Collections.<FakeIncludePair>emptyList() : other.fakeIncludeRegistrations),
                createFakeFunctions(emptyContent || other.fakeFunctionRegistrations.isEmpty()? Collections.<CsmUID<FunctionImplEx<?>>>emptyList() : other.fakeFunctionRegistrations),
                createErrors(emptyContent || other.errors.isEmpty()? Collections.<ErrorDirectiveImpl>emptySet() : other.errors), 
                emptyContent ? 0 : other.parserErrorsCount,
                createParserErrors(emptyContent || other.parserErrors.isEmpty() ? Collections.<CsmParserProvider.ParserError>emptyList() : other.parserErrors));
    }
    
    /**
     * content of this instance is converted to weak referenced one.
     * @return copy which reference internal containers by weak reference and repository keys
     */
    public FileContent toWeakReferenceBasedCopy() {
        checkValid();
        try {
            // convert this instance as is into persistent copy
            // it not legal to use it later on for appending elements
            return new FileContent(this.fileImpl, this.fileImpl, true,
                    this.getFileDeclarations(), this.getFileMacros(),
                    this.getFileIncludes(), this.hasBrokenIncludes(),
                    this.getFileInstantiations(), this.getFileReferences(),
                    this.fakeIncludeRegistrations,
                    this.fakeFunctionRegistrations,
                    this.errors, this.parserErrorsCount,
                    this.parserErrors);
        } finally {
            // mark object as no more usable
            this.parserErrorsCount = -1;
        }
    }
    
    public final int getErrorCount() {
        checkValid();
        return parserErrorsCount;
    }

    public void setErrorCount(int errorCount) {
        checkValid();
        this.parserErrorsCount = errorCount;
        checkValid();
    }

    public final void onFakeRegisration(FunctionImplEx<?> decl, Pair<AST, MutableDeclarationsContainer> data) {
        checkValid();
        CsmUID<?> aUid = UIDCsmConverter.declarationToUID(decl);
        @SuppressWarnings("unchecked")
        CsmUID<FunctionImplEx<?>> uidDecl = (CsmUID<FunctionImplEx<?>>) aUid;
        fakeFunctionRegistrations.add(uidDecl);
        trackFakeFunctionData(uidDecl, data);
    }

    public final boolean onFakeIncludeRegistration(IncludeImpl include, CsmOffsetableDeclaration container) {
        checkValid();
        if (include != null && container != null) {
            CsmUID<IncludeImpl> includeUid = UIDCsmConverter.identifiableToUID(include);
            CsmUID<CsmOffsetableDeclaration> containerUID = UIDCsmConverter.declarationToUID(container);
            if (includeUid != null && containerUID != null) {
                // extra check to track possible double registrations like
                // namespace AAA {
                //   namespace Inner {
                //        class B {
                // #include "classBody.h"
                //           class Inner {
                // #include "innerBody.h"
                //           }; end of class Inner
                //        }; end of class B
                //   } // end of namespace Inner
                // } // end of namespace AAA
                // 
                for (FakeIncludePair fakeIncludePair : fakeIncludeRegistrations) {
                    if (fakeIncludePair.includeUid.equals(includeUid)) {
                        // inner object always has higher priority
                        if (!fakeIncludePair.containerUid.equals(containerUID)) {
                            // but sometimes it has a collision when included twice, i.e. 
                            // namespace QtConcurrent {
                            // ...
                            // #include <QtCore/qtconcurrentmedian.h>
                            // #ifndef QQQ
                            // }
                            // #else
                            // void extra();
                            // }
                            // #endif
                            // namespace QtConcurrent had [2075-2729] postitions when QQQ was not defined
                            // then it has [2075-9963] offsets when QQQ is defined
                            CndUtils.assertTrueInConsole(false, "trying to replace? " + include + " for container " + container + " was: " + fakeIncludePair);
                        }
                        return false;
                    }
                }
                fakeIncludeRegistrations.add(new FakeIncludePair(includeUid, containerUID));
                return true;
            }
        }
        return false;
    }

    public List<CsmUID<FunctionImplEx<?>>> getFakeFunctionRegistrations() {
        checkValid();
        return fakeFunctionRegistrations;
    }

    public List<FakeIncludePair> getFakeIncludeRegistrations() {
        checkValid();
        return fakeIncludeRegistrations;
    }
    
    private final Set<FileContent> includedFileContents = new HashSet<>(0);

    public final void addIncludedFileContent(FileContent includedFileContent) {
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
        boolean added = includedFileContents.add(includedFileContent);
        traceAddRemove(added ? "+FILE" : "SKIP", includedFileContent.fileImpl.getAbsolutePath()); // NOI18N
    }

    public Set<FileContent> getIncludedFileContents() {
        return Collections.unmodifiableSet(includedFileContents);
    }

    public void addError(ErrorDirectiveImpl error) {
        checkValid();
        traceAddRemove("ERROR", error); // NOI18N
        errors.add(error);
    }

    public void addParsingError(CsmParserProvider.ParserError error) {
        parserErrors.add(error);
    }
    
    public void addMacro(CsmMacro macro) {
        checkValid();
        traceAddRemove("MACRO", macro); // NOI18N
        FileComponentMacros fileMacros = getFileMacros();
        fileMacros.addMacro(macro);
        if (persistent) {
            fileMacros.put();
        }
    }       

    @Override
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        checkValid();
        traceAddRemove("DECL", decl); // NOI18N
        FileComponentDeclarations fileDeclarations = getFileDeclarations();
        fileDeclarations.addDeclaration(decl);
        if (persistent) {
            fileDeclarations.put();
        }
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, int endOffset, CharSequence name) {
        checkValid();
        return getFileDeclarations().findExistingDeclaration(startOffset, endOffset, name);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, CharSequence name, CsmDeclaration.Kind kind) {
        checkValid();
        return getFileDeclarations().findExistingDeclaration(startOffset, name, kind);
    }

    public Collection<CsmInclude> getIncludes() {
        checkValid();
        return getFileIncludes().getIncludes();
    }
    
    public void addInclude(IncludeImpl includeImpl, boolean broken) {
        checkValid();
        traceAddRemove("INCL", includeImpl); // NOI18N
        FileComponentIncludes fileIncludes = getFileIncludes();
        // addInclude can remove added one from list of broken includes =>
        hasBrokenIncludes.set(fileIncludes.addInclude(includeImpl, broken));
        if (persistent) {
            fileIncludes.put();
        }
    }

    public void addInstantiation(CsmInstantiation inst) {
        checkValid();
        traceAddRemove("INST", inst); // NOI18N
        FileComponentInstantiations fileInstantiations = getFileInstantiations();
        fileInstantiations.addInstantiation(inst);
        if (persistent) {
            fileInstantiations.put();
        }
    }

    public boolean addReference(CsmReference ref, CsmObject referencedObject) {
        checkValid();
        traceAddRemove("REF", ref); // NOI18N
        FileComponentReferences fileReferences = getFileReferences();
        boolean out = fileReferences.addReference(ref, referencedObject);
        if (persistent) {
            fileReferences.put();
        }
        return out;
    }

    public boolean addResolvedReference(CsmReference ref, CsmObject referencedObject) {
        checkValid();
        traceAddRemove("RREF", ref); // NOI18N
        FileComponentReferences fileReferences = getFileReferences();
        boolean out = fileReferences.addResolvedReference(ref, referencedObject);
        if (persistent) {
            fileReferences.put();
        }
        return out;
    }

    @Override
    public String toString() {
        return ((parserErrorsCount < 0) ? "INVALID " :"") + (persistent ? "PERSISTENT " :"") + "File Content for " + fileImpl; // NOI18N
    }

    private static List<CsmUID<FunctionImplEx<?>>> createFakeFunctions(List<CsmUID<FunctionImplEx<?>>> in) {
        return new CopyOnWriteArrayList<>(in);
    }
    
    private static List<FakeIncludePair> createFakeIncludes(List<FakeIncludePair> in) {
        return new CopyOnWriteArrayList<>(in);
    }
    
    private static Set<ErrorDirectiveImpl> createErrors(Set<ErrorDirectiveImpl> in) {
        Set<ErrorDirectiveImpl> out = new TreeSet<>(FileImpl.START_OFFSET_COMPARATOR);
        out.addAll(in);
        return out;
    }

    private static Collection<CsmParserProvider.ParserError> createParserErrors(Collection<CsmParserProvider.ParserError> in) {
        Collection<CsmParserProvider.ParserError> out = new ArrayList<>();
        out.addAll(in);
        return out;
    }
    
    public FileComponentDeclarations getFileDeclarations() {
        checkValid();
        return getFileComponent(fileComponentDeclarations);
    }

    public Set<? extends CsmErrorDirective> getErrors() {
        checkValid();
        return Collections.unmodifiableSet(errors);
    }

    public Collection<CsmParserProvider.ParserError> getParserErrors() {
        return Collections.unmodifiableCollection(parserErrors);
    }
    
    public FileComponentMacros getFileMacros() {
        checkValid();
        return getFileComponent(fileComponentMacros);
    }

    public FileComponentIncludes getFileIncludes() {
        checkValid();
        return getFileComponent(fileComponentIncludes);
    }

    public boolean hasBrokenIncludes() {
        checkValid();
        return hasBrokenIncludes.get();
    }

    public FileComponentReferences getFileReferences() {
        checkValid();
        return getFileComponent(fileComponentReferences);
    }

    public FileComponentInstantiations getFileInstantiations() {
        checkValid();
        return getFileComponent(fileComponentInstantiations);
    }
    
    private <T extends FileComponent> T getFileComponent(Union2<T, WeakContainer<T>> ref) {
        if (ref.hasFirst()) {
            assert !persistent : "non persistent must have hard reference";
            return ref.first();
        } else {
            assert persistent : "persistent must have weak reference";
            return ref.second().getContainer();
        }
    }

    private <T extends FileComponent> Key getFileComponentKey(Union2<T, WeakContainer<T>> ref) {
        if (ref.hasFirst()) {
            assert !persistent : "non persistent must have hard reference";
            return ref.first().getKey();
        } else {
            assert persistent : "persistent must have weak reference";
            return ref.second().getKey();
        }
    }
    
    /* collection to keep fake ASTs during parse phase */
    private final Map<CsmUID<FunctionImplEx<?>>, Pair<AST, MutableDeclarationsContainer>> fakeFuncData = new HashMap<>();

    private void trackFakeFunctionData(CsmUID<FunctionImplEx<?>> funUID, Pair<AST, MutableDeclarationsContainer> data) {
        if (data == null) {
            fakeFuncData.remove(funUID);
        } else {
            fakeFuncData.put(funUID, data);
        }
    }

    public void write(RepositoryDataOutput output) throws IOException {
        checkValid();
        assert persistent : "only persistent content can be put into repository";
        FileDeclarationsKey fileDeclarationsKey = (FileDeclarationsKey) getFileComponentKey(fileComponentDeclarations);
        assert fileDeclarationsKey != null : "file declaratios key can not be null";
        fileDeclarationsKey.write(output);
        FileIncludesKey fileIncludesKey = (FileIncludesKey) getFileComponentKey(fileComponentIncludes);
        assert fileIncludesKey != null : "file includes key can not be null";
        fileIncludesKey.write(output);
        output.writeBoolean(hasBrokenIncludes.get());
        FileMacrosKey fileMacrosKey = (FileMacrosKey) getFileComponentKey(fileComponentMacros);
        assert fileMacrosKey != null : "file macros key can not be null";
        fileMacrosKey.write(output);
        fileComponentReferences.second().getKey();
        FileReferencesKey fileReferencesKey = (FileReferencesKey) getFileComponentKey(fileComponentReferences);
        assert fileReferencesKey != null : "file referebces key can not be null";
        fileReferencesKey.write(output);
        FileInstantiationsKey fileInstantiationsKey = (FileInstantiationsKey) getFileComponentKey(fileComponentInstantiations);
        assert fileInstantiationsKey != null : "file instantiation references key can not be null";
        fileInstantiationsKey.write(output);
        
        PersistentUtils.writeErrorDirectives(this.errors, output);
        output.writeInt(parserErrorsCount);
        
        FakeIncludePair.write(fakeIncludeRegistrations, output);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(this.fakeFunctionRegistrations, output, false);
        
        // TODO : store parser errors
    }
    
    public FileContent(FileImpl file, CsmValidable owner, RepositoryDataInput input) throws IOException {
        this.fileImpl = file;
        this.persistent = true;
        FileDeclarationsKey fileDeclarationsKey = new FileDeclarationsKey(input);
        assert fileDeclarationsKey != null : "file declaratios key can not be null";
        fileComponentDeclarations = Union2.createSecond(new WeakContainer<FileComponentDeclarations>(owner, fileDeclarationsKey));

        FileIncludesKey fileIncludesKey = new FileIncludesKey(input);
        assert fileIncludesKey != null : "file includes key can not be null";
        fileComponentIncludes = Union2.createSecond(new WeakContainer<FileComponentIncludes>(owner, fileIncludesKey));
        hasBrokenIncludes = new AtomicBoolean(input.readBoolean());

        FileMacrosKey fileMacrosKey = new FileMacrosKey(input);
        assert fileMacrosKey != null : "file macros key can not be null";
        fileComponentMacros = Union2.createSecond(new WeakContainer<FileComponentMacros>(owner, fileMacrosKey));

        FileReferencesKey fileReferencesKey = new FileReferencesKey(input);
        assert fileReferencesKey != null : "file referebces key can not be null";
        fileComponentReferences = Union2.createSecond(new WeakContainer<FileComponentReferences>(owner, fileReferencesKey));

        FileInstantiationsKey fileInstantiationsKey = new FileInstantiationsKey(input);
        assert fileInstantiationsKey != null : "file instantiation references key can not be null";
        fileComponentInstantiations = Union2.createSecond(new WeakContainer<FileComponentInstantiations>(owner, fileInstantiationsKey));
        
        this.errors = createErrors(Collections.<ErrorDirectiveImpl>emptySet());
        // ErrorDirectiveImpl does not have UID, so deserialize using containingFile directly
        PersistentUtils.readErrorDirectives(this.errors, file, input);
        parserErrorsCount = input.readInt();
        
        this.fakeIncludeRegistrations = createFakeIncludes(Collections.<FakeIncludePair>emptyList());
        FakeIncludePair.read(this.fakeIncludeRegistrations, input);
        
        this.fakeFunctionRegistrations = createFakeFunctions(Collections.<CsmUID<FunctionImplEx<?>>>emptyList());
        UIDObjectFactory.getDefaultFactory().readUIDCollection(this.fakeFunctionRegistrations, input);
        
        this.parserErrors = createParserErrors(Collections.<CsmParserProvider.ParserError>emptyList());
        // TODO : load parser errors
        
        checkValid();
    }

    private <T extends FileComponent> Union2<T, WeakContainer<T>> asUnion(CsmValidable stateOwner, T fc, boolean persistent) {
        if (persistent) {
            return Union2.createSecond(new WeakContainer<T>(stateOwner, fc.getKey()));
        } else {
            return Union2.createFirst(fc);
        }
    }

    private void checkValid() {
        assert this.parserErrorsCount >= 0 : "invalid object for " + fileImpl.getAbsolutePath();
    }

    @Override
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        FileComponentDeclarations fileDeclarations = getFileDeclarations();
        traceAddRemove("remove", declaration); // NOI18N
        fileDeclarations.removeDeclaration(declaration);
        if (persistent) {
            fileDeclarations.put();
        }
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        return getFileDeclarations().getDeclarations();
    }

    public void cleanOther() {
        FileComponentIncludes fileIncludes = getFileIncludes();
        fileIncludes.clean();
        FileComponentMacros fileMacros = getFileMacros();
        fileMacros.clean();
        FileComponentReferences fileReferences = getFileReferences();
        fileReferences.clean();
        FileComponentInstantiations fileInstantiations = getFileInstantiations();
        fileInstantiations.clean();
        if (persistent) {
            fileIncludes.put();
            fileMacros.put();
            fileReferences.put();
            fileInstantiations.put();
        }
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> cleanDeclarations() {
        FileComponentDeclarations fileDeclarations = getFileDeclarations();
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = fileDeclarations.clean();
        if (persistent) {
            fileDeclarations.put();
        }
        return uids;
    }
    
    public void put() {
        assert persistent;
        getFileIncludes().put();
        getFileMacros().put();
        getFileReferences().put();
        getFileInstantiations().put();
        getFileDeclarations().put();
    }

    public FileImpl getFile() {
        return this.fileImpl;
    }

    public Map<CsmUID<FunctionImplEx<?>>, Pair<AST, MutableDeclarationsContainer>> getFakeFuncData() {
        return this.fakeFuncData;
    }

    private static final boolean TRACE = false;
    private void traceAddRemove(String mark, Object obj) {
        if (TRACE) {
            CharSequence path = fileImpl.getAbsolutePath();
            CndUtils.assertTrueInConsole(persistent, "MODIFYING ", path);
            if (path.toString().contains("NetBeansProjects")) { // NOI18N
                System.err.printf("%-65s %-5s %s\n", path, mark, obj);
            }
        }
    }
}
