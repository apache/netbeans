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

package org.netbeans.modules.cnd.editor.fortran;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndLexerUtilities.FortranFormat;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;

/**
* Fortran editor kit with appropriate document
*/

public class FKit extends NbEditorKit {

    @Override
    public String getContentType() {
        return MIMENames.FORTRAN_MIME_TYPE;
    }

    @Override
    public void install(JEditorPane c) {
        super.install(c);
    }

    @Override
    public Document createDefaultDocument() {
        BaseDocument doc = new NbEditorDocument(MIMENames.FORTRAN_MIME_TYPE);
        // Probably it is a workaround old compiler that does not work with "\r\n"?
        // Remove the hack
        //doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, BaseDocument.LS_LF);
        return doc; 
    }

    /** Initialize document by adding the draw-layers for example. */
    @Override
    protected void initDocument(BaseDocument doc) {
        super.initDocument(doc);
        doc.putProperty(Language.class, getLanguage());
        initCodeStyle(doc);
    }

    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        super.read(in, doc, pos);
        initCodeStyle(doc);
    }

    @Override
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        super.read(in, doc, pos);
        initCodeStyle(doc);
    }

    private void initCodeStyle(Document doc) {
        InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (lexerAttrs == null) {
            lexerAttrs = new InputAttributes();
            doc.putProperty(InputAttributes.class, lexerAttrs);
        }
        FortranCodeStyle codeStyle = FortranCodeStyle.get(doc);
        Filter<?> fortranFilter = CndLexerUtilities.getDefaultFilter(getLanguage(), doc);
        lexerAttrs.setValue(getLanguage(), CndLexerUtilities.LEXER_FILTER, fortranFilter, true);
        lexerAttrs.setValue(getLanguage(), CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH, codeStyle.getRrightMargin(), true);
        lexerAttrs.setValue(getLanguage(), CndLexerUtilities.FORTRAN_FREE_FORMAT, codeStyle.getFormatFortran(), true);
    }
    
    private Language<FortranTokenId> getLanguage() {
        return FortranTokenId.languageFortran();
    }

    @Override
    protected Action[] createActions() {
	int arraySize = 3;
	int numAddClasses = 0;
	if (actionClasses != null) {
	    numAddClasses = actionClasses.size();
	    arraySize += numAddClasses;
	}
        Action[] fortranActions = new Action[arraySize];
	int index = 0;
	if (actionClasses != null) {
	    for (int i = 0; i < numAddClasses; i++) {
		Class<?> c = actionClasses.get(i);
		try {
		    fortranActions[index] = (Action)c.newInstance();
		} catch (java.lang.InstantiationException e) {
		    e.printStackTrace(System.err);
		} catch (java.lang.IllegalAccessException e) {
		    e.printStackTrace(System.err);
		}
		index++;
	    }
	}
	fortranActions[index++] = new CommentAction("!"); // NOI18N
	fortranActions[index++] = new UncommentAction("!"); // NOI18N
	fortranActions[index++] = new ToggleCommentAction("!"); // NOI18N
        return TextAction.augmentList(super.createActions(), fortranActions);
    }

    /** Holds action classes to be created as part of createAction.
        This allows dependent modules to add editor actions to this
        kit on startup.
    */
    private static ArrayList<Class<?>> actionClasses = null;


    public static void addActionClass(Class<?> action) {
	if (actionClasses == null) {
	    actionClasses = new ArrayList<Class<?>>(2);
	}
	actionClasses.add(action);
    }
}
