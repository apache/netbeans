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
package org.netbeans.modules.git.ui.history;

import java.awt.Color;
import java.awt.Component;
import org.openide.util.lookup.Lookups;
import org.openide.ErrorManager;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.CharConversionException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.git.options.AnnotationColorProvider;
import org.netbeans.modules.versioning.history.AbstractSummaryView.SummaryViewMaster.SearchHighlight;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;

import static org.netbeans.modules.git.utils.GitUtils.getColorString;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class RevisionNode extends AbstractNode {

    private static final Image COMMIT_ICON = ImageUtilities.loadImage("/org/netbeans/modules/git/resources/icons/commit.png", false);
    private static final Image NO_ICON = ImageUtilities.icon2Image(new NoIcon());

    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_DATE        = "date"; // NOI18N
    static final String COLUMN_NAME_USERNAME    = "username"; // NOI18N
    static final String COLUMN_NAME_MESSAGE     = "message"; // NOI18N
    static final String COLUMN_NAME_PATH        = "path"; // NOI18N
        
    private final RepositoryRevision.Event event;
    private RepositoryRevision          container;
    private final String path;
    private String bgColor;
    private String fgColor;
    
    public RevisionNode(RepositoryRevision container, SearchHistoryPanel master) {
        super(new RevisionNodeChildren(container, master), Lookups.fixed(master, container));
        this.container = container;
        this.event = null;
        this.path = null;
        String rev = container.getLog().getRevision();
        String name = rev.length() > 7 ? rev.substring(0, 7) : rev;
        setName(name);
        setShortDescription(rev);
        initProperties();
    }

    public RevisionNode(RepositoryRevision.Event revision, SearchHistoryPanel master) {
        super(Children.LEAF, Lookups.fixed(master, revision));
        this.path = revision.getPath();
        this.event = revision;
        setName(revision.getName());
        setShortDescription(path);
        initProperties();
    }

    RepositoryRevision.Event getEvent() {
        return event;
    }

    @Override
    public String getHtmlDisplayName() {
        // Note: quicksearch highlighting impl for the filename colum is missing.
        // NB uses a custom html renderer for the tree's primary column which
        // doesn't support background-color.
        String name = escape(getName());
        if (isCommitNode()) {
            return "<b>"+name+"</b>";
        } else {
            String c = annotationColorForAction(event.getAction());
            return c != null ? "<font color="+c+">"+name+"</font>" : name;
        }
    }

    private static String annotationColorForAction(char action) {
        AnnotationColorProvider acp = AnnotationColorProvider.getInstance();
        switch (action) {
            case 'A': return getColorString(acp.ADDED_FILE.getActualColor());
            case 'C': return getColorString(acp.ADDED_FILE.getActualColor());
            case 'M': return getColorString(acp.MODIFIED_FILE.getActualColor());
            case 'R': return null; // no color for renamed files?
            case 'D': return getColorString(acp.REMOVED_FILE.getActualColor());
            default : return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        return isCommitNode() ? COMMIT_ICON : NO_ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions (boolean context) {
        if (context) return null;
        if (isCommitNode()) {
            return container.getActions();
        } else {
            return event.getActions(true);
        }
    }
    
    private void initProperties() {
        AttributeSet searchHighlightAttrs = ((FontColorSettings) MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class)).getFontColors("highlight-search"); //NOI18N
        Color c = (Color) searchHighlightAttrs.getAttribute(StyleConstants.Background);
        if (c != null) {
            bgColor = getColorString(c);
        }
        c = (Color) searchHighlightAttrs.getAttribute(StyleConstants.Foreground);
        if (c != null) {
            fgColor = getColorString(c);
        }

        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new PathProperty());
        ps.put(new DateProperty());
        ps.put(new UsernameProperty());
        ps.put(new MessageProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    public boolean isCommitNode() {
        return event == null;
    }

    private abstract class CommitNodeProperty<T> extends PropertySupport.ReadOnly<T> {

        protected CommitNodeProperty(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            try {
                return new RevisionPropertyEditor(getValue());
            } catch (Exception e) {
                return super.getPropertyEditor();
            }
        }
    }
    
    private static String escape(String text) {
        try {
            return XMLUtil.toElementContent(text);
        } catch (CharConversionException ex) {
            Logger.getLogger(RevisionNode.class.getName()).log(Level.INFO, "Can not HTML escape: " + text);  //NOI18N
            return "";  //NOI18N
        }
    }

    private static String highlight(String text, String needle, String bgColor, String fgColor) {
        if (fgColor != null && bgColor != null) {
            int idx = text.toLowerCase(Locale.ROOT).indexOf(needle);
            if (idx != -1) {
                return new StringBuilder(256)
                    .append("<html><body><nobr>").append(escape(text.substring(0, idx))) //NOI18N
                    .append("<span style=\"background-color: ").append(bgColor).append("; color: ").append(fgColor).append(";\">") //NOI18N
                    .append(escape(text.substring(idx, idx + needle.length()))).append("</span>") //NOI18N
                    .append(escape(text.substring(idx + needle.length()))).append("</nobr></body></html>").toString(); //NOI18N
            }
        }
        return text;
    }
    
    private class UsernameProperty extends CommitNodeProperty {

        @SuppressWarnings("unchecked")
        public UsernameProperty() {
            super(COLUMN_NAME_USERNAME, String.class, COLUMN_NAME_USERNAME, COLUMN_NAME_USERNAME);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (isCommitNode()) {
                for (SearchHighlight h : getLookup().lookup(SearchHistoryPanel.class).getSearchHighlights()) {
                    if (h.getKind() == SearchHighlight.Kind.AUTHOR) {
                        return highlight(container.getLog().getAuthor().toString(), h.getSearchText(), bgColor, fgColor);
                    }
                }
                return container.getLog().getAuthor();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class PathProperty extends CommitNodeProperty {

        @SuppressWarnings("unchecked")
        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, COLUMN_NAME_PATH, COLUMN_NAME_PATH);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (isCommitNode()) {
                return "";
            } else {
                return event.getPath();
            }
        }
    }

    private class DateProperty extends CommitNodeProperty {

        @SuppressWarnings("unchecked")
        public DateProperty() {
            super(COLUMN_NAME_DATE, String.class, COLUMN_NAME_DATE, COLUMN_NAME_DATE);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (isCommitNode()) {
                return new Date(container.getLog().getCommitTime());
            } else {
                return ""; // NOI18N
            }
        }

        @Override
        public Class getValueType() {
            return Date.class;
        }
    }

    private class MessageProperty extends CommitNodeProperty {
        
        @SuppressWarnings("unchecked")
        public MessageProperty() {
            super(COLUMN_NAME_MESSAGE, String.class, COLUMN_NAME_MESSAGE, COLUMN_NAME_MESSAGE);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (isCommitNode()) {
                for (SearchHighlight h : getLookup().lookup(SearchHistoryPanel.class).getSearchHighlights()) {
                    if (h.getKind() == SearchHighlight.Kind.MESSAGE) {
                        return highlight(container.getLog().getFullMessage(), h.getSearchText(), bgColor, fgColor);
                    }
                }
                return container.getLog().getFullMessage();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private static class RevisionPropertyEditor extends PropertyEditorSupport {

        private static final JLabel renderer = new JLabel();

        static {
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        public RevisionPropertyEditor(Object value) {
            setValue(value);
        }

        @Override
        public void paintValue(Graphics gfx, Rectangle box) {
            renderer.setForeground(gfx.getColor());
            Object val = getValue();
            if (val instanceof Date) {
                val = DateFormat.getDateTimeInstance().format((Date) val);
            }
            renderer.setText(val.toString());
            renderer.setBounds(box);
            renderer.paint(gfx);
        }

        @Override
        public boolean isPaintable() {
            return true;
        }
    }
    
    private static final class NoIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override public int getIconWidth() { return 0; }
        @Override public int getIconHeight() { return 0; }
    }
}
