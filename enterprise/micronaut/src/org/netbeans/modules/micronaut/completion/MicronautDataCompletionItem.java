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
package org.netbeans.modules.micronaut.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Dusan Balek
 */
public abstract class MicronautDataCompletionItem implements CompletionItem {

    private static final String ICON = "org/netbeans/modules/micronaut/resources/micronaut.png"; //NOI18N

    public static MicronautDataCompletionItem createFinderMethodItem(String propName, String returnType, int offset) {
        return new FinderMethodItem(propName, returnType, offset);
    }

    public static MicronautDataCompletionItem createFinderMethodNameItem(String prefix, String propName, int offset) {
        return new FinderMethodNameItem(prefix, propName, offset);
    }

    protected final String name;
    protected final int offset;
    private ImageIcon icon;

    private MicronautDataCompletionItem(String name, int offset) {
        this.name = name;
        this.offset = offset;
    }

    @Override
    public int getSortPriority() {
        return 10;
    }

    @Override
    public CharSequence getSortText() {
        return name;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return name;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            process(component, false);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_PRESSED && evt.getKeyCode() == KeyEvent.VK_ENTER && (evt.getModifiers() & InputEvent.CTRL_MASK) > 0) {
            JTextComponent component = (JTextComponent)evt.getSource();
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            process(component, true);
            evt.consume();
        }
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
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
        return false;
    }

    protected String getRightHtmlText() {
        return null;
    }

    protected ImageIcon getIcon() {
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon(ICON, false);
        }
        return icon;
    }

    protected abstract String getLeftHtmlText();

    protected abstract void process(JTextComponent component, boolean overwrite);

    private static final class FinderMethodItem extends MicronautDataCompletionItem {

        private final String returnType;

        private FinderMethodItem(String name, String returnType, int offset) {
            super(name, offset);
            this.returnType = returnType;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>" + name + "</b></font>";
        }

        @Override
        protected void process(JTextComponent component, boolean overwrite) {
            StringBuilder template = returnType != null
                    ? new StringBuilder("${PAR#1 default=\"")
                            .append(returnType)
                            .append("\"} ")
                            .append(name)
                            .append("${cursor completionInvoke}()")
                    : null;
            final BaseDocument doc = (BaseDocument) component.getDocument();
            doc.runAtomic (new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.remove(offset, component.getCaretPosition() - offset);
                        if (template == null) {
                            doc.insertString(offset, name, null);
                        }
                    } catch (BadLocationException e) {
                    }
                }
            });
            if (template != null) {
                CodeTemplateManager.get(doc).createTemporary(template.toString()).insert(component);
            }
        }
    }

    private static final class FinderMethodNameItem extends MicronautDataCompletionItem {

        private final String prefix;

        private FinderMethodNameItem(String prefix, String name, int offset) {
            super(name, offset);
            this.prefix = prefix;
        }

        @Override
        protected String getLeftHtmlText() {
            return prefix + "<b>" + name + "</b>";
        }

        @Override
        protected void process(JTextComponent component, boolean overwrite) {
            final BaseDocument doc = (BaseDocument) component.getDocument();
            doc.runAtomic (new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.remove(offset, component.getCaretPosition() - offset);
                        doc.insertString(offset, prefix + name, null);
                    } catch (BadLocationException e) {
                    }
                }
            });
        }
    }
}
