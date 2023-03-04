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
package org.netbeans.modules.gsf.codecoverage;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Window which displays a code coverage report.
 *
 * <p>
 * <b>NOTE</b>: You must compile this module before attempting to open this form in the GUI builder!
 * The design depends on the CoverageBar class and Matisse can only load the form if the .class, not
 * just the .java file, is available!
 */
final class CoverageReportTopComponent extends TopComponent {

    private CoverageTableModel model;
    private Project project;
    private static final String PREFERRED_ID = "CoverageReportTopComponent"; // NOI18N
    private final int[] minColumnWidths = {300, 100, 50, 110};

    CoverageReportTopComponent(Project project, List<FileCoverageSummary> results) {
        model = new CoverageTableModel(results);
        this.project = project;
        initComponents();

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setShowGrid(false);

        // Pad out the cells a bit more - causes clipping so we have to increase
        // the row height as well!
        table.setIntercellSpacing(new Dimension(6, 4));
        table.setRowHeight(table.getRowHeight() + 4);

        //Color color = table.getBackground();
        //table.setGridColor(color.darker());
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resizeColumnWidth(table);

        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        setName(NbBundle.getMessage(CoverageReportTopComponent.class, "CTL_CoverageReportTopComponent", projectName));
        setToolTipText(NbBundle.getMessage(CoverageReportTopComponent.class, "HINT_CoverageReportTopComponent"));
        //setIcon(Utilities.loadImage(ICON_PATH, true));
        // Make the Total row bigger
        //if (results != null && results.size() > 0) {
        //    int rowHeight = table.getRowHeight();
        //    table.setRowHeight(model.getRowCount()-1, 2*rowHeight);
        //}

        try {
            // Paint the full table
            // JDK6 only...
            //JTable.setFillsViewportHeight(true);
            // Try with reflection:
            Method method = JTable.class.getMethod("setFillsViewportHeight", new Class[0]); // NOI18N
            if (method != null) {
                method.invoke(null, new Object[0]);
            }
        } catch (InvocationTargetException ex) {
            // No complaints - we may not be on JDK6
        } catch (IllegalArgumentException ex) {
            // No complaints - we may not be on JDK6
        } catch (IllegalAccessException ex) {
            // No complaints - we may not be on JDK6
        } catch (NoSuchMethodException ex) {
            // No complaints - we may not be on JDK6
        } catch (SecurityException ex) {
            // No complaints - we may not be on JDK6
        }

        table.setDefaultRenderer(Float.class, new CoverageRenderer());
        table.setDefaultRenderer(String.class, new FileRenderer());

        //JDK6 only - row sorting
        //table.setAutoCreateRowSorter(true);
        try {
            // Try with reflection:
            Method method = JTable.class.getMethod("setAutoCreateRowSorter", new Class[]{Boolean.TYPE}); // NOI18N
            if (method != null) {
                method.invoke(table, Boolean.TRUE);
            }
        } catch (InvocationTargetException ex) {
            // No complaints - we may not be on JDK6
        } catch (IllegalArgumentException ex) {
            // No complaints - we may not be on JDK6
        } catch (IllegalAccessException ex) {
            // No complaints - we may not be on JDK6
        } catch (NoSuchMethodException ex) {
            // No complaints - we may not be on JDK6
        } catch (SecurityException ex) {
            // No complaints - we may not be on JDK6
        }

        // JDK6 only
        //    import javax.swing.table.TableRowSorter;
        //    ...
        //    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        //    table.setRowSorter(sorter);
        //    Comparator comparableComparator = new Comparator() {
        //        @SuppressWarnings("unchecked")
        //        public int compare(Object o1, Object o2) {
        //            return ((Comparable) o1).compareTo(o2);
        //        }
        //    };
        //    for (int i = 0; i < 4; i++) {
        //        sorter.setComparator(i, comparableComparator);
        //    }
        totalCoverage.setCoveragePercentage(model.getTotalCoverage());
        updateStats();
    }

    public void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 50; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
            columnModel.getColumn(column).setMinWidth(minColumnWidths[column]);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new JScrollPane();
        table = new EmptyPaintingTable();
        clearResultsButton = new JButton();
        jLabel1 = new JLabel();
        totalCoverage = new CoverageBar();
        allTestsButton = new JButton();
        doneButton = new JButton();

        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                clicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        Mnemonics.setLocalizedText(clearResultsButton, NbBundle.getMessage(CoverageReportTopComponent.class, "CoverageReportTopComponent.clearResultsButton.text"));
        clearResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearResultsButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(CoverageReportTopComponent.class, "CoverageReportTopComponent.jLabel1.text"));

        javax.swing.GroupLayout totalCoverageLayout = new javax.swing.GroupLayout(totalCoverage);
        totalCoverage.setLayout(totalCoverageLayout);
        totalCoverageLayout.setHorizontalGroup(
            totalCoverageLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 146, Short.MAX_VALUE)
        );
        totalCoverageLayout.setVerticalGroup(
            totalCoverageLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );
        Mnemonics.setLocalizedText(allTestsButton, NbBundle.getMessage(CoverageReportTopComponent.class, "CoverageReportTopComponent.allTestsButton.text"));
        allTestsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                runAllTests(evt);
            }
        });
        Mnemonics.setLocalizedText(doneButton, NbBundle.getMessage(CoverageReportTopComponent.class, "CoverageReportTopComponent.doneButton.text"));
        doneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                done(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(clearResultsButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(allTestsButton)
                        .addPreferredGap(ComponentPlacement.RELATED, 216, Short.MAX_VALUE)
                        .addComponent(doneButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(totalCoverage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(totalCoverage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(clearResultsButton)
                    .addComponent(allTestsButton)
                    .addComponent(doneButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearResultsButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearResultsButtonActionPerformed
        CoverageManagerImpl.getInstance().clear(project);
}//GEN-LAST:event_clearResultsButtonActionPerformed

    private void clicked(MouseEvent evt) {//GEN-FIRST:event_clicked
        if (evt.getClickCount() == 2) {
            int row = table.getSelectedRow();
            if (row != -1) {
                try {
                    // If sorting is in effect.
                    // JDK6 only...
                    // Try with reflection:
                    //row = table.convertRowIndexToModel(row);
                    Method method = JTable.class.getMethod("convertRowIndexToModel", new Class[]{Integer.TYPE}); // NOI18N
                    if (method != null) {
                        row = (Integer) method.invoke(table, Integer.valueOf(row));
                    }
                } catch (InvocationTargetException ex) {
                    // No complaints - we may not be on JDK6
                } catch (IllegalArgumentException ex) {
                    // No complaints - we may not be on JDK6
                } catch (IllegalAccessException ex) {
                    // No complaints - we may not be on JDK6
                } catch (NoSuchMethodException ex) {
                    // No complaints - we may not be on JDK6
                } catch (SecurityException ex) {
                    // No complaints - we may not be on JDK6
                }

                FileCoverageSummary result = (FileCoverageSummary) model.getValueAt(row, -1);
                CoverageManagerImpl.getInstance().showFile(project, result);
            }
        }
    }//GEN-LAST:event_clicked

    private void runAllTests(ActionEvent evt) {//GEN-FIRST:event_runAllTests
        Lookup lookup = project.getLookup();
        ActionProvider actionProvider = project.getLookup().lookup(ActionProvider.class);
        CoverageProvider coverageProvider = CoverageManagerImpl.getProvider(project);
        String action = ActionProvider.COMMAND_TEST;
        if (coverageProvider != null && coverageProvider.getTestAllAction() != null) {
            action = coverageProvider.getTestAllAction();
        }
        if (actionProvider != null) {
            if (actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST, lookup)) {
                actionProvider.invokeAction(action, lookup);
            }
        }
    }//GEN-LAST:event_runAllTests

    private void done(ActionEvent evt) {//GEN-FIRST:event_done
        CoverageManagerImpl.getInstance().setEnabled(project, false);
        close();
    }//GEN-LAST:event_done

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton allTestsButton;
    private JButton clearResultsButton;
    private JButton doneButton;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JTable table;
    private CoverageBar totalCoverage;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        CoverageManagerImpl.getInstance().closedReport(project);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    void updateData(List<FileCoverageSummary> results) {
        assert EventQueue.isDispatchThread();
        model = new CoverageTableModel(results);
        table.setModel(model);
        totalCoverage.setCoveragePercentage(model.getTotalCoverage());
        updateStats();
        resizeColumnWidth(table);
    }

    private void updateStats() {
        FileCoverageSummary summary = model.getCoverageSummary();
        if (summary != null) {
            totalCoverage.setStats(summary.getLineCount(), summary.getExecutedLineCount(),
                    summary.getPartialCount(), summary.getInferredCount());
        }    
    }

    private static class CoverageTableModel implements TableModel {

        List<FileCoverageSummary> results;
        FileCoverageSummary total;
        //List<TableModelListener> listeners = new ArrayList<TableModelListener>();
        float totalCoverage = 0.0f;

        public CoverageTableModel(List<FileCoverageSummary> results) {
            if (results == null || results.isEmpty()) {
                results = new ArrayList<>();
            } else {
                Collections.sort(results);
            }

            int lineCount = 0;
            int executedLineCount = 0;
            int inferredCount = 0;
            int partialCount = 0;
            for (FileCoverageSummary result : results) {
                lineCount += result.getLineCount();
                executedLineCount += result.getExecutedLineCount();
                inferredCount += result.getInferredCount();
                partialCount += result.getPartialCount();
            }

            if (results.isEmpty()) {
                results.add(new FileCoverageSummary(null, NbBundle.getMessage(CoverageReportTopComponent.class, "NoData"), 0, 0, 0, 0));
            } else {
                total = new FileCoverageSummary(null, "<html><b>"
                    + // NOI18N
                    NbBundle.getMessage(CoverageReportTopComponent.class, "Total")
                    + "</b></html>", lineCount, executedLineCount, inferredCount, partialCount); // NOI18N
                totalCoverage = total.getCoveragePercentage();
                results.add(total);
            }
            this.results = results;
        }

        FileCoverageSummary getCoverageSummary() {
            return total;
        }

        float getTotalCoverage() {
            return totalCoverage;
        }

        @Override
        public int getRowCount() {
            return results.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "Filename");
                case 2:
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "TotalStatements");
                case 3:
                    //return NbBundle.getMessage(CoverageReportTopComponent.class, "ExecutedStatements");
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "NotExecutedStatements");
                case 1:
                default:
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "Coverage");
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 1:
                    return Float.class;
                case 2:
                    return Integer.class;
                case 3:
                    return Integer.class;
                case 0:
                default:
                    return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            FileCoverageSummary result = results.get(row);
            switch (col) {
                case -1: // Special contract with table selection handler
                    return result;
                case 0:
                    return result.getDisplayName();
                case 1:
                    return result.getCoveragePercentage();
                case 2:
                    return result.getLineCount();
                case 3:
                    //return result.getExecutedLineCount();
                    return result.getLineCount() - result.getExecutedLineCount();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object arg0, int arg1, int arg2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addTableModelListener(TableModelListener listener) {
            // nothing to do
        }

        @Override
        public void removeTableModelListener(TableModelListener listener) {
            // nothing to do
        }
    }

    private static class FileRenderer extends JLabel implements TableCellRenderer {

        @Override
        public boolean isOpaque() {
            return true;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
                return new DefaultTableCellRenderer().getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            }

            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
            }

            setFont(table.getFont());

            if (hasFocus) {
                Border border = null;
                if (isSelected) {
                    border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder"); // NOI18N
                }
                if (border == null) {
                    border = UIManager.getBorder("Table.focusCellHighlightBorder"); // NOI18N
                }
                setBorder(border);
            } else {
                setBorder(new EmptyBorder(1, 1, 1, 1));
            }

            FileCoverageSummary summary = (FileCoverageSummary) table.getValueAt(row, -1);
            FileObject file = summary.getFile();

            setText(summary.getDisplayName());
            if (file != null && file.isValid()) {
                try {
                    DataObject dobj = DataObject.find(file);
                    Node node = dobj.getNodeDelegate();
                    Image icon = node.getIcon(BeanInfo.ICON_COLOR_32x32);
                    setIcon(new ImageIcon(icon));
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                setIcon(null);
            }

            return this;
        }
    }

    private class CoverageRenderer extends CoverageBar implements TableCellRenderer {

        public CoverageRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
                return new DefaultTableCellRenderer().getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            }

            // This doesn't work in the presence of table row sorting:
            //boolean isTotalRow = row == table.getModel().getRowCount()-1;
            FileCoverageSummary summary = (FileCoverageSummary) table.getValueAt(row, -1);
            boolean isTotalRow = summary == ((CoverageTableModel) table.getModel()).total;
            setEmphasize(isTotalRow);
            setSelected(isSelected);

            float coverage = (Float) value;
            setCoveragePercentage(coverage);
            setStats(summary.getLineCount(), summary.getExecutedLineCount(),
                summary.getInferredCount(), summary.getPartialCount());

            return this;
        }
    }

    private static class EmptyPaintingTable extends JTable {

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport && getPreferredSize().height < getParent().getHeight();
        }
    }
}
