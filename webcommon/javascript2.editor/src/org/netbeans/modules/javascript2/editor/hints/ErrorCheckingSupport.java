/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebPageMetadata;
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

    private static final RequestProcessor RP = new RequestProcessor(ErrorCheckingSupport.class);

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

    public static String getMimeType(Parser.Result info) {
        String mime = getWebPageMetadataMime(info);
        if (mime != null) {
            return mime;
        }
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        if (fo != null) {
            return fo.getMIMEType();
        } else {
            // no fileobject?
            return info.getSnapshot().getMimeType();
        }
    }
    
    private static String getWebPageMetadataMime(Parser.Result info) {
        String mime = WebPageMetadata.getContentMimeType(info, false);
        if (mime != null) {
            return mime;
        }
        Snapshot snapshot = info.getSnapshot();
        // safeguard
        if (!containsHtml(snapshot)) {
            return null;
        }

        final AtomicReference<String> res = new AtomicReference<>();
        try {
            ParserManager.parse(Collections.singletonList(snapshot.getSource()), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    String mime2 = WebPageMetadata.getContentMimeType(info, false);
                    if (mime2 != null) {
                        res.set(mime2);
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
        RP.post(() -> {
            //refresh Action Items for this file
            IndexingManager.getDefault().refreshIndexAndWait(fo.getParent().toURL(),
                    Collections.singleton(fo.toURL()), true, false);
        });
    }

    private static void reindexActionItems() {
        RP.post(() -> {
            //refresh all Action Items
            IndexingManager.getDefault().refreshAllIndices("TLIndexer"); //NOI18N
        });
    }

    private static void refreshDocument(final FileObject fo) throws IOException {
        RP.post(() -> {
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
                StyledDocument document = editorCookie.openDocument();
                forceReparse(document);
            } catch (IOException  ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    private static void forceReparse(final Document doc) {
        SwingUtilities.invokeLater(() -> {
            NbEditorDocument nbdoc = (NbEditorDocument) doc;
            nbdoc.runAtomic(() -> {
                MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                if (mti != null) {
                    mti.tokenHierarchyControl().rebuild();
                }
            });
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

