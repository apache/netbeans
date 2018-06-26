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

package org.netbeans.modules.web.core.syntax.completion;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.java.JavaCompletionProvider;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.core.syntax.SimplifiedJspServlet;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;

/**
 * Code completion functionality for Java code embedded in JSP files:
 * - scriptlets (<% ... %>)
 * - JSP declarations (<%! ... %> )
 * - expressions (<%= ... %>)
 *
 * @author Tomasz.Slota@Sun.COM
 */
@Deprecated
public class JavaJspCompletionProvider implements CompletionProvider {
    private final JavaCompletionProvider javaCompletionProvider = new JavaCompletionProvider();
    private static final Logger logger = Logger.getLogger(JavaJspCompletionProvider.class.getName());

    public CompletionTask createTask(int queryType, final JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) != 0){
            Document doc = Utilities.getDocument(component);
            int caretOffset = component.getCaret().getDot();

            if (isWithinScriptlet(doc, caretOffset)){
                //delegate to java cc provider if the context is really java code
                return new AsyncCompletionTask(new EmbeddedJavaCompletionQuery(component, queryType), component);
            }
        }

        return null;
    }

    private boolean isWithinScriptlet(Document doc, int offset){
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();

        tokenSequence.move(offset);
        if (tokenSequence.moveNext() || tokenSequence.movePrevious()) {
            Object tokenID = tokenSequence.token().id();
            if (tokenID == JspTokenId.SCRIPTLET){
                return true;
            } else if (tokenID == JspTokenId.SYMBOL2) {
                // maybe the caret is placed just before the ending script delimiter?
                tokenSequence.movePrevious();

                if (tokenSequence.token().id() == JspTokenId.SCRIPTLET){
                    return true;
                }
            }
        }

        return false;
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return javaCompletionProvider.getAutoQueryTypes(component, typedText);
    }


    @Deprecated
    static class EmbeddedJavaCompletionQuery extends AsyncCompletionQuery {
        protected int queryType;
        protected JTextComponent component;

        public EmbeddedJavaCompletionQuery(JTextComponent component, int queryType){
            this.queryType = queryType;
            this.component = component;
        }

        protected void query(CompletionResultSet resultSet, Document doc,
                int caretOffset) {

            Source source = Source.create(doc);
            SimplifiedJspServlet simplifiedJSPServlet = new SimplifiedJspServlet(source.createSnapshot(), doc);
            try{
                simplifiedJSPServlet.process();
                Embedding fakedClassBody = simplifiedJSPServlet.getSimplifiedServlet();
                int shiftedOffset = fakedClassBody.getSnapshot().getEmbeddedOffset(caretOffset); /*simplifiedJSPServlet.getShiftedOffset(caretOffset);*/

                if (shiftedOffset >= 0){
                    logger.fine("JSP CC: delegating CC query to java file:\n" //NOI18N
                            + fakedClassBody.getSnapshot().getText().toString().substring(0, shiftedOffset)
                            + "|" + fakedClassBody.getSnapshot().getText().toString().substring(shiftedOffset) + "\n"); //NOI18N

                    CompletionQueryDelegatedToJava delegate = new CompletionQueryDelegatedToJava(
                            caretOffset, shiftedOffset, queryType);

                    delegate.create(doc, fakedClassBody.getSnapshot().getText().toString());
                    List<? extends CompletionItem> items =  delegate.getCompletionItems();
                    resultSet.addAllItems(items);
                } else{
                    logger.severe("caretOffset outside of embedded java code"); //NOI18N
                }
            } catch (BadLocationException e){
                logger.log(Level.SEVERE, e.getMessage(), e);
            } finally{
                resultSet.finish();
            }
        }
    }

    @Deprecated
    static class CompletionQueryDelegatedToJava extends SimplifiedJspServlet.VirtualJavaClass{
        private int caretOffset;
        private int queryType;
        private int shiftedOffset;
        private List<? extends CompletionItem> completionItems;

        CompletionQueryDelegatedToJava(int caretOffset, int shiftedOffset, int queryType){
            this.caretOffset = caretOffset;
            this.shiftedOffset = shiftedOffset;
            this.queryType = queryType;
        }

        protected void process(FileObject fileObject, Source javaSource){
            try{
                completionItems = JavaCompletionProvider.query(
                        javaSource, queryType, shiftedOffset, caretOffset);
            } catch (Exception e){
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        List<? extends CompletionItem> getCompletionItems(){
            return completionItems;
        }
    }

}

