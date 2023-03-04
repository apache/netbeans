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

import org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem;
import java.net.URL;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/** JSP completion provider implementation
 *
 * @author mfukala@netbeans.org
 */
public class JspCompletionProvider implements CompletionProvider {

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int type = JspSyntaxSupport.get(component.getDocument()).checkCompletion(component, typedText, false);
        if (type == ExtSyntaxSupport.COMPLETION_POPUP) {
            return COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE;
        } else {
            return 0;
        }
    }

    public CompletionTask createTask(int type, JTextComponent component) {
        if ((type & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            return new AsyncCompletionTask(new Query(), component);
        } else if (type == DOCUMENTATION_QUERY_TYPE) {
            return new AsyncCompletionTask(new DocQuery(null), component);
        }
        return null;
    }

    public static class Query extends AbstractQuery {

        private JTextComponent component;

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        protected void doQuery(CompletionResultSet resultSet, Document doc,
                int caretOffset) {
            JspCompletionQuery.CompletionResultSet<? extends CompletionItem> jspResultSet = new JspCompletionQuery.CompletionResultSet<CompletionItem>();
            JspCompletionQuery.instance().query(jspResultSet, component, caretOffset);

            resultSet.addAllItems(jspResultSet.getItems());
            resultSet.setAnchorOffset(jspResultSet.getAnchor());
        }
    }

    public static class DocQuery extends AbstractQuery {

        private JTextComponent component;
        private JspCompletionItem item;

        public DocQuery(JspCompletionItem item) {
            this.item = item;
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item != null) {
                //this instance created from jsp completion item
                if (item.hasHelp()) {
                    resultSet.setDocumentation(new DocItem(item));
                }
            } else {
                //called directly by infrastructure - run query
                JspCompletionQuery.CompletionResultSet<? extends CompletionItem> jspResultSet = new JspCompletionQuery.CompletionResultSet<CompletionItem>();
                JspCompletionQuery.instance().query(jspResultSet, component, caretOffset);
                if (jspResultSet.getItems().size() > 0) {
                    CompletionItem citem = jspResultSet.getItems().get(0);
                    if(citem instanceof JspCompletionItem) {
                        resultSet.setDocumentation(new DocItem((JspCompletionItem) citem));
                        resultSet.setAnchorOffset(jspResultSet.getAnchor());
                    }
                }

            }
        }
    }

    private static class DocItem implements CompletionDocumentation {

        private JspCompletionItem ri;

        public DocItem(JspCompletionItem ri) {
            this.ri = ri;
        }

        public String getText() {
            return ri.getHelp();
        }

        public URL getURL() {
            return ri.getHelpURL();
        }

        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        public Action getGotoSourceAction() {
            return null;
        }
    }

    public abstract static class AbstractQuery extends AsyncCompletionQuery {

        @Override
        protected void preQueryUpdate(JTextComponent component) {
        }

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            doQuery(resultSet, doc, caretOffset);
            resultSet.finish();
        }

        abstract void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset);
    }

}
