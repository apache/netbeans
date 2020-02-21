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
package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Implementation of the hyperlink provider for java language.
 * <br>
 * The hyperlinks are constructed for #include directives.
 * <br>
 * The click action corresponds to performing the open file action.
 *
 */
public class CsmIncludeHyperlinkProvider extends CsmAbstractHyperlinkProvider {

    private static final boolean NEED_TO_TRACE_UNRESOLVED_INCLUDE = CndUtils.getBoolean("cnd.modelimpl.trace.failed.include", false); // NOI18N
    private final static boolean TRACE_INCLUDES = CndUtils.getBoolean("cnd.trace.includes", false); // NOI18N

    /** Creates a new instance of CsmIncludeHyperlinkProvider */
    public CsmIncludeHyperlinkProvider() {
    }

    @Override
    protected boolean isValidToken(TokenItem<TokenId> token, HyperlinkType type) {
        return isSupportedToken(token, type);
    }

    public static boolean isSupportedToken(TokenItem<TokenId> token, HyperlinkType type) {
        if (token != null) {
            if (type == HyperlinkType.ALT_HYPERLINK) {
                return !CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory()) &&
                        !CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory());
            }
            if (token.id() == CppTokenId.PREPROCESSOR_INCLUDE ||
                    token.id() == CppTokenId.PREPROCESSOR_INCLUDE_NEXT ||
                    token.id() == CppTokenId.PREPROCESSOR_SYS_INCLUDE ||
                    token.id() == CppTokenId.PREPROCESSOR_USER_INCLUDE ||
                    token.id() == CppTokenId.PREPROCESSOR_ERROR) {
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void performAction(final Document originalDoc, final JTextComponent target, final int offset, final HyperlinkType type) {
        UIGesturesSupport.submit("USG_CND_INCLUDE_HYPERLINK", type); //NOI18N
        goToInclude(originalDoc, target, offset, type);
    }

    public boolean goToInclude(Document doc, JTextComponent target, int offset, HyperlinkType type) {
        if (!preJump(doc, target, offset, "opening-include-element", type)) { //NOI18N
            return false;
        }
        IncludeTarget item = findTargetObject(doc, offset);
        if (type == HyperlinkType.ALT_HYPERLINK && ((item != null))) {
            CsmInclude incl = item.getInclude();
            CsmFile toShow = incl.getIncludeFile();
            if (toShow == null) {
                toShow = incl.getContainingFile();
            } else {
                CsmInclude brokenInclude = getFirstBrokenIncludeInsideIncludedFiles(toShow, new HashSet<CsmFile>());
                if (brokenInclude != null) {
                    toShow = brokenInclude.getContainingFile();
                    // jump into problem file
                    return postJump(brokenInclude, "goto_source_source_not_found", "cannot-open-include-element");//NOI18N
                }
            }
            CsmIncludeHierarchyResolver.showIncludeHierachyView(toShow);
            return true;
        } else {
            return postJump(item, "goto_source_source_not_found", "cannot-open-include-element");//NOI18N
        }
    }

    /*package*/ IncludeTarget findTargetObject(final Document doc, final int offset) {
        CsmInclude incl = findInclude(doc, offset);
        IncludeTarget item = incl == null ? null : new IncludeTarget(incl);
        if (incl != null && NEED_TO_TRACE_UNRESOLVED_INCLUDE && incl.getIncludeFile() == null) {
            System.setProperty("cnd.modelimpl.trace.trace_now", "yes"); //NOI18N
            try {
                incl.getIncludeFile();
            } finally {
                System.setProperty("cnd.modelimpl.trace.trace_now", "no"); //NOI18N
            }
        }
        return item;
    }

    private CsmInclude findInclude(Document doc, int offset) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, true, false);
        if (csmFile != null) {
            return ReferencesSupport.findInclude(csmFile, offset);
        }
        return null;
    }

    private static final class IncludeTarget implements CsmOffsetable {

        private final CsmInclude include;

        public IncludeTarget(CsmInclude include) {
            this.include = include;
        }

        public CsmInclude getInclude() {
            return include;
        }

        @Override
        public CsmFile getContainingFile() {
            return include.getIncludeFile();
        }

        @Override
        public int getStartOffset() {
            // start of the file
            return DUMMY_POSITION.getOffset();
        }

        @Override
        public int getEndOffset() {
            // DUMMY of the file
            return DUMMY_POSITION.getOffset();
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public CharSequence getText() {
            return include.getIncludeName();
        }
    }
    private static final CsmOffsetable.Position DUMMY_POSITION = new CsmOffsetable.Position() {

        @Override
        public int getOffset() {
            return -1;
        }

        @Override
        public int getLine() {
            return -1;
        }

        @Override
        public int getColumn() {
            return -1;
        }
    };

    @Override
    protected String getTooltipText(Document doc, TokenItem<TokenId> token, int offset, HyperlinkType type) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, true, false);
        if (csmFile == null) {
            return null;
        }
        CsmInclude includeTarget = ReferencesSupport.findInclude(csmFile, offset);
        CsmErrorDirective errorDirective = null;
        if (includeTarget == null) {
            errorDirective = ReferencesSupport.findErrorDirective(csmFile, offset);
        }
        CharSequence tooltip = null;
        if (includeTarget != null) {
            tooltip = CsmDisplayUtilities.getTooltipText(includeTarget);
        } else if (errorDirective != null) {
            tooltip = CsmDisplayUtilities.getTooltipText(errorDirective);
        }
        boolean extraText = (type == HyperlinkType.ALT_HYPERLINK);
        if (tooltip != null) {
            StringBuilder buf = new StringBuilder();
            String altKey = "AltIncludeHyperlinkHint"; // NOI18N
            List<CsmInclude> includeStack;
            if (includeTarget != null) {
                includeStack = CsmFileInfoQuery.getDefault().getIncludeStack(csmFile);
            } else {
                includeStack = CsmFileInfoQuery.getDefault().getIncludeStack(errorDirective);
            }
            CsmFile includedFile = includeTarget == null ? null : includeTarget.getIncludeFile();
            if (includedFile != null) {
                // check if inside file include hierarchy we have unresolved includes
                CsmInclude brokenInclude = getFirstBrokenIncludeInsideIncludedFiles(includedFile, new HashSet<CsmFile>());
                if (brokenInclude != null) {
                    altKey = "AltIncludeHyperlinkHintBrokenIncludeFile"; // NOI18N
                    buf = new StringBuilder(tooltip);
                    String inclString;
                    if (brokenInclude.isSystem()) {
                        inclString = "#include <" + brokenInclude.getIncludeName() + ">"; // NOI18N
                    } else {
                        inclString = "#include \"" + brokenInclude.getIncludeName() + "\""; // NOI18N
                    }
                    String key = brokenInclude.getIncludeState() == CsmInclude.IncludeState.Recursive ? "RecursionInFile" : "UnresolvedInFile"; // NOI18N
                    buf.append(i18n(key, CsmDisplayUtilities.htmlize(inclString), brokenInclude.getContainingFile().getAbsolutePath()));
                    tooltip = buf.toString();
                }
            }
            if (extraText || includedFile == null) {
                buf = new StringBuilder(tooltip);
                buf.append("<br><pre>"); // NOI18N
                // append search paths
                appendPaths(buf, i18n("SourceUserPaths"), CsmFileInfoQuery.getDefault().getUserIncludePaths(csmFile));// NOI18N
                appendPaths(buf, i18n("SourceSystemPaths"), CsmFileInfoQuery.getDefault().getSystemIncludePaths(csmFile));// NOI18N
                // append include stack
                appendInclStack(buf, includeStack);
                buf.append("</pre>"); // NOI18N
                altKey = "AltIncludeHyperlinkHintNoPaths";// NOI18N
                tooltip = buf.toString();
            }
            // for testing put info into output window
            if (extraText && (TRACE_INCLUDES || NEED_TO_TRACE_UNRESOLVED_INCLUDE)) {
                InputOutput io = IOProvider.getDefault().getIO("Test Inlcudes", false); // NOI18N
                OutputWriter out = io.getOut();
                if (!includeStack.isEmpty()) {
                    try {
                        out.println("path to file " + csmFile.getAbsolutePath(), new RefLink(csmFile)); // NOI18N
                        for (CsmInclude incl : includeStack) {
                            out.println(incl.getText() + " from file " + incl.getContainingFile().getAbsolutePath(), new RefLink(incl)); // NOI18N
                        }
                    } catch (IOException iOException) {
                    }
                }
                out.println(buf.toString().replaceAll("<br>", "\n"));   // NOI18N             
                out.flush();
            }
            tooltip = getAlternativeHyperlinkTip(doc, altKey, tooltip);
        }
        return tooltip == null ? null : tooltip.toString();
    }

    private void appendInclStack(StringBuilder buf, List<CsmInclude> includeStack) {
        if (!includeStack.isEmpty()) {
            buf.append("<i>").append(i18n("PathToCurFile")).append("</i>\n");  // NOI18N
            for (CsmInclude inc : includeStack) {
                if (inc != null) {
                    final CsmFile file = inc.getContainingFile();
                    CharSequence path = null;
                    if (file != null) {
                        path = file.getAbsolutePath();
                    }
                    if (path == null) {
                        path = "?"; //NOI18N
                    }
                    final CsmOffsetable.Position startPosition = inc.getStartPosition();
                    int line = -1;
                    if (startPosition != null) {
                        line = startPosition.getLine();
                    }
                    if (file != null && startPosition != null){
                        String msg = i18n("PathToHeaderOnLine", path.toString(), line); // NOI18N
                        buf.append(msg).append('\n');
                    }
                }
            }
        }
    }

    private void appendPaths(StringBuilder buf, String title, List<IncludePath> includePaths) {
        if (!includePaths.isEmpty()) {
            buf.append("<i>").append(title).append("</i>\n");  // NOI18N
            for (IncludePath path : includePaths) {
                FileObject fo = path.getFSPath().getFileObject();
                if (fo !=  null && fo.isValid() && fo.isFolder()) {
                    buf.append(path.getFSPath().getPath());
                    if (path.isFramework()) {
                        buf.append(" (framework directory)"); // NOI18N
                    }
                } else if (fo !=  null && fo.isValid() && fo.isData()) {
                    buf.append("<font color='blue'>");  // NOI18N
                    buf.append(path.getFSPath().getPath());
                    buf.append("</font>");  // NOI18N
                } else {
                    buf.append("<font color='red'>");  // NOI18N
                    buf.append(path.getFSPath().getPath());
                    buf.append("</font>");  // NOI18N
                }
                buf.append('\n');
            }
        }
    }

    private String i18n(String key) {
        return NbBundle.getMessage(CsmIncludeHyperlinkProvider.class, key);
    }

    private String i18n(String key, Object param1, Object param2) {
        return NbBundle.getMessage(CsmIncludeHyperlinkProvider.class, key, param1, param2);
    }

    private static final class RefLink implements OutputListener {

        private final CsmUID<? extends CsmObject> uid;

        public RefLink(CsmInclude incl) {
            uid = UIDs.get(incl);
        }

        public RefLink(CsmFile file) {
            uid = UIDs.get(file);
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            CsmObject obj = uid.getObject();
            if (obj != null) {
                CsmUtilities.openSource(obj);
            }
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }

    private CsmInclude getFirstBrokenIncludeInsideIncludedFiles(CsmFile file, Collection<CsmFile> visited) {
        if (visited.contains(file)) {
            return null;
        }
        visited.add(file);
        if (CsmFileInfoQuery.getDefault().hasBrokenIncludes(file)) {
            Collection<CsmInclude> brokenIncludes = CsmFileInfoQuery.getDefault().getBrokenIncludes(file);
            if (!brokenIncludes.isEmpty()) {
                return brokenIncludes.iterator().next();
            }
        }
        for (CsmInclude incl : file.getIncludes()) {
            CsmFile newFile = incl.getIncludeFile();
            if (newFile != null) {
                CsmInclude brokenIncl = getFirstBrokenIncludeInsideIncludedFiles(newFile, visited);
                if (brokenIncl != null) {
                    return brokenIncl;
                }
            }
        }
        return null;
    }
}
