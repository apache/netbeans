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

