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
package org.netbeans.modules.css.editor.module.spi;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssCompletion;
import org.netbeans.modules.css.editor.csl.CssElement;
import org.netbeans.modules.css.editor.csl.CssValueElement;
import org.netbeans.modules.css.lib.api.CssColor;
import org.netbeans.modules.css.lib.api.properties.GrammarElement;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.UnitGrammarElement;
import org.netbeans.modules.css.lib.api.properties.ValueGrammarElement;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.ui.api.WebUIUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents a CSS completion proposal. Various predefined item types can be
 * used or the client of the SPI may override some of the defaults.
 *
 * For more info see the CompletionProposal from csl.api
 *
 * @todo support for more completion type providers - like colors => subclass
 * this class, remove the kind field, it's just temp. hack
 *
 */
public abstract class CssCompletionItem extends DefaultCompletionProposal {

    private int anchorOffset;
    private String value;
    private ElementHandle element;
    protected boolean addSemicolon;

    /**
     * @since 1.40
     */
    public static CssCompletionItem createValueCompletionItem(CssValueElement element,
            String value,
            String origin,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ValueCompletionItem(element, value, origin, anchorOffset, addSemicolon, addSpaceBeforeItem);
    }

    public static CssCompletionItem createValueCompletionItem(CssValueElement element,
            ValueGrammarElement value,
            String origin,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ValueCompletionItem(element, value.getValue(), origin, anchorOffset, addSemicolon, addSpaceBeforeItem);
    }

    public static CssCompletionItem createValueCompletionItem(CssValueElement element,
            GrammarElement value,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ValueCompletionItem(element, value.toString(), value.getVisibleOrigin(), anchorOffset, addSemicolon, addSpaceBeforeItem);
    }

    public static CssCompletionItem createColorValueCompletionItem(CssValueElement element,
            GrammarElement value,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ColorCompletionItem(element, value.toString(), value.getVisibleOrigin(), anchorOffset, addSemicolon, addSpaceBeforeItem);

    }

    public static CssCompletionItem createPropertyCompletionItem(CssElement element,
            PropertyDefinition property,
            String propertyInsertPrefix,
            int anchorOffset,
            boolean addSemicolon) {

        return new PropertyCompletionItem(element, property, propertyInsertPrefix, anchorOffset, addSemicolon);
    }

    public static CssCompletionItem createRAWCompletionItem(CssElement element,
            String value,
            ElementKind kind,
            int anchorOffset,
            boolean addSemicolon) {

        return new RAWCompletionItem(element, kind, value, anchorOffset, addSemicolon);
    }

    public static CssCompletionItem createHashColorCompletionItem(CssElement element,
            String value,
            String origin,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem,
            boolean usedInCurrentFile) {

        return new HashColorCompletionItem(element, value, origin, anchorOffset, addSemicolon, addSpaceBeforeItem, usedInCurrentFile);
    }

    public static CompletionProposal createColorChooserCompletionItem(int anchor, String origin, boolean addSemicolon) {
        return new ColorChooserItem(anchor, origin, addSemicolon);
    }

    public static CssCompletionItem createSelectorCompletionItem(CssElement element,
            String value,
            int anchorOffset,
            boolean related) {
        return createSelectorCompletionItem(element, value, anchorOffset, related, false);
    }

    public static CssCompletionItem createSelectorCompletionItem(CssElement element,
            String value,
            int anchorOffset,
            boolean related,
            boolean escape) {

        return new SelectorCompletionItem(element, value, anchorOffset, related, escape);
    }

    public static CssCompletionItem createFileCompletionItem(CssElement element,
            String value,
            int anchorOffset,
            Color color,
            ImageIcon icon,
            boolean addQuotes,
            boolean addSemicolon) {

        return new FileCompletionItem(element, value, anchorOffset, color, icon, addQuotes, addSemicolon);
    }

    public static CompletionProposal createUnitCompletionItem(UnitGrammarElement element) {
        return new UnitItem(element);
    }

    protected static final int SORT_PRIORITY = 300;

    private CssCompletionItem() {
    }

    protected CssCompletionItem(ElementHandle element, String value, int anchorOffset, boolean addSemicolon) {
        this.anchorOffset = anchorOffset;
        this.value = value;
        this.element = element;
        this.addSemicolon = addSemicolon;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public String getName() {
        return value;
    }

    @Override
    public String getInsertPrefix() {
        return getName(); //XXX should be return value properly...
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public int getSortPrioOverride() {
        return SORT_PRIORITY;
    }

    static class RAWCompletionItem extends CssCompletionItem {

        private ElementKind kind;

        public RAWCompletionItem(CssElement element, ElementKind kind, String value, int anchorOffset, boolean addSemicolon) {
            super(element, value, anchorOffset, addSemicolon);
            this.kind = kind;
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }
    }

    static class ValueCompletionItem extends CssCompletionItem {

        private String origin; //property name to which this value belongs
        private boolean addSpaceBeforeItem;

        private ValueCompletionItem(CssElement element,
                String value,
                String origin,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem) {

            super(element, value, anchorOffset, addSemicolon);
            this.origin = origin;
            this.addSpaceBeforeItem = addSpaceBeforeItem;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.GLOBAL;
        }

        @Override
        public String getInsertPrefix() {
            return (addSpaceBeforeItem && textsStartsWith(getName()) ? " " : "") + getName() + (addSemicolon ? ";" : ""); //NOI18N
        }

        private boolean textsStartsWith(String text) {
            char ch = text.charAt(0);
            return Character.isLetterOrDigit(ch);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + (origin == null ? "" : origin) + "</font>"; //NOI18N
        }
    }

    //XXX fix the CssCompletionItem class so the Value and Property normally subclass it!!!!!!!!!
    static class ColorCompletionItem extends ValueCompletionItem {

        private ColorCompletionItem(CssElement element,
                String value,
                String origin,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem) {

            super(element, value, origin, anchorOffset, addSemicolon, addSpaceBeforeItem);
        }

        @Override
        public ImageIcon getIcon() {
            CssColor color = CssColor.getColor(getName());
            return WebUIUtils.createColorIcon(color == null ? null : color.colorCode());
        }
    }

    static class HashColorCompletionItem extends ColorCompletionItem {

        private static final int HASH_COLOR_SORT_PRIORITY = SORT_PRIORITY - 10;
        private boolean usedInCurrentFile;

        private HashColorCompletionItem(CssElement element,
                String value,
                String origin,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem,
                boolean usedInCurrentFile) {

            super(element, value, origin, anchorOffset, addSemicolon, addSpaceBeforeItem);
            this.usedInCurrentFile = usedInCurrentFile;
        }

        @Override
        public ImageIcon getIcon() {
            return WebUIUtils.createColorIcon(getName().substring(1)); //strip off the hash
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return new StringBuilder().append(usedInCurrentFile ? "" : "<font color=999999>").
                    append(getName()).append(usedInCurrentFile ? "" : "</font>").toString(); //NOI18N
        }

        @Override
        public int getSortPrioOverride() {
            return HASH_COLOR_SORT_PRIORITY - (usedInCurrentFile ? 1 : 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HashColorCompletionItem other = (HashColorCompletionItem) obj;

            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            return hash;
        }
    }

    static class ColorChooserItem extends DefaultCompletionProposal {

        private static JColorChooser COLOR_CHOOSER;
        private Color color;
        private boolean addSemicolon;
        private String origin;

        private ColorChooserItem(int anchor, String origin, boolean addSemicolon) {
            this.anchorOffset = anchor;
            this.addSemicolon = addSemicolon;
            this.origin = origin;
        }

        private static synchronized JColorChooser getColorChooser() {
            if (COLOR_CHOOSER == null) {
                COLOR_CHOOSER = new JColorChooser();
            }
            return COLOR_CHOOSER;
        }

        @Override
        public boolean beforeDefaultAction() {
            final JColorChooser colorChooser = getColorChooser();
            JDialog dialog = JColorChooser.createDialog(EditorRegistry.lastFocusedComponent(),
                    NbBundle.getMessage(CssCompletion.class, "MSG_Choose_Color"), //NOI18N
                    true, colorChooser, new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            color = colorChooser.getColor();
                        }
                    }, new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            color = null;
                        }
                    });
            dialog.setVisible(true);
            dialog.dispose();

            return color == null;
        }

        @Override
        public int getAnchorOffset() {
            return anchorOffset;
        }

        @Override
        public ElementHandle getElement() {
            return new CssElement(null);
        }

        @Override
        public ElementKind getKind() {
            return getElement().getKind();
        }

        @Override
        public ImageIcon getIcon() {
            Color c = getColorChooser().getColor();
            String colorCode = c == null ? "ffffff" : WebUIUtils.toHexCode(c).substring(1); //strip off the hash
            return WebUIUtils.createColorIcon(colorCode);
        }

        @Override
        public String getName() {
            return color == null ? "$color_chooser" : (WebUIUtils.toHexCode(color) + (addSemicolon ? ";" : "")); //NOI18N
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return "<b>" + NbBundle.getMessage(CssCompletion.class, "MSG_OpenColorChooser") + "</b>"; //NOI18N
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + origin + "</font>"; //NOI18N
        }

        @Override
        public boolean isSmart() {
            return true;
        }
    }

    static class UnitItem extends DefaultCompletionProposal {

        private UnitGrammarElement element;

        private UnitItem(UnitGrammarElement element) {
            this.element = element;
        }

        @Override
        public boolean beforeDefaultAction() {
            return true; //do not do anything
        }

        @Override
        public ElementHandle getElement() {
            return new CssElement(null);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getName() {
            return element.getValue().toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return "<font color=#aaaaaa>" + element.getTokenAcceptorId() + "</font>"; //NOI18N
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + element.getVisibleOrigin() + "</font>"; //NOI18N
        }

        @Override
        public boolean isSmart() {
            return false;
        }
    }

    static class PropertyCompletionItem extends CssCompletionItem {

        private String propertyInsertPrefix;
        private PropertyDefinition property;
        private boolean vendorProperty;

        private PropertyCompletionItem(CssElement element,
                PropertyDefinition property,
                String propertyInsertPrefix,
                int anchorOffset,
                boolean addSemicolon) {

            super(element, property.getName(), anchorOffset, addSemicolon);
            this.property = property;
            this.propertyInsertPrefix = propertyInsertPrefix;
            this.vendorProperty = Css3Utils.isVendorSpecificProperty(property.getName());
        }

        @Override
        public int getSortPrioOverride() {
            //list the vendor specific properties after the standard properties
            return vendorProperty ? super.getSortPrioOverride() + 50 : super.getSortPrioOverride();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (vendorProperty) {
                formatter.appendHtml("<i>"); //NOI18N
                formatter.appendText(getName());
                formatter.appendHtml("</i>"); //NOI18N

                return formatter.getText();
            } else {
                return super.getLhsHtml(formatter);
            }
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            formatter.appendHtml("<font color=999999>"); //NOI18N
            formatter.appendText(property.getPropertyCategory().getDisplayName());
            formatter.appendHtml("</font>");

            return formatter.getText();

        }

        @Override
        public String getInsertPrefix() {
            return propertyInsertPrefix + ": "; //NOI18N
        }
    }

    static class SelectorCompletionItem extends CssCompletionItem {

        private static final String RELATED_SELECTOR_COLOR = "007c00"; //NOI18N
        private static final String GRAY_COLOR_CODE = Integer.toHexString(Color.GRAY.getRGB()).substring(2);
        private final boolean related;
        private final String displayName;

        private SelectorCompletionItem(CssElement element,
                String value,
                int anchorOffset,
                boolean related,
                boolean escape) {
            super(element, escape ? escape(value) : value, anchorOffset, false);
            this.displayName = value;
            this.related = related;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            StringBuilder buf = new StringBuilder();
            if (related) {
                buf.append("<b><font color=#"); //NOI18N
                buf.append(RELATED_SELECTOR_COLOR);
            } else {
                buf.append("<font color=#"); //NOI18N
                buf.append(GRAY_COLOR_CODE);
            }
            buf.append(">");
            buf.append(displayName);
            buf.append("</font>"); //NOI18N
            if (related) {
                buf.append("</b>"); //NOI18N
            }

            formatter.appendHtml(buf.toString());
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public int getSortPrioOverride() {
            return super.getSortPrioOverride() - (related ? 1 : 0);
        }

        /**
         * Escape the input for usage in CSS files.
         *
         * @param input
         * @return
         */
        static String escape(String input) {
            if(input == null) {
                return null;
            }
            StringBuilder result = new StringBuilder(input.length());
            boolean limitedSet = true;
            for(int offset = 0; offset < input.length(); offset += Character.charCount(input.codePointAt(offset))) {
                int entry = input.codePointAt(offset);
                boolean mustBeEscaped = false;
                if (limitedSet) {
                    if (!((entry == '_') || (entry >= 'a' && entry <= 'z') || (entry >= 'A' && entry <= 'Z') || entry == '-')) {
                        mustBeEscaped = true;
                    }
                    limitedSet = offset == 0 || entry == '-';
                } else {
                    if (!((entry == '_') || (entry >= 'a' && entry <= 'z') || (entry >= 'A' && entry <= 'Z') || (entry >= '0' && entry <= '9') || entry == '-')) {
                        mustBeEscaped = true;
                    }
                }
                if (mustBeEscaped) {
                    result.append("\\");
                    if (entry > 127 || Character.isWhitespace(entry) || Character.isISOControl(entry)) {
                        result.append(String.format("%06x", (int) entry));
                    } else {
                        result.appendCodePoint(entry);
                    }
                } else {
                    result.appendCodePoint(entry);
                }
            }
            return result.toString();
        }
    }

    private static class FileCompletionItem extends CssCompletionItem {

        private final ImageIcon icon;
        private final String colorCode;
        private final boolean addQuotes;

        private FileCompletionItem(CssElement element,
                String value,
                int anchorOffset,
                Color color,
                ImageIcon icon,
                boolean addQuotes,
                boolean addSemicolon) {
            super(element, value, anchorOffset, false);
            this.icon = icon;
            this.colorCode = color == null ? null : WebUIUtils.toHexCode(color).substring(1);
            this.addQuotes = addQuotes;
            this.addSemicolon = addSemicolon;
        }

        @Override
        public boolean beforeDefaultAction() {
            //XXX pretty hacky!
            //XXX there should be a corresponding method like afterDefaultAction
            //triggered after the atomic change so we don't have to use the dirty tricks
            //with tasks put of by some magic delay!
            
            //reopen/refresh the completion for folders
            FileObject file = getElement().getFileObject();
            if (file != null && file.isFolder()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse("text/css", new UserTask() { //NOI18N
                                @Override
                                public void run(ResultIterator ri) {
                                    Completion.get().showCompletion();
                                }
                            });
                        } catch (ParseException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }, 200);

            }
            return false;
        }

        @Override
        public ImageIcon getIcon() {
            return icon;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FILE;
        }

        @Override
        public String getInsertPrefix() {
            StringBuilder b = new StringBuilder();
            if (addQuotes) {
                b.append('"'); //NOI18N
            }
            b.append(getName());
            FileObject file = getElement().getFileObject();
            if (file != null && file.isFolder()) {
                b.append(Css3Utils.FILE_SEPARATOR); //NOI18N
            }
            if (addQuotes) {
                b.append('"'); //NOI18N
            }
            if (addSemicolon) {
                b.append(';'); //NOI18N
            }
            return b.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (colorCode != null) {
                formatter.appendHtml(String.format("<font color=\"%s\">", colorCode)); //NOI18N
            }
            formatter.appendText(getName());
            if (colorCode != null) {
                formatter.appendHtml("</font>"); //NOI18N
            }
            return formatter.getText();
        }
    }
}
