/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.sql.editor.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.modules.db.sql.editor.api.completion.SubstitutionHandler;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public abstract class SQLCompletionItem implements CompletionItem {

    private static final String CATALOG_COLOR = getHtmlColor(81, 95, 197); // NOI18N
    private static final String SCHEMA_COLOR = getHtmlColor(0, 102, 102); // NOI18N
    private static final String TABLE_COLOR = getHtmlColor(204, 120, 0); // NOI18N
    private static final String VIEW_COLOR = getHtmlColor(187, 120, 0); // NOI18N
    private static final String COLUMN_COLOR = getHtmlColor(7, 7, 171); // NOI18N
    private static final String KEYWORD_COLOR = getHtmlColor(0, 81, 128); // NOI18N
    private static final String COLOR_END = "</font>"; // NOI18N

    private static final String BOLD = "<b>"; // NOI18N
    private static final String BOLD_END = "</b>"; // NOI18N

    private static final ImageIcon CATALOG_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/editor/completion/resources/catalog.png", false); // NOI18N
    private static final ImageIcon SCHEMA_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/editor/completion/resources/schema.png", false); // NOI18N
    private static final ImageIcon TABLE_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/editor/completion/resources/table.png", false); // NOI18N
    private static final ImageIcon VIEW_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/editor/completion/resources/view.gif", false); // NOI18N
    private static final ImageIcon ALIAS_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/editor/completion/resources/alias.png", false); // NOI18N
    private static final ImageIcon COLUMN_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/editor/completion/resources/column.png", false); // NOI18N
    private static final ImageIcon KEYWORD_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/editor/completion/resources/keyword.png", false); // NOI18N

    private final SubstitutionHandler substHandler;
    private final String substText;
    private final int substOffset;

    public static SQLCompletionItem catalog(String catalogName, String substText, int substOffset, SubstitutionHandler substHandler) {
        return new Catalog(catalogName, substText, substOffset, substHandler);
    }

    public static SQLCompletionItem schema(String schemaName, String substText, int substOffset, SubstitutionHandler substHandler) {
        return new Schema(schemaName, substText, substOffset, substHandler);
    }

    public static SQLCompletionItem table(String tableName, String substText, int substOffset, SubstitutionHandler substHandler) {
        return new Table(tableName, substText, substOffset, substHandler);
    }

    public static SQLCompletionItem view(String viewName, String substText, int substOffset, SubstitutionHandler substHandler) {
        return new View(viewName, substText, substOffset, substHandler);
    }

    public static SQLCompletionItem alias(String alias, QualIdent tableName, String substText, int substOffset, SubstitutionHandler substHandler) {
        return new Alias(alias, tableName, substText, substOffset, substHandler);
    }

    // view - bit ugly but can be easily refactored
    public static SQLCompletionItem column(boolean view,
            QualIdent tupleName,
            String columnName,
            String dataType,
            String substText,
            int substOffset,
            SubstitutionHandler substHandler) {
        return new Column(view, tupleName, columnName, dataType, substText, substOffset, substHandler);
    }

    public static SQLCompletionItem keyword(String keyword, int substOffset, SubstitutionHandler substHandler) {
        return new Keyword(keyword, substOffset, substHandler);
    }

    protected SQLCompletionItem(String substText, int substOffset, SubstitutionHandler substHandler) {
        this.substText = substText;
        this.substOffset = substOffset;
        this.substHandler = substHandler;
    }

    public void defaultAction(JTextComponent component) {
        Completion.get().hideDocumentation();
        Completion.get().hideCompletion();
        substHandler.substituteText(component, substOffset, substText);
    }

    public void processKeyEvent(KeyEvent evt) {
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        int width = CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
        width += 20; // give some more room for visible seperation
        return width;
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getImageIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    public int getSortPriority() {
        return 0;
    }

    public CharSequence getSortText() {
        return null;
    }

    public CharSequence getInsertPrefix() {
        return substText;
    }

    protected abstract ImageIcon getImageIcon();

    protected abstract String getLeftHtmlText();

    protected abstract String getRightHtmlText();

    private void substituteText(JTextComponent component, final int offset, final int len) {

    }

    private static final class Catalog extends SQLCompletionItem {

        private final String catalogName;
        private String leftText;

        public Catalog(String catalogName, String substText, int substOffset, SubstitutionHandler substHandler) {
            super(substText, substOffset, substHandler);
            this.catalogName = catalogName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return CATALOG_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(CATALOG_COLOR);
                sb.append(catalogName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Catalog {0}", catalogName); // NOI18N
        }
    }

    private static final class Schema extends SQLCompletionItem {

        private final String schemaName;
        private String leftText;

        public Schema(String schemaName, String substText, int substOffset, SubstitutionHandler substHandler) {
            super(substText, substOffset, substHandler);
            this.schemaName = schemaName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return SCHEMA_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(SCHEMA_COLOR);
                sb.append(schemaName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Schema {0}", schemaName); // NOI18N
        }
    }

    private static final class Table extends SQLCompletionItem {

        private final String tableName;
        private String leftText;

        public Table(String tableName, String substText, int substOffset, SubstitutionHandler substHandler) {
            super(substText, substOffset, substHandler);
            this.tableName = tableName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return TABLE_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(TABLE_COLOR);
                sb.append(tableName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            // XXX should have schema here.
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Table {0}", tableName); // NOI18N
        }
    }

    private static final class View extends SQLCompletionItem {

        private final String viewName;
        private String leftText;

        public View(String viewName, String substText, int substOffset, SubstitutionHandler substHandler) {
            super(substText, substOffset, substHandler);
            this.viewName = viewName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return VIEW_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(VIEW_COLOR);
                sb.append(viewName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            // XXX should have schema here.
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format("View {0}", viewName); // NOI18N
        }
    }

    private static final class Alias extends SQLCompletionItem {

        private final String alias;
        private final QualIdent tableName;
        private String rightText;

        public Alias(String alias, QualIdent tableName, String substText, int substOffset, SubstitutionHandler substHandler) {
            super(substText, substOffset, substHandler);
            this.alias = alias;
            this.tableName = tableName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return ALIAS_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            return alias;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(TABLE_COLOR);
                sb.append(tableName.toString());
                sb.append(COLOR_END);
                rightText = MessageFormat.format(NbBundle.getMessage(SQLCompletionItem.class, "MSG_Alias"), sb.toString());
            }
            return rightText;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Alias {0} to {1}", alias, tableName); // NOI18N
        }
    }

    private static class Column extends SQLCompletionItem {

        private final boolean view;
        private final QualIdent tableName;
        private final String columnName;
        private String leftText;
        private String rightText;
        private final String dataType;

        public Column(boolean view,
                QualIdent tableName,
                String columnName,
                String dataType,
                String substText,
                int substOffset,
                SubstitutionHandler substHandler) {
            super(substText, substOffset, substHandler);
            this.view = view;
            this.tableName = tableName;
            this.columnName = columnName;
            this.dataType = dataType;
        }

        protected String getColumnName() {
            return columnName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return COLUMN_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(COLUMN_COLOR);
                sb.append(BOLD); // NOI18N
                sb.append(columnName);
                sb.append(BOLD_END); // NOI18N
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(TABLE_COLOR);
                sb.append(tableName.toString());
                sb.append(COLOR_END);
                rightText = MessageFormat.format(
                        NbBundle.getMessage(SQLCompletionItem.class, view ? "MSG_View" : "MSG_Table"), sb.toString());
                sb.setLength(0);
                if (dataType != null && (! dataType.trim().isEmpty())) {
                    sb.append(" (");
                    sb.append(dataType.trim());
                    sb.append(")");
                    rightText += sb.toString();
            }
            }
            return rightText;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Column {0} in {1} {2}", columnName, view ? "view" : "table", tableName); // NOI18N
        }
    }
    
    private static String getHtmlColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }

    private static class Keyword extends SQLCompletionItem {

        private final String keyword;
        private String leftText;

        public Keyword(String keyword, int substOffset, SubstitutionHandler substHandler) {
            super(keyword, substOffset, substHandler);
            this.keyword = keyword;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return KEYWORD_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(KEYWORD_COLOR);
                sb.append(BOLD); // NOI18N
                sb.append(keyword);
                sb.append(BOLD_END); // NOI18N
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            return null;
        }

        @Override
        public String toString() {
            return "Keyword " + keyword; // NOI18N
        }
    }
}
