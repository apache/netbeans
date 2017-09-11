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

package org.netbeans.modules.xml.text.completion;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLCompletionProvider implements CompletionProvider {
    
    private static final boolean ENABLED = true;
    
    /**
     * Creates a new instance of XMLCompletionProvider
     */
    public XMLCompletionProvider() {
        System.err.println("");
    }
    
    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(component.getDocument());
        if( (support == null) || !(support instanceof XMLSyntaxSupport))
            return 0;
        
        int type = checkCompletion(support, component, typedText, false);
        if(type == ExtSyntaxSupport.COMPLETION_POPUP)
            return COMPLETION_QUERY_TYPE;
        
        return 0;
    }

    /** Schedule content update making completion visible. */
    public static final int COMPLETION_POPUP = 0;
    /** Cancel request without changing completion visibility. */
    public static final int COMPLETION_CANCEL = 1;
    /** Update content immediatelly if it's currently visible. */
    public static final int COMPLETION_REFRESH = 2;
    /** Schedule content update if it's currently visible. */
    public static final int COMPLETION_POST_REFRESH = 3;
    /** Hide completion. */
    public static final int COMPLETION_HIDE = 4;
    
    private int checkCompletion(XMLSyntaxSupport support, JTextComponent target, String typedText, boolean visible ) {
        
        if( !visible ) {
            int retVal = COMPLETION_CANCEL;
            switch( typedText.charAt( typedText.length()-1 ) ) {
                case '/':
                    int dotPos = target.getCaret().getDot();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    if (dotPos >= 2) { // last char before inserted slash
                        try {
                            String txtBeforeSpace = doc.getText(dotPos-2, 2);
                            if( txtBeforeSpace.equals("</") )  // NOI18N
                                retVal = COMPLETION_POPUP;
                        } catch (BadLocationException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                    break;
                    
                case '<':
                case '&':
                case '"':
                case '\'':
                    retVal = COMPLETION_POPUP;
                    break;
                case '>':
                    dotPos = target.getCaret().getDot();
                    try {
                        SyntaxElement sel = support.getElementChain(dotPos);
                        if(support.isStartTag(sel)) {
                            retVal = COMPLETION_POPUP;
                        }
                    } catch (BadLocationException e) {
                        //ignore
                    }
                    break;
            }
            if(noCompletion(support, target))
                return COMPLETION_HIDE;            
            return retVal;
        } else { // the pane is already visible
            switch (typedText.charAt(0)) {
                case '>':
                case ';':
                    return COMPLETION_HIDE;
            }
            //requestedAutoCompletion = true;
            return COMPLETION_POST_REFRESH; //requery it
        }
    }
    
    /**
     * No completion inside PI, CDATA, comment section.
     * True only inside PI or CDATA section, false otherwise.
     * @param target
     */
    boolean noCompletion(XMLSyntaxSupport support, JTextComponent target) {
        if(target == null || target.getCaret() == null)
            return false;
        int offset = target.getCaret().getDot();
        if(offset < 0)
            return false;            
        //no completion inside CDATA or comment section
        try {
            return support.<Boolean>runWithSequence(offset, (TokenSequence ts) -> {
                Token<XMLTokenId> token = ts.token();
                if (token == null) {
                    ts.moveNext();
                    token = ts.token();
                    if (token == null) {
                        return false;
                    }
                }
                if (token.id() == XMLTokenId.CDATA_SECTION
                        || token.id() == XMLTokenId.BLOCK_COMMENT
                        || token.id() == XMLTokenId.PI_START
                        || token.id() == XMLTokenId.PI_END
                        || token.id() == XMLTokenId.PI_CONTENT
                        || token.id() == XMLTokenId.PI_TARGET) {
                    return true;
                }
                return false;
            });
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
    
    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE || queryType == COMPLETION_ALL_QUERY_TYPE) {
            return new AsyncCompletionTask(new Query(), component);
        }
        
        return null;
    }
    
    static class Query extends AsyncCompletionQuery {
        
        private static final XMLCompletionQuery QUERY = new XMLCompletionQuery();
        private JTextComponent component;
        
        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
            if (!ENABLED || support == null) {
                resultSet.finish();
                return;
            }
            
            resultSet.setWaitText(NbBundle.getMessage(XMLCompletionProvider.class, "MSG_loading_dtd"));
            List<CompletionItem> items = QUERY.query(component, caretOffset, XMLSyntaxSupport.getSyntaxSupport((BaseDocument)doc));
            if(items != null) resultSet.addAllItems(items);
            resultSet.finish();
        }
        
//        protected boolean doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
//            if (ENABLED) {
//                XMLSyntaxSupport support = (XMLSyntaxSupport)Utilities.getSyntaxSupport(component);
//                if (support != null) {
//                    XMLCompletionQuery.XMLCompletionResult res =
//                        (XMLCompletionQuery.XMLCompletionResult) QUERY.query(component, caretOffset, support);
//
//                    if(res != null) {
//                        List/*<CompletionItem>*/ results = res.getData();
//                        resultSet.addAllItems(results);
//                        resultSet.setTitle(res.getTitle());
//                        resultSet.setAnchorOffset(res.getSubstituteOffset());
//                        return results.size() == 0;
//                    }
//                }
//            }
//
//            return true;
//        }
    }
    
// XXX: remove dependency on the old org.netbeans.editor.ext.Completion & co.
//    private static XMLCompletionQuery.XMLCompletionResult queryImpl(JTextComponent component, int offset) {
//        if (!ENABLED) return null;
//        
//        Class kitClass = Utilities.getKitClass(component);
//        if (kitClass != null) {
//            ExtEditorUI eeui = (ExtEditorUI)Utilities.getEditorUI(component);
//            org.netbeans.editor.ext.Completion compl = ((XMLKit)Utilities.getKit(component)).createCompletionForProvider(eeui);
//            XMLSyntaxSupport support = (XMLSyntaxSupport)Utilities.getSyntaxSupport(component);
//            return (XMLCompletionQuery.XMLCompletionResult)compl.getQuery().query(component, offset, support);
//        }
//        
//        return null;
//    }
    
    private static void checkHideCompletion(BaseDocument doc, int caretOffset) {
        //test whether we are just in text and eventually close the opened completion
        //this is handy after end tag autocompletion when user doesn't complete the
        //end tag and just types a text
        XMLSyntaxSupport sup = XMLSyntaxSupport.getSyntaxSupport(doc);
        try {
            Token<XMLTokenId> ti = sup.getNextToken(caretOffset <= 0 ? 1 : caretOffset - 1);
            if(ti != null && ti.id()== XMLTokenId.TEXT && !ti.text().toString().startsWith("<") && !ti.text().toString().startsWith("&")) {
                hideCompletion();
            }
        }catch(BadLocationException e) {
            //do nothing
        }
    }
    
    private static void hideCompletion() {
        Completion.get().hideCompletion();
    }
    
}
