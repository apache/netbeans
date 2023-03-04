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
package org.netbeans.modules.git.ui.history;

import java.awt.Color;
import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import org.openide.ErrorManager;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.versioning.history.AbstractSummaryView;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class RevisionNode extends AbstractNode {
    
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

    RepositoryRevision getContainer() {
        return container;
    }

    RepositoryRevision.Event getEvent() {
        return event;
    }

    @Override
    public Action[] getActions (boolean context) {
        if (context) return null;
        if (event == null) {
            return container.getActions();
        } else {
            return event.getActions(true);
        }
    }
    
    private void initProperties() {
        AttributeSet searchHiliteAttrs = ((FontColorSettings) MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class)).getFontColors("highlight-search"); //NOI18N
        Color c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background);
        if (c != null) {
            bgColor = getColorString(c);
        }
        c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground);
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

    private static String getColorString(Color c) {
        return "#" + getHex(c.getRed()) + getHex(c.getGreen()) + getHex(c.getBlue()); //NOI18N
    }
    
    private static String getHex(int i) {
        String hex = Integer.toHexString(i & 0x000000FF);
        if (hex.length() == 1) {
            hex = "0" + hex; //NOI18N
        }
        return hex;
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
    
    private static String highlight (String author, String needle, String bgColor, String fgColor) {
        if (fgColor != null && bgColor != null) {
            int idx = author.toLowerCase().indexOf(needle);
            if (idx != -1) {
                return new StringBuilder("<html><body>").append(author.substring(0, idx)) //NOI18N
                        .append("<span style=\"background-color: ").append(bgColor).append("; color: ").append(fgColor).append(";\">") //NOI18N
                        .append(author.substring(idx, idx + needle.length())).append("</span>") //NOI18N
                        .append(author.substring(idx + needle.length())).append("</body></html>").toString(); //NOI18N
            }
        }
        return author;
    }
    
    private class UsernameProperty extends CommitNodeProperty {

        @SuppressWarnings("unchecked")
        public UsernameProperty() {
            super(COLUMN_NAME_USERNAME, String.class, COLUMN_NAME_USERNAME, COLUMN_NAME_USERNAME);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                for (AbstractSummaryView.SummaryViewMaster.SearchHighlight h : getLookup().lookup(SearchHistoryPanel.class).getSearchHighlights()) {
                    if (h.getKind() == AbstractSummaryView.SummaryViewMaster.SearchHighlight.Kind.AUTHOR) {
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
            if (event == null) {
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
            if (event == null) {
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
            if (event == null) {
                for (AbstractSummaryView.SummaryViewMaster.SearchHighlight h : getLookup().lookup(SearchHistoryPanel.class).getSearchHighlights()) {
                    if (h.getKind() == AbstractSummaryView.SummaryViewMaster.SearchHighlight.Kind.MESSAGE) {
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
}
