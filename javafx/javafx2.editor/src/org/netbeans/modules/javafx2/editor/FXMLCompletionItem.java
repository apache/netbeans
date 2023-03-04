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
package org.netbeans.modules.javafx2.editor;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Lahoda, David Strupl
 */
public class FXMLCompletionItem implements CompletionItem {
    
    private int substituteOffset;
    private String word;
    
    /** Creates a new instance of WordCompletionItem */
    public FXMLCompletionItem(int substituteOffset, String word) {
        this.substituteOffset = substituteOffset;
        this.word = word;
    }
    
    public void setSubstituteOffset(int substituteOffset) {
        this.substituteOffset = substituteOffset;
    }
    
    public int getSubstituteOffset() {
        return substituteOffset;
    }

    @Override
    public void defaultAction(final JTextComponent component) {
        Completion.get().hideCompletion();
        Completion.get().hideDocumentation();
        NbDocument.runAtomic((StyledDocument) component.getDocument(), new Runnable() {
            @Override
            public void run() {
                Document doc = component.getDocument();
                
                try {
                    doc.remove(substituteOffset, component.getCaretPosition() - substituteOffset);
                    doc.insertString(substituteOffset, getText(), null);
                } catch (BadLocationException e) {
                    Logger.getLogger(FXMLCompletionItem.class.getName()).log(Level.FINE, null, e);
                }
            }
        });
    }
    
    @Override
    public void processKeyEvent(KeyEvent evt) {
    }
    
    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(
                escapeFirstLessThanSign(getText()), null, g, defaultFont);
    }
    
    private static String escapeFirstLessThanSign(String text) {
        String result = text;
        if ((result != null) && (result.startsWith("<"))) {
            // a hack to pass valid HTML text into the getPrefferredWidth
            result = "&lt;" + result.substring(1); // NOI18N
        }
        return result;
    }
    
    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        if (selected) {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);
            g.setColor(defaultColor);
        }
        CompletionUtilities.renderHtml(null, escapeFirstLessThanSign(getText()), null, g, defaultFont, defaultColor, width, height, selected);
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
        return true;
    }
    
    @Override
    public int getSortPriority() {
        return 100;
    }
    
    @Override
    public CharSequence getSortText() {
        return getText();
    }
    
    protected String getText() {
        return word;
    }
    
    @Override
    public CharSequence getInsertPrefix() {
        return getText();
    }

    @Override
    public String toString() {
        return word + "|" + substituteOffset; // NOI18N
    }
    
}
