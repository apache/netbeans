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


package org.netbeans.modules.quicksearch;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.quicksearch.ProviderModel.Category;
import org.netbeans.modules.quicksearch.recent.RecentSearches;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * Component representing drop down for quick search
 * @author  Jan Becicka
 */
public class QuickSearchPopup extends javax.swing.JPanel 
        implements ListDataListener, ActionListener, TaskListener, Runnable {

    private static final String CUSTOM_WIDTH = "customWidth";           //NOI18N
    private static final int RESIZE_AREA_WIDTH = 5;
    private AbstractQuickSearchComboBar comboBar;

    private ResultsModel rModel;

    /* Rect to store repetitive bounds computation */
    private Rectangle popupBounds = new Rectangle();

    private Timer updateTimer;
    private static final int COALESCE_TIME = 300;

    /** text to search for */
    private String searchedText;

    private int catWidth;
    private int resultWidth;
    private int defaultResultWidth = -1;
    private int customWidth = -1;
    private int longestText = -1;
    private boolean canResize = false;
    private Task evalTask;
    private Task saveTask;
    private static final RequestProcessor RP = new RequestProcessor(QuickSearchPopup.class);
    private static final RequestProcessor evaluatorRP = new RequestProcessor(QuickSearchPopup.class + ".evaluator"); //NOI18N
    private static final Logger LOG = Logger.getLogger(QuickSearchPopup.class.getName());

    public QuickSearchPopup (AbstractQuickSearchComboBar comboBar) {
        this.comboBar = comboBar;
        initComponents();
        loadSettings();
        makeResizable();
        rModel = ResultsModel.getInstance();
        jList1.setModel(rModel);
        jList1.setCellRenderer(new SearchResultRender(this));
        rModel.addListDataListener(this);

        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            jList1.setBackground(QuickSearchComboBar.getResultBackground());

        updateStatusPanel(evalTask != null);
        setVisible(false);
    }

    void invoke() {
        ItemResult result = ((ItemResult) jList1.getModel().getElementAt(jList1.getSelectedIndex()));
        if (result != null) {
            RecentSearches.getDefault().add(result);
            result.getAction().run();
            if (comboBar.getCommand().isFocusOwner()) {
                // Needed in case the focus couldn't be returned to the caller,
                // see #228668.
                comboBar.getCommand().setText("");                      //NOI18N
            }
            clearModel();
        }
    }

    void selectNext() {
        int oldSel = jList1.getSelectedIndex();
        if (oldSel >= 0 && oldSel < jList1.getModel().getSize() - 1) {
            jList1.setSelectedIndex(oldSel + 1);
        }
        if (jList1.getModel().getSize() > 0) {
            setVisible(true);
        }
    }

    void selectPrev() {
        int oldSel = jList1.getSelectedIndex();
        if (oldSel > 0) {
            jList1.setSelectedIndex(oldSel - 1);
        }
        if (jList1.getModel().getSize() > 0) {
            setVisible(true);
        }
    }

    public JList getList() {
        return jList1;
    }

    public void clearModel () {
        rModel.setContent(null);
        longestText = -1;
    }

    public void maybeEvaluate (String text) {
        this.searchedText = text;
        if (text.length()>0) {
            updateStatusPanel(true);
            updatePopup(true);
        }

        if (updateTimer == null) {
            updateTimer = new Timer(COALESCE_TIME, this);
        }

        if (!updateTimer.isRunning()) {
            // first change in possible flurry, start timer
            updateTimer.start();
        } else {
            // text change came too fast, let's wait until user calms down :)
            updateTimer.restart();
        }
    }

    /** implementation of ActionListener, called by timer,
     * actually runs search */
    @Override
    public void actionPerformed(ActionEvent e) {
        updateTimer.stop();
        // search only if we are not cancelled already
        if (comboBar.getCommand().isFocusOwner()) {
            evaluatorRP.post(new Runnable() {

                @Override
                public void run() {
                    if (evalTask != null) {
                        evalTask.removeTaskListener(QuickSearchPopup.this);
                    }
                    evalTask = CommandEvaluator.evaluate(searchedText, rModel);
                    evalTask.addTaskListener(QuickSearchPopup.this);
                    // start waiting on all providers execution
                    RP.post(evalTask);
                }
            });
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        statusPanel = new javax.swing.JPanel();
        searchingSep = new javax.swing.JSeparator();
        searchingLabel = new javax.swing.JLabel();
        noResultsLabel = new javax.swing.JLabel();
        hintSep = new javax.swing.JSeparator();
        hintLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(QuickSearchComboBar.getPopupBorderColor()));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jList1.setFocusable(false);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jList1MousePressed(evt);
            }
        });
        jList1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jList1MouseDragged(evt);
            }
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jList1MouseMoved(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        statusPanel.setBackground(QuickSearchComboBar.getResultBackground());
        statusPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(searchingSep, gridBagConstraints);

        searchingLabel.setText(org.openide.util.NbBundle.getMessage(QuickSearchPopup.class, "QuickSearchPopup.searchingLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        statusPanel.add(searchingLabel, gridBagConstraints);

        noResultsLabel.setForeground(java.awt.Color.red);
        noResultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noResultsLabel.setText(org.openide.util.NbBundle.getMessage(QuickSearchPopup.class, "QuickSearchPopup.noResultsLabel.text")); // NOI18N
        noResultsLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(noResultsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(hintSep, gridBagConstraints);

        hintLabel.setBackground(QuickSearchComboBar.getResultBackground());
        hintLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        statusPanel.add(hintLabel, gridBagConstraints);

        add(statusPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

private void jList1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseMoved
    // toggle resize/default cursor
    if (evt.getX() < RESIZE_AREA_WIDTH) {
        QuickSearchPopup.this.setCursor(Cursor.getPredefinedCursor(
                Cursor.W_RESIZE_CURSOR));
    } else {
        QuickSearchPopup.this.setCursor(Cursor.getDefaultCursor());
    }
    // selection follows mouse move
    Point loc = evt.getPoint();
    int index = jList1.locationToIndex(loc);
    if (index == -1) {
        return;
    }
    Rectangle rect = jList1.getCellBounds(index, index);
    if (rect != null && rect.contains(loc)) {
        jList1.setSelectedIndex(index);
    }

}//GEN-LAST:event_jList1MouseMoved

private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
    if (!SwingUtilities.isLeftMouseButton(evt)) {
        return;
    }
    // mouse left button click works the same as pressing Enter key
    comboBar.invokeSelectedItem();

}//GEN-LAST:event_jList1MouseClicked

    private void jList1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseDragged
        QuickSearchPopup.this.processMouseMotionEvent(evt);
    }//GEN-LAST:event_jList1MouseDragged

    private void jList1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MousePressed
        QuickSearchPopup.this.processMouseEvent(evt);
    }//GEN-LAST:event_jList1MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hintLabel;
    private javax.swing.JSeparator hintSep;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel noResultsLabel;
    private javax.swing.JLabel searchingLabel;
    private javax.swing.JSeparator searchingSep;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables


    /*** impl of reactions to results data change */

    public void intervalAdded(ListDataEvent e) {
        updatePopup(evalTask != null);
    }

    public void intervalRemoved(ListDataEvent e) {
        updatePopup(evalTask != null);
    }

    public void contentsChanged(ListDataEvent e) {
        if (customWidth < 0) {
            if (rModel.getContent() == null) {
                longestText = -1;
                resultWidth = -1;
            } else {
                for (CategoryResult r : rModel.getContent()) {
                    for (ItemResult i : r.getItems()) {
                        int l = i.getDisplayName().length();
                        if (l > longestText) {
                            longestText = l;
                            resultWidth = -1;
                        }
                    }
                }
            }
        }
        updatePopup(evalTask != null);
    }

    /**
     * Updates size and visibility of this panel according to model content
     */
    public void updatePopup (boolean isInProgress) {
        updatePopup(isInProgress, true);
    }

    private void updatePopup (boolean isInProgress, boolean canRetry) {
        int modelSize = rModel.getSize();
        if (modelSize > 0 && jList1.getSelectedIndex()<0) {
            jList1.setSelectedIndex(0);
        }

        // plug this popup into layered pane if needed
        JLayeredPane lPane = JLayeredPane.getLayeredPaneAbove(comboBar);
        if (lPane == null) {
            // #162075 - return when comboBar not yet seeded in AWT hierarchy
            return;
        }
        if (!isDisplayable()) {
            lPane.add(this, new Integer(JLayeredPane.POPUP_LAYER + 1) );
        }

        boolean statusVisible = updateStatusPanel(isInProgress);

        try {
            computePopupBounds(popupBounds, lPane, modelSize);
        } catch (Exception e) { //sometimes the hack in computePopupBounds fails
            LOG.log(canRetry ? Level.INFO : Level.SEVERE, null, e);
            retryUpdatePopup(canRetry, isInProgress);
            return;
        }
        setBounds(popupBounds);

        // popup visibility constraints
        if ((modelSize > 0 || statusVisible) && comboBar.getCommand().isFocusOwner()) {
            if (modelSize > 0 && !isVisible()) {
                jList1.setSelectedIndex(0);
            }
            if (jList1.getSelectedIndex() >= modelSize) {
                jList1.setSelectedIndex(modelSize - 1);
            }
            if (explicitlyInvoked || !searchedText.isEmpty()) {
                setVisible(true);
            }
        } else {
            setVisible(false);
        }
        explicitlyInvoked = false;

        // needed on JDK 1.5.x to repaint correctly
        revalidate();
    }

    /**
     * Retry to update popup if something went wrong, but only if it is allowed.
     * See bug 205356.
     */
    private void retryUpdatePopup(boolean canRetry,
            final boolean isInProgress) {
        if (canRetry) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updatePopup(isInProgress, false); // do not retry again
                }
            });
        }
    }

    private boolean explicitlyInvoked = false;
    /** User actually pressed Ctrl-I; display popup even just for Recent Searches. */
    void explicitlyInvoked() {
        explicitlyInvoked = true;
    }

    public int getCategoryWidth () {
        if (catWidth <= 0) {
            catWidth = computeWidth(jList1, 20, 30);
        }
        return catWidth;
    }

    public int getResultWidth () {
        if (customWidth > 0) {
            return Math.max(customWidth, getDefaultResultWidth());
        } else {
            if (resultWidth <= 0) {
                resultWidth = computeWidth(
                        jList1, limit(longestText, 42, 128), 50);
            }
            return resultWidth;
        }
    }

    private int getDefaultResultWidth() {
        if (defaultResultWidth <= 0) {
            defaultResultWidth = computeWidth(jList1, 42, 50);
        }
        return defaultResultWidth;
    }

    private int limit(int value, int min, int max) {
        assert min <= max;
        return Math.min(max, Math.max(min, value));
    }

    public int getPopupWidth() {
        int maxWidth = this.getParent() == null
                ? Integer.MAX_VALUE : this.getParent().getWidth() - 10;
        return Math.min(getCategoryWidth() + getResultWidth() + 3, maxWidth);
    }

    /** Implementation of TaskListener, listen to when providers are finished
     * with their searching work
     */
    public void taskFinished(Task task) {
        evalTask = null;
        // update UI in ED thread
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    /** Runnable implementation, updates popup */
    public void run() {
        updatePopup(evalTask != null);
    }

    private void computePopupBounds (Rectangle result, JLayeredPane lPane, int modelSize) {
        Dimension cSize = comboBar.getSize();
        int width = getPopupWidth();
        Point location = new Point(cSize.width - width - 1, comboBar.getBottomLineY() - 1);
        if (SwingUtilities.getWindowAncestor(comboBar) != null) {
            location = SwingUtilities.convertPoint(comboBar, location, lPane);
        }
        result.setLocation(location);

        // hack to make jList.getpreferredSize work correctly
        // JList is listening on ResultsModel same as us and order of listeners
        // is undefined, so we have to force update of JList's layout data
        jList1.setFixedCellHeight(15);
        jList1.setFixedCellHeight(-1);
        // end of hack

        jList1.setVisibleRowCount(modelSize);
        Dimension preferredSize = jList1.getPreferredSize();

        preferredSize.width = width;
        preferredSize.height += statusPanel.getPreferredSize().height + 3;

        result.setSize(preferredSize);
    }

    /** Computes width of string up to maxCharCount, with font of given JComponent
     * and with maximum percentage of owning Window that can be taken */
    private static int computeWidth (JComponent comp, int maxCharCount, int percent) {
        FontMetrics fm = comp.getFontMetrics(comp.getFont());
        int charW = fm.charWidth('X');
        int result = charW * maxCharCount;
        // limit width to 50% of containing window
        Window w = SwingUtilities.windowForComponent(comp);
        if (w != null) {
            result = Math.min(result, w.getWidth() * percent / 100);
        }
        return result;
    }

    /** Updates visibility and content of status labels.
     *
     * @return true when update panel should be visible (some its part is visible),
     * false otherwise
     */
    private boolean updateStatusPanel (boolean isInProgress) {
        boolean shouldBeVisible = false;

        searchingSep.setVisible(isInProgress);
        searchingLabel.setVisible(isInProgress);
        if (comboBar instanceof QuickSearchComboBar) {
            if (isInProgress) {
                ((QuickSearchComboBar) comboBar).startProgressAnimation();
            } else {
                ((QuickSearchComboBar) comboBar).stopProgressAnimation();
            }
        }
        shouldBeVisible = shouldBeVisible || isInProgress;

        boolean searchedNotEmpty = searchedText != null && searchedText.trim().length() > 0;
        boolean areNoResults = rModel.getSize() <= 0 && searchedNotEmpty && !isInProgress;
        noResultsLabel.setVisible(areNoResults);
        comboBar.setNoResults(areNoResults);
        shouldBeVisible = shouldBeVisible || areNoResults;

        hintLabel.setText(getHintText());
        boolean isNarrowed = CommandEvaluator.isTemporaryCatSpecified() && searchedNotEmpty;
        hintSep.setVisible(isNarrowed);
        hintLabel.setVisible(isNarrowed);
        shouldBeVisible = shouldBeVisible || isNarrowed;

        return shouldBeVisible;
    }

    private String getHintText () {
        Category temp = CommandEvaluator.getTemporaryCat();
        if (temp != null) {
            return NbBundle.getMessage(QuickSearchPopup.class,
                    "QuickSearchPopup.hintLabel.text", //NOI18N
                    temp.getDisplayName(), SearchResultRender.getKeyStrokeAsText(
                    comboBar.getKeyStroke()));
        } else {
            return null;
        }
    }

    /**
     * Register listeners that make this pop-up resizable.
     */
    private void makeResizable() {
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (canResize) {
                    customWidth = Math.max(1, getResultWidth() - e.getX());
                    run();
                    saveSettings();
                }
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                canResize = e.getX() < RESIZE_AREA_WIDTH;
            }
        });
    }

    /**
     * Load settings from preferences file.
     */
    private void loadSettings() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Preferences p = NbPreferences.forModule(QuickSearchPopup.class);
                customWidth = p.getInt(CUSTOM_WIDTH, -1);
            }
        });
    }

    /**
     * Save settings to preferences file. Do nothing if this operation is
     * already scheduled.
     */
    private synchronized void saveSettings() {
        if (saveTask == null) {
            saveTask = RP.create(new Runnable() {
                @Override
                public void run() {
                    Preferences p = NbPreferences.forModule(
                            QuickSearchPopup.class);
                    p.putInt(CUSTOM_WIDTH, customWidth);
                    synchronized (QuickSearchPopup.this) {
                        saveTask = null;
                    }
                }
            });
            RP.post(saveTask, 1000);
        }
    }
}
