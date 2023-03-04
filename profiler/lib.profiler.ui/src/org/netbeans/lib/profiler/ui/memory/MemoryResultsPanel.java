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

package org.netbeans.lib.profiler.ui.memory;

import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.ui.ResultsPanel;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.FilterComponent;
import org.netbeans.lib.profiler.ui.components.JExtendedTable;
import org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.ExtendedTableModel;
import org.netbeans.lib.profiler.utils.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;


/**
 * Base abstract class for panels containing memory profiling results in table form.
 * It consists of a JPanel with embedded JScrollPane, plus a popup menu that is not attached to
 * anything (that should be done by subclasses). The common functionality provided in it is:
 * - initialization and generation of displayable data
 * - reset
 * - showing results
 * - getting string title
 * - sorting results.
 *
 * @author Misha Dmitriev
 * @author Jiri Sedlacek
 * @author Ian Formanek
 */
public abstract class MemoryResultsPanel extends ResultsPanel {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle"); // NOI18N
    private static final String CLASS_NAME_FILTER = messages.getString("MemoryResultsPanel_ClassNameFilterHint"); // NOI18N
                                                                                                                // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected ArrayList filteredToFullIndexes;
    protected CustomBarCellRenderer barRenderer;
    protected ExtendedTableModel resTableModel;
    protected FilterComponent filterComponent;
    protected JButton cornerButton;
    protected JExtendedTable resTable;
    protected JPopupMenu headerPopup;
    protected JScrollPane jScrollPane;
    protected MemoryResUserActionsHandler actionsHandler;
    protected String filterString = ""; // NOI18N
    protected String[] columnNames;
    protected TableCellRenderer[] columnRenderers;
    protected String[] columnToolTips;
    protected Class[] columnTypes;
    protected int[] columnWidths;
    protected int[] sortedClassIds; // this maps row idx to classId (classId = original, before-sort, row index)
    protected String[] sortedClassNames; // this is effectively a copy of the class names contained in profilingSessionStatus
                                         // or MemoryResultsSnapshot, in user-level format and sorted according to current
                                         // sorting criteria
    protected boolean registeredMouseListenerWithResTable;
    protected boolean sortOrder; // Defines the sorting order (ascending or descending)
    protected double valueFilterValue = 0.0d;
    protected int clickedLine;
    protected int filterType = CommonConstants.FILTER_CONTAINS;
    protected int nDisplayedItems;
    protected int nInfoLines;
    protected int nTrackedItems;
    protected int selectedClassId;
    protected int sortBy; // Defines sorting criteria (concrete values provided in subclasses)
    protected long maxValue; // Used by the bar representation management code
    protected long totalAllocations;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public MemoryResultsPanel(MemoryResUserActionsHandler actionsHandler) {
        this.actionsHandler = actionsHandler;

        filteredToFullIndexes = new ArrayList();

        headerPopup = new JPopupMenu();
        jScrollPane = createScrollPaneVerticalScrollBarAlways();
        jScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createHeaderPopupCornerButton(headerPopup));
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        initFilterPanel();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public BufferedImage getCurrentViewScreenshot(boolean onlyVisibleArea) {
        if (resTable == null) {
            return null;
        }

        if (onlyVisibleArea) {
            return UIUtils.createScreenshot(jScrollPane);
        } else {
            return UIUtils.createScreenshot(resTable);
        }
    }

    // --- Find functionality stuff
    public void setFindString(String findString) {
        resTable.setFindParameters(findString, 0);
    }

    public String getFindString() {
        return resTable.getFindString();
    }

    public boolean isFindStringDefined() {
        return resTable.isFindStringDefined();
    }

    public boolean findFirst() {
        return resTable.findFirst();
    }

    public boolean findNext() {
        return resTable.findNext();
    }

    public boolean findPrevious() {
        return resTable.findPrevious();
    }

    public boolean fitsVisibleArea() {
        return !jScrollPane.getVerticalScrollBar().isEnabled();
    }

    public void prepareResults() {
        final JExtendedTable table = getResultsTable();
        resTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "DEFAULT_ACTION"); // NOI18N
        resTable.getActionMap().put("DEFAULT_ACTION",
                                    new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    performDefaultAction(selectedClassId);
                }
            }); // NOI18N

        jScrollPane.setViewportView(table);
        jScrollPane.getViewport().setBackground(table.getBackground());

        if (!registeredMouseListenerWithResTable) {
            jScrollPane.addMouseWheelListener(new MouseWheelListener() {
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        table.mouseWheelMoved(e);
                    }
                });

            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        int selectedRow = table.getSelectedRow();

                        if (selectedRow == -1) {
                            return;
                        }

                        selectedClassId = sortedClassIds[((Integer) filteredToFullIndexes.get(selectedRow)).intValue()];
                    }
                });

            table.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if ((e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
                                || ((e.getKeyCode() == KeyEvent.VK_F10) && (e.getModifiers() == InputEvent.SHIFT_MASK))) {
                            int selectedRow = table.getSelectedRow();

                            if (selectedRow != -1) {
                                selectedClassId = sortedClassIds[((Integer) filteredToFullIndexes.get(selectedRow)).intValue()];

                                Rectangle cellRect = table.getCellRect(selectedRow, 0, false);
                                JPopupMenu popup = getPopupMenu();

                                if (popup != null) {
                                    popup.show(e.getComponent(), ((cellRect.x + table.getSize().width) > 50) ? 50 : 5, cellRect.y);
                                }
                            }
                        }
                    }
                });

            table.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
                            int line = table.rowAtPoint(e.getPoint());

                            if (line != -1) {
                                table.setRowSelectionInterval(line, line);
                            }
                        }
                    }

                    public void mouseClicked(MouseEvent e) {
                        clickedLine = table.rowAtPoint(e.getPoint());

                        if (clickedLine != -1) {
                            resTable.getSelectionModel().setSelectionInterval(clickedLine, clickedLine);
                            //selectedClassId = sortedClassIds[clickedLine];
                            selectedClassId = sortedClassIds[((Integer) filteredToFullIndexes.get(clickedLine)).intValue()];

                            if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
                                JPopupMenu popup = getPopupMenu();

                                if (popup != null) {
                                    popup.show(e.getComponent(), e.getX(), e.getY());
                                }
                            } else if ((e.getModifiers() == InputEvent.BUTTON1_MASK) && (e.getClickCount() == 2)) {
                                performDefaultAction(selectedClassId);
                            }
                        }
                    }
                });
            registeredMouseListenerWithResTable = true;
        }
    }

    public void requestFocus() {
        if (resTable != null) {
            SwingUtilities.invokeLater(new Runnable() { // must be invoked lazily to override default focus of first component (top-right cornerButton)
                    public void run() {
                        resTable.requestFocus();
                    }
                });
        }
    }

    public void reset() {
        jScrollPane.setViewportView(null);
    }

    public void updateValueFilter(double value) {
        valueFilterValue = value / 3f; // maximum 33.3%
        createFilteredIndexes();
        resTable.invalidate();
        jScrollPane.revalidate();
        resTable.repaint();
    }

    protected abstract String getClassName(int classid);

    protected abstract String[] getClassNames();

    protected abstract JPopupMenu getPopupMenu();

    // ---------------------------------------------------------------------------
    // Abstract methods that should be implemented by a concrete - data type frame
    protected abstract JExtendedTable getResultsTable();

    protected void createFilteredIndexes() {
        filteredToFullIndexes.clear();

        for (int i = 0; i < nInfoLines; i++) {
            if (passesFilters(i) && passesValueFilter(i)) {
                filteredToFullIndexes.add(Integer.valueOf(i));
            }
        }

        nDisplayedItems = filteredToFullIndexes.size();
    }

    protected final void doCreateClassNamesFromScratch() {
        String[] classNames = getClassNames();

        if ((sortedClassNames == null) || (sortedClassNames.length < classNames.length)) {
            sortedClassNames = new String[classNames.length];
            sortedClassIds = new int[classNames.length];
        }

        for (int i = 0; i < classNames.length; i++) {
            sortedClassNames[i] = StringUtils.userFormClassName(classNames[i]);
            sortedClassIds[i] = i;
        }
    }

    protected void initDataUponResultsFetch() {
//        if (barRenderer != null) {
//            barRenderer.setMaximum(maxValue); // updateState the bar renderer if already exists
//        }

        doCreateClassNamesFromScratch();
    }

    protected boolean passesValueFilter(int i) {
        return true;
    }

    protected abstract void performDefaultAction(int selectedClassId);

    protected void showSourceForClass(int classId) {
        if (classId < 0) {
            return;
        }

        String className = StringUtils.userFormClassName(getClassName(classId));

        actionsHandler.showSourceForMethod(className, null, null);
    }

    //----------------------------------------------------------------------------
    // Sorting results according to different criteria - used in subclasses

    /**
     * Sorts the results[] array, aligning secondaryData with it if it's present.
     * Returns the number of non-zero elements, which may be smaller than the array size.
     */
    protected int sortResults(final int[] results, final int[][] secondaryIntData, final long[][] secondaryLongData,
                              final float[][] secondaryFloatData, int off, int len, boolean truncateZeroItems) {
        //if (createNamesFromScratch) doCreateClassNamesFromScratch();
        final int nSecIDataArrays = (secondaryIntData != null) ? secondaryIntData.length : 0;
        final int nSecLDataArrays = (secondaryLongData != null) ? secondaryLongData.length : 0;
        final int nSecFDataArrays = (secondaryFloatData != null) ? secondaryFloatData.length : 0;

        (new IntSorter(results, off, len) {
                protected void swap(int a, int b) {
                    if (results[a] != results[b]) {
                        super.swap(a, b);

                        String tmp = sortedClassNames[a];
                        sortedClassNames[a] = sortedClassNames[b];
                        sortedClassNames[b] = tmp;

                        int tmpI = sortedClassIds[a];
                        sortedClassIds[a] = sortedClassIds[b];
                        sortedClassIds[b] = tmpI;

                        for (int i = 0; i < nSecIDataArrays; i++) {
                            tmpI = secondaryIntData[i][a];
                            secondaryIntData[i][a] = secondaryIntData[i][b];
                            secondaryIntData[i][b] = tmpI;
                        }

                        for (int i = 0; i < nSecLDataArrays; i++) {
                            long tmpL = secondaryLongData[i][a];
                            secondaryLongData[i][a] = secondaryLongData[i][b];
                            secondaryLongData[i][b] = tmpL;
                        }

                        for (int i = 0; i < nSecFDataArrays; i++) {
                            float tmpF = secondaryFloatData[i][a];
                            secondaryFloatData[i][a] = secondaryFloatData[i][b];
                            secondaryFloatData[i][b] = tmpF;
                        }
                    }
                }
            }).sort(sortOrder);

        len = off + len; // Note that supplied len may be for a subset of the array, but what's ultimately needed is
                         // the number of non-zero elements for the whole array
                         //if (truncateZeroItems) {  // Deal with the fact that some items in the bottom may be just zero
                         //  while (len > 0 && results[len - 1] == 0) len--;
                         //}

        return len;
    }

    /**
     * Sorts the results[] array, aligning secondaryData with it if it's present.
     * Returns the number of non-zero elements, which may be smaller than the array size.
     */
    protected int sortResults(final long[] results, final int[][] secondaryIntData, final long[][] secondaryLongData,
                              final float[][] secondaryFloatData, int off, int len, boolean truncateZeroItems) {
        //if (createNamesFromScratch) doCreateClassNamesFromScratch();
        final int nSecIDataArrays = (secondaryIntData != null) ? secondaryIntData.length : 0;
        final int nSecLDataArrays = (secondaryLongData != null) ? secondaryLongData.length : 0;
        final int nSecFDataArrays = (secondaryFloatData != null) ? secondaryFloatData.length : 0;

        (new LongSorter(results, off, len) {
                protected void swap(int a, int b) {
                    if (results[a] != results[b]) {
                        super.swap(a, b);

                        String tmp = sortedClassNames[a];
                        sortedClassNames[a] = sortedClassNames[b];
                        sortedClassNames[b] = tmp;

                        int tmpI = sortedClassIds[a];
                        sortedClassIds[a] = sortedClassIds[b];
                        sortedClassIds[b] = tmpI;

                        for (int i = 0; i < nSecIDataArrays; i++) {
                            tmpI = secondaryIntData[i][a];
                            secondaryIntData[i][a] = secondaryIntData[i][b];
                            secondaryIntData[i][b] = tmpI;
                        }

                        for (int i = 0; i < nSecLDataArrays; i++) {
                            long tmpL = secondaryLongData[i][a];
                            secondaryLongData[i][a] = secondaryLongData[i][b];
                            secondaryLongData[i][b] = tmpL;
                        }

                        for (int i = 0; i < nSecFDataArrays; i++) {
                            float tmpF = secondaryFloatData[i][a];
                            secondaryFloatData[i][a] = secondaryFloatData[i][b];
                            secondaryFloatData[i][b] = tmpF;
                        }
                    }
                }
            }).sort(sortOrder);

        len = off + len; // Note that supplied len may be for a subset of the array, but what's ultimately needed is
                         // the number of non-zero elements for the whole array
                         //if (truncateZeroItems) {  // Deal with the fact that some items in the bottom may be just zero
                         //  while (len > 0 && results[len - 1] == 0) len--;
                         //}

        return len;
    }

    /**
     * Sorts the results[] array, aligning secondaryData with it if it's present.
     * Returns the number of non-zero elements, which may be smaller than the array size.
     */
    protected int sortResults(final float[] results, final int[][] secondaryIntData, final long[][] secondaryLongData, int off,
                              int len, boolean truncateZeroItems) {
        //if (createNamesFromScratch) doCreateClassNamesFromScratch();
        final int nSecIDataArrays = (secondaryIntData != null) ? secondaryIntData.length : 0;
        final int nSecLDataArrays = (secondaryLongData != null) ? secondaryLongData.length : 0;

        (new FloatSorter(results, off, len) {
                protected void swap(int a, int b) {
                    if (results[a] != results[b]) {
                        super.swap(a, b);

                        String tmp = sortedClassNames[a];
                        sortedClassNames[a] = sortedClassNames[b];
                        sortedClassNames[b] = tmp;

                        int tmpI = sortedClassIds[a];
                        sortedClassIds[a] = sortedClassIds[b];
                        sortedClassIds[b] = tmpI;

                        for (int i = 0; i < nSecIDataArrays; i++) {
                            tmpI = secondaryIntData[i][a];
                            secondaryIntData[i][a] = secondaryIntData[i][b];
                            secondaryIntData[i][b] = tmpI;
                        }

                        for (int i = 0; i < nSecLDataArrays; i++) {
                            long tmpL = secondaryLongData[i][a];
                            secondaryLongData[i][a] = secondaryLongData[i][b];
                            secondaryLongData[i][b] = tmpL;
                        }
                    }
                }
            }).sort(sortOrder);

        len = off + len; // Note that supplied len may be for a subset of the array, but what's ultimately needed is
                         // the number of non-zero elements for the whole array
                         //if (truncateZeroItems) {  // Deal with the fact that some items in the bottom may be just zero
                         //  while (len > 0 && results[len - 1] == 0) len--;
                         //}

        return len;
    }

    /**
     * Sorts the results by class name, aligning secondaryIntData and secondaryFloatData with it if it's present.
     * Additionally, if truncateZeroItems is true, gets items for which secondaryIntData[0][i] is 0, to bottom.
     * Returns the number of non-zero (as above) elements, which may be smaller than the array size.
     */
    protected int sortResultsByClassName(final int[][] secondaryIntData, final long[][] secondaryLongData,
                                         final float[][] secondaryFloatData, int len, boolean truncateZeroItems) {
        if (len == 0) {
            return 0; // no processing for zero-length
                      //if (createNamesFromScratch) doCreateClassNamesFromScratch();
        }

        final int nSecIDataArrays = (secondaryIntData != null) ? secondaryIntData.length : 0;
        final int nSecLDataArrays = (secondaryLongData != null) ? secondaryLongData.length : 0;
        final int nSecFDataArrays = (secondaryFloatData != null) ? secondaryFloatData.length : 0;

        if (truncateZeroItems) { // Move zero items to the bottom

            int head = 0;
            int tail = len - 1;

            while (head < tail) {
                while ((secondaryIntData[0][tail] == 0) && (tail > head)) {
                    tail--;
                }

                if (tail <= head) {
                    break;
                }

                while ((secondaryIntData[0][head] != 0) && (head < tail)) {
                    head++;
                }

                if (head >= tail) {
                    break;
                }

                // Now data[headPos] == 0 and data[tailPos] != 0 - swap them
                String tmpS = sortedClassNames[head];
                sortedClassNames[head] = sortedClassNames[tail];
                sortedClassNames[tail] = tmpS;

                int tmpI = sortedClassIds[head];
                sortedClassIds[head] = sortedClassIds[tail];
                sortedClassIds[tail] = tmpI;

                for (int i = 0; i < nSecIDataArrays; i++) {
                    tmpI = secondaryIntData[i][head];
                    secondaryIntData[i][head] = secondaryIntData[i][tail];
                    secondaryIntData[i][tail] = tmpI;
                }

                for (int i = 0; i < nSecLDataArrays; i++) {
                    long tmpL = secondaryLongData[i][head];
                    secondaryLongData[i][head] = secondaryLongData[i][tail];
                    secondaryLongData[i][tail] = tmpL;
                }

                for (int i = 0; i < nSecFDataArrays; i++) {
                    float tmpF = secondaryFloatData[i][head];
                    secondaryFloatData[i][head] = secondaryFloatData[i][tail];
                    secondaryFloatData[i][tail] = tmpF;
                }

                head++;
                tail--;
            }

            len = head;

//            if ((secondaryIntData == null) || (secondaryIntData[0] == null) || (secondaryIntData.length == 0)
//                    || (secondaryIntData[0].length == 0)) {
//                System.out.println("break me"); // NOI18N
//            }

            if (secondaryIntData[0][len] != 0) {
                len++;
            }
        }

        (new StringSorter(sortedClassNames, 0, len) {
                protected void swap(int a, int b) {
                    super.swap(a, b);

                    int tmpI = sortedClassIds[a];
                    sortedClassIds[a] = sortedClassIds[b];
                    sortedClassIds[b] = tmpI;

                    for (int i = 0; i < nSecIDataArrays; i++) {
                        tmpI = secondaryIntData[i][a];
                        secondaryIntData[i][a] = secondaryIntData[i][b];
                        secondaryIntData[i][b] = tmpI;
                    }

                    for (int i = 0; i < nSecLDataArrays; i++) {
                        long tmpL = secondaryLongData[i][a];
                        secondaryLongData[i][a] = secondaryLongData[i][b];
                        secondaryLongData[i][b] = tmpL;
                    }

                    for (int i = 0; i < nSecFDataArrays; i++) {
                        float tmpF = secondaryFloatData[i][a];
                        secondaryFloatData[i][a] = secondaryFloatData[i][b];
                        secondaryFloatData[i][b] = tmpF;
                    }
                }
            }).sort(sortOrder);

        return len;
    }

    protected boolean truncateZeroItems() {
        return true;
    }

    // ---
    private void initFilterPanel() {
        filterComponent = FilterComponent.create(true, true);
        filterComponent.setHint(CLASS_NAME_FILTER);

        filterComponent.setFilter(filterString, filterType);
        filterComponent.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    String selectedRowContents = null;
                    int selectedRow = resTable.getSelectedRow();

                    if (selectedRow != -1) {
                        selectedRowContents = (String) resTable.getValueAt(selectedRow, 0);
                    }

                    filterString = filterComponent.getFilterValue();
                    filterType = filterComponent.getFilterType();
                    createFilteredIndexes();
                    resTable.invalidate();
                    jScrollPane.revalidate();
                    resTable.repaint();

                    if (selectedRowContents != null) {
                        resTable.selectRowByContents(selectedRowContents, 0, true);
                    }
                }
            });

        add(filterComponent.getComponent(), BorderLayout.SOUTH);
    }

    private boolean passesFilter(int idx, String filter) {
        String value = sortedClassNames[idx];

        if ("".equals(filter)) {
            return true; // NOI18N
        }

        // Case sensitive comparison:
        /*switch (type) {
           case CommonConstants.FILTER_STARTS_WITH:
             return value.startsWith(filter);
           case CommonConstants.FILTER_CONTAINS:
             return value.indexOf(filter) != -1;
           case CommonConstants.FILTER_ENDS_WITH:
             return value.endsWith(filter);
           case CommonConstants.FILTER_EQUALS:
             return value.equals(filter);
           case CommonConstants.FILTER_REGEXP:
             return value.matches(filter);
           }*/

        // Case insensitive comparison (except regexp):
        switch (filterType) {
//            case CommonConstants.FILTER_STARTS_WITH:
//                return value.regionMatches(true, 0, filter, 0, filter.length()); // case insensitive startsWith, optimized
            case CommonConstants.FILTER_CONTAINS:
                return value.toLowerCase().contains(filter); // case insensitive indexOf, NOT OPTIMIZED!!!
            case CommonConstants.FILTER_NOT_CONTAINS:
                return !value.toLowerCase().contains(filter);
//            case CommonConstants.FILTER_ENDS_WITH:
//                return value.regionMatches(true, value.length() - filter.length(), filter, 0, filter.length()); // case insensitive endsWith, optimized
//            case CommonConstants.FILTER_EQUALS:
//                return value.equalsIgnoreCase(filter); // case insensitive equals
            case CommonConstants.FILTER_REGEXP:
                try {
                    return value.matches(filter); //  case sensitive!
                } catch (java.util.regex.PatternSyntaxException e) {
                    return false;
                }
        }

        return false;
    }

    private boolean passesFilters(int idx) {
        if (filterType == CommonConstants.FILTER_NONE) {
            return true;
        }

        String[] filters = FilterComponent.getFilterValues(filterString);

        if (filters == null) {
            return true;
        }

        for (int i = 0; i < filters.length; i++) {
            if (passesFilter(idx, filters[i])) {
                return true;
            }
        }

        return false;
    }
}
