/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

