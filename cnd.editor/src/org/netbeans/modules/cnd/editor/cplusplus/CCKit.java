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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.CommentAction;
import org.netbeans.editor.ext.ExtKit.UncommentAction;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.util.NbPreferences;

/** C++ editor kit with appropriate document */
public class CCKit extends NbEditorKit {
    /* package */ static final String previousCamelCasePosition = "previous-camel-case-position"; //NOI18N
    /* package */ static final String nextCamelCasePosition = "next-camel-case-position"; //NOI18N
    /* package */ static final String selectPreviousCamelCasePosition = "select-previous-camel-case-position"; //NOI18N
    /* package */ static final String selectNextCamelCasePosition = "select-next-camel-case-position"; //NOI18N
    /* package */ static final String deletePreviousCamelCasePosition = "delete-previous-camel-case-position"; //NOI18N
    /* package */ static final String deleteNextCamelCasePosition = "delete-next-camel-case-position"; //NOI18N
    /* package */ static final String selectNextElementAction = "select-element-next"; //NOI18N
    /* package */ static final String selectPreviousElementAction = "select-element-previous"; //NOI18N

    public CCKit() {
        // default constructor needed to be created from services
    }
    
    @Override
    public String getContentType() {
        return MIMENames.CPLUSPLUS_MIME_TYPE;
    }

    @Override
    public Document createDefaultDocument() {
        Document doc = super.createDefaultDocument();
        return doc;
    }

    /** Initialize document by adding the draw-layers for example. */
    @Override
    protected void initDocument(BaseDocument doc) {
        super.initDocument(doc);
        Language<CppTokenId> language = getLanguage();
        doc.putProperty(Language.class, language);
        doc.putProperty(InputAttributes.class, getLexerAttributes(language, doc));
        // make sure that preferences in mime lookup are inited
        CodeStyle.getDefault(doc);
    }

    protected Language<CppTokenId> getLanguage() {
        return CppTokenId.languageCpp();
    }
    
    protected final InputAttributes getLexerAttributes(Language<?> language, BaseDocument doc) {
        // filter can be overwritten later on for real editors in CppEditorSupport.setupSlowDocumentProperties
        InputAttributes lexerAttrs = new InputAttributes();
        lexerAttrs.setValue(language, CndLexerUtilities.LEXER_FILTER, getFilter(language, doc), true);  // NOI18N
        return lexerAttrs;
    }

    protected Filter<?> getFilter(Language<?> language, BaseDocument doc) {
        return CndLexerUtilities.getDefaultFilter(language, doc);
    }

    protected Action getCommentAction() {
        return new CommentAction("//"); // NOI18N
    }

    protected Action getUncommentAction() {
        return new UncommentAction("//"); // NOI18N
    }

    protected Action getToggleCommentAction() {
        return new ToggleCommentAction("//"); // NOI18N
    }

    protected 
    @Override
    Action[] createActions() {
        Action[] superActions = super.createActions();
        Action[] ccActions = new Action[]{
            getToggleCommentAction(),
            getCommentAction(),
            getUncommentAction(),

            new CCNextWordAction(nextWordAction),
            new CCPreviousWordAction(previousWordAction),
            new CCNextWordAction(selectionNextWordAction),
            new CCPreviousWordAction(selectionPreviousWordAction),
            new DeleteToNextCamelCasePosition(findAction(superActions, removeNextWordAction)),
            new DeleteToPreviousCamelCasePosition(findAction(superActions, removePreviousWordAction)),
            new SelectCodeElementAction(selectNextElementAction, true),
            new SelectCodeElementAction(selectPreviousElementAction, false),
            new InsertSemicolonAction(true),
            new InsertSemicolonAction(false),};
        ccActions = TextAction.augmentList(superActions, ccActions);

        return ccActions;
    }

    private static Action findAction(Action[] actions, String name) {
        for (Action a : actions) {
            Object nameObj = a.getValue(Action.NAME);
            if (nameObj instanceof String && name.equals(nameObj)) {
                return a;
            }
        }
        return null;
    }

    private static boolean isUsingCamelCase() {
        return NbPreferences.root().getBoolean("useCamelCaseStyleNavigation", true); // NOI18N
    }

    public static class CCNextWordAction extends NextWordAction {

        CCNextWordAction(String name) {
            super(name);
        }
        
        @Override
        protected int getNextWordOffset(JTextComponent target) throws BadLocationException {
            return isUsingCamelCase()
                    ? CamelCaseOperations.nextCamelCasePosition(target, true)
                    : super.getNextWordOffset(target);
        }

    }

    public static class CCPreviousWordAction extends PreviousWordAction {

        CCPreviousWordAction(String name) {
            super(name);
        }

        @Override
        protected int getPreviousWordOffset(JTextComponent target) throws BadLocationException {
            return isUsingCamelCase()
                    ? CamelCaseOperations.previousCamelCasePosition(target)
                    : super.getPreviousWordOffset(target);
        }

    }

}
