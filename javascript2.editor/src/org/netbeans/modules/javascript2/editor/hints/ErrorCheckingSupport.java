/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.hints;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public final class ErrorCheckingSupport {

    private static RequestProcessor RP = new RequestProcessor(ErrorCheckingSupport.class);

    private static final String DISABLE_ERROR_CHECKS_KEY = ErrorCheckingSupport.class.getName() + ".disableErrorChecking"; //NOI18N

    public static boolean isErrorCheckingEnabled(Parser.Result result, String mimeType) {
        return isErrorCheckingEnabledForFile(result)
                && isErrorCheckingEnabledForMimetype(mimeType);
    }

    public static boolean isErrorCheckingEnabledForFile(Parser.Result result) {
        FileObject fo = result.getSnapshot().getSource().getFileObject();
        return fo == null || fo.getAttribute(DISABLE_ERROR_CHECKS_KEY) == null;
    }

    public static boolean isErrorCheckingEnabledForMimetype(String mimeType) {
        Preferences prefs = NbPreferences.forModule(ErrorCheckingSupport.class);
        boolean enabled = isHtmlMimeType(mimeType);
        Preferences user = prefs.node(ErrorCheckingSupport.class.getName());
        enabled = user.getBoolean(mimeType, enabled);
        return enabled;
    }

    public static void setErrorCheckingEnabledForMimetype(String mimeType, boolean enabled) {
        Preferences prefs = NbPreferences.forModule(ErrorCheckingSupport.class);
        Preferences user = prefs.node(ErrorCheckingSupport.class.getName());
        if (isHtmlMimeType(mimeType) && enabled) {
            user.remove(mimeType);
        } else {
            user.putBoolean(mimeType, enabled);
        }
    }

    public static String getMimeType(Parser.Result result) {
        SyntaxAnalyzerResult syntax = getSyntaxAnalyzerResult(result);
        if (syntax != null) {
            return Utils.getWebPageMimeType(syntax);
        }
        FileObject fo = result.getSnapshot().getSource().getFileObject();
        if (fo != null) {
            return fo.getMIMEType();
        } else {
            // no fileobject?
            return result.getSnapshot().getMimeType();
        }
    }

    private static SyntaxAnalyzerResult getSyntaxAnalyzerResult(Parser.Result result) {
        if (result instanceof HtmlParserResult) {
            SyntaxAnalyzerResult saresult = ((HtmlParserResult) result).getSyntaxAnalyzerResult();
            return saresult;
        }

        Snapshot snapshot = result.getSnapshot();
        // safeguard
        if (!containsHtml(snapshot)) {
            return null;
        }

        final AtomicReference<SyntaxAnalyzerResult> res = new AtomicReference<SyntaxAnalyzerResult>();
        try {
            ParserManager.parse(Collections.singletonList(snapshot.getSource()), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result r = resultIterator.getParserResult();
                    if (r instanceof HtmlParserResult) {
                        SyntaxAnalyzerResult saresult = ((HtmlParserResult) r).getSyntaxAnalyzerResult();
                        res.set(saresult);
                        return;
                    }
                    for (Embedding e : resultIterator.getEmbeddings()) {
                        run(resultIterator.getResultIterator(e));
                    }
                }
            });
        } catch (ParseException ex) {
            // XXX
            return null;
        }
        return res.get();
    }

    public static HintFix createErrorFixForFile(Snapshot snapshot, boolean enable) {
        return new ErrorChecksFileFix(snapshot, enable);
    }

    public static HintFix createErrorFixForMimeType(Snapshot snapshot, String mimeType, boolean enable) {
        return new ErrorChecksMimeTypeFix(snapshot, mimeType, enable);
    }

    private static boolean containsHtml(Snapshot snapshot) {
        for (MimePath path : snapshot.getMimePath().getIncludedPaths()) {
            if (path.getPath().startsWith("text/html") || path.getPath().startsWith("text/xhtml")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    private static boolean isHtmlMimeType(String mimetype) {
        return "text/html".equals(mimetype) || "text/xhtml".equals(mimetype); // NOI18N
    }

    private static void reindexFile(final FileObject fo) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                //refresh Action Items for this file
                IndexingManager.getDefault().refreshIndexAndWait(fo.getParent().toURL(),
                        Collections.singleton(fo.toURL()), true, false);
            }
        });
    }

    private static void reindexActionItems() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                //refresh all Action Items
                IndexingManager.getDefault().refreshAllIndices("TLIndexer"); //NOI18N
            }
        });

    }

    private static void refreshDocument(final FileObject fo) throws IOException {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
                    StyledDocument document = editorCookie.openDocument();
                    forceReparse(document);
                } catch (IOException  ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

    }

    private static void forceReparse(final Document doc) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NbEditorDocument nbdoc = (NbEditorDocument) doc;
                nbdoc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                        if (mti != null) {
                            mti.tokenHierarchyControl().rebuild();
                        }
                    }
                });
            }
        });
    }

    private static final class ErrorChecksFileFix implements HintFix {

        private final FileObject fo;

        private final boolean enable;

        public ErrorChecksFileFix(Snapshot snapshot, boolean enable) {
            this.fo = snapshot.getSource().getFileObject();
            this.enable = enable;
        }

        @NbBundle.Messages({
            "MSG_HINT_ENABLE_ERROR_CHECKS_FILE=Enable JavaScript error checking for this file",
            "MSG_HINT_DISABLE_ERROR_CHECKS_FILE=Disable JavaScript error checking for this file"
        })
        @Override
        public String getDescription() {
            if (enable) {
                return Bundle.MSG_HINT_ENABLE_ERROR_CHECKS_FILE();
            } else {
                return Bundle.MSG_HINT_DISABLE_ERROR_CHECKS_FILE();
            }
        }

        @Override
        public void implement() throws Exception {
            if (fo == null) {
                return;
            }


            if (enable) {
                fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, null);
            } else {
                fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, Boolean.TRUE);
            }

            //refresh Action Items for this file
            reindexFile(fo);
            refreshDocument(fo);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static final class ErrorChecksMimeTypeFix implements HintFix {

        private final FileObject fo;

        private final String mimeType;

        private final boolean enable;

        public ErrorChecksMimeTypeFix(Snapshot snapshot, String mimeType, boolean enable) {
            this.fo = snapshot.getSource().getFileObject();
            this.mimeType = mimeType;
            this.enable = enable;
        }

        @NbBundle.Messages({
            "# {0} - file mime type",
            "MSG_HINT_ENABLE_ERROR_CHECKS_MIMETYPE=Enable JavaScript error checking for all files of the {0} mimetype",
            "# {0} - file mime type",
            "MSG_HINT_DISABLE_ERROR_CHECKS_MIMETYPE=Disable JavaScript error checking for all files of the {0} mimetype"
        })
        @Override
        public String getDescription() {
            if (enable) {
                return Bundle.MSG_HINT_ENABLE_ERROR_CHECKS_MIMETYPE(mimeType);
            } else {
                return Bundle.MSG_HINT_DISABLE_ERROR_CHECKS_MIMETYPE(mimeType);
            }
        }

        @Override
        public void implement() throws Exception {
            setErrorCheckingEnabledForMimetype(mimeType, enable);
            reindexActionItems();
            reindexFile(fo);
            refreshDocument(fo);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }
}

