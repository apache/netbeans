/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.javascript2.editor.hints;

import java.io.IOException;
import java.util.Collections;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorDocument;
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

    private static void refreshDocument(final FileObject fo) throws IOException {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
                    StyledDocument document = editorCookie.openDocument();
                    forceReparse(document);
                } catch (IOException ex) {
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
}
