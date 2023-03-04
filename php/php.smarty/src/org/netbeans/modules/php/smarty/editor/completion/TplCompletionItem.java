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
package org.netbeans.modules.php.smarty.editor.completion;

import java.awt.Font;
import java.awt.Graphics;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.text.Caret;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.completion.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 * Code completion result item base class
 *
 * @author Martin Fousek
 */
public class TplCompletionItem implements CompletionItem {

    protected static final int DEFAULT_SORT_PRIORITY = 22;

    //------------------------------------------
    protected String text, help, helpUrl;
    protected boolean shift;

    protected TplCompletionItem(String text) {
        this.text = text;
    }

    protected TplCompletionItem(String text, String help) {
        this.text = text;
        this.help = help;
    }

    protected TplCompletionItem(String text, String help, String helpUrl) {
        this(text);
        this.help = help;
        this.helpUrl = helpUrl;
    }

    public String getItemText() {
        return text;
    }

    public String getItemHelp() {
        return help;
    }

    public int getSortPriority() {
        return DEFAULT_SORT_PRIORITY;
    }

    public CharSequence getSortText() {
        return getItemText();
    }

    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }

    public void defaultAction(JTextComponent component) {
        if (component != null) {
            if (!shift) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
            substituteText(component, CodeCompletionUtils.getSubstitutionLenght(component.getDocument(), component.getCaretPosition()));
        }

    }

    protected int getMoveBackLength() {
        return 0; //default
    }

    /** 
     * Subclasses may override to customize the completed text 
     * if they do not want to override the substituteText method. 
     */
    protected String getSubstituteText() {
        return getItemText();
    }

    protected boolean substituteText(JTextComponent c, int len) {
        return substituteText(c, len, getMoveBackLength());
    }

    protected boolean substituteText(final JTextComponent c, final int len, int moveBack) {
        return substituteText(c, getSubstituteText(), len, moveBack);
    }

    protected boolean substituteText(final JTextComponent c, final String substituteText, final int len, int moveBack) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        final boolean[] result = new boolean[1];
        result[0] = true;

        doc.runAtomic(new Runnable() {

            public void run() {
                try {
                    int substitutionOffset = c.getCaretPosition() - CodeCompletionUtils.getSubstitutionLenght(c.getDocument(), c.getCaretPosition());
                    //test whether we are trying to insert sg. what is already present in the text
                    String currentText = doc.getText(substitutionOffset, (doc.getLength() - substitutionOffset) < substituteText.length() ? (doc.getLength() - substitutionOffset) : substituteText.length());
                    if (!substituteText.equals(currentText)) {
                        //remove common part
                        doc.remove(substitutionOffset, len);
                        doc.insertString(substitutionOffset, substituteText, null);
                    } else {
                        c.setCaretPosition(c.getCaret().getDot() + substituteText.length() - len);
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

    private void reindent(JTextComponent component) {

        final BaseDocument doc = (BaseDocument) component.getDocument();
        final int dotPos = component.getCaretPosition();
        final Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        int startOffset = Utilities.getRowStart(doc, dotPos);
                        int endOffset = Utilities.getRowEnd(doc, dotPos);
                        indent.reindent(startOffset, endOffset);
                    } catch (BadLocationException ex) {
                        //ignore
                        }
                }
            });
        } finally {
            indent.unlock();
        }

    }

    public boolean instantSubstitution(JTextComponent component) {
        if (component != null) {
            try {
                int substitutionOffset = CodeCompletionUtils.getSubstitutionLenght(component.getDocument(), component.getCaretPosition());
                int caretOffset = component.getSelectionEnd();
                if (caretOffset > substitutionOffset) {
                    String currentText = component.getDocument().getText(substitutionOffset, caretOffset - substitutionOffset);
                    if (!getSubstituteText().toString().startsWith(currentText)) {
                        return false;
                    }
                }
            } catch (BadLocationException ble) {
            }
        }
        defaultAction(component);
        return true;
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, Color.BLACK, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/org/netbeans/modules/php/smarty/resources/tpl-cc-icon.png"));
    }

    protected String getLeftHtmlText() {
        return getItemText();
    }

    protected String getRightHtmlText() {
        return null;
    }

    /** Returns help for the item. If the item doesn't have a help than returns null.
     *  The class can overwrite this method and compounds the help realtime.
     */
    public String getHelp() {
        return getItemHelp();
    }

    /** Returns whether the item has a help.
     */
    public boolean hasHelp() {
        return (help != null && help.length() > 0);
    }

    /** Returns a url or null, if the help is not URL or the help is not defined.
     */
    public URL getHelpURL() {
        if (help == null || help.equals("")) {
            return null;
        }
        try {
            return new URL(helpUrl);
        } catch (java.io.IOException e) {
        }
        return null;
    }

    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new TplCompletionProvider.DocQuery(this));
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public static class BuiltInFunctionsCompletionItem extends TplCompletionItem {

        protected static final String BUILT_IN_FUNC_COLOR = "529854";

        public BuiltInFunctionsCompletionItem(String value, String help, String helpUrl) {
            super(value, help, helpUrl);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + BUILT_IN_FUNC_COLOR + ">" + getItemText() + "</font>"; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return 20;
        }

    }

    public static class CustomFunctionsCompletionItem extends BuiltInFunctionsCompletionItem {

        protected static final String CUSTOM_FUNC_COLOR = "3B713B";

        public CustomFunctionsCompletionItem(String value, String help, String helpUrl) {
            super(value, help, helpUrl);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + CUSTOM_FUNC_COLOR + ">" + getItemText() + "</font>"; //NOI18N
        }

    }

    public static class FunctionParametersCompletionItem extends TplCompletionItem {

        protected static final String CUSTOM_FUNC_COLOR = "D6822D";

        public FunctionParametersCompletionItem(String value, String help) {
            super(value, help);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + CUSTOM_FUNC_COLOR + ">" + getItemText() + "</font>"; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return 18;
        }

    }

    public static class VariableModifiersCompletionItem extends TplCompletionItem {

        protected static final String ATTR_NAME_COLOR = hexColorCode(Color.blue.darker());

        public VariableModifiersCompletionItem(String value, String help, String helpUrl) {
            super(value, help, helpUrl);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + ATTR_NAME_COLOR + ">" + getItemText() + "</font>"; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return 25;
        }
    }

    public static final String hexColorCode(Color c) {
        return Integer.toHexString(c.getRGB()).substring(2);
    }

}
