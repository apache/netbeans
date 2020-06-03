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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.NbBundle;

/**
 * Misc static methods used for processing in Clank mode.
 */
/*package*/final class ClankToCsmSupport {

    private ClankToCsmSupport() {
    }

    /*package*/static void addPreprocessorDirectives(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankPreprocessorOutput ppOutput) {
        assert parsingFileContent != null;
        assert curFile != null;
        assert ppOutput != null;
        for (ClankDriver.ClankPreprocessorDirective cur : ppOutput.getPreprocessorDirectives()) {
            if (cur instanceof ClankDriver.ClankInclusionDirective) {
                ClankToCsmSupport.addInclude(curFile, parsingFileContent, (ClankDriver.ClankInclusionDirective)cur);
            } else if (cur instanceof ClankDriver.ClankErrorDirective) {
                ClankToCsmSupport.addError(curFile, parsingFileContent, (ClankDriver.ClankErrorDirective)cur);
            } else if (cur instanceof ClankDriver.ClankMacroDirective) {
                ClankToCsmSupport.addMacro(curFile, parsingFileContent, (ClankDriver.ClankMacroDirective)cur);
            } else {
              CndUtils.assertTrueInConsole(false, "unknown directive " + cur.getClass().getSimpleName() + " " + cur);
            }
        }
    }

    /*package*/static void setFileGuard(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankPreprocessorOutput ppOutput) {
        assert ppOutput != null;        
        ClankDriver.FileGuard fileGuard = ppOutput.getFileGuard();
        if (fileGuard != null) {
            curFile.setFileGuard(fileGuard.getStartOfset(), fileGuard.getEndOfset());
        } else {
            curFile.setFileGuard(-1, -1);
        }
    }

    /*package*/static void addMacroExpansions(FileImpl curFile, FileContent parsingFileContent, FileImpl startFile, ClankDriver.ClankPreprocessorOutput ppOutput) {
        assert ppOutput != null;        
        for (ClankDriver.MacroExpansion cur : ppOutput.getMacroExpansions()) {
            ClankDriver.ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                MacroReference macroRef = MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getStartOfset() + cur.getMacroNameLength(), startFile, directive);
                if (macroRef == null) {
                    if (!curFile.isValid()) {
                        break;
                    }
                } else {
                    addMacroUsage(curFile, parsingFileContent, macroRef);
                }
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective " + cur;
            }
        }
        for (ClankDriver.MacroUsage cur : ppOutput.getMacroUsages()) {
            ClankDriver.ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                MacroReference macroRef = MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getEndOfset(), startFile, directive);
                if (macroRef == null) {
                    if (!curFile.isValid()) {
                        break;
                    }
                } else {
                    addMacroUsage(curFile, parsingFileContent, macroRef);
                }
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective " + cur;
            }
        }
    }

    private static void addMacroUsage(FileImpl curFile, FileContent parsingFileContent, MacroReference macroReference) {
        parsingFileContent.addReference(macroReference, macroReference.getReferencedObject());
    }

    private static void addMacro(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankMacroDirective ppDirective) {
        if (!ppDirective.isDefined()) {
            // only #define are handled by old model, not #undef
            return;
        }
        CsmMacro.Kind kind = CsmMacro.Kind.DEFINED;
        List<CharSequence> params = ppDirective.getParameters();
        CharSequence name = ppDirective.getMacroName();
        String body = "";
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        int macroNameOffset = ppDirective.getMacroNameOffset();
        CsmMacro impl = MacroImpl.create(name, params, body/*sb.toString()*/, curFile, startOffset, endOffset, kind);
        parsingFileContent.addMacro(impl);
        parsingFileContent.addReference(new MacroDeclarationReference(curFile, impl, macroNameOffset), impl);
    }

    /*package*/static List<CsmReference> getMacroUsages(FileImpl fileImpl, FileImpl startFile, ClankDriver.ClankPreprocessorOutput ppOutput) {
        if (ppOutput == null) {
            // could be broken restoring from include chain, wait for the next parse
            return Collections.emptyList();
        }
        List<CsmReference> res = new ArrayList<>();        
        addPreprocessorDirectives(fileImpl, res, ppOutput);
        addMacroExpansions(fileImpl, res, startFile, ppOutput);
        Collections.sort(res, new Comparator<CsmReference>() {
            @Override
            public int compare(CsmReference o1, CsmReference o2) {
                return o1.getStartOffset() - o2.getStartOffset();
            }
        });
        return res;
    }

    private static void addPreprocessorDirectives(FileImpl curFile, List<CsmReference> res, ClankDriver.ClankPreprocessorOutput ppOutput) {
        assert res != null;
        assert curFile != null;
        assert ppOutput != null;
        for (ClankDriver.ClankPreprocessorDirective cur : ppOutput.getPreprocessorDirectives()) {
            if (cur instanceof ClankDriver.ClankMacroDirective) {
                addMacro(curFile, res, (ClankDriver.ClankMacroDirective) cur);
            }
        }
    }

    private static void addMacroExpansions(FileImpl curFile, List<CsmReference> res, FileImpl startFile, ClankDriver.ClankPreprocessorOutput ppOutput) {
        for (ClankDriver.MacroExpansion cur : ppOutput.getMacroExpansions()) {
            ClankDriver.ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                MacroReference macroRef = MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getStartOfset() + cur.getMacroNameLength(), startFile, directive);
                if (macroRef == null) {
                    if (!curFile.isValid()) {
                        break;
                    }
                } else {
                    res.add(macroRef);
                }
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective " + cur;
            }
        }
        for (ClankDriver.MacroUsage cur : ppOutput.getMacroUsages()) {
            ClankDriver.ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                MacroReference macroRef = MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getEndOfset(), startFile, directive);
                if (macroRef == null) {
                    if (!curFile.isValid()) {
                        break;
                    }
                } else {
                    res.add(macroRef);
                }
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective " + cur;
            }
        }
    }

    private static void addMacro(FileImpl curFile, List<CsmReference> res, ClankDriver.ClankMacroDirective ppDirective) {
        if (!ppDirective.isDefined()) {
            // only #define are handled by old model, not #undef
            return;
        }
        CsmMacro.Kind kind = CsmMacro.Kind.DEFINED;
        List<CharSequence> params = ppDirective.getParameters();
        CharSequence name = ppDirective.getMacroName();
        String body = "";
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        int macroNameOffset = ppDirective.getMacroNameOffset();
        CsmMacro impl = MacroImpl.create(name, params, body/*sb.toString()*/, curFile, startOffset, endOffset, kind);
        MacroDeclarationReference macroDeclarationReference = new MacroDeclarationReference(curFile, impl, macroNameOffset);
        res.add(macroDeclarationReference);
    }

    private static void addError(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankErrorDirective ppDirective) {
        CharSequence msg = ppDirective.getMessage();
        PreprocHandler.State state = ppDirective.getStateWhenMetErrorDirective();
        int start = ppDirective.getDirectiveStartOffset();
        int end = ppDirective.getDirectiveEndOffset();
        ErrorDirectiveImpl impl = ErrorDirectiveImpl.create(curFile, msg, new CsmOffsetableImpl(curFile, start, end), state);
        parsingFileContent.addError(impl);
    }

    private static void addInclude(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankInclusionDirective ppDirective) {
        ResolvedPath resolvedPath = ppDirective.getResolvedPath();
        CharSequence fileName = ppDirective.getSpellingName();
        boolean system = ppDirective.isAngled();
        boolean broken = (resolvedPath == null);
        Object includeAnnotation = ppDirective.getAnnotation();
        boolean unresolvedInclude = includeAnnotation == UnresolvedIncludeDirectiveReason.NULL_PATH;
        FileImpl includedFile = null;
        if (unresolvedInclude != broken) {
            if (CsmModelAccessor.isModelAlive()) {
                assert false : "broken " + broken + " vs. " + includeAnnotation + " in " + ppDirective;
            }
        }
        if (includeAnnotation instanceof FileImpl) {
            includedFile = (FileImpl)includeAnnotation;
        }
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        int includeDirectiveIndex = ppDirective.getIncludeDirectiveIndex();
        IncludeImpl incl = IncludeImpl.create(fileName.toString(), system, ppDirective.isRecursive(), includedFile, curFile, startOffset, endOffset, includeDirectiveIndex);
        parsingFileContent.addInclude(incl, broken || ppDirective.isRecursive());
    }

    private static final class CsmOffsetableImpl implements CsmOffsetable {

        private final CsmFile file;
        private final int selectionStart;
        private final int selectionEnd;

        public CsmOffsetableImpl(CsmFile file, int selectionStart, int selectionEnd) {
            this.file = file;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        @Override
        public CsmFile getContainingFile() {
            return file;
        }

        @Override
        public int getStartOffset() {
            return selectionStart;
        }

        @Override
        public int getEndOffset() {
            return selectionEnd;
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException();
        }
    }

    /*package*/static enum UnresolvedIncludeDirectiveReason {
        NULL_PATH,
        UNRESOLVED_FILE_OWNER,
        START_PROJECT_CLOSED,
        INVALID_START_PROJECT,
        START_PROJECT_CANNOT_CREATE_FILE,
        NULL_START_PROJECT;
    }

    /*package*/static final class UnresolvedIncludeDirectiveAnnotation {

        private final UnresolvedIncludeDirectiveReason reason;
        private final Object[] args;
        private final Exception stack;
        /*package*/UnresolvedIncludeDirectiveAnnotation(UnresolvedIncludeDirectiveReason reason, Object ... args) {
            this.reason = reason;
            this.args = args;
            if (CndUtils.isDebugMode()) {
                stack = new Exception();
            } else {
                stack = null;
            }
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(NbBundle.getMessage(ClankToCsmSupport.class, reason.name(), args));
            if (stack != null) {
                StackTraceElement[] stackTrace = stack.getStackTrace();
                if (stackTrace != null) {
                    for(StackTraceElement line : stackTrace) {
                        buf.append("\n\tat ").append(line.toString()); //NOI18N
                    }
                }
            }
            return buf.toString();
        }
    }
}
