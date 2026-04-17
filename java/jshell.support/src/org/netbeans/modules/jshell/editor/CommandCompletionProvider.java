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
package org.netbeans.modules.jshell.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import jdk.jshell.SourceCodeAnalysis;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import static org.netbeans.modules.jshell.editor.HistoryCompletionProvider.checkInputSection;
import static org.netbeans.modules.jshell.editor.HistoryCompletionProvider.isFirstJavaLine;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.JShellToken;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.parsing.JShellLexer;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.jshell.tool.JShellTool;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_ALL_QUERY_TYPE;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Attempts to convert JShell completion providers and help system into NB completion items.
 *
 * @author sdedic
 */
@MimeRegistrations({
    @MimeRegistration(mimeType="text/x-repl", service=CompletionProvider.class, position = 150),
})
public class CommandCompletionProvider implements CompletionProvider{
    private static final String ICON_JAR = "org/netbeans/modules/jshell/resources/jar.png"; // NOI18N
    private static final String ICON_FILE = "org/netbeans/modules/jshell/resources/file.png"; // NOI18N
    private static final String ICON_FOLDER = "org/netbeans/modules/jshell/resources/folder.png"; // NOI18N

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        int a = isFirstJavaLine(component);
        if ((queryType != COMPLETION_ALL_QUERY_TYPE && queryType != COMPLETION_QUERY_TYPE) || 
                isFirstJavaLine(component) == -1) {
            return null;
        }
        // check that the caret is at the first line of the editable area:
        ShellSession session = checkInputSection(component);
        if (session == null) {
            return null;
        }
        return new AsyncCompletionTask(new T(
            session,
            session.getModel(),
            session.getModel().getInputSection()
        ), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    private static class T extends AsyncCompletionQuery {
        private final ShellSession session;
        private final ConsoleModel  model;
        private final ConsoleSection section;

        public T(ShellSession session, ConsoleModel model, ConsoleSection section) {
            this.session = session;
            this.model = model;
            this.section = section;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                query2(resultSet, doc, caretOffset);
            } finally {
                resultSet.finish();
            }
        }
        
        private void query2(CompletionResultSet resultSet, Document doc, int caretOffset) {
            Rng[] ranges = section.getAllSnippetBounds();
            if (ranges == null || ranges.length == 0) {
                return;
            }
            if (ranges.length > 1 && caretOffset >= ranges[0].end) {
                return;
            }
            int s = ranges[0].start;
            int l = caretOffset - s;
            String prefix;
            
            try {
                prefix = doc.getText(s, l);
            } catch (BadLocationException ex) {
                return;
            }
            
            if (prefix.isEmpty()) {
                completeCommand(resultSet, "", s);
            }
            
            if (!prefix.startsWith("/")) {
                return;
            }
            
            // finally we may have a command:
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence seq = th.tokenSequence(JShellToken.language());
            if (seq == null || !seq.isValid()) {
                return;
            }
            Token<JShellToken> tukac = null;
            seq.move(s);
            if (!seq.moveNext()) {
                return; // ?? cannot find command
            }
            tukac = seq.token();
            if (tukac.id() == JShellToken.ERR_COMMAND) {
                // the caret must be at most at the end of the token:
                if (caretOffset <= seq.offset() + tukac.length()) {
                    completeCommand(resultSet, tukac.text().toString(), s);
                    return;
                }
            }
            // some unrecognized stuff
            if (tukac.id() != JShellToken.COMMAND) {
                return;
            }
            String partName = tukac.text().toString().substring(1);
            String cmdName;
            boolean inCommand = caretOffset <= seq.offset() + tukac.length();
            List<String> candidates = JShellLexer.getCommandsFromPrefix(partName);
            if (candidates.size() > 1) {
                if (inCommand) {
                    completeCommand(resultSet, tukac.text().toString(), s);
                }
                return;
            } else if (candidates.size() == 1) {
                cmdName = candidates.get(0);
                
                if (inCommand && !partName.equals(candidates.get(0))) {
                    completeCommand(resultSet, tukac.text().toString(), s);
                    prefix = prefix + " ";
                }
            } else {
                return;
            }
            
            if (seq.move(caretOffset) > 0) {
                if (!seq.moveNext()) {
                    return;
                }
            } else {
                seq.movePrevious();
            }
            
            tukac = seq.token();
            
            
            // fall back to the JShell Tool's completions:
            String sectionContents = section.getContents(doc);
            int sectionOffset = caretOffset - s;
            int sectionTokenStart = seq.offset() - s;
            if (tukac.id() == JShellToken.WHITESPACE) {
                sectionTokenStart = -1;
            }
            int[] anchor = new int[1];
            // sometimes the model lags after the document update; avoid SIOOBE.
            if (isTaskCancelled()) { 
                return;
            }
            if (sectionContents.length() < sectionOffset) {
                try {
                    sectionContents = doc.getText(s, doc.getLength() - s);
                } catch (BadLocationException ex) {
                    return;
                }
            }

            List<SourceCodeAnalysis.Suggestion> suggestions = session.getJShellTool().commandCompletionSuggestions(
                    sectionContents,
                    sectionOffset,
                    anchor);
            
            boolean onlyOptions = true;
            
            int insertAt;
            int deleteLen;
            insertAt = anchor[0] + s;
            deleteLen = sectionOffset - anchor[0];
            prefix = sectionContents.substring(anchor[0], anchor[0] + deleteLen);
            List<CompletionItem> otherItems = new ArrayList<>();
            
            for (SourceCodeAnalysis.Suggestion sugItem : suggestions) {
                String text = sugItem.continuation();
                if (text.trim().equals("/" + cmdName)) {
                    continue; // already covered
                }
                if (onlyOptions) {
                    if (text.startsWith("-")) {
                        resultSet.addItem(new OptionCompletionItem(/*prefix + */text, insertAt, deleteLen, session, cmdName));
                        continue;
                     } else {
                        onlyOptions = false;
                    }
                }
                switch (cmdName) {
                    case "open":    // NOI18N
                    case "save":    // NOI18N
                    case "classpath": // NOI18N
                    {
                        // add suggested files:
                        Path p = session.resolvePath(sectionTokenStart == -1 ? 
                                "" : sectionContents.substring(sectionTokenStart, anchor[0]));
                        otherItems.add(new FileCompletionItem(/*prefix + */text, insertAt, deleteLen, session, ICON_JAR, p));
                        break;
                    }
                    default:
                        otherItems.add(new ToolCompletionItem(/*prefix + */text, insertAt, deleteLen, session, false));
                        break;
                        
                }
            }
            resultSet.addAllItems(otherItems);
        }
        
        private void completeCommand(CompletionResultSet resultSet, String prefix, int startAt) {
            if (prefix.startsWith("/")) {// NOI18N
                prefix = prefix.substring(1).trim();
            }
            List<String>    candidates = JShellLexer.getCommandsFromPrefix(prefix);
            for (String s : candidates) {
                resultSet.addItem(new CommandCompletionItem(session, startAt, s, prefix.length() < 2));
            }
        }
    }
    
    private static class FileCompletionItem extends ToolCompletionItem {
        private final Path basePath;
        private final String fileResource;
        private ImageIcon icon;
        
        public FileCompletionItem(String insertText, int insertAt, int delete, ShellSession session, String fileResource, Path basePath) {
            super(insertText, insertAt, delete, session, false);
            this.fileResource = fileResource;
            this.basePath = basePath;
        }

        @Override
        protected boolean closeCompletion() {
            return !insertText.endsWith("/"); // NOI18N
        }

        /**
         * Only provide the basename
         * @return 
         */
        @Override
        protected String getLeftHtmlText() {
            int from = insertText.endsWith("/") ? insertText.length() - 2 : insertText.length() -1;
            int lastSlash = insertText.lastIndexOf("/", from); // NOI18N
            if (lastSlash == 0 || lastSlash + 1 > from) {
                return insertText;
            } else {
                return insertText.substring(lastSlash + 1);
            }
        }
        
        

        @Override
        protected ImageIcon getIcon() {
            if (insertText.endsWith("/")) { // NOI18N
                return ImageUtilities.loadImageIcon(ICON_FOLDER, false);
            } else if (fileResource != null) {
                return ImageUtilities.loadImageIcon(fileResource, true);
            } else if (icon != null) {
                return icon;
            } else {
                Path p = basePath.resolve(insertText);
                if (p != null) {
                    FileObject f = FileUtil.toFileObject(p.toFile());
                    if (f != null) {
                        Node n;
                        
                        try {
                            DataObject d = DataObject.find(f);
                            n = d.getNodeDelegate();
                        } catch (DataObjectNotFoundException ex) {
                            n = f.getLookup().lookup(Node.class);
                        }
                        if (n != null) {
                            return ImageUtilities.icon2ImageIcon(ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16)));
                        }
                    }
                }
                return icon = ImageUtilities.loadImageIcon(ICON_FILE, false);
            }
        }
    }
    
    private static class CommandDocumentationTask implements CompletionTask, CompletionDocumentation {
        final JShellTool shellTool;
        final String command;
        String htmlText;

        public CommandDocumentationTask(JShellTool shellTool, String command) {
            this.shellTool = shellTool;
            this.command = command;
        }

        @Override
        public void query(CompletionResultSet resultSet) {
            String cmd = "/" + command + " ";
            String doc = shellTool.commandDocumentation(cmd, cmd.length(), false);
//COMMENTED BY MERGE
//=======
//            String doc = session.getJShellTool().commandDocumentation(cmd, cmd.length(), false);
//>>>>>>> merge rev
            if (doc == null) {
                resultSet.finish();
                return;
            }
            String[] lines = doc.split("\n");   // NOI18N
            StringBuilder htmlStringContents = new StringBuilder();
            String match = "^/" + command;
            Pattern p = Pattern.compile(match);
            boolean paragraph = false;
            for (String l : lines) {
                String l2 = l.trim();
                if (l2.isEmpty() && !paragraph) {
                    htmlStringContents.append("<p/>"); // NOI18N
                    paragraph = true;
                } else {
                    paragraph = false;
                }
                if (p.matcher(l).find()) {
                    htmlStringContents.append("<code><pre><b>").append(l).append("</b></pre></code><br/>"); // NOI18N
                } else {
                    String emphasis = l.replaceAll("(/\\p{Alpha}+)", "<code><i><u>$1</u></i><code>");
                    htmlStringContents.append(emphasis).append(" "); // NOI18N
                }
            }
            this.htmlText = htmlStringContents.toString();
            
            resultSet.setDocumentation(this);
            resultSet.finish();
        }

        @Override
        public void refresh(CompletionResultSet resultSet) {
        }

        @Override
        public void cancel() {
        }

        @Override
        public String getText() {
            return htmlText;
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
    
    /**
     * Extracts portion relevant for the option from the JShell's help system. The relevant portion starts with line
     * that contains command name + the option, and ends with the first line that starts with /command.
     */
    private static class OptionDocumentationTask implements CompletionTask, CompletionDocumentation {
        final ShellSession session;
        final String command;
        final String option;
        String htmlText;

        public OptionDocumentationTask(ShellSession session, String command, String option) {
            this.session = session;
            this.command = command;
            this.option = option;
        }
        
        @Override
        public void query(CompletionResultSet resultSet) {
            String cmd = "/" + command + " ";
            String doc = session.getJShellTool().commandDocumentation(cmd, cmd.length(), false);
            String[] lines = doc.split("\n");   // NOI18N
            StringBuilder htmlStringContents = new StringBuilder();
            String match = "^/" + command + ".*" + "-" + option;
            Pattern p = Pattern.compile(match);
            
            String endMarker = "/" + command;
            boolean copy = false;
            
            for (String l : lines) {
                if (copy) {
                    if (l.trim().startsWith(endMarker)) { // NOI18N
                        break;
                    }
                    String l2 = l.trim();
                    if (l2.isEmpty()) {
                        htmlStringContents.append("<p/>"); // NOI18N
                    } else {
                        htmlStringContents.append(l).append(" "); // NOI18N
                    }
                }
                if (p.matcher(l).find()) {
                    copy = true;
                    htmlStringContents.append("<b>").append(l).append("</b><br/>"); // NOI18N
                }
            }
            
            this.htmlText = htmlStringContents.toString();
            resultSet.setDocumentation(this);
            resultSet.finish();
        }

        @Override
        public void refresh(CompletionResultSet resultSet) {
            query(resultSet);
        }

        @Override
        public void cancel() {
        }

        @Override
        public String getText() {
            return htmlText;
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

    private static class OptionCompletionItem extends ToolCompletionItem {
        private final String command;
        
        public OptionCompletionItem(String insertText, int insertAt, int delete, ShellSession session, String command) {
            super(insertText, insertAt, delete, session, false);
            this.command = command;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>" + insertText + "</b>"; // NOI18N
        }
        
        

        @Override
        public CompletionTask createDocumentationTask() {
            return new OptionDocumentationTask(session, command, insertText.substring(1));
        }
    }
    
    private static class ToolCompletionItem implements CompletionItem {
        protected final String insertText;
        private final int   insertAt;
        private final int   delete;
        protected final ShellSession session;
        protected final boolean lowerPriority;

        public ToolCompletionItem(String insertText, int insertAt, int delete, ShellSession session, boolean empty) {
            this.insertText = insertText;
            this.insertAt = insertAt;
            this.delete = delete;
            this.session = session;
            this.lowerPriority = empty;
        }
        
        protected boolean closeCompletion() {
            return true;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            Document doc = component.getDocument();
            try {
                doc.insertString(insertAt, insertText, null);
                doc.remove(insertAt + insertText.length(), delete);
            } catch (BadLocationException ex) {
                // ignore
            }
            if (closeCompletion()) {
                Completion.get().hideAll();
            }
        }
        
        protected ImageIcon getIcon() {
            return null;
        }
        
        protected String getLeftHtmlText() {
            return insertText;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), null, g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), null, g, defaultFont, defaultColor, width, height, selected);
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
            return lowerPriority ? 3000 : 500;
        }

        @Override
        public CharSequence getSortText() {
            return insertText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return insertText;
        }
    }
    
    private static class CommandCompletionItem implements CompletionItem, CompletionDocumentation {
        private final int       fromOffset;
        private final String    command;
        private final ShellSession session;
        private final boolean lowerPriority;
        public CommandCompletionItem(ShellSession session, int fromOffset, String command, boolean lowerPriority) {
            this.fromOffset = fromOffset;
            this.command = command;
            this.session = session;
            this.lowerPriority = lowerPriority;
        }
        
        @Override
        public void defaultAction(JTextComponent component) {
            if (component == null) {
                return;
            }
            Completion.get().hideAll();
            
            Document doc = component.getDocument();
            if (doc == null) {
                return;
            }
            int caret = component.getCaretPosition();
            int l = caret - fromOffset;
            try {
                doc.insertString(fromOffset, "/" + command + " ", null); // NOI18N
                doc.remove(fromOffset + command.length() + 2, l);
            } catch (BadLocationException ex) {
                // ignore
            }
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }
        
        private ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/jshell/resources/jshell-command.png", false); // NOI18N
        }
        
        @NbBundle.Messages({
            "# {0} - command name",
            "Completion_CommandTemplate=<b>/{0}</b>"
        })
        public String getLeftHtmlText() {
            return Bundle.Completion_CommandTemplate(command);
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), null, g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), null, g, defaultFont, defaultColor, width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            JShellTool tool = session.getJShellTool();
            return tool == null ? null : new CommandDocumentationTask(tool, command);
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
            return lowerPriority ? 3000 : 500;
        }

        @Override
        public CharSequence getSortText() {
            return getInsertPrefix();
        }

        @Override
        public CharSequence getInsertPrefix() {
            return "/" + command; // NOI18N
        }

        @Override
        public String getText() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    
    private void completeClasspathParameter(CompletionResultSet results, Document doc, int caretOffset) {
        
    }

}
