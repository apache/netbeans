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
package org.netbeans.modules.javascript2.editor.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Pisl
 */
public class ParsingErrorFilter {

    private static final RequestProcessor RP = new RequestProcessor(ParsingErrorFilter.class);

    private static final String DISABLE_JS_ERROR_KEY = "disable_error_checking_CSS"; //NOI18N

    public static Collection<FilterableError.SetFilterAction> getEnableFilterAction(@NonNull FileObject file) {
        FileObject source = file;
        Collection<FilterableError.SetFilterAction> actions = new ArrayList<>();
        for (; file != null && FileOwnerQuery.getOwner(file) != null; file = file.getParent()) {
            actions.add(new ParsingErrorFilter.SetFileFilterAction(source, file, true));
        }
        return actions;
    }

    /**
     * Checks if the parsing errors are filtered for this file or any of its
     * parent folders.
     *
     * @param file
     * @return
     */
    public static FilterableError.SetFilterAction getDisableFilterAction(@NonNull FileObject file) {
        FileObject source = file;
        for (; file != null && FileOwnerQuery.getOwner(file) != null; file = file.getParent()) {
            if (file.getAttribute(DISABLE_JS_ERROR_KEY) != null) {
                return new ParsingErrorFilter.SetFileFilterAction(source, file, false);
            }
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "disableFilterForFile=Disable filtering of JS errors in \"{0}\"",
        "# {0} - file name",
        "enableFilterForFile=Filter out JS parsing errors in \"{0}\""
    })
    private static class SetFileFilterAction implements FilterableError.SetFilterAction {

        private final FileObject file, source;
        private final boolean enable;

        public SetFileFilterAction(FileObject source, FileObject file, boolean enable) {
            this.source = source;
            this.file = file;
            this.enable = enable;
        }

        @Override
        public void run() {
            try {
                file.setAttribute(DISABLE_JS_ERROR_KEY, enable ? Boolean.TRUE.toString() : null);
                refresh(source);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public String getDisplayName() {
            String path = file.getPath();
            return enable ? Bundle.enableFilterForFile(path) : Bundle.disableFilterForFile(path);
        }
    }

     private static void refresh(FileObject file) {
        try {
//            reindexActionItems();
//            reindexFile(file);
            refreshDocument(file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

     private static void refreshDocument(final FileObject fo) throws IOException {
        RP.post(() -> {
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
                StyledDocument document = editorCookie.openDocument();
                forceReparse(document);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });

    }

    //force reparse of *THIS document only* => hints update
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
}
