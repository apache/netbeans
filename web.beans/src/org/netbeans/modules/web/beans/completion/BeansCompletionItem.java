/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.beans.completion;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 * @author Dusan Balek, Andrei Badea, Marek Fukala
 */
public abstract class BeansCompletionItem implements CompletionItem {

    static BeansCompletionItem createBeansTagValueItem(int substitutionOffset, String fullName, String simpleName) {
        return new TagClassValueItem(substitutionOffset, fullName, simpleName);
    }
    protected int substituteOffset = -1;

    public abstract String getItemText();

    public String getSubstitutionText() {
        return getItemText();
    }

    public int getSubstituteOffset() {
        return substituteOffset;
    }

    public boolean substituteCommonText(JTextComponent c, int offset, int len, int subLen) {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }

    public boolean substituteText(JTextComponent c, int offset, int len, boolean shifted) {
        BaseDocument doc = (BaseDocument) c.getDocument();
        String text = getSubstitutionText();

        if (text != null) {
            if (toAdd != null && !toAdd.equals("\n")) // NOI18N
            {
                text += toAdd;
            }
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) {
                    return false;
                }

                if(!shifted) {//we are not in part of literal completion
                    //dirty hack for @Table(name=CUS|
                    if (!text.startsWith("\"")) {
                        text = quoteText(text);
                    }

                    //check if there is already an end quote
                    char ch = doc.getText(offset + len, 1).charAt(0);
                    if (ch == '"') {
                        //remove also this end quote since the inserted value is always quoted
                        len++;
                    }
                }

                doc.remove(offset, len);
                doc.insertString(offset, text, null);
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
            return true;

        } else {
            return false;
        }
    }

    public boolean canFilter() {
        return true;
    }

    public boolean cutomPosition() {
        return false;
    }

    public int getCutomPosition() {
        return -1;
    }

    public Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
        Component ret = getPaintComponent(isSelected);
        if (ret == null) {
            return null;
        }
        if (isSelected) {
            ret.setBackground(list.getSelectionBackground());
            ret.setForeground(list.getSelectionForeground());
        } else {
            ret.setBackground(list.getBackground());
            ret.setForeground(list.getForeground());
        }
        ret.getAccessibleContext().setAccessibleName(getItemText());
        ret.getAccessibleContext().setAccessibleDescription(getItemText());
        return ret;
    }

    public abstract Component getPaintComponent(boolean isSelected);

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        Component renderComponent = getPaintComponent(false);
        return renderComponent.getPreferredSize().width;
    }

    @Override
    public String toString() {
        return getItemText();
    }
    // CompletionItem implementation
    public static final String COMPLETION_SUBSTITUTE_TEXT = "completion-substitute-text"; //NOI18N
    static String toAdd;

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            Completion completion = Completion.get();
            switch (evt.getKeyChar()) {
                case ' ':
                    if (evt.getModifiers() == 0) {
                        completion.hideCompletion();
                        completion.hideDocumentation();
                    }
                    break;
            }
        }
    }

    protected String quoteText(String s) {
        return "\"" + s + "\"";
    }

    @Override
    public CharSequence getSortText() {
        return getItemText();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return getItemText();
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
    public boolean instantSubstitution(JTextComponent c) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(c);
        return true;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(component, "");
    }

    private boolean defaultAction(JTextComponent component, String addText) {
        int substOffset = substituteOffset;
        if (substOffset == -1) {
            substOffset = component.getCaret().getDot();
        }
        BeansCompletionItem.toAdd = addText;
        return substituteText(component, substOffset, component.getCaret().getDot() - substOffset, false);
    }

    abstract private static class BeansXmlCompletionItem extends BeansCompletionItem {
        /////////

        protected int substitutionOffset;

        protected BeansXmlCompletionItem(int substitutionOffset) {
            this.substitutionOffset = substitutionOffset;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            if (component != null) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
                int caretOffset = component.getSelectionEnd();
                substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
            }
        }

        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            BaseDocument doc = (BaseDocument) c.getDocument();
            CharSequence prefix = getInsertPrefix();
            String text = prefix.toString();
            if (toAdd != null) {
                text += toAdd;
            }

            doc.atomicLock();
            try {
                Position position = doc.createPosition(offset);
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text.toString(), null);
            } catch (BadLocationException ble) {
                // nothing can be done to update
            } finally {
                doc.atomicUnlock();
            }
        }

        @Override
        public String getSubstitutionText() {
            return getInsertPrefix().toString();
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(getLeftHtmlText(),
                    getRightHtmlText(), g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor,
                Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(),
                    getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
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
            defaultAction(component);
            return true;
        }

        protected String getLeftHtmlText() {
            return null;
        }

        protected String getRightHtmlText() {
            return null;
        }

        protected ImageIcon getIcon() {
            return null;
        }

        public abstract String getDisplayText();
        /////////
    }

    private static class TagClassValueItem extends BeansXmlCompletionItem {

        private final String displayText, simpleName;
        CCPaintComponent.DBElementPaintComponent paintComponent;

        public TagClassValueItem(int substitutionOffset, String fullName, String simpleName) {
            super(substitutionOffset);
            this.displayText = fullName;
            this.simpleName = simpleName;
        }

        @Override
        public int getSortPriority() {
                return 100;
        }

        @Override
        public CharSequence getSortText() {
            return displayText;
        }
        

        @Override
        public CharSequence getInsertPrefix() {
            return displayText;
        }

        @Override
        public String getDisplayText() {
            return simpleName + (simpleName.length()<displayText.length() ? (" ("+displayText.substring(0,displayText.length()-simpleName.length()-1)+")"): "");
         }

        @Override
        protected String getLeftHtmlText() {
            return getDisplayText();
        }

        @Override
         public String getItemText() {
            
                return getDisplayText();
            
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.DBElementPaintComponent();
            }
            paintComponent.setString(getDisplayText()); // NOI18N
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
    }
}
