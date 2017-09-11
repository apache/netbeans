/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.javafx2.editor.completion.impl.Completer;
import org.netbeans.modules.javafx2.editor.completion.impl.CompletionContext;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=CompletionProvider.class)
public class FXMLCompletion2 implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (component == null) {
            return null;
        }
        return new AsyncCompletionTask(new Q(component, queryType), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    public static List<CompletionItem> testQuery(Source s, Document doc, int queryType, int caretOffset) throws ParseException {
        Q q = new Q(null, queryType);
        ClasspathInfo cpInfo = ClasspathInfo.create(doc);
        UserTask t = q.createTask(cpInfo, null, null, doc, caretOffset, queryType);
        ParserManager.parse(Collections.singleton(s), t);
        return q.items;
    }

    public static class Q extends AsyncCompletionQuery {
        private JTextComponent component;
        private int queryType;
        private List<CompletionItem> items = Collections.emptyList();
        private boolean additionalItems;

        public Q(JTextComponent component, int queryType) {
            this.component = component;
            this.queryType = queryType;
        }
        
        private Task createTask(ClasspathInfo cpInfo, JTextComponent component,
                CompletionResultSet rs, Document doc, int caretOffset, int qT) {
            return new Task(cpInfo, 
                    component, 
                    rs, 
                    doc, caretOffset, qT);
        }

        @Override
        public void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                ParserManager.parse(Collections.singleton(Source.create(doc)), 
                        createTask(cpInfo, 
                            component, 
                            resultSet, 
                            doc, caretOffset, queryType)
                );
                resultSet.setHasAdditionalItems(additionalItems);
                resultSet.addAllItems(items);
                resultSet.finish();
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public List<CompletionItem> getItems() {
            return items;
        }

        
        private class Task extends UserTask implements ClasspathInfoProvider {
            private CompletionResultSet resultSet;
            private Document doc;
            private int caretOffset;
            private JTextComponent component;
            private ClasspathInfo cpInfo;
            private int queryType;
            private boolean fxmlParsing = true;
            private CompletionContext ctx;
            private CompilationInfo ci;
            private FxmlParserResult fxmlResult;

            public Task(ClasspathInfo cpInfo, JTextComponent component, CompletionResultSet resultSet, Document doc, int caretOffset, int queryType) {
                this.resultSet = resultSet;
                this.doc = doc;
                this.caretOffset = caretOffset;
                this.component = component;
                this.cpInfo = cpInfo;
                this.queryType = queryType;
            }

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                if (fxmlParsing) {
                    Parser.Result result = resultIterator.getParserResult(caretOffset);

                    fxmlResult = FxmlParserResult.get(result);
                    
                    if (fxmlResult == null) {
                        return;
                    }

                    fxmlParsing = false;
                    // next round, with Java parser to get access to java typesystem
                    ParserManager.parse(JavaFXEditorUtils.JAVA_MIME_TYPE, this);
                    return;
                }
                Parser.Result result = resultIterator.getParserResult();
                ci = CompilationInfo.get(result);

                ctx = new CompletionContext(doc, caretOffset, queryType);

                // initialize the Context under read lock, it moves through TokenHierarchy back & forward
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readLock();
                }
                // bug in parsing API: snapshot source not modified just after modification to the source file
                try {
                    TokenHierarchy<?> th = TokenHierarchy.get(doc);
                    ctx.init(th, ci, fxmlResult); 
                } finally {
                    if (doc instanceof AbstractDocument) {
                        ((AbstractDocument)doc).readUnlock();
                    }
                }

                items = new ArrayList<CompletionItem>();
                Collection<? extends Completer.Factory> completers = MimeLookup.getLookup(JavaFXEditorUtils.FXML_MIME_TYPE).lookupAll(Completer.Factory.class);
                for (Iterator<? extends Completer.Factory> it = completers.iterator(); it.hasNext(); ) {
                    Completer.Factory f = it.next();
                    Completer c = f.createCompleter(ctx);
                    if (c != null) {
                        List<? extends CompletionItem> newItems = c.complete();
                        if (newItems != null) {
                            items.addAll(newItems);
                            additionalItems |= c.hasMoreItems();
                        }
                    }
                }
            }

            public ClasspathInfo getClasspathInfo() {
                return cpInfo;
            }

        }
    }
}
