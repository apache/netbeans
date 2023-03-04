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

package org.netbeans.modules.editor.java;

import java.awt.event.MouseEvent;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.completion.JavaDocumentationTask;
import org.netbeans.modules.java.completion.JavaTooltipTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 100) //NOI18N
public class JavaCompletionProvider implements CompletionProvider {
    
    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (typedText != null && typedText.length() == 1
                && (Utilities.getJavaCompletionAutoPopupTriggers().indexOf(typedText.charAt(0)) >= 0
                || (Utilities.autoPopupOnJavaIdentifierPart() && JavaCompletionQuery.isJavaIdentifierPart(typedText, false)))) {
            if (Utilities.isJavaContext(component, component.getSelectionStart() - 1, true)) {
                return COMPLETION_QUERY_TYPE;
            }
        }
        return 0;
    }
    
    @Override
    public CompletionTask createTask(int type, JTextComponent component) {
        if ((type & COMPLETION_QUERY_TYPE) != 0 || type == TOOLTIP_QUERY_TYPE || type == DOCUMENTATION_QUERY_TYPE) {
            return new AsyncCompletionTask(new JavaCompletionQuery(type, component.getSelectionStart()), component);
        }
        return null;
    }
    
    static CompletionTask createDocTask(ElementHandle element) {
        JavaCompletionQuery query = new JavaCompletionQuery(DOCUMENTATION_QUERY_TYPE, -1);
        query.element = element;
        return new AsyncCompletionTask(query, EditorRegistry.lastFocusedComponent());
    }
    
    public static List<? extends CompletionItem> query(Source source, int queryType, int offset, int substitutionOffset) throws Exception {
        assert source != null;
        assert (queryType & COMPLETION_QUERY_TYPE) != 0;
        JavaCompletionTask<JavaCompletionItem> task = JavaCompletionTask.create(offset, new JavaCompletionItemFactory(source.getFileObject()), EnumSet.noneOf(JavaCompletionTask.Options.class), null);
        ParserManager.parse(Collections.singletonList(source), task);
        if (offset != substitutionOffset) {
            for (JavaCompletionItem jci : task.getResults()) {
                jci.substitutionOffset += (substitutionOffset - offset);
            }
        }
        return task.getResults();
    }
    
    static final class JavaCompletionQuery extends AsyncCompletionQuery {
        
        static final AtomicBoolean javadocBreak = new AtomicBoolean();
        static final AtomicReference<CompletionDocumentation> outerDocumentation = new AtomicReference<>();
        
        private static final String EMPTY = ""; //NOI18N
        private static final String SKIP_ACCESSIBILITY_CHECK = "org.netbeans.modules.editor.java.JavaCompletionProvider.skipAccessibilityCheck"; //NOI18N
        
        private List<JavaCompletionItem> results;
        private boolean hasAdditionalItems;
        private MethodParamsTipPaintComponent toolTip;
        private CompletionDocumentation documentation;
        private int anchorOffset;
        private int toolTipOffset;

        private JTextComponent component;

        private final int queryType;
        private int caretOffset;
        private String filterPrefix;
        
        private ElementHandle element;
        
        private JavaCompletionQuery(int queryType, int caretOffset) {
            this.queryType = queryType;
            this.caretOffset = caretOffset;
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int newCaretOffset = component.getSelectionStart();
            if (newCaretOffset >= caretOffset) {
                try {
                    if (isJavaIdentifierPart(component.getDocument().getText(caretOffset, newCaretOffset - caretOffset), false)) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }
            Completion.get().hideCompletion();
        }
        
        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
            if (queryType == TOOLTIP_QUERY_TYPE) {
                this.toolTip = new MethodParamsTipPaintComponent(component);
            }
        }
        
        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                this.caretOffset = caretOffset;
                CompletionDocumentation outerDoc = outerDocumentation.getAndSet(null);
                if (queryType == DOCUMENTATION_QUERY_TYPE && outerDoc != null) {
                    resultSet.setDocumentation(outerDoc);
                    ToolTipSupport tts = org.netbeans.editor.Utilities.getEditorUI(component).getToolTipSupport();
                    if (tts != null) {
                        MouseEvent lme = tts.getLastMouseEvent();
                        if (lme != null) {
                            int offset = component.viewToModel(lme.getPoint());
                            if (offset > -1) {
                                resultSet.setAnchorOffset(offset);
                            }
                        }
                    }
                } else if (queryType == TOOLTIP_QUERY_TYPE || Utilities.isJavaContext(component, caretOffset, true)) {
                    results = null;
                    documentation = null;
                    if (toolTip != null) {
                        toolTip.clearData();
                    }
                    anchorOffset = -1;
                    Source source = Source.create(doc);
                    if (source != null) {
                        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                            Set<JavaCompletionTask.Options> options = EnumSet.noneOf(JavaCompletionTask.Options.class);
                            Object prop = component != null ? component.getDocument().getProperty(SKIP_ACCESSIBILITY_CHECK) : null;
                            if (prop instanceof String && Boolean.parseBoolean((String)prop)) {
                                options.add(JavaCompletionTask.Options.SKIP_ACCESSIBILITY_CHECK);
                            }
                            if (queryType == COMPLETION_ALL_QUERY_TYPE) {
                                options.add(JavaCompletionTask.Options.ALL_COMPLETION);
                            }
                            JavaCompletionTask<JavaCompletionItem> task = JavaCompletionTask.create(caretOffset, new JavaCompletionItemFactory(source.getFileObject()), options, new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    return isTaskCancelled();
                                }
                            });
                            if (component != null) {
                                component.putClientProperty("completion-active", Boolean.TRUE); //NOI18N
                            }
                            ParserManager.parse(Collections.singletonList(source), task);
                            if (component != null) {
                                component.putClientProperty("completion-active", Boolean.FALSE); //NOI18N
                            }
                            results = task.getResults();
                            if (results != null) {
                                resultSet.addAllItems(results);
                            }
                            hasAdditionalItems = task.hasAdditionalClasses() || task.hasAdditionalMembers();
                            resultSet.setHasAdditionalItems(hasAdditionalItems);
                            if (task.hasAdditionalClasses()) {
                                resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JavaCompletionProvider.class, "JCP-imported-items")); //NOI18N
                            }
                            if (task.hasAdditionalMembers()) {
                                resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JavaCompletionProvider.class, "JCP-instance-members")); //NOI18N
                            }
                            anchorOffset = task.getAnchorOffset();
                            if (anchorOffset > -1) {
                                resultSet.setAnchorOffset(anchorOffset);
                            }
                        } else if (queryType == TOOLTIP_QUERY_TYPE) {
                            JavaTooltipTask task = JavaTooltipTask.create(caretOffset, new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    return isTaskCancelled();
                                }
                            });
                            ParserManager.parse(Collections.singletonList(source), task);
                            if (task.getTooltipData() != null) {
                                toolTip.setData(task.getTooltipData(), task.getTooltipIndex());
                                resultSet.setToolTip(toolTip);
                                toolTipOffset = task.getTooltipOffset();
                            }
                            anchorOffset = task.getAnchorOffset();
                            if (anchorOffset > -1) {
                                resultSet.setAnchorOffset(anchorOffset);
                            }
                        } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                            JavaDocumentationTask<ElementJavadoc> task = JavaDocumentationTask.create(caretOffset, element, new JavaDocumentationTask.DocumentationFactory<ElementJavadoc>() {

                                @Override
                                public ElementJavadoc create(CompilationInfo compilationInfo, Element element, Callable<Boolean> cancel) {
                                    return ElementJavadoc.create(compilationInfo, element, cancel);
                                }
                            }, new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    return isTaskCancelled();
                                }
                            });
                            ParserManager.parse(Collections.singletonList(source), task);
                            if (task.getDocumentation() != null) {
                                documentation = new JavaCompletionDoc(task.getDocumentation());
                                while (!isTaskCancelled()) {
                                    try {
                                        if (javadocBreak.getAndSet(false)) {
                                            Completion c = Completion.get();
                                            c.hideDocumentation();
                                            c.showDocumentation();
                                        }
                                        ((JavaCompletionDoc)documentation).getFutureText().get(250, TimeUnit.MILLISECONDS);
                                        resultSet.setDocumentation(documentation);
                                        break;
                                    } catch (TimeoutException timeOut) {/*retry*/}
                                }                                
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            } finally {
                resultSet.finish();
            }
        }
        
        @Override
        protected boolean canFilter(JTextComponent component) {
            if (component.getCaret() == null) {
                return false;
            }
            filterPrefix = null;
            final int newOffset = component.getSelectionStart();
            final Document doc = component.getDocument();
            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                final int offset = Math.min(anchorOffset, caretOffset);
                if (offset > -1) {
                    if (newOffset < offset) {
                        return true;
                    }
                    if (newOffset >= caretOffset) {
                        try {
                            final int len = newOffset - offset;
                            if (len == 0) {
                                filterPrefix = EMPTY;
                            } else if (len > 0) {
                                String prefix = doc.getText(offset, newOffset - offset);
                                if (prefix.length() > 0 && Utilities.getJavaCompletionAutoPopupTriggers().indexOf(prefix.charAt(prefix.length() - 1)) >= 0) {
                                    return false;
                                }
                                if (isJavaIdentifierPart(prefix, true)) {
                                    filterPrefix = prefix;
                                }
                            } else if (filterPrefix.length() == 0) {
                                anchorOffset = newOffset;
                            }
                        } catch (BadLocationException e) {}
                        return true;
                    }
                }
                return false;
            } else if (queryType == TOOLTIP_QUERY_TYPE) {
                try {
                    if (newOffset == caretOffset) {
                        filterPrefix = EMPTY;
                    } else if (newOffset - caretOffset > 0) {
                        filterPrefix = doc.getText(caretOffset, newOffset - caretOffset);
                    } else if (newOffset - caretOffset < 0) {
                        filterPrefix = newOffset > toolTipOffset ? doc.getText(newOffset, caretOffset - newOffset) : null;
                    }
                } catch (BadLocationException ex) {}
                return (filterPrefix != null && filterPrefix.indexOf(',') == -1 && filterPrefix.indexOf('(') == -1 && filterPrefix.indexOf(')') == -1); // NOI18N
            }
            return false;
        }
        
        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                    if (results != null) {
                        if (filterPrefix != null) {
                            resultSet.addAllItems(getFilteredData(results, filterPrefix));
                            resultSet.setHasAdditionalItems(hasAdditionalItems);
                        } else {
                            Completion.get().hideDocumentation();
                            Completion.get().hideCompletion();
                        }
                    }
                } else if (queryType == TOOLTIP_QUERY_TYPE) {
                    resultSet.setToolTip(toolTip != null && toolTip.hasData() ? toolTip : null);
                }
                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            resultSet.finish();
        }

        private static boolean isJavaIdentifierPart(String text, boolean allowForDor) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i)) || allowForDor && text.charAt(i) == '.')) {
                    return false;
                }
            }
            return true;
        }

        private Collection getFilteredData(Collection<JavaCompletionItem> data, String prefix) {
            if (prefix.length() == 0) {
                return data;
            }
            List ret = new ArrayList();
            boolean camelCase = isCamelCasePrefix(prefix);
            for (Iterator<JavaCompletionItem> it = data.iterator(); it.hasNext();) {
                CompletionItem itm = it.next();
                CharSequence cs = itm != null ? itm.getInsertPrefix() : null;
                if (cs != null) {
                    if (org.netbeans.modules.java.completion.Utilities.startsWith(cs.toString(), prefix)
                            || (camelCase && org.netbeans.modules.java.completion.Utilities.startsWithCamelCase(cs.toString(), prefix))) {
                        ret.add(itm);
                    } else {
                        for (String s : cs.toString().split("\\.")) { //NOI18N
                            if (org.netbeans.modules.java.completion.Utilities.startsWith(s, prefix)
                                    || (camelCase && org.netbeans.modules.java.completion.Utilities.startsWithCamelCase(s, prefix))) {
                                ret.add(itm);
                                break;
                            }
                        }
                    }
                }
            }
            return ret;
        }
                
        private static boolean isCamelCasePrefix(String prefix) {
            if (prefix == null || prefix.length() < 2 || prefix.charAt(0) == '"') {
                return false;
            }
            for (int i = 1; i < prefix.length(); i++) {
                if (Character.isUpperCase(prefix.charAt(i))) {
                    return true;
                }                
            }
            return false;
        }
    }
}
