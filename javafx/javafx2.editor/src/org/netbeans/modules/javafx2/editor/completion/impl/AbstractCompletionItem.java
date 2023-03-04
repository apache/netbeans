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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
abstract class AbstractCompletionItem implements CompletionItem {
    private final int substOffset;
    private final int length;
    private final String text;
    protected final CompletionContext ctx;
    private Position  substPos;
    
    protected AbstractCompletionItem(CompletionContext ctx, String text) {
        this.substOffset = ctx.getStartOffset();
        this.length = ctx.getReplaceLength();
        this.text = text;
        this.ctx = ctx;
    }
    
    protected int getStartOffset() {
        return substOffset;
    }
    
    protected int getLength() {
        return length;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        substituteText(component, getSubstituteText());
    }
    
    protected String getSubstituteText() {
        return text;
    }
    
    protected int getCaretShift(Document d) {
        return getSubstituteText().length();
    }

    public int getSubstOffset() {
        if (substPos != null) {
            return substPos.getOffset();
        }
        return substOffset;
    }
    
    protected void substituteText(final JTextComponent c, final String text) {
        final Document d = c.getDocument();
        BaseDocument bd = (BaseDocument)d;
        bd.extWriteLock();
        try {
            substPos = bd.createPosition(substOffset);
            doSubstituteText(c, bd, text);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            bd.extWriteUnlock();
        }
    }
    
    protected void doSubstituteText(JTextComponent c, BaseDocument d, String text) throws BadLocationException {
        int offset = getSubstOffset();
        String old = d.getText(offset, length);
        int nextOffset = ctx.getNextCaretPos();
        Position p = null;
        
        if (nextOffset >= 0) {
            p = d.createPosition(nextOffset);
        }
        if (text.equals(old)) {
            if (p != null) {
                c.setCaretPosition(p.getOffset());
            } else {
                c.setCaretPosition(offset + getCaretShift(d));
            }
        } else {
            d.remove(offset, length);
            d.insertString(offset, text, null);
            if (p != null) {
                c.setCaretPosition(p.getOffset());
            } else {
                c.setCaretPosition(offset + getCaretShift(d));
            }
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }
    
    protected String getLeftHtmlText() {
        return text;
    }
    
    protected String getRightHtmlText() {
        return null;
    }
    
    protected ImageIcon getIcon() {
        return null;
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

    @Override
    public int getSortPriority() {
        return -1;
    }

    @Override
    public CharSequence getSortText() {
        return text;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return text;
    }
}
