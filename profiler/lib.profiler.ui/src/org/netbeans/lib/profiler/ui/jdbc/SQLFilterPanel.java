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
package org.netbeans.lib.profiler.ui.jdbc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.swing.InvisibleToolbar;
import org.netbeans.lib.profiler.ui.swing.PopupButton;
import org.netbeans.modules.profiler.api.ActionsSupport;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class SQLFilterPanel extends JPanel {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.jdbc.Bundle"); // NOI18N
    private static final String QUERIES_CAPTION = messages.getString("SQLFilterPanel_QueriesCaption"); // NOI18N
    private static final String FILTER_BUTTON = messages.getString("SQLFilterPanel_FilterButton"); // NOI18N
    private static final String COMMANDS_DROPDOWN = messages.getString("SQLFilterPanel_CommandsDropdown"); // NOI18N
    private static final String COMMANDS_NOTAVAILABLE = messages.getString("SQLFilterPanel_CommandsNotAvailable"); // NOI18N
    private static final String TABLES_DROPDOWN = messages.getString("SQLFilterPanel_TablesDropdown"); // NOI18N
    private static final String TABLES_NOTAVAILABLE = messages.getString("SQLFilterPanel_TablesNotAvailable"); // NOI18N
    private static final String STATEMENTS_DROPDOWN = messages.getString("SQLFilterPanel_StatementsDropdown"); // NOI18N
    private static final String STATEMENT_REGULAR = messages.getString("SQLFilterPanel_StatementRegular"); // NOI18N
    private static final String STATEMENT_PREPARED = messages.getString("SQLFilterPanel_StatementPrepared"); // NOI18N
    private static final String STATEMENT_CALLABLE = messages.getString("SQLFilterPanel_StatementCallable"); // NOI18N
    private static final String FILTER_TOOLTIP = messages.getString("SQLFilterPanel_FilterTooltip"); // NOI18N
    private static final String COMMANDS_TOOLTIP = messages.getString("SQLFilterPanel_CommandsTooltip"); // NOI18N
    private static final String TABLES_TOOLTIP = messages.getString("SQLFilterPanel_TablesTooltip"); // NOI18N
    private static final String STATEMENTS_TOOLTIP = messages.getString("SQLFilterPanel_StatementsTooltip"); // NOI18N
    // -----
    
    
    private static final String APPLY_ACTION_KEY = "apply-action-key"; // NOI18N
    
    private boolean initialized = false;
    
    private JButton applyB;
    
    private Configuration current = new Configuration();
    private Configuration applied = new Configuration();
    
    SQLFilterPanel() {
        super(new BorderLayout());
        
        setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("controlShadow")), // NOI18N
                                     BorderFactory.createMatteBorder(6, 3, 6, 3, UIUtils.getProfilerResultsBackground())));
        setOpaque(true);
        setBackground(UIUtils.getProfilerResultsBackground());
        
        JToolBar toolbar = new InvisibleToolbar();
        if (UIUtils.isWindowsModernLookAndFeel())
            toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 2));
        else if (!UIUtils.isNimbusLookAndFeel() && !UIUtils.isAquaLookAndFeel())
            toolbar.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        
        KeyStroke applyKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        Action applyAction = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() { if (applyB.isEnabled()) applyB.doClick(); }
                });
            }
        };
        
        toolbar.add(Box.createHorizontalStrut(3));
        toolbar.add(new JLabel(QUERIES_CAPTION));
        toolbar.add(Box.createHorizontalStrut(3));
        
        final JTextField filterF = new JTextField(20) {
            public Dimension getMaximumSize() {
                Dimension dim = super.getMaximumSize();
                dim.height = super.getPreferredSize().height;
                if (UIUtils.isMetalLookAndFeel()) dim.height += 4;
                return dim;
            }
        };
        filterF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { handle(); }
            public void removeUpdate(DocumentEvent e)  { handle(); }
            public void changedUpdate(DocumentEvent e) { handle(); }
            private void handle() { current.filter = filterF.getText().trim().toLowerCase(Locale.ENGLISH); changed(); }
        });
        filterF.getActionMap().put(APPLY_ACTION_KEY, applyAction);
        filterF.getInputMap().put(applyKey, APPLY_ACTION_KEY);
        toolbar.add(filterF);
        
        toolbar.add(Box.createHorizontalStrut(10));
        
        applyB = new JButton(FILTER_BUTTON) {
            protected void fireActionPerformed(ActionEvent e) { apply(); }
        };
        String filterAccelerator = ActionsSupport.keyAcceleratorString(applyKey);
        applyB.setToolTipText(MessageFormat.format(FILTER_TOOLTIP, filterAccelerator));
        applyB.setOpaque(false);
        JPanel applyP = new JPanel(new BorderLayout()) {
            public Dimension getMaximumSize() { return getMinimumSize(); }
        };
        applyP.add(applyB, BorderLayout.CENTER);
        applyP.setOpaque(false);
        toolbar.add(applyP);
        
        toolbar.add(Box.createHorizontalStrut(10));
        
        toolbar.addSeparator();
        
        toolbar.add(Box.createHorizontalStrut(8));
        
        PopupButton commands = new PopupButton(" " + COMMANDS_DROPDOWN + " ") { // NOI18N
            protected void populatePopup(JPopupMenu popup) {
                List<String> commands = new ArrayList<>(getCommands());
                if (commands.isEmpty()) {
                    JLabel l = new JLabel(COMMANDS_NOTAVAILABLE);
                    l.setBorder(BorderFactory.createEmptyBorder(9, 6, 9, 6));
                    popup.add(l);
                } else {
                    Collections.sort(commands);
                    current.commands.retainAll(commands);
                    for (final String command : commands) {
                        JCheckBoxMenuItem i = new JCheckBoxMenuItem(command, !current.commands.contains(command)) {
                            protected void fireActionPerformed(ActionEvent e) {
                                if (!isSelected()) current.commands.add(command);
                                else current.commands.remove(command);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() { apply(); }
                                });
                            }
                        };
                        popup.add(i);
                    }
                }
            }
        };
        commands.setToolTipText(COMMANDS_TOOLTIP);
        commands.setPopupAlign(SwingConstants.NORTH_WEST);
        toolbar.add(commands);
        
        toolbar.add(Box.createHorizontalStrut(5));
        
        PopupButton tables = new PopupButton(" " + TABLES_DROPDOWN + " ") { // NOI18N
            protected void displayPopup() {
                Set<String> tablesSet = new HashSet<>(getTables());
                if (tablesSet.isEmpty()) {
                    super.displayPopup();
                } else {
                    current.tables.retainAll(tablesSet);
                    new TablesSelector(tablesSet, current.tables) {
                        protected void selectionChanged(Collection<String> selected) {
                            current.tables.clear();
                            current.tables.addAll(selected);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() { apply(); }
                            });
                        }
                    }.show(this);
                }
            }
            protected void populatePopup(JPopupMenu popup) {
                JLabel l = new JLabel(TABLES_NOTAVAILABLE);
                l.setBorder(BorderFactory.createEmptyBorder(9, 6, 9, 6));
                popup.add(l);
            }
        };
        tables.setToolTipText(TABLES_TOOLTIP);
        toolbar.add(tables);
        
        toolbar.add(Box.createHorizontalStrut(5));
        
        PopupButton statements = new PopupButton(" " + STATEMENTS_DROPDOWN + " ") { // NOI18N
            protected void populatePopup(JPopupMenu popup) {
                popup.add(new JCheckBoxMenuItem(STATEMENT_REGULAR, !current.statements.contains(JdbcCCTProvider.SQL_STATEMENT)) {
                    protected void fireActionPerformed(ActionEvent e) {
                        if (!isSelected()) current.statements.add(JdbcCCTProvider.SQL_STATEMENT);
                        else current.statements.remove(JdbcCCTProvider.SQL_STATEMENT);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() { apply(); }
                        });
                    }
                });
                
                popup.add(new JCheckBoxMenuItem(STATEMENT_PREPARED, !current.statements.contains(JdbcCCTProvider.SQL_PREPARED_STATEMENT)) {
                    protected void fireActionPerformed(ActionEvent e) {
                        if (!isSelected()) current.statements.add(JdbcCCTProvider.SQL_PREPARED_STATEMENT);
                        else current.statements.remove(JdbcCCTProvider.SQL_PREPARED_STATEMENT);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() { apply(); }
                        });
                    }
                });
                
                popup.add(new JCheckBoxMenuItem(STATEMENT_CALLABLE, !current.statements.contains(JdbcCCTProvider.SQL_CALLABLE_STATEMENT)) {
                    protected void fireActionPerformed(ActionEvent e) {
                        if (!isSelected()) current.statements.add(JdbcCCTProvider.SQL_CALLABLE_STATEMENT);
                        else current.statements.remove(JdbcCCTProvider.SQL_CALLABLE_STATEMENT);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() { apply(); }
                        });
                    }
                });
            }
        };
        statements.setToolTipText(STATEMENTS_TOOLTIP);
        statements.setPopupAlign(SwingConstants.NORTH_EAST);
        toolbar.add(statements);
        
        toolbar.add(Box.createHorizontalStrut(3));
        
        add(toolbar, BorderLayout.CENTER);
        
        initialized = true;
        changed();
    }
    
    private void changed() {
        if (initialized) applyB.setEnabled(!applied.equals(current));
    }
    
    private void apply() {
        applyB.setEnabled(false);
        applied.set(current);
        applyFilter();
    }
    
    
    abstract Set<String> getCommands();
    
    abstract Set<String> getTables();
    
    abstract void applyFilter();
    
    
    boolean passes(String query, String command, String[] tables, int statement) {
        
        if (!applied.filter.isEmpty() && !query.toLowerCase(Locale.ENGLISH).contains(applied.filter)) return false;
        
        if (!applied.commands.isEmpty() && applied.commands.contains(command)) return false;
        
        if (!applied.statements.isEmpty() && applied.statements.contains(statement)) return false;
        
        if (applied.tables.isEmpty()) return true;
        for (String table : tables) if (!applied.tables.contains(table)) return true;
        
        return false;
    }
    
    
    private static class Configuration {
        
        String filter = ""; // NOI18N
        
        final Set<String> commands = new HashSet<>();
        final Set<String> tables = new HashSet<>();
        final Set<Integer> statements = new HashSet<>();
        
        void set(Configuration o) {
            filter = o.filter;
            
            commands.clear();
            commands.addAll(o.commands);
            
            tables.clear();
            tables.addAll(o.tables);
            
            statements.clear();
            statements.addAll(o.statements);
        }
        
        public boolean equals(Object o) {
            Configuration c = (Configuration)o;
            return filter.equals(c.filter) &&
                   commands.equals(c.commands) &&
                   tables.equals(c.tables) &&
                   statements.equals(c.statements);
        }
        
    }
    
}
