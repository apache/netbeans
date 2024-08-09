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
package org.netbeans.modules.php.blade.editor.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.php.blade.editor.ResourceUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;

/**
 *
 * @author bhaidu
 */
public class BladeCompletionItem implements CompletionItem {

    protected static final int DEFAULT_SORT_PRIORITY = 20;
    protected int substitutionOffset;
    protected String text;
    protected boolean shift;

    //----------- Factory methods --------------
    public static BladeCompletionItem createTag(String name, int substitutionOffset) {
        return new BladeTag(name, substitutionOffset);
    }

    public static BladeCompletionItem createViewPath(String name,
            int substitutionOffset, boolean isFolder, String path) {
        return new ViewPath(name, substitutionOffset, isFolder, path);
    }

    public static BladeCompletionItem createInlineDirective(String directive,
            int substitutionOffset, String description) {
        return new InlineDirective(directive, substitutionOffset, description);
    }

    public static BladeCompletionItem createDirectiveWithArg(String directive,
            int substitutionOffset, String description) {
        return new DirectiveWithArg(directive, substitutionOffset, description);
    }

    public static BladeCompletionItem createBlockDirective(String directive,
            String endTag, int substitutionOffset, String description) {
        return new BlockDirective(directive, endTag, substitutionOffset, description);
    }

    public static BladeCompletionItem createBlockDirectiveWithArg(String directive,
            String endTag, int substitutionOffset, String description) {
        return new BlockDirectiveWithArg(directive, endTag, substitutionOffset, description);
    }
    
    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            if (!shift) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
            int caretOffset = component.getSelectionEnd();
            int len = caretOffset - substitutionOffset;
            if (len >= 0) {
                substituteText(component, len);
            }
        }
    }

    @Override
    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }

    @Override
    public int getPreferredWidth(Graphics grphcs, Font font) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), grphcs, font);
    }

    protected String getLeftHtmlText() {
        return text;
    }

    protected String getRightHtmlText() {
        return null;
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return null;
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
        if (component != null) {
            try {
                int caretOffset = component.getSelectionEnd();
                if (caretOffset > substitutionOffset) {
                    String currentText = component.getDocument().getText(substitutionOffset, caretOffset - substitutionOffset);
                    if (!getSubstituteText().startsWith(currentText)) {
                        return false;
                    }
                }
            } catch (BadLocationException ble) {
            }
        }
        defaultAction(component);
        return true;
    }

    @Override
    public int getSortPriority() {
        return DEFAULT_SORT_PRIORITY;
    }

    @Override
    public CharSequence getSortText() {
        return getItemText();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    protected String getSubstituteText() {
        return getItemText();
    }

    public String getItemText() {
        return text;
    }

    private boolean substituteText(JTextComponent component, int len) {
        return substituteText(component, getSubstituteText(), len, 0);
    }

    private boolean substituteText(JTextComponent c, final String substituteText, final int len, int moveBack) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        final boolean[] result = new boolean[1];
        result[0] = true;

        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    //test whether we are trying to insert sg. what is already present in the text
                    String currentText = doc.getText(substitutionOffset, (doc.getLength() - substitutionOffset) < substituteText.length() ? (doc.getLength() - substitutionOffset) : substituteText.length());
                    if (!substituteText.equals(currentText)) {
                        //remove common part
                        doc.remove(substitutionOffset, len);
                        insertString(doc, substitutionOffset, substituteText, c);
                    } else {
                        c.setCaretPosition(c.getSelectionEnd() + substituteText.length() - len);
                    }
                } catch (BadLocationException ex) {
                    result[0] = false;
                }

            }
        });

        //format the inserted text
        reindent(c);

        if (moveBack != 0) {
            Caret caret = c.getCaret();
            int dot = caret.getDot();
            caret.setDot(dot - moveBack);
        }

        return result[0];
    }

    protected void insertString(BaseDocument doc, int substitutionOffset,
            String substituteText, JTextComponent c) throws BadLocationException {
        doc.insertString(substitutionOffset, substituteText, null);
    }

    protected void reindent(JTextComponent c) {

    }

    public static class BladeTag extends BladeCompletionItem {

        public BladeTag(String name, int substitutionOffset) {
            this.text = name;
            this.substitutionOffset = substitutionOffset;
        }
    }

    public static class InlineDirective extends BladeCompletionItem {

        protected String description;

        public InlineDirective(String directive, int substitutionOffset,
                String description) {
            this.text = directive;
            this.substitutionOffset = substitutionOffset;
            this.description = description;
        }

        @Override
        protected String getRightHtmlText() {
            return description;
        }

        @Override
        protected ImageIcon getIcon() {
            String path = ResourceUtilities.DIRECTIVE_ICON;
            return ImageUtilities.loadImageIcon(path, false);
        }
    }

    public static class DirectiveWithArg extends InlineDirective {

        public DirectiveWithArg(String directive, int substitutionOffset,
                String description) {
            super(directive, substitutionOffset, description);
        }

        @Override
        protected String getSubstituteText() {
            String template = getItemText() + "($$${arg})"; // NOI18N
            switch (text){
                case "@include": // NOI18N
                case "@extends": // NOI18N   
                    template = getItemText() + "('${path}')"; // NOI18N
                    break;
            }
            return template;
        }

        @Override
        protected String getLeftHtmlText() {
            return text + "()";
        }

        @Override
        protected void insertString(BaseDocument doc, int substitutionOffset,
                String substituteText, JTextComponent ctx) throws BadLocationException {
            ctx.setCaretPosition(substitutionOffset);
            CodeTemplateManager.get(doc).createTemporary(substituteText).insert(ctx);
        }
    }

    public static class BlockDirective extends BladeCompletionItem {

        protected String description;
        protected String endTag;

        public BlockDirective(String directive, String endTag, int substitutionOffset,
                String description) {
            this.text = directive;
            this.substitutionOffset = substitutionOffset;
            this.description = description;
            this.endTag = endTag;
        }

        @Override
        protected String getSubstituteText() {
            return getItemText() + "\n${selection}${cursor}\n" + endTag;
        }

        @Override
        protected String getLeftHtmlText() {
            return text + " ... " + endTag;
        }

        @Override
        protected String getRightHtmlText() {
            return description;
        }

        @Override
        protected ImageIcon getIcon() {
            String path = ResourceUtilities.DIRECTIVE_ICON;
            return ImageUtilities.loadImageIcon(path, false);
        }

        @Override
        protected void insertString(BaseDocument doc, int substitutionOffset,
                String substituteText, JTextComponent ctx) throws BadLocationException {
            ctx.setCaretPosition(substitutionOffset);
            CodeTemplateManager.get(doc).createTemporary(substituteText).insert(ctx);
        }
    }
    
    public static class BlockDirectiveWithArg extends BlockDirective {

        public BlockDirectiveWithArg(String directive, String endTag, int substitutionOffset, String description) {
            super(directive, endTag, substitutionOffset, description);
        }

        @Override
        protected String getSubstituteText() {
            String template = getItemText() + "($$${arg})\n\n${selection}${cursor}\n" + endTag; // NOI18N
            
            switch (text){
                case "@foreach":
                    template = getItemText() + "($$${array} as $$${item})\n${selection}${cursor}\n" + endTag; // NOI18N
                    break;
            }
            
            return template;
        }

        @Override
        protected String getLeftHtmlText() {
            return text + "() ... " + endTag;
        }

    }

    public static class ViewPath extends BladeCompletionItem {

        protected boolean isFolder;
        protected String filePath;

        public ViewPath(String name, int substitutionOffset,
                boolean isFolder, String filePath) {
            this.text = name;
            this.substitutionOffset = substitutionOffset;
            this.isFolder = isFolder;
            this.filePath = filePath;
        }

        @Override
        protected ImageIcon getIcon() {
            String path = ResourceUtilities.BLADE_VIEW;
            if (isFolder) {
                path = ResourceUtilities.FOLDER;
            }
            return ImageUtilities.loadImageIcon(path, false);
        }

        @Override
        protected String getRightHtmlText() {
            int viewsPos = filePath.indexOf("/views/"); // NOI18N
            return filePath.substring(viewsPos, filePath.length());
        }
    }
}
