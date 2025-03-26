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
package org.netbeans.modules.php.dbgp.ui.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class ExceptionCompletionItem implements CompletionItem {

    @StaticResource
    private static final String CLASS_ICON = "org/netbeans/modules/php/dbgp/resources/class.png"; // NOI18N

    @StaticResource
    private static final String PLATFORM_ICON = "org/netbeans/modules/php/dbgp/resources/php16Key.png"; // NOI18N

    private final ClassElement element;
    private final boolean isPlatform;
    private final boolean isDeprecated;

    public ExceptionCompletionItem(ClassElement element) {
        this.element = element;
        if (element instanceof ClassElement) {
            isPlatform = element.isPlatform();
            isDeprecated = element.isDeprecated();
        } else {
            isPlatform = false;
            isDeprecated = false;
        }
    }

    @Override
    public void defaultAction(JTextComponent component) {
        Document doc = component.getDocument();
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, getName(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        Completion.get().hideAll();
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftText(), getRightText(), g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        ImageIcon icon = ImageUtilities.loadImageIcon(getIcon(), false);

        CompletionUtilities.renderHtml(icon, getLeftText(), getRightText(), g, defaultFont,
        Color.black, width, height, selected);
    }

    public String getIcon() {
        return CLASS_ICON;
    }

    public String getLeftText() {
        StringBuilder sb = new StringBuilder();
        if (isDeprecated) {
            sb.append("<s>"); // NOI18N
        }
        sb.append("<font color=\"#560000\">"); // NOI18N
        sb.append(element.getName());
        sb.append("</font>"); // NOI18N
        if (isDeprecated) {
            sb.append("</s>"); // NOI18N
        }

        return sb.toString();
    }

    public String getRightText() {
        StringBuilder sb = new StringBuilder();

        final String in = element.getIn();
        if (in != null && in.length() > 0) {
            sb.append(in);
            return sb.toString();
        } else if (element instanceof PhpElement) {
            PhpElement ie = (PhpElement) element;
            if (isPlatform) {
                return "PHP Platform"; // NOI18N
            }

            String filename = ie.getFilenameUrl();
            int index = filename.lastIndexOf('/');
            if (index != -1) {
                filename = filename.substring(index + 1);
            }

            sb.append(filename);
            return sb.toString();
        }

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
        return false;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return element.getName();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return element.getName();
    }

    public String getName() {
        return element.getNamespaceName().append(element.getName()).toString();
    }

    public static class Builtin extends ExceptionCompletionItem {

        private final String element;

        public Builtin(String element) {
            super(null);
            this.element = element;
        }

        @Override
        public String getIcon() {
            return PLATFORM_ICON;
        }

        @Override
        public String getRightText() {
            return "";
        }

        @Override
        public String getLeftText() {
            return this.element;
        }

        @Override
        public String getName() {
            return this.element;
        }

        @Override
        public int getSortPriority() {
            return -1;
        }

        @Override
        public CharSequence getSortText() {
            return this.element;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return element;
        }
    }

}
