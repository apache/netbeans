/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.subversion.ui.history;

import java.awt.Color;
import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.subversion.Subversion;
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
        
    private RepositoryRevision.Event    event;
    private RepositoryRevision          container;
    private String                      path;
    private String bgColor;
    private String fgColor;

    public RevisionNode(RepositoryRevision container, SearchHistoryPanel master) {
        super(new RevisionNodeChildren(container, master), Lookups.fixed(master, container));
        this.container = container;
        this.event = null;
        this.path = null;
        int changedPaths = container.getLog().getChangedPaths().length;

        String name = container.getLog().getRevision().getNumber() +
                (changedPaths > 0 ? NbBundle.getMessage(RevisionNode.class, "LBL_NumberOfChangedPaths", changedPaths) : "");
        setName(name);
        setShortDescription(name);
        initProperties();
    }

    public RevisionNode(RepositoryRevision.Event revision, SearchHistoryPanel master) {
        super(Children.LEAF, Lookups.fixed(master, revision));
        this.path = revision.getChangedPath().getPath();
        this.event = revision;
        setName(revision.getName());
        setShortDescription(path);
        initProperties();
    }

    RepositoryRevision.Event getRevision() {
        return event;
    }

    RepositoryRevision getContainer() {
        return container;
    }

    RepositoryRevision.Event getEvent() {
        return event;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) return null;
        if (event == null) {
            return container.getActions();
        } else {
            return event.getActions();
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
                Subversion.LOG.log(Level.INFO, null, e);
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
    
    private class UsernameProperty extends CommitNodeProperty<String> {

        public UsernameProperty() {
            super(COLUMN_NAME_USERNAME, String.class, COLUMN_NAME_USERNAME, COLUMN_NAME_USERNAME);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                for (AbstractSummaryView.SummaryViewMaster.SearchHighlight h : getLookup().lookup(SearchHistoryPanel.class).getSearchHighlights()) {
                    if (h.getKind() == AbstractSummaryView.SummaryViewMaster.SearchHighlight.Kind.AUTHOR) {
                        return highlight(container.getLog().getAuthor(), h.getSearchText(), bgColor, fgColor);
                    }
                }
                return container.getLog().getAuthor();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class PathProperty extends CommitNodeProperty {

        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, COLUMN_NAME_PATH, COLUMN_NAME_PATH);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                return ""; //NOI18N
            } else {
                return event.getChangedPath().getPath();
            }
        }
    }

    private class DateProperty extends CommitNodeProperty<Object> {

        public DateProperty() {
            super(COLUMN_NAME_DATE, Object.class, COLUMN_NAME_DATE, COLUMN_NAME_DATE);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                return container.getLog().getDate();
            } else {
                return ""; //NOI18N
            }
        }
    }

    private class MessageProperty extends CommitNodeProperty<String> {

        public MessageProperty() {
            super(COLUMN_NAME_MESSAGE, String.class, COLUMN_NAME_MESSAGE, COLUMN_NAME_MESSAGE);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                for (AbstractSummaryView.SummaryViewMaster.SearchHighlight h : getLookup().lookup(SearchHistoryPanel.class).getSearchHighlights()) {
                    if (h.getKind() == AbstractSummaryView.SummaryViewMaster.SearchHighlight.Kind.MESSAGE) {
                        return highlight(container.getLog().getMessage(), h.getSearchText(), bgColor, fgColor);
                    }
                }
                return container.getLog().getMessage();
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
            renderer.setText(val == null ? "" : val.toString());
            renderer.setBounds(box);
            renderer.paint(gfx);
        }

        @Override
        public boolean isPaintable() {
            return true;
        }
    }
}
