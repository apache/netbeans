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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.spi.model.services.CsmReferenceStorage;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTElif;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTIf;
import org.netbeans.modules.cnd.apt.structure.APTIfdef;
import org.netbeans.modules.cnd.apt.structure.APTIfndef;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.SystemMacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Offsetable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import static org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker.getDefineOffsets;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileSystem;


/**
 * Walker to find macros used in file for semantic highlighting
 *
 */
/*package*/ final class APTFindMacrosWalker extends APTSelfWalker {
    private final List<CsmReference> references = new ArrayList<>();
    private final CsmFile csmFile;
    private final Interrupter interrupter;
    private APTFindMacrosWalker(APTFile apt, CsmFile csmFile, PreprocHandler preprocHandler, 
            APTFileCacheEntry cacheEntry, Interrupter interrupter) {
        super(apt, preprocHandler, cacheEntry);
        this.csmFile = csmFile;
        this.interrupter = interrupter;
    }

    @Override
    protected void onDefine(APT apt) {
        APTDefine defineNode = (APTDefine) apt;
        int index = references.size();
        analyzeList(defineNode.getBody());
        super.onDefine(apt);
        APTToken name = defineNode.getName();
        if (name != null) {
            APTMacro m = getMacroMap().getMacro(name);
            if (m != null) {
                MacroReference mr = new MacroReference(csmFile, name, m, CsmReferenceKind.DECLARATION);
                if (references.size() == index) {
                    references.add(mr);
                } else {
                    references.add(index, mr);
                }
            }
        }
    }

    @Override
    protected boolean onIf(APT apt) {
        analyzeStream(((APTIf) apt).getCondition(), false);
        return super.onIf(apt);
    }

    @Override
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        analyzeStream(((APTElif) apt).getCondition(), false);
        return super.onElif(apt, wasInPrevBranch);
    }

    @Override
    protected boolean onIfndef(APT apt) {
        analyzeToken(((APTIfndef) apt).getMacroName(), false);
        return super.onIfndef(apt);
    }

    @Override
    protected boolean onIfdef(APT apt) {
        analyzeToken(((APTIfdef) apt).getMacroName(), false);
        return super.onIfdef(apt);
    }

    @Override
    protected void onUndef(APT apt) {
        analyzeToken(((APTUndefine) apt).getName(), false);
        super.onUndef(apt);
    }

    @Override
    protected void onInclude(APT apt) {
        analyzeStream(((APTInclude)apt).getInclude(), true);
        super.onInclude(apt);
    }

    @Override
    protected void onIncludeNext(APT apt) {
        analyzeStream(((APTIncludeNext)apt).getInclude(), true);
        super.onIncludeNext(apt);
    }

    @Override
    protected boolean isStopped() {
      return super.isStopped() || interrupter.cancelled();
    }

    private List<CsmReference> collectMacros() {
        TokenStream ts = super.getTokenStream();
        analyzeStream(ts, true);
        return references;
    }

    private CsmReference analyzeToken(APTToken token, boolean addOnlyIfNotFunLikeMacro) {
        CsmReference mf = null;
        boolean funLike = false;
        if (token != null && !APTUtils.isEOF(token)) {
            APTMacro m = getMacroMap().getMacro(token);
            if (m != null) {
                // macro either doesn't need params or has "(" after name
                funLike = m.isFunctionLike();
                switch(m.getKind()){
                    case DEFINED:
                        mf = new MacroReference(csmFile, token, m, CsmReferenceKind.DIRECT_USAGE);
                        break;
                    case COMPILER_PREDEFINED:
                    case POSITION_PREDEFINED:
                    case USER_SPECIFIED:
                    default:
                        mf = new SysMacroReference(csmFile, token, m);
                        break;
                }
            }
        }
        if (mf != null) {
            // add any not fun-like macro
            // or add all if specified by input parameter
            if (!funLike || !addOnlyIfNotFunLikeMacro) {
                references.add(mf);
                // clear return value, because already added
                mf = null;
            }
        }
        return mf;
    }

    private void analyzeList(List<APTToken> tokens) {
        if (tokens != null) {
            for (APTToken token : tokens) {
                analyzeToken(token, false);
            }
        }
    }

    private void analyzeStream(TokenStream ts, boolean checkFunLikeMacro) {
        if (ts != null) {
            try {
                for (APTToken token = (APTToken) ts.nextToken(); !APTUtils.isEOF(token); ) {
                    CsmReference mr = analyzeToken(token, checkFunLikeMacro);
                    token = (APTToken) ts.nextToken();
                    if (mr != null) {
                        // it is fun-like macro candidate
                        assert checkFunLikeMacro;
                        // add only if next token is "("
                        if (token.getType() == APTTokenTypes.LPAREN) {
                            references.add(mr);
                        }
                    }
                }
            } catch (TokenStreamException ex) {
		DiagnosticExceptoins.register(ex);
            }
        }
    }

    private static final class SysMacroReference extends OffsetableBase implements CsmReference {

        private final CsmObject ref;
        private final CharSequence text;
        public SysMacroReference(CsmFile file, APTToken token, APTMacro macro) {
            super(file, token.getOffset(), token.getEndOffset());
            text = token.getTextID();
            CsmMacro.Kind kind;
            switch(macro.getKind()) {
                case COMPILER_PREDEFINED:
                    kind = CsmMacro.Kind.COMPILER_PREDEFINED;
                    break;
                case POSITION_PREDEFINED:
                    kind = CsmMacro.Kind.POSITION_PREDEFINED;
                    break;
                case DEFINED:
                    kind = CsmMacro.Kind.DEFINED;
                    break;
                case USER_SPECIFIED:
                    kind = CsmMacro.Kind.USER_SPECIFIED;
                    break;
                default:
                    System.err.println("unexpected kind in macro " + macro);
                    kind = CsmMacro.Kind.USER_SPECIFIED;
                    break;
            }
            ref = SystemMacroImpl.create(token.getTextID(), APTUtils.stringize(macro.getBody(), false), null, ((ProjectBase)file.getProject()).getUnresolvedFile(), kind);
        }

        @Override
        public CsmObject getReferencedObject() {
            return ref;
        }

        @Override
        public CsmObject getOwner() {
            return getContainingFile();
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DIRECT_USAGE;
        }

        @Override
        public CharSequence getText() {
            return text;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return getContainingFile();
        }
    }

    private static final class MacroReference extends OffsetableBase implements CsmReference {

        private volatile CsmMacro ref = null;
        private final CharSequence macroName;
        private final APTMacro macro;
        private final CsmReferenceKind kind;
        public MacroReference(CsmFile macroUsageFile, APTToken macroUsageToken, APTMacro macro, CsmReferenceKind kind) {
            super(macroUsageFile, macroUsageToken.getOffset(), macroUsageToken.getEndOffset());
            this.macroName = macroUsageToken.getTextID();
            assert macroName != null;
            this.macro = macro;
            this.kind = kind;
        }

        @Override
        public CsmObject getReferencedObject() {
            CsmMacro refObj = ref;
            if (refObj == null) {
                CsmReference candidate = CsmReferenceStorage.getDefault().get(this);
                if (candidate != null) {
                    CsmObject referencedObject = candidate.getReferencedObject();
                    if (referencedObject instanceof CsmMacro) {
                        refObj = (CsmMacro) referencedObject;
                    } else if (referencedObject != null){
                        Logger.getLogger("xRef").log(Level.INFO, "Reference storage returns {0} where is expected macro\n", referencedObject); //NOI18N
                    }
                }
            }
            if (refObj == null && macro != null) {
                synchronized (this) {
                    refObj = ref;
                    if (refObj == null) {
                        int macroStartOffset = macro.getDefineNode().getOffset();
                        CsmFile target = getTargetFile();
                        if (target != null) {
                            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(macroName, true, true, false);
                            for (Iterator<CsmMacro> it = CsmSelect.getMacros(target, filter); it.hasNext();) {
                                CsmMacro targetFileMacro = it.next();
                                if (targetFileMacro!=null && macroStartOffset == targetFileMacro.getStartOffset()) {
                                    refObj = targetFileMacro;
                                    break;
                                }
                            }
                            if (refObj == null) {
                                // reference was made so it was macro during APTFindMacrosWalker's walk. Parser missed this variance of header and
                                // we have to create MacroImpl for skipped filepart on the spot (see IZ#130897)
                                if (target instanceof Unresolved.UnresolvedFile) {
                                    refObj = SystemMacroImpl.create(macroName, "", null, target, CsmMacro.Kind.USER_SPECIFIED);
                                } else {
                                    // TODO: maybe in future lastParam should be passed, but now it is unused anyway
                                    int offsets[] = getDefineOffsets(macro.getDefineNode(), null);
                                    refObj = MacroImpl.create(
                                        macroName, 
                                        null, 
                                        "", 
                                        target, 
                                        offsets[0], 
                                        offsets[1], 
                                        CsmMacro.Kind.DEFINED
                                    );
                                    Utils.setSelfUID(refObj);
                                }
                            }
                        }
                        ref = refObj;
                    }
                }
            }
            return refObj;
        }

        private CsmFile getTargetFile() {
            CsmFile current = this.getContainingFile();
            CsmFile target;
            if (kind == CsmReferenceKind.DECLARATION) {
                target = current;
            } else {
                target = null;
                CharSequence macroContainerFile = macro.getFile();
                if (current != null && macroContainerFile.length() > 0) {
                    FileSystem fs = null;
                    ProjectBase currentPrj = (ProjectBase) current.getProject();
                    ProjectBase targetPrj = currentPrj.findFileProject(macroContainerFile, true);
                    if (targetPrj != null) {
                        target = targetPrj.findFile(macroContainerFile, true, false);
                        fs = targetPrj.getFileSystem();
                    } else {
                        fs = currentPrj.getFileSystem();
                    }
                    // try full model?
                    if (target == null) {
                        target = CsmModelAccessor.getModel().findFile(new FSPath(fs, macroContainerFile.toString()), true, false);
                    }
                    if (target == null && targetPrj != null) {
                        target = targetPrj.getUnresolvedFile();
                    }
                    if (target == null) {
                        target = currentPrj.getUnresolvedFile();
                    }
                }
            }
            return target;
        }

        @Override
        public CsmObject getOwner() {
            return (kind == CsmReferenceKind.DECLARATION) ? getContainingFile() : getReferencedObject();
        }
        
        @Override
        public CsmReferenceKind getKind() {
            return kind;
        }

        @Override
        public CharSequence getText() {
            return macroName;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return (kind == CsmReferenceKind.DECLARATION) ? getContainingFile() : getReferencedObject();
        }
    }
    
    /*package*/ static List<CsmReference> getAPTMacroUsagesImpl(FileImpl fileImpl, final Interrupter interrupter) throws IOException {
      List<CsmReference> out = Collections.<CsmReference>emptyList();
      APTFile apt = APTDriver.findAPT(fileImpl.getBuffer(), fileImpl.getAPTFileKind());
      if (apt != null) {
        Collection<PreprocHandler> handlers = fileImpl.getPreprocHandlersForParse(interrupter);
        if (interrupter.cancelled()) {
          return out;
        }
        if (handlers.isEmpty()) {
          DiagnosticExceptoins.register(new IllegalStateException("Empty preprocessor handlers for " + fileImpl.getAbsolutePath())); //NOI18N
          return Collections.<CsmReference>emptyList();
        } else if (handlers.size() == 1) {
          PreprocHandler handler = handlers.iterator().next();
          PreprocHandler.State state = handler.getState();
          // ask for concurrent entry if absent
          APTFileCacheEntry cacheEntry = fileImpl.getAPTCacheEntry(state, Boolean.FALSE);
          APTFindMacrosWalker walker = new APTFindMacrosWalker(apt, fileImpl, handler, cacheEntry, interrupter);
          out = walker.collectMacros();
          // remember walk info
          fileImpl.setAPTCacheEntry(state, cacheEntry, false);
        } else {
          TreeSet<CsmReference> result = new TreeSet<>(CsmOffsetable.OFFSET_COMPARATOR);
          for (PreprocHandler handler : handlers) {
            // ask for concurrent entry if absent
            PreprocHandler.State state = handler.getState();
            APTFileCacheEntry cacheEntry = fileImpl.getAPTCacheEntry(state, Boolean.FALSE);
            APTFindMacrosWalker walker = new APTFindMacrosWalker(apt, fileImpl, handler, cacheEntry, interrupter);
            result.addAll(walker.collectMacros());
            // remember walk info
            fileImpl.setAPTCacheEntry(state, cacheEntry, false);
          }
          out = new ArrayList<>(result);
        }
      }
      return out;
    }
    
    /*package*/ static boolean hasGuardBlockImpl(FileImpl fileImpl) {
        try {
            APTFile apt = APTDriver.findAPT(fileImpl.getBuffer(), fileImpl.getAPTFileKind());
            if (apt.getGuardMacro().length() > 0) {
                return true;
            }
        } catch (FileNotFoundException ex) {
            // file could be removed
        } catch (IOException ex) {
            System.err.println("IOExeption in getGuardOffset:" + ex.getMessage()); //NOI18N
        }
        return false;
    }
    
    /*package*/ static CsmOffsetable getGuardOffsetImpl(FileImpl fileImpl) {
      assert !APTTraceFlags.USE_CLANK;
        try {
            APTFile apt = APTDriver.findAPT(fileImpl.getBuffer(), fileImpl.getAPTFileKind());

            GuardBlockWalker guardWalker = new GuardBlockWalker(apt);
            TokenStream ts = guardWalker.getTokenStream();
            try {
                Token token = ts.nextToken();
                while (!APTUtils.isEOF(token)) {
                    if (!APTUtils.isCommentToken(token)) {
                        guardWalker.clearGuard();
                        break;
                    }
                    token = ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                guardWalker.clearGuard();
            }

            Token guard = guardWalker.getGuard();
            if (guard != null) {
                if (guard instanceof APTToken) {
                    APTToken aptGuard = ((APTToken) guard);
                    return new Offsetable(fileImpl, aptGuard.getOffset(), aptGuard.getEndOffset());
                }
            }
        } catch (FileNotFoundException ex) {
            // file could be removed
        } catch (IOException ex) {
            System.err.println("IOExeption in getGuardOffset:" + ex.getMessage()); //NOI18N
        }
        return null;
    }
}
