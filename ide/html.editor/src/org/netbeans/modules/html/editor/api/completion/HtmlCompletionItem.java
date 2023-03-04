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
package org.netbeans.modules.html.editor.api.completion;

import java.util.Arrays;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.completion.*;
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
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.HtmlPreferences;
import org.netbeans.modules.html.editor.javadoc.HelpManager;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import org.netbeans.modules.web.common.ui.api.FileReferenceCompletion;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;

/**
 * Code completion result item base class
 *
 * @author Dusan Balek, Marek Fukala
 */
public class HtmlCompletionItem implements CompletionItem {

    protected static final int DEFAULT_SORT_PRIORITY = 20;
    private static final String END_FONT = "</font>"; // NOI18N
    
    //----------- Factory methods --------------
    public static HtmlCompletionItem createTag(HtmlTag tag, String name, int substitutionOffset, String helpId, boolean possible) {
        return new Tag(tag, name, substitutionOffset, helpId, possible);
    }

    public static HtmlCompletionItem createEndTag(HtmlTag tag, String name, int substitutionOffset, String helpId, int order, EndTag.Type type) {
        return new EndTag(tag, name, substitutionOffset, helpId, order, type);
    }

    public static HtmlCompletionItem createEndTag(String name, int substitutionOffset, String helpId, int order, EndTag.Type type) {
        return new EndTag(name, substitutionOffset, helpId, order, type);
    }

    public static HtmlCompletionItem createAutocompleteEndTag(String name, int substitutionOffset) {
        return new AutocompleteEndTag(name, substitutionOffset);
    }

    public static HtmlCompletionItem createBooleanAttribute(String name, int substitutionOffset, boolean required, String helpId) {
        return new BooleanAttribute(name, substitutionOffset, required, helpId);
    }

    public static HtmlCompletionItem createAttribute(HtmlTagAttribute attribute, String name, int substitutionOffset, boolean required, String helpId) {
        return new Attribute(attribute, name, substitutionOffset, required, helpId);
    }

    public static HtmlCompletionItem createAttributeValue(String name, int substitutionOffset, boolean addQuotation) {
        return new AttributeValue(name, substitutionOffset, addQuotation);
    }

    public static HtmlCompletionItem createAttributeValue(String name, int substitutionOffset) {
        return createAttributeValue(name, substitutionOffset, false);
    }

    public static HtmlCompletionItem createCharacterReference(String name, char value, int substitutionOffset, String helpId) {
        return new CharRefItem(name, value, substitutionOffset, helpId);
    }

    /**
     * @param file
     * @param substitutionOffset
     * @return an instance of {@link HtmlCompletionItem}
     * @since 2.40
     */
    @NonNull
    public static HtmlCompletionItem createFileCompletionItem(FileObject file, int substitutionOffset) {
        boolean folder = file.isFolder();
        String name = new StringBuilder().append(file.getNameExt()).append(folder ? '/' : "").toString();
        // Should match color in o.n.m.web.common.ui.api.FileReferenceCompletion.getItems()
        Color color = folder ? new Color(224, 160, 65) : null;
        ImageIcon icon = FileReferenceCompletion.getIcon(file);
        
        return new FileAttributeValue(folder, name, substitutionOffset, color, icon);
    }

    public static HtmlCompletionItem createGoUpFileCompletionItem(int substitutionOffset, Color color, ImageIcon icon) {
        return new GoUpFileAttributeValue(substitutionOffset, color, icon);
    }
    //------------------------------------------
    protected int substitutionOffset;
    protected String text, helpId;
    protected boolean shift;
    protected HelpItem help;

    protected HtmlCompletionItem(HelpItem help, String text, int substitutionOffset, String helpId) {
        this(text, substitutionOffset, helpId);
        this.help = help;
    }

    protected HtmlCompletionItem(String text, int substituteOffset) {
        this.substitutionOffset = substituteOffset;
        this.text = text;
    }

    protected HtmlCompletionItem(String text, int substituteOffset, String helpId) {
        this(text, substituteOffset);
        this.helpId = helpId;
    }

    public String getItemText() {
        return text;
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

    @Override
    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
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

    /**
     * @since 2.30
     * @return
     */
    public int getAnchorOffset() {
        return substitutionOffset;
    }

    protected int getMoveBackLength() {
        return 0; //default
    }

    /**
     * Subclasses may override to customize the completed text if they do not
     * want to override the substituteText method.
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
            @Override
            public void run() {
                try {
                    //test whether we are trying to insert sg. what is already present in the text
                    String currentText = doc.getText(substitutionOffset, (doc.getLength() - substitutionOffset) < substituteText.length() ? (doc.getLength() - substitutionOffset) : substituteText.length());
                    if (!substituteText.equals(currentText)) {
                        //remove common part
                        doc.remove(substitutionOffset, len);
                        doc.insertString(substitutionOffset, substituteText, null);
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

    private void reindent(JTextComponent component) {

        final BaseDocument doc = (BaseDocument) component.getDocument();
        final int dotPos = component.getCaretPosition();
        final Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.runAtomic(new Runnable() {
                @Override
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

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        if (component != null) {
            try {
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

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return null;
    }

    protected String getLeftHtmlText() {
        return getItemText();
    }

    protected String getRightHtmlText() {
        return null;
    }

    public String getHelpId() {
        return this.helpId;
    }

    /**
     * Returns a url or null, if the help is not URL or the help is not defined.
     */
    public URL getHelpURL() {
        if (helpId == null || helpId.equals("")) {
            return null;
        }
        try {
            return new URL(helpId);
        } catch (java.io.IOException e) {
        }
        return null;
    }

    /**
     * Returns help for the item. It can be only url. If the item doesn't have a
     * help than returns null. The class can overwrite this method and compounds
     * the help realtime.
     */
    public String getHelp() {
        return HelpManager.getDefault().getHelp(helpId);
    }

    private boolean hasLegacyHelp() {
        return (helpId != null && helpId.length() > 0);
    }

    public boolean hasHelp() {
        return getHelpItem() != null || hasLegacyHelp();
    }

    /**
     * Override this method and do the help content initialization here.
     *
     * Used by HtmlCompletionItem$DocQuery - called when an instance of
     * CompletionDocumentation is created.
     *
     * @since 2.34
     */
    public void prepareHelp() {
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new HtmlCompletionProvider.DocQuery(this, false));
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    public HelpItem getHelpItem() {
        return help;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HtmlCompletionItem other = (HtmlCompletionItem) obj;
        if (this.substitutionOffset != other.substitutionOffset) {
            return false;
        }
        if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
            return false;
        }
        if ((this.helpId == null) ? (other.helpId != null) : !this.helpId.equals(other.helpId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.substitutionOffset;
        hash = 97 * hash + (this.text != null ? this.text.hashCode() : 0);
        hash = 97 * hash + (this.helpId != null ? this.helpId.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getItemText();
    }
    
    

    //------------------------------------------------------------------------------
    /**
     * Completion item representing a JSP tag including its prefix eg.
     * <jsp:useBean />
     */
    public static class Tag extends HtmlCompletionItem {

        private static final ImageIcon HTML_TAG_ICON
                = ImageUtilities.loadImageIcon("org/netbeans/modules/csl/source/resources/icons/html_element.png", false); // NOI18N
        private static final ImageIcon SVG_TAG_ICON
                = ImageUtilities.loadImageIcon("org/netbeans/modules/csl/source/resources/icons/class.png", false); // NOI18N
        private static final ImageIcon MATHML_TAG_ICON
                = ImageUtilities.loadImageIcon("org/netbeans/modules/html/editor/resources/mathml.png", false); // NOI18N

        private static final Color GRAY_COLOR = Color.GRAY;
        private static final Color DEFAULT_FG_COLOR = new Color(64, 64, 217);
        private boolean possible;
        private HtmlTag tag;

        protected Tag(HtmlTag tag, String name, int substitutionOffset, String helpId, boolean possible) {
            super(tag.getHelp(), name, substitutionOffset, helpId);
            this.tag = tag;
            this.possible = possible;
        }

        protected Tag(String text, int substitutionOffset, String helpId, boolean possible) {
            super(text, substitutionOffset, helpId);
            this.possible = possible;
        }

        //end tag autocomplete handling
        @Override
        public void defaultAction(JTextComponent component) {
            super.defaultAction(component);
        }

        @Override
        protected String getSubstituteText() {
            return new StringBuilder().append("<").append(getItemText()).toString();
        }

        @Override
        public int getSortPriority() {
            return super.getSortPriority() + (possible ? -10 : 0);
        }

        @Override
        protected String getLeftHtmlText() {
            Color tagColor = possible ? DEFAULT_FG_COLOR : GRAY_COLOR;
            boolean isPossibleHtmlTag = possible && tag != null && tag.getTagClass() == HtmlTagType.HTML;
            StringBuilder b = new StringBuilder();
            if (isPossibleHtmlTag) {
                b.append("<b>");
            }
            b.append(getHtmlColor(tagColor));
            b.append("&lt;"); // NO18N
            b.append(getItemText());
            b.append("&gt;"); // NOI18N
            b.append(END_FONT);
            if (isPossibleHtmlTag) {
                b.append("</b>");
            }
            return b.toString();
        }

        @Override
        protected String getRightHtmlText() {
            return null;
        }

        @Override
        protected ImageIcon getIcon() {
            if (tag != null) {
                switch (tag.getTagClass()) {
                    case HTML:
                        return HTML_TAG_ICON;
                    case SVG:
                        return SVG_TAG_ICON;
                    case MATHML:
                        return MATHML_TAG_ICON;
                    default:
                        return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public boolean hasHelp() {
            return tag != null && tag.getHelp() != null || super.hasHelp();
        }
    }

    /**
     * Completion item representing a JSP tag including its prefix eg.
     * <jsp:useBean />
     */
    public static class EndTag extends HtmlCompletionItem {

        public enum Type {

            DEFAULT(Color.BLUE, false, DEFAULT_SORT_PRIORITY), //NOI18N
            OPTIONAL_EXISTING(Color.GRAY, false, DEFAULT_SORT_PRIORITY),
            OPTIONAL_MISSING(Color.BLUE, false, DEFAULT_SORT_PRIORITY - 10), //NOI18N
            REQUIRED_EXISTING(Color.GRAY, false, DEFAULT_SORT_PRIORITY),
            REQUIRED_MISSING(Color.BLUE, false, DEFAULT_SORT_PRIORITY - 10); //NOI18N

            private Color color;
            private boolean bold;
            private int sortPriority;

            private Type(Color color, boolean bold, int sortPriority) {
                this.color = color;
                this.bold = bold;
                this.sortPriority = sortPriority;
            }
        }

        private int orderIndex;
        private Type type;
        private HtmlTag tag;

        EndTag(HtmlTag tag, String name, int substitutionOffset, String helpId, int order, Type type) {
            super(tag.getHelp(), name, substitutionOffset, helpId);
            this.orderIndex = order;
            this.type = type;
            this.tag = tag;
        }

        EndTag(String text, int substitutionOffset, String helpId, int order, Type type) {
            super(text, substitutionOffset, helpId);
            this.orderIndex = order;
            this.type = type;
        }

        @Override
        public CharSequence getSortText() {
            if (orderIndex == -1) {
                return super.getSortText();
            } else {
                char[] result = new char[Integer.toString(Integer.MAX_VALUE).length()];
                char[] orderIndexChars = Integer.toString(orderIndex).toCharArray();
                Arrays.fill(result, '0'); //NOI18N
                System.arraycopy(orderIndexChars, 0, result, result.length - orderIndexChars.length, orderIndexChars.length);

                return new String(result);
            }
        }

        @Override
        protected String getSubstituteText() {
            return new StringBuilder().append("</").append(getItemText()).append(">").toString(); //NOI18N
        }

        @Override
        public int getSortPriority() {
            return this.type.sortPriority;
        }

        @Override
        protected String getLeftHtmlText() {
            return (type.bold ? "<b>" : "") + //NOI18N
                    getHtmlColor(type.color) + "&lt;/" + getItemText() + "&gt;" + END_FONT + //NOI18N
                    (type.bold ? "</b>" : ""); //NOI18N
        }

        @Override
        public boolean hasHelp() {
            return tag != null && (tag.getHelp() != null || (tag.getTagClass() != HtmlTagType.UNKNOWN && super.hasHelp()));
        }
    }

    public static class AutocompleteEndTag extends EndTag {

        public AutocompleteEndTag(String text, int substitutionOffset) {
            super(text, substitutionOffset, null, -1, Type.DEFAULT);
        }

        @Override
        protected int getMoveBackLength() {
            return getSubstituteText().length(); //jump before the completed tag
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
    }

    /**
     * Completion item representing html entity reference.
     */
    public static class CharRefItem extends HtmlCompletionItem {

        private char value;
        private static final Color FG = new Color(0x99, 0, 0);

        CharRefItem(String name, char value, int substitutionOffset, String helpId) {
            super(name, substitutionOffset, helpId);
            this.value = value;
        }

        @Override
        protected String getSubstituteText() {
            return new StringBuilder().append("&").append(getItemText()).append(';').toString();
        }

        @Override
        protected String getLeftHtmlText() {
            return new StringBuilder()
                    .append("<b>&amp;")
                    .append(escape(getItemText()))
                    .append(";</b>").toString();
        }

        @Override
        protected String getRightHtmlText() {
            String strVal;
            if (value == '>') { //NOI18N
                strVal = "&gt;"; //NOI18N
            } else if (value == '<') { //NOI18N
                strVal = "&lt;"; //NOI18N
            } else {
                strVal = Character.toString(value);
            }
            return new StringBuilder()
                    .append("<b>") // NOI18N
                    .append(getHtmlColor(FG))
                    .append(strVal)
                    .append(END_FONT)
                    .append("</b>").toString(); // NOI18N
        }
    }

    /**
     * Item representing a JSP attribute value.
     */
    public static class AttributeValue extends HtmlCompletionItem {

        private boolean addQuotation;

        public AttributeValue(String value, int offset, boolean addQuotation) {
            super(value, offset);
            this.addQuotation = addQuotation;
        }

        @Override
        protected String getSubstituteText() {
            StringBuilder sb = new StringBuilder();
            if (addQuotation) {
                sb.append("\"");
            }
            sb.append(super.getSubstituteText());
            if (addQuotation) {
                sb.append("\"");
            }
            return sb.toString();
        }
    }

    public static class Attribute extends HtmlCompletionItem {

        private boolean required;
        private boolean autocompleteQuotes;
        private HtmlTagAttribute attr;

        public Attribute(HtmlTagAttribute attr, String value, int offset, boolean required, String helpId) {
            super(attr != null ? attr.getHelp() : null, value, offset, helpId);
            this.attr = attr;
            this.required = required;
            this.autocompleteQuotes = HtmlPreferences.autocompleteQuotesAfterEqualSign();
        }

        public Attribute(String value, int offset, boolean required, HelpItem helpItem) {
            super(helpItem, value, offset, null);
            this.required = required;
            this.autocompleteQuotes = HtmlPreferences.autocompleteQuotesAfterEqualSign();
        }

        public Attribute(String value, int offset, boolean required, String helpId) {
            super(value, offset, helpId);
            this.required = required;
            this.autocompleteQuotes = HtmlPreferences.autocompleteQuotesAfterEqualSign();
        }

        /**
         * @since 2.18
         *
         * @param value
         * @param offset
         * @param required
         * @param helpId
         * @param autoCompleteValue S
         */
        public Attribute(String value, int offset, boolean required, String helpId, boolean autoCompleteValue) {
            super(value, offset, helpId);
            this.required = required;
            this.autocompleteQuotes = autoCompleteValue && HtmlPreferences.autocompleteQuotesAfterEqualSign();
        }

        protected Color getAttributeColor() {
            return Color.green.darker();
        }

        @Override
        protected String getSubstituteText() {
            StringBuilder sb = new StringBuilder();
            sb.append(getItemText());
            if (autocompleteQuotes) {
                sb.append("=\"\""); //NOI18N
            }
            return sb.toString();
        }

        @Override
        protected int getMoveBackLength() {
            return autocompleteQuotes ? 1 : 0; //last quotation
        }

        @Override
        public int getSortPriority() {
            return super.getSortPriority() - (required ? 1 : 0);
        }

        @Override
        protected String getLeftHtmlText() {
            StringBuilder sb = new StringBuilder();
            if (required) {
                sb.append("<b>"); //NOI18N
            }
            sb.append(getHtmlColor(getAttributeColor()));
            sb.append(getItemText());
            sb.append(END_FONT);
            if (required) {
                sb.append("</b>"); //NOI18N
            }

            return sb.toString();
        }

        @Override
        public boolean hasHelp() {
            return attr != null && attr.getHelp() != null || super.hasHelp();
        }
    }

    public static class BooleanAttribute extends HtmlCompletionItem {

        private boolean required;
        private static final Color ATTR_NAME_COLOR = Color.green.darker();

        public BooleanAttribute(String value, int offset, boolean required, String helpId) {
            super(value, offset, helpId);
            this.required = required;
        }

        @Override
        protected String getLeftHtmlText() {
            StringBuilder sb = new StringBuilder();
            if (required) {
                sb.append("<b>"); //NOI18N
            }
            sb.append(getHtmlColor(ATTR_NAME_COLOR));
            sb.append(getItemText());
            sb.append(END_FONT);
            if (required) {
                sb.append("</b>"); //NOI18N
            }

            return sb.toString();
        }
    }

    /**
     * Item representing a File attribute
     */
    public static class FileAttributeValue extends HtmlCompletionItem implements PropertyChangeListener, LazyCompletionItem {

        private javax.swing.ImageIcon icon;
        private final Color color;
        private boolean visible;
        private final boolean folder;

        FileAttributeValue(boolean folder, String text, int substitutionOffset, Color color, javax.swing.ImageIcon icon) {
            super(text, substitutionOffset);
            this.folder = folder;
            this.color = color;
            this.icon = icon;
        }

        @Override
        protected ImageIcon getIcon() {
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (color == null) {
                return getItemText();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(getHtmlColor(color));
                sb.append(getItemText());
                sb.append(END_FONT);
                return sb.toString();
            }
        }

        @Override
        public CharSequence getSortText() {
            return folder ? new StringBuilder().append("_").append(getItemText()).toString() : getItemText();
        }
        
        private void iconLoaded(ImageIcon icon) {
            this.icon = icon;
            if (visible) {
                repaintCompletionView_EDT();
            }
        }

        private void repaintCompletionView_EDT() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    repaintCompletionView();
                }
            });
        }

        private static void repaintCompletionView() {
            try {
                Completion completion = Completion.get();
                Class<? extends Completion> clz = completion.getClass();
                Method method = clz.getDeclaredMethod("repaintCompletionView"); //NOI18N
                method.setAccessible(true);
                method.invoke(completion);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                //ignore
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("iconLoaded")) { //NOI18N
                iconLoaded((ImageIcon) evt.getNewValue());
            }
        }

        @Override
        public boolean accept() {
            visible = true;
            return true;
        }
    }

    public static class GoUpFileAttributeValue extends FileAttributeValue {

        GoUpFileAttributeValue(int substitutionOffset, Color color, javax.swing.ImageIcon icon) {
            super(true, "../", substitutionOffset, color, icon); //NOI18N
        }

        @Override
        public int getSortPriority() {
            return super.getSortPriority() - 1; //be first of the file compl. items
        }
    }

    public static String hexColorCode(Color c) {
        Color tweakedToLookAndFeel = LFCustoms.shiftColor(c);
        return Integer.toHexString(tweakedToLookAndFeel.getRGB()).substring(2);
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {
            }
        }
        return s;
    }
    
    private static String getHtmlColor(Color c) {
        return "<font color=#" + hexColorCode(c) + ">"; // NOI18N
    }
}
