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


package org.netbeans.modules.properties;


import java.awt.event.*;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * FindPerformer is a performer of the FindAction which is invoked on Resource Bundles table view component.
 * Does actual dirty job, search on the actual activated table and sets the results as highlighted text on particular cell.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 */
public class FindPerformer extends javax.swing.AbstractAction
                           implements PropertyChangeListener {

    /** Table on which perform the search. */
    private JTable table;
    
    /** String to find. */
    private String findString;
    
    /** Stores values which are used to start search from and store the results for next search. 
     * 1st item - row index of cell with found string,
     * 2nd item - column index of cell with found string,
     * 3rd item - start offset of found string.
     * 4th item - end offset of found string.
     */
    private int[] searchValues;

    /** Flag if it is set highliht search. */
    private boolean highlightSearch = true;
    
    /** Flag if it is set match case search. */
    private boolean matchCaseSearch = false;
    
    /** Flag if it is set forward search. */
    private boolean backwardSearch = false;
    
    /** Flag if it is set wrap search. */
    private boolean wrapSearch = true;
    
    /** Flag if it is set search by rows. */
    private boolean rowSearch = true;
    
    /** Listener for registering keystrokes to find next action. */
    private final ActionListener findNextActionListener;
    
    /** Listener for registering keystrokes to find previous action. */
    private final ActionListener findPreviousActionListener;
    
    /** Listener for registering keystrokes to toggle highlight action. */
    private final ActionListener toggleHighlightListener;
    
    /** Keeps history of found strings. */
    private Set<String> history = new HashSet<String>();

    /** Helper variable keeping <code>settings</code>. */
    private TableViewSettings settings;

    // Initializes action listener use to register for key strokes to table.
    {
        findNextActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // Find next action invoked (default F3) -> search next occurence.
                if(searchValues != null) {
                    synchronized(this) {
                        backwardSearch = false;
                        performSearch();
                    }
                }
            }
        };
        findPreviousActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // Find previous action invoked (default Shift-F3)-> search previous occurence.
                if(searchValues != null) {
                    synchronized(this) {
                        backwardSearch = true;
                        performSearch();
                    }
                }
            }
        };

        toggleHighlightListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // Toggle highlight action invoked (defauilt Shift-Alt-H) -> toggle highlight.
                highlightSearch = !highlightSearch;
                table.repaint();
            }
        };
    } // end of initializer
    
    /** Soft reference for caching singleton find performer on last table view. */
    private static SoftReference<FindPerformer> softRef;
    
    /** Dialog for perform search. */
    private static JDialog findDialog;
    
    /** Name of client property used to store search result values. */
    public static final String TABLE_SEARCH_RESULT = "table.search.result"; // NOI18N
    
    
    /** Creates new FindPerformer. Used internally only, for getting the singleton use #getFindPerformer method. */
    private  FindPerformer(JTable table) {
        this.table = table;
        
        settings = TableViewSettings.getDefault();
        settings.addPropertyChangeListener(
            WeakListeners.propertyChange(this, settings)
        );
        
                    registerKeyStrokes();
                }
            
        
    /**
     * Listen on setting changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // Settings were changed reset registered key strokes.
        registerKeyStrokes();
    }
    
    /** Gets find performer. */
    public static FindPerformer getFindPerformer(JTable table) {
        if(softRef != null) {
            FindPerformer fp = softRef.get();
            if(fp != null) {
                if(!fp.validateTable(table)) {
                    fp.resetTable(table);
                    fp.registerKeyStrokes();
                }
                return fp;
            }
        }
        
        FindPerformer fp = new FindPerformer(table);
        softRef = new SoftReference<FindPerformer>(fp);
        
        return fp;
    }
    
    /** Resets the table if necessary. */
    private void resetTable(JTable table) {
        this.table = table;
    }
    
    /** Validates if the table is the same one as last opened find panel. */
    private boolean validateTable(JTable table) {
        if(this.table != null && this.table.equals(table))
            return true;
        
        return false;
    }
    
    /** Register key strokes F3 and Shift-F3 (next & previous search) to table. */
    private synchronized void registerKeyStrokes() {
        // Register key strokes to table.
        KeyStroke[] keyStrokes = settings.getKeyStrokesFindNext();
        for(int i=0; i<keyStrokes.length; i++) {
            table.registerKeyboardAction(
                findNextActionListener,
                keyStrokes[i],
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }

        keyStrokes = settings.getKeyStrokesFindPrevious();
        for(int i=0; i<keyStrokes.length; i++) {
            table.registerKeyboardAction(
                findPreviousActionListener,
                keyStrokes[i],
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }

        keyStrokes = settings.getKeyStrokesToggleHighlight();
        for(int i=0; i<keyStrokes.length; i++) {
            table.registerKeyboardAction(
                toggleHighlightListener,
                keyStrokes[i],
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }
    }
    
    /** Getter for find string. */
    public String getFindString() {
        return findString;
    }
    
    /** Getter for highlight search flag. */
    public boolean isHighlightSearch() {
        return highlightSearch;
    }

    /* implements interface javax.swing.Action */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if(findDialog == null)
            createFindDialog();
        else {
            // There is already the find dialog open.
            findDialog.setVisible(true);
            findDialog.requestFocusInWindow();
        }
    }

    /** Methods which does the dirty job. It creates and shows find dialog. */
    private void createFindDialog() {
        final JDialog dialog;
        final FindPanel panel = new FindPanel();
        DialogDescriptor dd = new DialogDescriptor(
            panel,
            NbBundle.getBundle(FindPerformer.class).getString("LBL_Title"), // title
            false, // modal
            new JButton[0], // empty options, we provide our owns
            null, // initvalue
            DialogDescriptor.DEFAULT_ALIGN,
            null, // helpCtx
            null // listener
        );

        dialog = (javax.swing.JDialog)DialogDisplayer.getDefault().createDialog(dd);

        // Static reference to the dialog.
        findDialog = dialog;

        // set findButton as default
        dialog.getRootPane().setDefaultButton(panel.getButtons()[0]);
        dialog.setFocusable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // set listeners
        // find button
        panel.getButtons()[0].addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    findString = ((JTextField)panel.getComboBox().getEditor().getEditorComponent()).getText();

                    // Set flags.
                    highlightSearch = panel.getHighlightCheck().isSelected();
                    matchCaseSearch = panel.getMatchCaseCheck().isSelected();
                    backwardSearch = panel.getBackwardCheck().isSelected();
                    wrapSearch = panel.getWrapCheck().isSelected();
                    rowSearch = panel.getRowCheck().isSelected();
                    
                    dialog.dispose();
                    
                    if(findString != null && !findString.trim().equals("")) {

                        history.add(findString.intern());
                        
                        performSearch();
                    }
                    
                    // For case previous search results need to be erased.
                    table.repaint();
                }
            }
        );

        // cancel button
        panel.getButtons()[1].addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    dialog.dispose();
                }
            }
        );
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                findDialog = null;
            }
        });
        
        // Set flags.
        panel.getHighlightCheck().setSelected(highlightSearch);
        panel.getMatchCaseCheck().setSelected(matchCaseSearch);
        panel.getBackwardCheck().setSelected(backwardSearch);
        panel.getWrapCheck().setSelected(wrapSearch);
        panel.getRowCheck().setSelected(rowSearch);

        // Set combo box list items.
        panel.getComboBox().setModel(new DefaultComboBoxModel(history.toArray()));
        // Set last found string as selected if exist.
        if (findString != null) {
            panel.getComboBox().setSelectedItem(findString);
            panel.getComboBox().getEditor().selectAll();
        }

        dialog.setVisible(true);
        panel.requestFocusInWindow();
    }
    
    /** Closes find dialog if one is opened. */
    public void closeFindDialog() {
        if(findDialog != null) {
            synchronized(findDialog) {
                findDialog.setVisible(false);
                findDialog.dispose();
                findDialog = null;
            }
        }
    }

    /** Prepares searchValues before search. The search will start
     * from selected cell or from beginning by forward or 
     * from end by backward search respectivelly.*/
    private void prepareSearch() {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();

        // Is selected the cell found by previous search.
        if(searchValues != null && row == searchValues[0] && column == searchValues[1])
            return;
        

        if(row != -1 && column != -1) {
            // Some cell is selected.
            int startOffset, endOffset;
            startOffset = endOffset = ((JTextField)((PropertiesTableCellEditor)table.getCellEditor(row, column)).getComponent()).getCaretPosition();
            
            searchValues = new int[] {row, column, startOffset, endOffset};
        } else {
            // Nothing is selected, set first (last) cell by forward (backward) search.
            if(backwardSearch) {
                int lastRow = table.getRowCount()-1;
                int lastColumn = table.getColumnCount()-1;
                int startOffset, endOffset;
                
                startOffset = endOffset = ((PropertiesTableModel.StringPair)table.getValueAt(lastRow, lastColumn)).getValue().length()-1;

                searchValues = new int[] {lastRow, lastColumn, startOffset, endOffset};
            } else {
                searchValues = new int[] {0, 0, 0, 0};
            }
        }
        
    }
    
    /** Perform search, store results and set editable cell if search was successful. */
    private synchronized void performSearch() {
        prepareSearch();
        // perform search not in AWT-thread
        PropertiesRequestProcessor.getInstance().post(
            new Runnable() {
                public void run() {
                    // Do wrap search?
                    boolean wrap = false;
                    
                    do {
                        final int[] result = search(searchValues[0], searchValues[1], backwardSearch ? searchValues[2]-1 : searchValues[3]);
                        
                        if(wrapSearch && !wrap && result == null) {
                            // Do wrapping.
                            
                            // Wrap search if was found something in second to last search.
                            if(backwardSearch) {
                                int lastRow = table.getRowCount()-1;
                                int lastColumn = table.getColumnCount()-1;

                                searchValues = new int[] {lastRow,
                                    lastColumn,
                                    ((PropertiesTableModel.StringPair)table.getValueAt(lastRow, lastColumn)).getValue().length()-1,
                                    0};
                            } else {
                                searchValues = new int[] {0, 0, 0, 0};
                            }
 
                            wrap = true;
                        } else {
                            if(result != null) {
                                // store new search results
                                searchValues = result;

                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        // autoscroll to cell if possible and necessary
                                        if (table.getAutoscrolls()) { 
                                            Rectangle cellRect = table.getCellRect(result[0], result[1], false);
                                            if (cellRect != null) {
                                                table.scrollRectToVisible(cellRect);
                                            }
                                        }
                                        // update selection & edit
                                        if (table.isEditing()) {
                                            /*
                                             * If the cell at (result[1], result[0]) is currently being edited,
                                             * method setSelectionInterval does not finish the current editing
                                             * session and the selection is not changed. But the result
                                             * of calling setSelectionInterval(...) is that method
                                             * BundleEditPanel.updateSelection(...) is called which sets value
                                             * and comment from the cell at (result[1], result[0]).
                                             * The consequence of these steps is that values from
                                             * cell (result[1], result[0]) are set to the cell currently being
                                             * edited - see bug #81934
                                             * (http://www.netbeans.org/issues/show_bug.cgi?id=81934).
                                             */
                                            table.getCellEditor().stopCellEditing();
                                        }
                                        table.getColumnModel().getSelectionModel().setSelectionInterval(result[1], result[1]);
                                        table.getSelectionModel().setSelectionInterval(result[0], result[0]);
                                        // set editable cell
                                        table.editCellAt(result[0], result[1]);
                                    }
                                });
                            }
                            
                            // Store new search results.
                            searchValues = result;

                            wrap = false;
                        }
                    } while (wrap);
                } // end of run method                    
            }
        );
    }
    
    /** Perform actual search on table started from the cell specified.
     * @param startColumn Ccolumn index of cell to start the search.
     * @param startRow Row index of cell to start the search. 
     * @param startOffset Offset from start the search.
     * @return srray of integers where first is column index, second row index, third offset,
     *         in case no thing was found null is returned */
    private int[] search(int startRow, int startColumn, int startOffset) {
        // Helper for reseting startOffset after first iteration.
        boolean firstIteration = true;
        
        // If rowSearch->row loop else->column loop.
        for(int i= rowSearch ? startRow : startColumn; 
                backwardSearch ?  i>=0 : i<(rowSearch ? table.getRowCount() : table.getColumnCount()); 
                i = backwardSearch ? i-1 : i+1 ) {
            // If rowSearch->column loop else->row loop.
            for(int j= rowSearch ? startColumn : startRow; 
                    backwardSearch ?  j>=0 : j<(rowSearch ? table.getColumnCount() : table.getRowCount()); 
                    j = backwardSearch ? j-1 : j+1) {
                // Set row and column indexes for this iteration.        
                int row = rowSearch ? i : j;
                int column = rowSearch ? j : i;
                
                String str = ((PropertiesTableModel.StringPair)table.getValueAt(row, column)).toString();
                // Skip to next iteration if value is null or is the string in cell is shorter than string to find.
                if(str == null || str.length() < findString.length())
                    continue;
                
                if(!firstIteration)
                    startOffset = backwardSearch ? str.length()-findString.length() : 0;
                
                int offset = containsFindString(str, startOffset);
                
                if(offset>=0) {
                    // puts client property which is then used by cell editor
                    // for setting and highlighting the find string
                    table.putClientProperty(TABLE_SEARCH_RESULT, new int[] {row, column, offset, offset+findString.length()});
                    return new int[] {row, column, offset, offset+findString.length()};
                }
                
                if(firstIteration) firstIteration = false;
            }
            
            // Next inner loop from beginning(end) for forward (backward) search.
            if(rowSearch)
                startColumn = backwardSearch ? table.getColumnCount()-1 : 0;
            else 
                startRow = backwardSearch ? table.getRowCount()-1 : 0;
        }
        
        return null;
    }
    
    /** The function search if findString occures whitin specified string.
     * @param str String which is looked if contains find string.
     * @param startOffset Offset from starts the search.
     * @return Offset on which starts find string whitin str or -1. */
    private int containsFindString(String str, int startOffset) {
        if(startOffset < 0 || startOffset >= str.length())
            return -1;
        
        for(int i=startOffset;
                backwardSearch ?  i>=0 : i<(str.length()-findString.length()+1);
                i = backwardSearch ? i-1 : i+1) {
                    
            if(findString.regionMatches(!matchCaseSearch, 0, str, i, findString.length()))
                return i;
        }
        
        return -1;
    }

}
