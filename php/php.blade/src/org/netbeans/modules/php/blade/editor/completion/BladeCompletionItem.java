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
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.php.blade.editor.ResourceUtilities;
import org.netbeans.modules.php.blade.syntax.BladeDirectivesUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;

/**
 *
 * @author bhaidu
 */
public abstract class BladeCompletionItem implements CompletionItem {

    protected static final int DEFAULT_SORT_PRIORITY = 20;
    private final int substitutionOffset;
    private final String name;
    @NullAllowed
    private final String description;
    private boolean shift;

    BladeCompletionItem(String name, int substitutionOffset, String description) {
        this.name = name;
        this.substitutionOffset = substitutionOffset;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
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

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
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
    
    protected ImageIcon getIcon() {
        return null;
    }
    
    protected String getLeftHtmlText() {
        return name;
    }

    protected String getRightHtmlText() {
        return null;
    }

    protected String getSubstituteText() {
        return getItemText();
    }

    public String getItemText() {
        return name;
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
            super(name, substitutionOffset, null);
        }
    }

    public static class InlineDirective extends BladeCompletionItem {

        public InlineDirective(String directive, int substitutionOffset,
                String description) {
            super(directive, substitutionOffset, description);
        }

        @Override
        protected String getRightHtmlText() {
            return getDescription();
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
            switch (getName()) {
                case BladeDirectivesUtils.DIRECTIVE_INCLUDE,
                        BladeDirectivesUtils.DIRECTIVE_EXTENDS ->
                    template = getItemText() + "('${path}')"; // NOI18N
            }
            return template;
        }

        @Override
        protected String getLeftHtmlText() {
            return getName() + "()"; // NOI18N
        }

        @Override
        protected void insertString(BaseDocument doc, int substitutionOffset,
                String substituteText, JTextComponent ctx) throws BadLocationException {
            ctx.setCaretPosition(substitutionOffset);
            CodeTemplateManager.get(doc).createTemporary(substituteText).insert(ctx);
        }
    }

    public static class BlockDirective extends BladeCompletionItem {

        private final String endTag;

        public BlockDirective(String directive, String endTag, int substitutionOffset,
                String description) {
            super(directive, substitutionOffset, description);
            this.endTag = endTag;
        }

        @Override
        protected String getSubstituteText() {
            return getItemText() + "\n${selection}${cursor}\n" + endTag; // NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            return getName() + " ... " + endTag; // NOI18N
        }

        @Override
        protected String getRightHtmlText() {
            return getDescription();
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

        public String getEndTag() {
            return endTag;
        }
    }

    public static class BlockDirectiveWithArg extends BlockDirective {

        public BlockDirectiveWithArg(String directive, String endTag, int substitutionOffset, String description) {
            super(directive, endTag, substitutionOffset, description);
        }

        @Override
        protected String getSubstituteText() {
            String template = getItemText() + "($$${arg})\n\n${selection}${cursor}\n" + getEndTag(); // NOI18N

            switch (getName()) {
                case BladeDirectivesUtils.DIRECTIVE_FOREACH ->
                    template = getItemText() + "($$${array} as $$${item})\n${selection}${cursor}\n" + getEndTag(); // NOI18N
            }

            return template;
        }

        @Override
        protected String getLeftHtmlText() {
            return getName() + "() ... " + getEndTag(); // NOI18N
        }

    }

    public static class ViewPath extends BladeCompletionItem {

        private final boolean isFolder;
        private final String filePath;

        public ViewPath(String name, int substitutionOffset,
                boolean isFolder, String filePath) {
            super(name, substitutionOffset, null);
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
