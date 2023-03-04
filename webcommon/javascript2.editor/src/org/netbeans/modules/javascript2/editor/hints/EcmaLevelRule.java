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
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.javascript2.editor.JsPreferences;
import org.netbeans.modules.javascript2.editor.JsVersion;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public abstract class EcmaLevelRule extends JsAstRule {

    private static final RequestProcessor RP = new RequestProcessor(EcmaLevelRule.class);

    public static void refresh(final FileObject fo) throws IOException {
        reindexFile(fo);
        refreshDocument(fo);
    }

    static boolean ecmaEditionProjectBelow(JsHintsProvider.JsRuleContext context, JsVersion targetVersion) {
        return JsPreferences.isPreECMAVersion(FileOwnerQuery.getOwner(context.getJsParserResult().getSnapshot().getSource().getFileObject()), targetVersion);
    }

    private static void reindexFile(final FileObject fo) {
        RP.post(() -> {
            //refresh Action Items for this file
            IndexingManager.getDefault().refreshIndexAndWait(fo.getParent().toURL(),
                    Collections.singleton(fo.toURL()), true, false);
        });
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
