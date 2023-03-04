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

package org.netbeans.modules.gradle.editor.cli;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lkishalmi
 */
@MimeRegistration(mimeType = GradleCliEditorKit.MIME_TYPE, service = CompletionProvider.class)
public class GradleCliCompletionProvider implements CompletionProvider {
    private static final Pattern PROP_INPUT = Pattern.compile("\\$\\{([\\w.]*)$"); //NOI18N
    private static final String INPUT_TOKEN = "input:"; //NOI18N
    private static final Set<GradleCommandLine.GradleOptionItem> GRADLE_OPTIONS;

    //TODO: Move this one to GradleCommandLine in NetBeans 17
    public static final String GRADLE_PROJECT_PROPERTY = "gradle-project"; //NOI18N
    
    static {
        Set<GradleCommandLine.GradleOptionItem> all = new HashSet<>();
        GRADLE_OPTIONS = Collections.unmodifiableSet(all);
        all.addAll(Arrays.asList(GradleCommandLine.Flag.values()));
        all.addAll(Arrays.asList(GradleCommandLine.Parameter.values()));
        all.addAll(Arrays.asList(GradleCommandLine.Property.values()));
    }
    
    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                String filter = null;
                int startOffset = caretOffset - 1;

                try {
                    final StyledDocument bDoc = (StyledDocument) doc;
                    final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    final int whiteOffset = indexOfWhite(line);
                    filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
                    if (whiteOffset > 0) {
                        startOffset = lineStartOffset + whiteOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                Project project = null;
                Object prop = doc.getProperty(GRADLE_PROJECT_PROPERTY);
                if (prop instanceof Project) {
                    project = (Project) prop;
                }
                Matcher tokenMatcher = PROP_INPUT.matcher(filter);
                boolean tokenInFilter = tokenMatcher.find();
                try {
                    GradleCommandLine cli = new GradleCommandLine(doc.getText(0, doc.getLength()));
                    if (!filter.startsWith("-") && !tokenInFilter) {
                        if (project != null) {
                            GradleBaseProject gbp = GradleBaseProject.get(project);
                            for (GradleTask task : gbp.getTasks()) {
                                if (!task.isPrivate() 
                                        && !cli.getTasks().contains(task.getName())
                                        && !cli.getExcludedTasks().contains(task.getName())
                                        && (task.getName().startsWith(filter) || task.matches(filter))) {
                                    resultSet.addItem(new GradleTaskCompletionItem(task, startOffset, caretOffset));
                                }
                            }
                        }
                    }
                    if (filter.isEmpty() || filter.startsWith("-")) {
                        for (GradleCommandLine.GradleOptionItem item : GRADLE_OPTIONS) {
                            if (cli.canAdd(item)) {
                                for (String f : item.getFlags()) {
                                    if (f.startsWith(filter)) {
                                        resultSet.addItem(new GradleOptionCompletionItem(item, f, startOffset, caretOffset));
                                    }
                                }
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    // Nothing to do.
                }
                if (tokenInFilter && (project != null)) {
                    String propFilter = tokenMatcher.group(1);
                    ReplaceTokenProvider tokenProvider = project.getLookup().lookup(ReplaceTokenProvider.class);
                    for (String token : tokenProvider.getSupportedTokens()) {
                        if (token.startsWith(propFilter)) {
                            resultSet.addItem(new TokenCompletionItem(token, startOffset + tokenMatcher.start(1), caretOffset));
                        }
                    }
                    if (INPUT_TOKEN.startsWith(propFilter)) {
                        resultSet.addItem(new TokenCompletionItem(INPUT_TOKEN, startOffset + tokenMatcher.start(1), caretOffset));
                    }
                }
                    
                resultSet.finish();
            }
        }, component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 1;
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset) throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start
                ).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    private abstract static class AbstractGradleCompletionItem implements CompletionItem {
        private final int startOffset;
        private final int caretOffset;

        public AbstractGradleCompletionItem(int startOffset, int caretOffset) {
            this.startOffset = startOffset;
            this.caretOffset = caretOffset;
        }
        
        protected abstract String getValue();
        @Override
        public void defaultAction(JTextComponent jtc) {
            try {
                Document doc = jtc.getDocument();
                doc.remove(startOffset, caretOffset - startOffset);
                doc.insertString(startOffset, getValue(), null);
                //This statement will close the code completion box:
                Completion.get().hideAll();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font font) {
            return CompletionUtilities.getPreferredWidth(getValue(), null, g, font);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(null, getValue(), null, g, defaultFont, defaultColor, width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
        
        @Override
        public int getSortPriority() {
            return Integer.MAX_VALUE;
        }
        
        @Override
        public CharSequence getSortText() {
            return getValue();
        }

        @Override
        public CharSequence getInsertPrefix() {
            return getValue();
        }
    }
    
    private static class GradleTaskCompletionItem extends AbstractGradleCompletionItem {

        //This resource is from Gradle Projects module
        private static final String TASK_ICON = "org/netbeans/modules/gradle/resources/gradle-task.gif"; //NOI18N
        private static final ImageIcon TASK_IMAGEICON = ImageUtilities.loadImageIcon(TASK_ICON, false);

        private final GradleTask task;

        public GradleTaskCompletionItem(GradleTask task, int startOffset, int caretOffset) {
            super(startOffset, caretOffset);
            this.task = task;
        }

        @Override
        public int getSortPriority() {
            switch (task.getGroup()) {
                case "application": return 0;
                case "build": return 1;
                case "distribution": return 2;
                default: return 3;
            }
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    resultSet.setDocumentation(new GradleTaskCompletionDocumentation());
                    resultSet.finish();
                }
            });
        }

        @Override
        protected String getValue() {
            return task.getName();
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(TASK_IMAGEICON, getValue(), null, g, defaultFont, defaultColor, width, height, selected);
        }

        private class GradleTaskCompletionDocumentation implements CompletionDocumentation {

            @Override
            public String getText() {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("<b>Name:</b> ").append(task.getName()).append("<br/>");
                sb.append("<b>Group:</b> ").append(task.getGroup()).append("<br/>");
                sb.append("<b>Path:</b> ").append(task.getPath()).append("<br/>");
                sb.append("<b>Description:</b><p>").append(task.getDescription());
                return sb.toString();
            }

            @Override
            public URL getURL() {
                return null;
            }

            @Override
            public CompletionDocumentation resolveLink(String link) {
                return null;
            }

            @Override
            public Action getGotoSourceAction() {
                return null;
            }

        }
    }
    
    private static class GradleOptionCompletionItem extends AbstractGradleCompletionItem {
        private final GradleCommandLine.GradleOptionItem item;
        private final String value;

        public GradleOptionCompletionItem(GradleCommandLine.GradleOptionItem item, String value, int startOffset, int caretOffset) {
            super(startOffset, caretOffset);
            this.item = item;
            this.value = value;
        }

        @Override
        public int getSortPriority() {
            return value.startsWith("--") ? 5 : 4;
        }

        
        
        @Override
        protected String getValue() {
            return value;
        }
        
        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            Map<TextAttribute, Object> attributes = new HashMap<>(defaultFont.getAttributes());
            if (!item.isSupported()) {
                attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            }
            Font font = new Font(attributes);
            CompletionUtilities.renderHtml(null, getValue(), null, g, font, defaultColor, width, height, selected);
        }
        
        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    resultSet.setDocumentation(new GradleItemCompletionDocumentation());
                    resultSet.finish();
                }
            });
        }
        
        private class GradleItemCompletionDocumentation implements CompletionDocumentation {

            @Override
            public String getText() {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                if (!item.isSupported()) {
                    sb.append("<b>Unsupported:</b> This argument will be ignored");
                }
                sb.append(item.getDescription());
                return sb.toString();
            }

            @Override
            public URL getURL() {
                return null;
            }

            @Override
            public CompletionDocumentation resolveLink(String link) {
                return null;
            }

            @Override
            public Action getGotoSourceAction() {
                return null;
            }

        }
        
        
    }

    private static class TokenCompletionItem extends AbstractGradleCompletionItem {

        final String token;

        public TokenCompletionItem(String token, int startOffset, int caretOffset) {
            super(startOffset, caretOffset);
            this.token = token;
        }
        
        @Override
        protected String getValue() {
            return token;
        }
        
    }
}
