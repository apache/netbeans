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
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public abstract class MicronautConfigCompletionItem implements CompletionItem {

    public static final String PROPERTY_NAME_COLOR = getHTMLColor(64, 64, 217);
    private static final String ICON = "org/netbeans/modules/editor/resources/completion/field_16.png"; //NOI18N

    public static MicronautConfigCompletionItem createTopLevelPropertyItem(String propName, int offset, int baseIndent, int indentLevelSize) {
        return new TopLevelItem(propName, offset, baseIndent, indentLevelSize);
    }

    public static MicronautConfigCompletionItem createPropertyItem(ConfigurationMetadataProperty property, int offset, int baseIndent, int indentLevelSize, int idx) {
        return new PropertyItem(property, offset, baseIndent, indentLevelSize, idx);
    }

    protected final String propName;
    protected final int offset;
    protected final int baseIndent;
    protected final int indentLevelSize;
    private ImageIcon icon;

    private MicronautConfigCompletionItem(String propName, int offset, int baseIndent, int indentLevelSize) {
        this.propName = propName;
        this.offset = offset;
        this.baseIndent = baseIndent;
        this.indentLevelSize = indentLevelSize;
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

    @Override
    public CharSequence getSortText() {
        return propName;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return propName;
    }

    protected String getLeftHtmlText() {
        return null;
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

    protected abstract void process(JTextComponent component, boolean overwrite);

    private static final class TopLevelItem extends MicronautConfigCompletionItem {

        private TopLevelItem(String propName, int offset, int baseIndent, int indentLevelSize) {
            super(propName, offset, baseIndent, indentLevelSize);
        }

        @Override
        public int getSortPriority() {
            return 10;
        }

        @Override
        protected String getLeftHtmlText() {
            return PROPERTY_NAME_COLOR + "<b>" + propName + "</b></font>";
        }

        @Override
        protected void process(JTextComponent component, boolean overwrite) {
            try {
                Document doc = component.getDocument();
                LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
                if (lineDocument != null) {
                    int caretOffset = component.getCaretPosition();
                    int end = LineDocumentUtils.getWordEnd(lineDocument, caretOffset);
                    if (overwrite && LineDocumentUtils.getWordStart(lineDocument, end) == offset) {
                        String textEnd = doc.getText(end, 1);
                        if (baseIndent < 0 && textEnd.endsWith(".") || textEnd.endsWith(":")) {
                            end++;
                        }
                        doc.remove(offset, Math.max(caretOffset, end) - offset);
                    } else if (offset < caretOffset) {
                        doc.remove(offset, caretOffset - offset);
                    }
                    StringBuilder sb = new StringBuilder();
                    if (baseIndent < 0) {
                        sb.append("*".equals(propName) ? "${PAR#1 default=\"\"}" : propName).append(".${cursor completionInvoke}");
                    } else {
                        int lineStart = LineDocumentUtils.getLineStart(lineDocument, caretOffset);
                        int lineIndent = IndentUtils.lineIndent(doc, lineStart);
                        ArrayUtilities.appendSpaces(sb, baseIndent - lineIndent);
                        sb.append("*".equals(propName) ? "${PAR#1 default=\"\"}" : propName).append(":\n");
                        ArrayUtilities.appendSpaces(sb, baseIndent + indentLevelSize);
                        sb.append("${cursor completionInvoke}");
                    }
                    CodeTemplateManager.get(doc).createTemporary(sb.toString()).insert(component);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class PropertyItem extends MicronautConfigCompletionItem {

        private static final Pattern FQN = Pattern.compile("(\\w+\\.)+(\\w+)");
        private final ConfigurationMetadataProperty property;
        private final int idx;
        private String left;
        private String right;

        private PropertyItem(ConfigurationMetadataProperty property, int offset, int baseIndent, int indentLevelSize, int idx) {
            super(property.getId(), offset, baseIndent, indentLevelSize);
            this.property = property;
            this.idx = idx;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return MicronautConfigCompletionProvider.createDocTask(property);
        }

        @Override
        public int getSortPriority() {
            return property.isDeprecated() ? 30 : 20;
        }

        @Override
        protected String getLeftHtmlText() {
            if (left == null) {
                if (property.isDeprecated()) {
                    left = PROPERTY_NAME_COLOR + "<s>" + propName + "</s></font>";
                } else {
                    left = PROPERTY_NAME_COLOR + propName + "</font>";
                }
            }
            return left;
        }

        @Override
        protected String getRightHtmlText() {
            if (right == null) {
                String type = property.getType();
                right = type != null ? escape(FQN.matcher(type).replaceAll("$2")) : "";
            }
            return right;
        }

        @Override
        protected void process(JTextComponent component, boolean overwrite) {
            try {
                Document doc = component.getDocument();
                LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
                if (lineDocument != null) {
                    int caretOffset = component.getCaretPosition();
                    int end = LineDocumentUtils.getWordEnd(lineDocument, caretOffset);
                    if (overwrite && LineDocumentUtils.getWordStart(lineDocument, end) == offset) {
                        String textEnd = doc.getText(end, 1);
                        while(baseIndent < 0 && textEnd.endsWith(".")) {
                            end = LineDocumentUtils.getWordEnd(lineDocument, end + 1);
                            textEnd = doc.getText(end, 1);
                        }
                        if (baseIndent < 0 && textEnd.endsWith("=") || textEnd.endsWith(":")) {
                            end++;
                        }
                        doc.remove(offset, Math.max(caretOffset, end) - offset);
                    } else if (offset < caretOffset) {
                        doc.remove(offset, caretOffset - offset);
                    }
                    StringBuilder sb = new StringBuilder();
                    String name = propName.substring(idx);
                    String[] parts = name.split("\\.");
                    if (baseIndent < 0) {
                        int num = 1;
                        for (int i = 0; i < parts.length; i++) {
                            String part = parts[i];
                            if ("*".equals(part)) {
                                sb.append("${PAR#" + num++ + " default=\"\"}");
                            } else {
                                sb.append(part);
                            }
                            if (i < parts.length - 1) {
                                sb.append(".");
                            } else {
                                sb.append("=${cursor}");
                            }
                        }
                    } else {
                        int lineStart = LineDocumentUtils.getLineStart(lineDocument, caretOffset);
                        int lineIndent = IndentUtils.lineIndent(doc, lineStart);
                        ArrayUtilities.appendSpaces(sb, baseIndent - lineIndent);
                        int indent = baseIndent;
                        int num = 1;
                        for (int i = 0; i < parts.length; i++) {
                            String part = parts[i];
                            if ("*".equals(part)) {
                                sb.append("${PAR#" + num++ + " default=\"\"}");
                            } else {
                                sb.append(part);
                            }
                            if (i < parts.length - 1) {
                                sb.append(":\n");
                                ArrayUtilities.appendSpaces(sb, (indent = indent + indentLevelSize));
                            } else {
                                sb.append(": ${cursor}");
                            }
                        }
                    }
                    CodeTemplateManager.get(doc).createTemporary(sb.toString()).insert(component);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static String getHTMLColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {}
        }
        return s;
    }
}
