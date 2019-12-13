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

package org.netbeans.modules.bugtracking.issuetable;

import java.util.regex.Matcher;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import static org.netbeans.modules.bugtracking.spi.IssueStatusProvider.Status.INCOMING_MODIFIED;
import static org.netbeans.modules.bugtracking.spi.IssueStatusProvider.Status.INCOMING_NEW;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QueryTableCellRenderer extends DefaultTableCellRenderer {
    public static final String PROPERTY_FORMAT = "format";                      // NOI18N
    public static final String PROPERTY_HIGHLIGHT_PATTERN = "highlightPattern"; // NOI18N

    private final IssueTable issueTable;

    private static final int VISIBLE_START_CHARS = 0;
    private static final Icon seenValueIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/commons/resources/seen-value.png")); // NOI18N

    private static final MessageFormat issueNewFormat       = getFormat("issueNewFormat", UIUtils.getTaskNewColor()); //NOI18N
    private static final MessageFormat issueObsoleteFormat  = getFormat("issueObsoleteFormat", UIUtils.getTaskObsoleteColor()); //NOI18N
    private static final MessageFormat issueModifiedFormat  = getFormat("issueModifiedFormat", UIUtils.getTaskModifiedColor()); //NOI18N

    private static final String labelNew = NbBundle.getMessage(QueryTableCellRenderer.class, "LBL_IssueStatusNew");             // NOI18N
    private static final String labelModified = NbBundle.getMessage(QueryTableCellRenderer.class, "LBL_IssueStatusModified");   // NOI18N
    private static final String labelObsolete = NbBundle.getMessage(QueryTableCellRenderer.class, "LBL_IssueStatusObsolete");   // NOI18N

    private static final String msgNew = NbBundle.getMessage(QueryTableCellRenderer.class, "MSG_IssueStatusNew");             // NOI18N
    private static final String msgModified = NbBundle.getMessage(QueryTableCellRenderer.class, "MSG_IssueStatusModified");   // NOI18N
    private static final String msgObsolete = NbBundle.getMessage(QueryTableCellRenderer.class, "MSG_IssueStatusObsolete");   // NOI18N

    private static Color evenLineColor                      = null;
    private static Color unevenLineColor                    = null;
    private static final Color newHighlightColor            = UIUtils.getTaskNewColor();
    private static final Color modifiedHighlightColor       = UIUtils.getTaskModifiedColor();
    private static final Color obsoleteHighlightColor       = UIUtils.getTaskObsoleteColor();

    static {
        evenLineColor = UIManager.getColor( "nb.bugtracking.table.background" ); //NOI18N
        unevenLineColor = UIManager.getColor( "nb.bugtracking.table.background.alternate" ); //NOI18N
        if (evenLineColor == null || unevenLineColor == null) {
            Color textColor = UIManager.getColor("Table.foreground"); // NOI18N
            boolean textOnBright = textColor == null || (textColor.getRed() < 192 && textColor.getGreen() < 192 && textColor.getBlue() < 192);
            if (evenLineColor == null) {
                evenLineColor = UIManager.getColor("Table.background"); // NOI18N
                if (evenLineColor == null) {
                    evenLineColor = textOnBright ? Color.white : Color.black;
                }
            }
            if (unevenLineColor == null) {
                unevenLineColor = textOnBright ? new Color(0xf3f6fd) : Color.darkGray;
            }
        }
    }

    private boolean isSaved;

    public QueryTableCellRenderer(IssueTable issueTable, boolean isSaved) {
        this.issueTable = issueTable;
        this.isSaved = isSaved;
    }

    void setSaved(boolean saved) {
        isSaved = saved;
    }
    
    private static MessageFormat getFormat (String key, Color c) {
        String format = NbBundle.getMessage(IssueTable.class, key,
                new Object[] { UIUtils.getColorString(c), "{0}" }); //NOI18N
        return new MessageFormat(format);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        renderer.setIcon(null);
        if(!isSaved) {
            TableCellStyle style = getDefaultCellStyle(table, issueTable, (IssueProperty) value, isSelected, row);
            setStyleProperties(renderer, style);
            return renderer;
        }
        
        TableCellStyle style = null;
        if(value instanceof IssueNode.SeenProperty) {
            IssueNode<?>.SeenProperty ps = (IssueNode<?>.SeenProperty) value;
            renderer.setIcon(!ps.getValue() ? seenValueIcon : null);
            renderer.setText("");                                               // NOI18N
        } 

        if(value instanceof IssueNode.IssueProperty) {
            style = getCellStyle(table, issueTable, (IssueProperty)value, isSelected, row);
        }
        setStyleProperties(renderer, style);
        return renderer;
    }

    public void setStyleProperties(JLabel renderer, TableCellStyle style) {
        if (style != null) {
            renderer.putClientProperty(PROPERTY_FORMAT, style.format); // NOI18N
            renderer.putClientProperty(PROPERTY_HIGHLIGHT_PATTERN, style.highlightPattern); // NOI18N
            ((JComponent) renderer).setToolTipText(style.tooltip);
            setRowColors(style, renderer);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {        
        processText(this);
        super.paintComponent(g);
    }

    public static void processText(JLabel label) {
        MessageFormat format = (MessageFormat) label.getClientProperty(PROPERTY_FORMAT);     // NOI18N
        Pattern pattern = (Pattern) label.getClientProperty(PROPERTY_HIGHLIGHT_PATTERN);     // NOI18N
        String s = computeFitText(label);
        if(format != null || pattern != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html>");                                                // NOI18N
            s = TextUtils.escapeForHTMLLabel(s);
            if(format != null) {
                format.format(new Object[] {s}, sb, null);
            }
            if(pattern != null) {
                sb.append(highLight(s, pattern));
            }
            sb.append("</html>");                                               // NOI18N
            s = sb.toString();
        } 
        label.setText(s);
    }

    private static String computeFitText(JLabel label) {
        String text = label.getText();
        if(text == null) text = "";
        if (text.length() <= VISIBLE_START_CHARS + 3) return text;
        
        Icon icon = label.getIcon();
        int iconWidth = icon != null ? icon.getIconWidth() : 0;
        
        FontMetrics fm = label.getFontMetrics(label.getFont());
        int width = label.getSize().width - iconWidth;

        String sufix = "...";                                                   // NOI18N
        int sufixLength = fm.stringWidth(sufix);
        int desired = width - sufixLength;
        if (desired <= 0) return text;

        for (int i = 0; i <= text.length() - 1; i++) {
            String prefix = text.substring(0, i);
            int swidth = fm.stringWidth(prefix);
            if (swidth >= desired) {
                return prefix.length() > 0 ? prefix + sufix: text;
            }
        }
        return text;
    }

    private static String highLight(String s, Pattern pattern) {
        Matcher matcher = pattern.matcher(s);
        int idx = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find(idx)) {
            int start = matcher.start();
            int end = matcher.end();
            if (start == end) {
                break;
            }
            sb.append(s.substring(idx, start));
            sb.append("<font bgcolor=\"FFB442\" color=\"black\">");
            sb.append(s.substring(start, end));
            sb.append("</font>");
            idx = matcher.end();
        }
        if(sb.length() > 0) {
            sb.append(idx < s.length() ? s.substring(idx, s.length()) : "");
            s = sb.toString();
        }
        return s;
    }

    public static class TableCellStyle {
        private MessageFormat format;
        private Color background;
        private Color foreground;
        private String tooltip;
        private Pattern highlightPattern;

        private TableCellStyle(MessageFormat format, Color background, Color foreground, String tooltip, Pattern highlightPattern) {
            this.background = background;
            this.foreground = foreground;
            this.tooltip = tooltip;
            this.format = format;
            this.highlightPattern = highlightPattern;
        }
        public Color getBackground() {
            return background;
        }
        public Color getForeground() {
            return foreground;
        }
        public MessageFormat getFormat() {
            return format;
        }
        public Pattern getHighlightPattern() {
            return highlightPattern;
        }
        public String getTooltip() {
            return tooltip;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");                                                     // NOI18N
            sb.append("background=");                                           // NOI18N
            sb.append(background);
            sb.append(", foreground=");                                         // NOI18N
            sb.append(foreground);
            sb.append(", format=");                                             // NOI18N
            sb.append(format != null ? format.toPattern() : null);
            sb.append(", tooltip=");                                            // NOI18N
            sb.append(tooltip);
            sb.append("]");                                                     // NOI18N
            return sb.toString();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TableCellStyle other = (TableCellStyle) obj;
            if (this.format != other.format && (this.format == null || !this.format.equals(other.format))) {
                return false;
            }
            if (this.background != other.background && (this.background == null || !this.background.equals(other.background))) {
                return false;
            }
            if (this.foreground != other.foreground && (this.foreground == null || !this.foreground.equals(other.foreground))) {
                return false;
            }
            if ((this.tooltip == null) ? (other.tooltip != null) : !this.tooltip.equals(other.tooltip)) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

    }
    
    public static TableCellStyle getCellStyle(JTable table, IssueTable issueTable, IssueNode<?>.IssueProperty<?> p, boolean isSelected, int row) {
        TableCellStyle style = getDefaultCellStyle(table, issueTable, p, isSelected, row);
        try {
            // set text format and background depending on selection and issue status
            IssueStatusProvider.Status status = p.getStatus();
            if(status != IssueStatusProvider.Status.SEEN) {
                switch(status) {
                    case INCOMING_NEW :
                        style.format     = isSelected ? style.format      : issueNewFormat;
                        style.background = isSelected ? newHighlightColor : style.background;
                        style.foreground = isSelected ? table.getBackground() : style.foreground;
                        break;
                    case INCOMING_MODIFIED :
                        style.format     = isSelected ? style.format           : issueModifiedFormat;
                        style.background = isSelected ? modifiedHighlightColor : style.background;
                        style.foreground = isSelected ? table.getBackground() : style.foreground;
                        break;
                }
            }
            
            Object o = p.getValue();
            if(o instanceof String) {
                String s = (String) o;
                if(s == null) {
                    s = "";
                }                                               // NOI18N
                s = TextUtils.escapeForHTMLLabel(s);
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");                                                // NOI18N
                sb.append(s);
                if(status == null) {
                    status = p.getStatus();
                }
                switch(status) {
                    case INCOMING_NEW :
                        sb.append("<br>").append(issueNewFormat.format(new Object[] { labelNew }, new StringBuffer(), null)); // NOI18N
                        sb.append(msgNew);
                        break;
                    case INCOMING_MODIFIED :
                        sb.append("<br>").append(issueModifiedFormat.format(new Object[] { labelModified }, new StringBuffer(), null)); // NOI18N
                        sb.append(MessageFormat.format(msgModified, p.getRecentChanges()));
                        break;
                }
                sb.append("</html>"); // NOI18N
                style.tooltip = sb.toString();
            }
        } catch (Exception ex) {
            IssueTable.LOG.log(Level.WARNING, null, ex);
        }
        return style;
    }
    public static TableCellStyle getDefaultCellStyle(JTable table, IssueTable issueTable, IssueNode<?>.IssueProperty<?> p, boolean isSelected, int row) {
        // set default values
        return new TableCellStyle(
            null,                                                                       // format
            isSelected ? table.getSelectionBackground() : getUnselectedBackground(row), // background
            isSelected ? Color.WHITE : table.getForeground(),                           // foreground
            null,                                                                       // tooltip
            getHightlightPattern(issueTable, p)
        );
    }

    private static Pattern getHightlightPattern(IssueTable issueTable, IssueNode<?>.IssueProperty<?> p) {
        if(p instanceof IssueNode.SummaryProperty) {            
            SummaryTextFilter f = issueTable.getSummaryFilter();
            if(f != null && f.isHighLightingOn()) {
                return f.getPattern();
            }
        }
        return null;
    }

    private static Color getUnselectedBackground(int row) {
        return row % 2 != 0 ? unevenLineColor : evenLineColor;
    }

    public static void setRowColors(TableCellStyle style, JComponent l) {
        if(style == null) {
            assert false;
            return; // prefer to do nothing instead of breaking the rendering with an NPE
        }
        if (style.background != null) {
            l.setBackground(style.background);
        }
        if (style.foreground != null) {
            l.setForeground(style.foreground);
        }
    }
}


