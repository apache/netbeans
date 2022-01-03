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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler.State;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTFileInfoQuerySupport;
import org.netbeans.modules.cnd.modelimpl.parser.clank.ClankFileInfoQuerySupport;
import org.netbeans.modules.cnd.modelimpl.platform.CndParserResult;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.text.NbDocument;
import org.openide.util.Pair;

/**
 * CsmFileInfoQuery implementation
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery.class)
public final class FileInfoQueryImpl extends CsmFileInfoQuery {
    
    @Override
    public boolean isCpp98OrLater(CsmFile csmFile) {
        if (csmFile != null) {
            Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> languageFlavor = getFileLanguageFlavor(csmFile);
            if (NativeFileItem.Language.CPP == languageFlavor.first() || NativeFileItem.Language.C_HEADER == languageFlavor.first()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean isCpp11OrLater(CsmFile csmFile) {
        if (csmFile != null) {
            Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> languageFlavor = getFileLanguageFlavor(csmFile);
            if (NativeFileItem.Language.CPP == languageFlavor.first() || NativeFileItem.Language.C_HEADER == languageFlavor.first()) {
                switch (languageFlavor.second()) {
                    case CPP11:
                    case CPP14:
                    case CPP17:
                        return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean isCpp14OrLater(CsmFile csmFile) {
        if (csmFile != null) {
            Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> languageFlavor = getFileLanguageFlavor(csmFile);
            if (NativeFileItem.Language.CPP == languageFlavor.first() || NativeFileItem.Language.C_HEADER == languageFlavor.first()) {
                switch (languageFlavor.second()) {
                    case CPP14:
                    case CPP17:
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<IncludePath> getSystemIncludePaths(CsmFile file) {
        return getIncludePaths(file, true);
    }

    @Override
    public List<IncludePath> getUserIncludePaths(CsmFile file) {
        return getIncludePaths(file, false);
    }

    private List<IncludePath> getIncludePaths(CsmFile file, boolean system) {
        List<IncludePath> out = Collections.<IncludePath>emptyList();
        if (file instanceof FileImpl) {
            NativeFileItem item = Utils.getCompiledFileItem((FileImpl) file);
            if (item != null) {
                if (item.getLanguage() == NativeFileItem.Language.C_HEADER) {
                    // It's an orphan (otherwise the getCompiledFileItem would return C or C++ item, not header).
                    // For headers, NativeFileItem does NOT contain necessary information
                    // (whe parsing, we use DefaultFileItem for headers)
                    // so for headers, we should use project iformation instead
                    NativeProject nativeProject = item.getNativeProject();
                    if (nativeProject != null) {
                        if (system) {
                            out = nativeProject.getSystemIncludePaths();
                        } else {
                            out = nativeProject.getUserIncludePaths();
                        }
                    }
                } else {
                    if (system) {
                        out = item.getSystemIncludePaths();
                    } else {
                        out = item.getUserIncludePaths();
                    }
                }
            }
        }
        return out;
    }

    @Override
    public List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file, Interrupter interrupter) {
        List<CsmOffsetable> out = Collections.<CsmOffsetable>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            Collection<PreprocessorStatePair> statePairs = fileImpl.getPreprocStatePairs();
            List<CsmOffsetable> result = new ArrayList<>();
            // to have visible code, we prefer non-error directive based dead blocks
            boolean first = true;
            for (PreprocessorStatePair pair : statePairs) {
                FilePreprocessorConditionState state = pair.pcState;
                if (state != FilePreprocessorConditionState.PARSING && !state.isFromErrorDirective()) {
                    List<CsmOffsetable> blocks = state.createBlocksForFile(fileImpl);
                    if (first) {
                        result = blocks;
                        first = false;
                    } else {
                        result = intersection(result, blocks);
                        if (result.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            if (!result.isEmpty()) {
                out = result;
            } else {
                // if no other dead blocks, check if we have error directive in file
                CsmOffsetable error = null;
                for (CsmErrorDirective csmErrorDirective : fileImpl.getErrors()) {
                    error = org.netbeans.modules.cnd.modelimpl.csm.core.Utils.createOffsetable(fileImpl, csmErrorDirective.getEndOffset(), Integer.MAX_VALUE);
                    out = Collections.singletonList(error);
                    break;
                }
            }
        }
        return out;
    }
    
    private static boolean contains(CsmOffsetable bigger, CsmOffsetable smaller) {
        if (bigger != null && smaller != null) {
            if (bigger.getStartOffset() <= smaller.getStartOffset() &&
                smaller.getEndOffset() <= bigger.getEndOffset()) {
                return true;
            }
        }
        return false;
    }
    
    private static List<CsmOffsetable> intersection(Collection<CsmOffsetable> first, Collection<CsmOffsetable> second) {
        List<CsmOffsetable> result = new ArrayList<>(Math.max(first.size(), second.size()));
        for (CsmOffsetable o1 : first) {
            for (CsmOffsetable o2 : second) {
                if (o1 != null) { //paranoia
                    if (o1.equals(o2)) {
                        result.add(o1);
                    } else if (contains(o1, o2)) {
                        result.add(o2);
                        
                    } else if (contains(o2, o1)) {
                        result.add(o1);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public CharSequence getName(CsmUID<CsmFile> fileUID) {
        return getFileName(fileUID);
    }

    public static CharSequence getFileName(CsmUID<CsmFile> fileUID) {
        CharSequence filePath = UIDUtilities.getFileName(fileUID);
        int indx = CharSequenceUtilities.lastIndexOf(filePath, '/'); // NOI18N
        if (indx < 0) {
            indx = CharSequenceUtilities.lastIndexOf(filePath, '\\'); // NOI18N
        }
        if (indx > 0 && indx < filePath.length()) {
            filePath = CharSequenceUtilities.toString(filePath, indx + 1, filePath.length());
        }    
        return filePath;
    }
    
    @Override
    public CharSequence getAbsolutePath(CsmUID<CsmFile> fileUID) {
        return UIDUtilities.getFileName(fileUID);
    }
    
    private final ConcurrentMap<CsmFile, Object> macroUsagesLocks = new ConcurrentHashMap<>();

    @Override
    public boolean isDocumentBasedFile(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            FileBuffer buffer = impl.getBuffer();
            return !buffer.isFileBased();
        }
        return false;
    }
    
    @Override
    public CsmFile getCsmFile(Parser.Result parseResult) {
        if (parseResult instanceof CndParserResult) {
            return ((CndParserResult) parseResult).getCsmFile();
        }
        return null;
    }

    public static int getIncludeDirectiveIndex(CsmInclude inc) {
        if (inc instanceof IncludeImpl) {
            return ((IncludeImpl) inc).getIncludeDirectiveIndex();
        }
        return -1;
    }

    private static final class NamedLock {
        private final CharSequence name;

        public NamedLock(CharSequence name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "getMacroUsages lock for " + this.name; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NamedLock other = (NamedLock) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

    }
    
    @Override
    public List<CsmReference> getMacroUsages(CsmFile file, Document doc, final Interrupter interrupter) {
        List<CsmReference> out = Collections.<CsmReference>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            Object lock = new NamedLock(file.getAbsolutePath());
            Object prevLock = macroUsagesLocks.putIfAbsent(fileImpl, lock);
            lock = prevLock != null ? prevLock : lock;
            try {
                synchronized (lock) {
                    List<CsmReference> res = fileImpl.getLastMacroUsages();
                    if (res != null) {
                        return res;
                    }
                    try {
                        long lastParsedTime = fileImpl.getLastParsedTime();
                        if (APTTraceFlags.USE_CLANK) {
                          out = ClankFileInfoQuerySupport.getMacroUsages(fileImpl, interrupter);
                        } else {
                          out = APTFileInfoQuerySupport.getMacroUsages(fileImpl, interrupter);
                        }
                        if (lastParsedTime == fileImpl.getLastParsedTime()) {
                            // cache only if calc wasn't interrupted
                            if (!interrupter.cancelled()) {
                                if (!out.isEmpty()) {
                                    if (doc != null) {
                                        List<CsmReference> wrapper = new ArrayList<>(out.size());
                                        for(CsmReference ref : out) {
                                            wrapper.add(new ProxyReference(ref, doc));
                                        }
                                        out = wrapper;
                                    }
                                }
                                fileImpl.setLastMacroUsages(out);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        // file could be removed
                    } catch (IOException ex) {
                        System.err.println("skip marking macros\nreason:" + ex.getMessage()); //NOI18N
                        DiagnosticExceptoins.register(ex);
                    }
                }
            } finally {
                macroUsagesLocks.remove(fileImpl, lock);
            }
        }
        return out;
    }
    
    @Override
    public CsmOffsetable getGuardOffset(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            if (APTTraceFlags.USE_CLANK) {
              return ClankFileInfoQuerySupport.getGuardOffset(fileImpl);
            } else {
              return APTFileInfoQuerySupport.getGuardOffset(fileImpl);
            }
        }
        return null;
    }
    
    @Override
    public boolean hasGuardBlock(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            if (APTTraceFlags.USE_CLANK) {
              return ClankFileInfoQuerySupport.hasGuardBlock(fileImpl);
            } else {
              return APTFileInfoQuerySupport.hasGuardBlock(fileImpl);
            }            
        }
        return false;
    }

    @Override
    public NativeFileItem getNativeFileItem(CsmFile file) {
        if (file instanceof FileImpl) {
            return ((FileImpl)file).getNativeFileItem();
        }
        return null;
    }

    @Override
    public Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> getFileLanguageFlavor(CsmFile csmFile) {
        if (csmFile != null) {
            Collection<CsmCompilationUnit> compilationUnits = getCompilationUnits(csmFile, 0);
            if (!compilationUnits.isEmpty()) {
                NativeFileItem.Language bestLanguage = null;
                NativeFileItem.LanguageFlavor bestFlavor = null;
                for (CsmCompilationUnit cu : compilationUnits) {
                    NativeFileItem startItem = getNativeFileItem(cu.getStartFile());
                    if (startItem != null) {
                        if (getLangPriority(bestLanguage) < getLangPriority(startItem.getLanguage())) {
                            bestLanguage = startItem.getLanguage();
                        }
                        if (getFlavorPriority(bestFlavor) < getFlavorPriority(startItem.getLanguageFlavor())) {
                            bestFlavor = startItem.getLanguageFlavor();
                        }
                    }
                }
                if (bestLanguage != null && bestFlavor != null) {
                    if (bestFlavor == NativeFileItem.LanguageFlavor.UNKNOWN) {
                        bestFlavor = NativeProjectSupport.getDefaultLanguageFlavor(bestLanguage);
                    }
                    return Pair.of(bestLanguage, bestFlavor);
                }
            }
            if (csmFile.isHeaderFile()) {
                return Pair.of(NativeFileItem.Language.C_HEADER,  NativeProjectSupport.getDefaultHeaderStandard());
            } else if (csmFile.isSourceFile()) {
                return Pair.of(NativeFileItem.Language.CPP,  NativeProjectSupport.getDefaultCppStandard());
            } 
        }
        return Pair.of(NativeFileItem.Language.OTHER, NativeFileItem.LanguageFlavor.UNKNOWN);
    }

    private int getLangPriority(NativeFileItem.Language lang) {
        if (lang == null) {
            return -1;
        }
        switch (lang) {
            case OTHER:
                return 0;
            case C_HEADER:
                return 1;
            case C:
                return 2;
            case CPP:
                return 3;
        }
        return 0;
    }
    
    private int getFlavorPriority(NativeFileItem.LanguageFlavor flavor) {
        if (flavor == null) {
            return -1;
        }
        switch (flavor) {
            case DEFAULT:
            case UNKNOWN:
                return 0;
            case C:
                return 1;
            case C89:
                return 2;
            case C99:
                return 3;
            case C11:
                return 4;
            case CPP98:
                return 5;
            case CPP11:
                return 6;
            case CPP14:
                return 7;
            case CPP17:
                return 8;
        }
        return 0;
    }

    @Override
    public Pair<String, String> getAPTLanguageFlavor(Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> langFlavor) {
        String aptLang = APTLanguageSupport.UNKNOWN;
        switch (langFlavor.first()) {
            case C:
                aptLang = APTLanguageSupport.GNU_C;
                break;
                
            case CPP:
                aptLang = APTLanguageSupport.GNU_CPP;
                break;
                
            case C_HEADER:
                aptLang = APTLanguageSupport.GNU_CPP;
                break;
                
            case FORTRAN:
                aptLang = APTLanguageSupport.FORTRAN;
                break;
        }
        String aptFlavor = APTLanguageSupport.FLAVOR_UNKNOWN;
        switch (langFlavor.second()) {
            case C:
            case C89:
            case C99:
            case C11:
                aptFlavor = APTLanguageSupport.FLAVOR_UNKNOWN;
                break;
                
            case CPP98:
                aptFlavor = APTLanguageSupport.FLAVOR_CPP98;
                break;
            case CPP11:
                aptFlavor = APTLanguageSupport.FLAVOR_CPP11;
                break;
            case CPP14:
                aptFlavor = APTLanguageSupport.FLAVOR_CPP14;
                break;
            case CPP17:
                aptFlavor = APTLanguageSupport.FLAVOR_CPP17;
                break;
                
            case F77:
            case F90:
            case F95:
                aptFlavor = APTLanguageSupport.FLAVOR_FORTRAN_FREE;
                break;
        }
        return Pair.of(aptLang, aptFlavor);
    }

    @Override
    public Collection<CsmCompilationUnit> getCompilationUnits(CsmFile file, int contextOffset) {
        Collection<CsmCompilationUnit> out = new ArrayList<>(1);
        boolean addBackup = true;
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            ProjectBase prjImpl = (ProjectBase) impl.getProject();
            Collection<State> states = prjImpl.getIncludedPreprocStates(impl);
            // put TUs from other projects at the end of out list
            Collection<CsmCompilationUnit> otherPrjCUs = new ArrayList<>(1);
            for (State state : states) {
                StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
                ProjectBase startProject = Utils.getStartProject(startEntry);
                if (startProject != null) {
                    CharSequence path = startEntry.getStartFile();
                    CsmFile startFile = startProject.getFile(path, false);
                    if (startFile != null) {
                        addBackup = false;
                    }
                    CsmCompilationUnit cu = CsmCompilationUnit.createCompilationUnit(startProject, path, startFile);
                    if (prjImpl.equals(startProject)) {
                        out.add(cu);
                    } else {
                        otherPrjCUs.add(cu);
                    }
                }
            }
            out.addAll(otherPrjCUs);
        }
        if (addBackup) {
            out.add(CsmCompilationUnit.createCompilationUnit(file.getProject(), file.getAbsolutePath(), file));
        }
        return out;
    }

    @Override
    public List<CsmInclude> getIncludeStack(CsmErrorDirective err) {
        PreprocHandler.State state = null;
        if (err instanceof ErrorDirectiveImpl) {
            state = ((ErrorDirectiveImpl)err).getState();
        }
        return getIncludeStackImpl(state);
    }

    @Override
    public List<CsmInclude> getIncludeStack(CsmInclude inc) {
        return getIncludeStack(inc.getContainingFile());
    }
    
    @Override
    public List<CsmInclude> getIncludeStack(CsmFile file) {
        PreprocHandler.State state = null;
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            // use stack from one of states (i.e. first)
            CharSequence fileKey = FileContainer.getFileKey(impl.getAbsolutePath(), false);
            state = ((ProjectBase) impl.getProject()).getFirstValidPreprocState(fileKey);
        }
        return getIncludeStackImpl(state);
    }
    
    private List<CsmInclude> getIncludeStackImpl(PreprocHandler.State state) {
        if (state == null) {
            return Collections.<CsmInclude>emptyList();
        }
        CndUtils.assertNotNull(state, "state must not be null in non empty collection");// NOI18N
        List<APTIncludeHandler.IncludeInfo> includeChain = APTHandlersSupport.extractIncludeStack(state);
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = Utils.getStartProject(startEntry);
        if (startProject != null) {
            CsmFile includeOwner = startProject.getFile(startEntry.getStartFile(), false);
            if (includeOwner != null) {
                List<CsmInclude> res = new ArrayList<>();
                Iterator<APTIncludeHandler.IncludeInfo> it = includeChain.iterator();
                while(it.hasNext()){
                    APTIncludeHandler.IncludeInfo info = it.next();
                    int includeDirectiveIndex = info.getIncludeDirectiveIndex();
                    CharSequence includedPath = info.getIncludedPath();
                    CsmInclude foundDirective = null;
                    CsmFile    foundIncludedFile = null;
                    for(CsmInclude inc : includeOwner.getIncludes()) {
                        int currentIncludeIndex = getIncludeDirectiveIndex(inc);
                        // fast check by index
                        if (includeDirectiveIndex == currentIncludeIndex) {
                            // but several CsmInclude directives
                            // in one header can have same index in different include states
                            CsmFile includedFile = inc.getIncludeFile();
                            // so check by expected name
                            if (includedFile != null && CharSequenceUtils.contentEquals(includedPath, includedFile.getAbsolutePath())) {
                                foundDirective = inc;
                                foundIncludedFile = includedFile;
                                if (inc.getStartOffset() == info.getIncludeDirectiveOffset()) {
                                    // full match
                                    break;
                                } else {
                                    // keep it as the best candidate
                                }
                            }
                        }
                    }
                    if (foundDirective == null) {
                        // break broken include chain
                        return Collections.<CsmInclude>emptyList();
                    } else {
                        assert foundIncludedFile != null : "must be initialized with " + foundDirective;
                        includeOwner = foundIncludedFile;
                        res.add(foundDirective);
                    }
                }
                return res;
            }
        }
        return Collections.<CsmInclude>emptyList();
    }

    @Override
    public boolean hasBrokenIncludes(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            boolean res = impl.hasBrokenIncludes();
            if (res) {
                if (isStandardHeadersIndexer(file)) {
                    return false;
                }
            }
            return res;
        }
        return false;
    }

    @Override
    public boolean isStandardHeadersIndexer(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            NativeFileItem nativeFileItem = impl.getNativeFileItem();
            if (nativeFileItem != null) {
                NativeProject nativeProject = nativeFileItem.getNativeProject();
                if (nativeProject != null) {
                    if (nativeProject.getStandardHeadersIndexers().contains(nativeFileItem)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Collection<CsmInclude> getBrokenIncludes(CsmFile file) {
        if (file instanceof FileImpl) {
            if (((FileImpl) file).hasBrokenIncludes()) {
                return ((FileImpl) file).getBrokenIncludes();
            }
        }
        return Collections.<CsmInclude>emptyList();
    }

    @Override
    public long getFileVersion(CsmFile file) {
        if (file instanceof FileImpl) {
            return FileImpl.getLongParseCount();
        }
        return 0;
    }

   @Override
    public long getOffset(CsmFile file, int line, int column) {
        if (file instanceof FileImpl) {
            return ((FileImpl) file).getOffset(line, column);
        }
        return 0;
    }

    @Override
    public int getLineCount(CsmFile file) {
        if (file instanceof FileImpl) {
            try {
                return ((FileImpl) file).getBuffer().getLineCount();
            } catch (IOException ex) {
                CndUtils.assertTrueInConsole(false, ex.getMessage());
            }
        }
        return 0;
    }   

    @Override
    public int[] getLineColumnByOffset(CsmFile file, int offset) {
        if (file instanceof FileImpl) {
            return ((FileImpl)file).getLineColumn(offset);
        }
        return new int[]{0, 0};
    }
    
    private static final class ProxyReference implements CsmReference {
        private final CsmReference delegate;
        private javax.swing.text.Position startPosition;
        private javax.swing.text.Position endPosition;
        
        private ProxyReference(CsmReference delegate, Document doc) {
            this.delegate = delegate;
            try {
                startPosition = NbDocument.createPosition(doc, delegate.getStartOffset(), javax.swing.text.Position.Bias.Forward);
                endPosition = NbDocument.createPosition(doc, delegate.getEndOffset(), javax.swing.text.Position.Bias.Backward);
            } catch (BadLocationException ex) {
            }
        }

        @Override
        public CsmReferenceKind getKind() {
            return delegate.getKind();
        }

        @Override
        public CsmObject getReferencedObject() {
            return delegate.getReferencedObject();
        }

        @Override
        public CsmObject getOwner() {
            return delegate.getOwner();
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return delegate.getClosestTopLevelObject();
        }

        @Override
        public CsmFile getContainingFile() {
            return delegate.getContainingFile();
        }

        @Override
        public int getStartOffset() {
            return getStartPosition().getOffset();
        }

        @Override
        public int getEndOffset() {
            return getEndPosition().getOffset();
        }

        @Override
        public Position getStartPosition() {
            return new ProxyPosition(startPosition, delegate.getStartPosition());
        }

        @Override
        public Position getEndPosition() {
            return new ProxyPosition(endPosition, delegate.getEndPosition());
        }

        @Override
        public CharSequence getText() {
            return delegate.getText();
        }
    }
    
    private static final class ProxyPosition implements CsmOffsetable.Position {
        private final javax.swing.text.Position position;
        private final CsmOffsetable.Position owner;
        private ProxyPosition(javax.swing.text.Position delegate, CsmOffsetable.Position owner) {
            this.position = delegate;
            this.owner = owner;
        }

        @Override
        public int getOffset() {
            if (position != null) {
                return position.getOffset();
            } else {
                return owner.getOffset();
            }
        }

        @Override
        public int getLine() {
            return owner.getLine();
        }

        @Override
        public int getColumn() {
            return owner.getColumn();
        }
    }
}
