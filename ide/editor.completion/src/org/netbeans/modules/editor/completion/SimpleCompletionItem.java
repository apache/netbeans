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
package org.netbeans.modules.editor.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Dusan Balek
 */
public class SimpleCompletionItem implements CompletionItem {

    private final String insertText;
    private final int startOffset;
    private final int endOffset;
    private final String iconResource;
    private final String leftHtmlText;
    private final String rightHtmlText;
    private final int sortPriority;
    private final CharSequence sortText;
    private final Supplier<CompletionTask> documentationTask;
    private final Supplier<CompletionTask> tooltipTask;
    private final Consumer<CompletionUtilities.OnSelectContext> onSelectCallback;

    private ImageIcon icon;

    public SimpleCompletionItem(String insertText, int startOffset, int endOffset, String iconResource, String leftHtmlText, String rightHtmlText,
            int sortPriority, CharSequence sortText, Supplier<CompletionTask> documentationTask, Supplier<CompletionTask> tooltipTask,
            Consumer<CompletionUtilities.OnSelectContext> onSelectCallback) {
        this.insertText = insertText;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.iconResource = iconResource;
        this.leftHtmlText = leftHtmlText;
        this.rightHtmlText = rightHtmlText;
        this.sortPriority = sortPriority;
        this.sortText = sortText;
        this.documentationTask = documentationTask;
        this.tooltipTask = tooltipTask;
        this.onSelectCallback = onSelectCallback;
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
        return CompletionUtilities.getPreferredWidth(leftHtmlText != null ? leftHtmlText : insertText, rightHtmlText, g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), leftHtmlText != null ? leftHtmlText : insertText, rightHtmlText, g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        if (documentationTask != null) {
            return documentationTask.get();
        }
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        if (tooltipTask != null) {
            tooltipTask.get();
        }
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return sortPriority;
    }

    @Override
    public CharSequence getSortText() {
        return sortText != null ? sortText : insertText;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return insertText;
    }

    private void process(JTextComponent component, boolean overwrite) {
        if (onSelectCallback != null) {
            CompletionUtilities.OnSelectContext ctx = CompletionSupportSpiPackageAccessor.get().createOnSelectContext(component, overwrite);
            onSelectCallback.accept(ctx);
        } else {
            final BaseDocument doc = (BaseDocument) component.getDocument();
            doc.runAtomic (new Runnable() {
                @Override
                public void run() {
                    try {
                        if (startOffset < 0) {
                            if (overwrite && endOffset > component.getCaretPosition()) {
                                doc.remove(component.getCaretPosition(), endOffset - component.getCaretPosition());
                            }
                            doc.insertString(component.getCaretPosition(), insertText, null);
                        } else {
                            doc.remove(startOffset, (overwrite && endOffset > component.getCaretPosition() ? endOffset : component.getCaretPosition()) - startOffset);
                            doc.insertString(startOffset, insertText, null);
                        }
                    } catch (BadLocationException e) {
                    }
                }
            });
        }
    }

    private ImageIcon getIcon() {
        if (icon == null && iconResource != null) {
            icon = ImageUtilities.loadImageIcon(iconResource, false);
        }
        return icon;
    }
}
