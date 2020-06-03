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
