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

package org.netbeans.modules.php.smarty.editor;


import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.NbEditorKit;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.php.smarty.editor.actions.ToggleBlockCommentAction;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.utlis.TplUtils;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 * Editor kit implementation for TPL content type
 *
 * @author Martin Fousek
 */
public class TplKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider{ // NbEditorKit implements org.openide.util.HelpCtx.Provider{

    /** serialVersionUID */
    private static final long serialVersionUID = 8922234837050367142L;

    public TplKit() {
        this(TplDataLoader.MIME_TYPE);
    }

    public TplKit(String mimeType) {
        super();
     //   createDefaultDocument();
    }

    @Override
    public String getContentType() {
        return TplDataLoader.MIME_TYPE;
    }

    @Override
    public Document createDefaultDocument() {
        final Document doc = super.createDefaultDocument();
        // see TplEditorSupport.createStyledDocument;
        doc.putProperty("postInitRunnable", new Runnable() { //NOI18N
            public void run() {
                initLexerColoringListener(doc);
            }
        });
        return doc;
    }

    @Override
    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
            CslActions.createSelectCodeElementAction(true),
            CslActions.createSelectCodeElementAction(false),
            CslActions.createInstantRenameAction(),
//            CslActions.createToggleBlockCommentAction(),
            new ToggleBlockCommentAction(),
//            new ExtKit.CommentAction(""), //NOI18N
//            new ExtKit.UncommentAction("") //NOI18N
        };

        return TextAction.augmentList(super.createActions(), javaActions);
    }

    public void initLexerColoringListener(final Document doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;

        TplMetaData tplMetaData = TplUtils.getProjectPropertiesForFileObject(fobj);

        //add an instance of InputAttributes to the document property,
        //lexer will use it to read coloring information
        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(TplTopTokenId.language(), TplMetaData.class, tplMetaData, false);
        doc.putProperty(InputAttributes.class, inputAttributes);

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

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(TplKit.class);
    }

}

