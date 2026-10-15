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

package org.netbeans.modules.debugger.ui.views.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Daniel Prusa
 */
public class InfoPanel extends javax.swing.JPanel {

    private static final int PANEL_HEIGHT = 40;

    private static final int FILTERS = 0;
    private static final int HITS = 1;
    private static final int DEADLOCKS = 2;
    private static final int DEADLOCKS_BY_DEBUGGER = 3;
    private static final int STEP_BRKP = 4;

    private Color hitsPanelColor;
    private Color deadlockPanelColor;
    private Color filterPanelColor;
    private Color stepBrkpColor;
    private int tapPanelMinimumHeight;
    private TapPanel tapPanel;
    private Item[] items;

    private JButton arrowButton;
    private JPopupMenu arrowMenu;
    private Map<DVThread, JMenuItem> threadToMenuItem = new WeakHashMap<DVThread, JMenuItem>();
    private DVThread debuggerDeadlockThread;
    private WeakReference<DVSupport> stepBrkpDVSupportRef;
    private DebuggingViewComponent debuggingView;

    /** Creates new form InfoPanel */
    public InfoPanel(TapPanel tapPanel, DebuggingViewComponent debuggingView) {
        this.tapPanel = tapPanel;
        this.debuggingView = debuggingView;
        filterPanelColor = tapPanel.getBackground();
        hitsPanelColor = DebuggingViewComponent.hitsColor;
        deadlockPanelColor = hitsPanelColor;
        stepBrkpColor = hitsPanelColor;
        tapPanelMinimumHeight = tapPanel.getMinimumHeight();

        initComponents();

        items = new Item[5];
        items[FILTERS] = new Item(filterPanelColor, PANEL_HEIGHT, createFilterToolBar()); // options and filters
        items[HITS] = new Item(hitsPanelColor, PANEL_HEIGHT, hitsInnerPanel); // breakpoint hits
        items[DEADLOCKS] = new Item(hitsPanelColor, PANEL_HEIGHT, deadlocksInnerPanel); // deadlock
        items[DEADLOCKS_BY_DEBUGGER] = new Item(deadlockPanelColor, PANEL_HEIGHT * 2, debuggerDeadlocksInnerPanel); // deadlock caused by debugger
        items[STEP_BRKP] = new Item(stepBrkpColor, PANEL_HEIGHT * 2, stepBrkpInnerPanel); // step interrupted by breakpoint)

        items[FILTERS].getPanel().setBorder(new EmptyBorder(1, 2, 1, 5)); // [TODO]

        arrowButton = createArrowButton();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        hitsInnerPanel.add(arrowButton, gridBagConstraints);

        removeAll();
        items[FILTERS].makeVisible(false, true, null);
        items[HITS].makeInvisible();
        items[DEADLOCKS].makeInvisible();
        items[DEADLOCKS_BY_DEBUGGER].makeInvisible();
        items[STEP_BRKP].makeInvisible();
        for (int x = items.length - 1; x >= 0; x--) {
            add(items[x].scrollPane);
            if (x > 0) {
                add(items[x].separator);
            }
        }
    }

    void clearBreakpointHits() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                arrowMenu.removeAll();
                threadToMenuItem.clear();
                hideHitsPanel();
            }
        });
    }

    void removeBreakpointHit(final DVThread thread, final int newHitsCount) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JMenuItem item = threadToMenuItem.remove(thread);
                if (item == null) {
                    return;
                }
                arrowMenu.remove(item);
                setHitsText(newHitsCount);
                if (newHitsCount == 0) {
                    hideHitsPanel();
                }
            }
        });
    }

    void addBreakpointHit(final DVThread thread, final int newHitsCount) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (threadToMenuItem.get(thread) != null) {
                    return;
                }
                JMenuItem item = createMenuItem(thread.getDVSupport(), thread);
                threadToMenuItem.put(thread, item);
                arrowMenu.add(item);
                setHitsText(newHitsCount);
                if (newHitsCount == 1) {
                    showHitsPanel();
                }
            }
        });
    }

    void setBreakpointHits(final DVSupport dvs, final List<DVThread> hits) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                arrowMenu.removeAll();
                threadToMenuItem.clear();
                for (DVThread thread : hits) {
                    JMenuItem item = createMenuItem(dvs, thread);
                    threadToMenuItem.put(thread, item);
                    arrowMenu.add(item);
                }
                if (hits.size() == 0) {
                    hideHitsPanel();
                } else {
                    setHitsText(hits.size());
                    showHitsPanel();
                }
            }
        });
    }

    public void recomputeMenuItems(final DVSupport dvs, final List<DVThread> hits) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                arrowMenu.removeAll();
                threadToMenuItem.clear();
                for (DVThread thread : hits) {
                    JMenuItem item = createMenuItem(dvs, thread);
                    threadToMenuItem.put(thread, item);
                    arrowMenu.add(item);
                }
            }
        });
    }

    private JMenuItem createMenuItem(final DVSupport dvs, final DVThread thread) {
        String displayName = dvs.getDisplayName(thread);
        Image image = dvs.getIcon(thread);
        Icon icon = image != null ? ImageUtilities.image2Icon(image) : null;
        JMenuItem item = new JMenuItem(displayName, icon);
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                debuggingView.makeThreadCurrent(thread);
            }
        });
        return item;
    }

    private void setHitsText(int hitsNumber) {
        String text;
        if (hitsNumber == 1) {
            text = NbBundle.getMessage(InfoPanel.class, "LBL_OneNewHit");
        } else {
            text = NbBundle.getMessage(InfoPanel.class, "LBL_NewHits", hitsNumber);
        }
        hitsLabel.setText(text);
    }

    void setShowDeadlock(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (visible) {
                    showDeadlocksPanel();
                } else {
                    hideDeadlocksPanel();
                }
            }
        });
    }

    void setShowThreadLocks(final DVThread thread, final List<DVThread> lockerThreads) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (lockerThreads != null) {
                    showDebuggerDeadlockPanel(thread, lockerThreads);
                } else {
                    hideDebuggerDeadlockPanel();
                }
            }
        });
    }

    void setShowStepBrkp(final DVSupport dvSupport, final DVThread thread, final Breakpoint breakpoint) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (breakpoint != null) {
                    showStepBrkpPanel(dvSupport, thread, breakpoint);
                } else {
                    hideStepBrkpPanel();
                }
            }
        });
    }

    // **************************************************************************

    private void hidePanel(int index) {
        Item item = items[index];
        if (!item.isVisible()) {
            return;
        }
        item.makeInvisible();
        boolean wasOnTop = true;
        for (int i = index + 1; i < items.length; i++) {
            if (items[i].isVisible()) {
                wasOnTop = false;
                break;
            } // if
        }  // for
        if (wasOnTop) {
            for (int i = index - 1; i >= 0; i--) {
                if (items[i].isVisible()) {
                    items[i].setTop(true);
                    break;
                } // if
            } // for
        } // if
    }

    private void showPanel(int index) {
        Item item = items[index];
        if (item.isVisible()) {
            return;
        }
        boolean isOnTop = true;
        for (int i = index + 1; i < items.length; i++) {
            if (items[i].isVisible()) {
                isOnTop = false;
                break;
            } // if
        }  // for
        Item previousTop = null;
        if (isOnTop) {
            for (int i = index - 1; i >= 0; i--) {
                if (items[i].isVisible()) {
                    previousTop = items[i];
                    break;
                } // if
            } // for
        } // if
        item.makeVisible(true, isOnTop, previousTop);
    }

    private void hideHitsPanel() {
        hidePanel(HITS);
    }

    private void showHitsPanel() {
        showPanel(HITS);
    }

    private void hideDeadlocksPanel() {
        hidePanel(DEADLOCKS);
    }

    private void showDeadlocksPanel() {
        showPanel(DEADLOCKS);
    }

    private void hideDebuggerDeadlockPanel() {
        hidePanel(DEADLOCKS_BY_DEBUGGER);
    }

    private void showDebuggerDeadlockPanel(DVThread thread, List<DVThread> lockerThreads) {
        //this.debuggerDeadlockThreads = lockerThreads;
        this.debuggerDeadlockThread = thread;
        String infoResource;
        String resumeResource;
        String resumeTooltipResource;
        int numThreads = lockerThreads.size();
        if (numThreads == 1) {
            if (thread.isInStep()) {
                infoResource = "InfoPanel.debuggerDeadlocksLabelThread.text"; // NOI18N
                resumeTooltipResource = "InfoPanel.resumeDebuggerDeadlockButtonThread.tooltip";
            } else {
                infoResource = "InfoPanel.debuggerDeadlocksLabelThread.Method.text"; // NOI18N
                resumeTooltipResource = "InfoPanel.resumeDebuggerDeadlockButtonThread.Method.tooltip"; // NOI18N
            }
            resumeResource = "InfoPanel.resumeDebuggerDeadlockLabelThread.text"; // NOI18N
            debuggerDeadlocksLabel.setToolTipText(null);
            resumeDebuggerDeadlockButton.setToolTipText(org.openide.util.NbBundle.getMessage(InfoPanel.class,
                    resumeTooltipResource, lockerThreads.get(0).getName()));
        } else {
            if (thread.isInStep()) {
                infoResource = "InfoPanel.debuggerDeadlocksLabel.text"; // NOI18N
                resumeTooltipResource = "InfoPanel.resumeDebuggerDeadlockButton.tooltip"; // NOI18N
            } else {
                infoResource = "InfoPanel.debuggerDeadlocksLabel.Method.text"; // NOI18N
                resumeTooltipResource = "InfoPanel.resumeDebuggerDeadlockButton.Method.tooltip"; // NOI18N
            }
            resumeResource = "InfoPanel.resumeDebuggerDeadlockLabel.text"; // NOI18N
            StringBuilder threadNamesBuilder = new StringBuilder(lockerThreads.get(0).getName());
            for (int i = 1; i < lockerThreads.size(); i++) {
                threadNamesBuilder.append(", ");
                threadNamesBuilder.append(lockerThreads.get(i).getName());
            }
            String threadNames = threadNamesBuilder.toString();
            debuggerDeadlocksLabel.setToolTipText(org.openide.util.NbBundle.getMessage(InfoPanel.class,
                    "InfoPanel.debuggerDeadlocksLabel.tooltip",
                    threadNames));
            resumeDebuggerDeadlockButton.setToolTipText(org.openide.util.NbBundle.getMessage(InfoPanel.class,
                    resumeTooltipResource, threadNames));
        }
        debuggerDeadlocksLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class,
                infoResource,
                lockerThreads.get(0).getName()));
        resumeDebuggerDeadlockLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class,
                resumeResource));
        if (items[DEADLOCKS].isVisible()) {
            // Show only if there is not a real deadlock.
            return;
        }
        showPanel(DEADLOCKS_BY_DEBUGGER);
    }

    private void hideStepBrkpPanel() {
        hidePanel(STEP_BRKP);
    }

    private void showStepBrkpPanel(DVSupport dvSupport, DVThread thread, Breakpoint breakpoint) {
        this.stepBrkpDVSupportRef = new WeakReference<DVSupport>(dvSupport);
        String text = org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.stepBrkpLabel.text", thread.getName()); // NOI18N
        stepBrkpLabel.setText(text);
        stepBrkpLabel.setToolTipText(text);
        showPanel(STEP_BRKP);
    }

    private JButton createArrowButton() {
        arrowMenu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(
            ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/unvisited_bpkt_arrow_small_16.png", false), arrowMenu);
        button.setPreferredSize(new Dimension(40, button.getPreferredSize().height)); // [TODO]
        button.setMaximumSize(new Dimension(40, button.getPreferredSize().height)); // [TODO]
        button.setFocusable(false);
        button.setOpaque(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (arrowMenu.getComponentCount() > 0) {
                    Object item = arrowMenu.getComponent(0);
                    for (Map.Entry<DVThread, JMenuItem> entry : threadToMenuItem.entrySet()) {
                        if (entry.getValue() == item) {
                            debuggingView.makeThreadCurrent(entry.getKey());
                        } // if
                    } // for
                } // if
            } // actionPerformed
        });
        return button;
    }

    private JToolBar createFilterToolBar() {
        final FiltersDescriptor filtersDesc = FiltersDescriptor.getInstance();
        // configure toolbar
        final JToolBar toolbar = new NoBorderToolBar();
        toolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolbar.setFloatable(false);
        //toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        toolbar.setOpaque(false);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            toolbar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        createFilterToolBarUI(toolbar, filtersDesc);
        filtersDesc.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        createFilterToolBarUI(toolbar, filtersDesc);
                    }
                });
            }
        });
        return toolbar;
    }
    
    private void createFilterToolBarUI(JToolBar toolbar, FiltersDescriptor filtersDesc) {
        toolbar.removeAll();
        // create toggle buttons
        int filterCount = filtersDesc.getFilterCount();
        ArrayList<JToggleButton> toggles = new ArrayList<JToggleButton>(filterCount);
        JToggleButton toggleButton = null;

        for (int i = 0; i < filterCount; i++) {
            toggleButton = createToggle(filtersDesc, i);
            toggles.add(toggleButton);
        }

        // add toggle buttons
        for (int i = 0; i < toggles.size(); i++) {
            JToggleButton curToggle = toggles.get(i);
            curToggle.addActionListener(new ToggleButtonActionListener(i));
            toolbar.add(curToggle);
            if (i != toggles.size() - 1) {
                toolbar.addSeparator(new Dimension(3, 0));
            }
        }
    }

    private static class ToggleButtonActionListener implements ActionListener {

        private int index;

        public ToggleButtonActionListener(int index) {
            this.index = index;
        }

        public void actionPerformed(ActionEvent e) {
            JToggleButton curToggle = (JToggleButton) e.getSource();
            FiltersDescriptor.getInstance().setSelected(index, curToggle.isSelected());
        }
    }

    private JToggleButton createToggle (FiltersDescriptor filtersDesc, int index) {
        boolean isSelected = filtersDesc.isSelected(index);
        Icon icon = filtersDesc.getSelectedIcon(index);
        // ensure small size, just for the icon
        JToggleButton toggleButton = new JToggleButton(icon, isSelected);
//        Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
//        toggleButton.setPreferredSize(size);
        toggleButton.setMargin(new Insets(2, 2, 2, 2));
        toggleButton.setToolTipText(filtersDesc.getTooltip(index));
        toggleButton.setFocusable(false);
        filtersDesc.connectToggleButton(index, toggleButton);
        return toggleButton;
    }

    private void resumeThreadToFreeMonitor(DVThread thread) {
        // Do not have monitor breakpoints in the API.
        // Have to do that in the implementation module.
        try {
            java.lang.reflect.Method resumeToFreeMonitorMethod = thread.getClass().getMethod("resumeBlockingThreads");
            resumeToFreeMonitorMethod.invoke(thread);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
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

        stepBrkpInnerPanel = new javax.swing.JPanel();
        infoIcon3 = new javax.swing.JLabel();
        stepBrkpLabel = new javax.swing.JLabel();
        stepBrkpIgnoreButton = new javax.swing.JButton();
        debuggerDeadlocksInnerPanel = new javax.swing.JPanel();
        infoIcon2 = new javax.swing.JLabel();
        debuggerDeadlocksLabel = new javax.swing.JLabel();
        resumeDebuggerDeadlockLabel = new javax.swing.JLabel();
        emptyPanel2 = new javax.swing.JPanel();
        resumeDebuggerDeadlockButton = new javax.swing.JButton();
        deadlocksInnerPanel = new javax.swing.JPanel();
        infoIcon1 = new javax.swing.JLabel();
        deadlocksLabel = new javax.swing.JLabel();
        emptyPanel1 = new javax.swing.JPanel();
        hitsInnerPanel = new javax.swing.JPanel();
        infoIcon = new javax.swing.JLabel();
        hitsLabel = new javax.swing.JLabel();
        emptyPanel = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        stepBrkpInnerPanel.setOpaque(false);
        stepBrkpInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/resources/debuggingView/info_big.png"))); // NOI18N
        infoIcon3.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        stepBrkpInnerPanel.add(infoIcon3, gridBagConstraints);

        stepBrkpLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.stepBrkpLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        stepBrkpInnerPanel.add(stepBrkpLabel, gridBagConstraints);

        stepBrkpIgnoreButton.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.stepBrkpIgnoreButton.text")); // NOI18N
        stepBrkpIgnoreButton.setToolTipText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.stepBrkpIgnoreButton.tooltip")); // NOI18N
        stepBrkpIgnoreButton.setMargin(new java.awt.Insets(0, 3, 0, 3));
        stepBrkpIgnoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepBrkpIgnoreButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        stepBrkpInnerPanel.add(stepBrkpIgnoreButton, gridBagConstraints);

        add(stepBrkpInnerPanel);

        debuggerDeadlocksInnerPanel.setOpaque(false);
        debuggerDeadlocksInnerPanel.setPreferredSize(new java.awt.Dimension(0, 16));
        debuggerDeadlocksInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/resources/wrong_pass.png"))); // NOI18N
        infoIcon2.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        debuggerDeadlocksInnerPanel.add(infoIcon2, gridBagConstraints);

        debuggerDeadlocksLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        debuggerDeadlocksLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.debuggerDeadlocksLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 9);
        debuggerDeadlocksInnerPanel.add(debuggerDeadlocksLabel, gridBagConstraints);

        resumeDebuggerDeadlockLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.resumeDebuggerDeadlockLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        debuggerDeadlocksInnerPanel.add(resumeDebuggerDeadlockLabel, gridBagConstraints);

        emptyPanel2.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        debuggerDeadlocksInnerPanel.add(emptyPanel2, gridBagConstraints);

        resumeDebuggerDeadlockButton.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.resumeDebuggerDeadlockButton.text")); // NOI18N
        resumeDebuggerDeadlockButton.setMargin(new java.awt.Insets(0, 3, 0, 3));
        resumeDebuggerDeadlockButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeDebuggerDeadlockButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 9);
        debuggerDeadlocksInnerPanel.add(resumeDebuggerDeadlockButton, gridBagConstraints);

        add(debuggerDeadlocksInnerPanel);

        deadlocksInnerPanel.setOpaque(false);
        deadlocksInnerPanel.setPreferredSize(new java.awt.Dimension(0, 16));
        deadlocksInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/resources/wrong_pass.png"))); // NOI18N
        infoIcon1.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        deadlocksInnerPanel.add(infoIcon1, gridBagConstraints);

        deadlocksLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        deadlocksLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.deadlocksLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        deadlocksInnerPanel.add(deadlocksLabel, gridBagConstraints);

        emptyPanel1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        deadlocksInnerPanel.add(emptyPanel1, gridBagConstraints);

        add(deadlocksInnerPanel);

        hitsInnerPanel.setOpaque(false);
        hitsInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/resources/debuggingView/info_big.png"))); // NOI18N
        infoIcon.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        hitsInnerPanel.add(infoIcon, gridBagConstraints);

        hitsLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.hitsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        hitsInnerPanel.add(hitsLabel, gridBagConstraints);

        emptyPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        hitsInnerPanel.add(emptyPanel, gridBagConstraints);

        add(hitsInnerPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void resumeDebuggerDeadlockButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeDebuggerDeadlockButtonActionPerformed
        //final List<DVThread> threadsToResume = debuggerDeadlockThreads;
        final DVThread blockedThread = debuggerDeadlockThread;
        RequestProcessor rp;
        try {
            DVSupport debugger = blockedThread.getDVSupport();
            rp = getRP(debugger);
            if (rp == null) {
                return ;
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return ;
        }
        rp.post(new Runnable() {
            public void run() {
                blockedThread.resumeBlockingThreads();
                //resumeThreadToFreeMonitor(blockedThread);
            }
        });
        hideDebuggerDeadlockPanel();
    }//GEN-LAST:event_resumeDebuggerDeadlockButtonActionPerformed

    private static RequestProcessor getRP(DVSupport debugger) {
        RequestProcessor rp;
        Session s = debugger.getSession();
        rp = s.lookupFirst(null, RequestProcessor.class);
        if (rp == null) {
            rp = new RequestProcessor(InfoPanel.class);
        }
        return rp;
    }

    private void stepBrkpIgnoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepBrkpIgnoreButtonActionPerformed
        if (stepBrkpDVSupportRef != null) {
            final DVSupport ds = stepBrkpDVSupportRef.get();
            if (ds != null) {
                RequestProcessor rp = getRP(ds);
                if (rp == null) {
                    return ;
                }
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ds.resume();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
    }//GEN-LAST:event_stepBrkpIgnoreButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel deadlocksInnerPanel;
    private javax.swing.JLabel deadlocksLabel;
    private javax.swing.JPanel debuggerDeadlocksInnerPanel;
    private javax.swing.JLabel debuggerDeadlocksLabel;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JPanel emptyPanel1;
    private javax.swing.JPanel emptyPanel2;
    private javax.swing.JPanel hitsInnerPanel;
    private javax.swing.JLabel hitsLabel;
    private javax.swing.JLabel infoIcon;
    private javax.swing.JLabel infoIcon1;
    private javax.swing.JLabel infoIcon2;
    private javax.swing.JLabel infoIcon3;
    private javax.swing.JButton resumeDebuggerDeadlockButton;
    private javax.swing.JLabel resumeDebuggerDeadlockLabel;
    private javax.swing.JButton stepBrkpIgnoreButton;
    private javax.swing.JPanel stepBrkpInnerPanel;
    private javax.swing.JLabel stepBrkpLabel;
    // End of variables declaration//GEN-END:variables

    public class Item {
        private Color backgroundColor;
        private int preferredHeight;
        private JPanel topGapPanel;
        private JPanel bottomGapPanel;
        private JPanel outerPanel;
        private JComponent innerPanel;
        private JScrollPane scrollPane;
        private JPanel separator;
        private boolean animationRunning = false;
        private boolean isTop = false;

        Item(Color backgroundColor, int preferredHeight, JComponent innerPanel) {
            this.backgroundColor = backgroundColor;
            this.preferredHeight = preferredHeight;
            this.innerPanel = innerPanel;
            topGapPanel = createGapPanel();
            bottomGapPanel = createGapPanel();
            separator = createSeparator();
            outerPanel = new JPanel();
            outerPanel.setBackground(backgroundColor);
            outerPanel.setLayout(new BorderLayout());
            outerPanel.add(BorderLayout.NORTH, topGapPanel);
            outerPanel.add(BorderLayout.CENTER, innerPanel);
            outerPanel.add(BorderLayout.SOUTH, bottomGapPanel);
            outerPanel.setPreferredSize(new Dimension(0, preferredHeight));
            scrollPane = new JScrollPane();
            scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            scrollPane.setPreferredSize(new Dimension(0, preferredHeight));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setViewportView(outerPanel);
        }

        public JPanel getPanel() {
            return outerPanel;
        }

        public boolean isVisible() {
            return scrollPane.isVisible() || animationRunning;
        }

        private JPanel createGapPanel() {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setPreferredSize(new java.awt.Dimension(0, tapPanelMinimumHeight));
            return panel;
        }

        private JPanel createSeparator() {
            JPanel panel = new JPanel();
            panel.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
            panel.setMaximumSize(new java.awt.Dimension(32767, 1));
            panel.setMinimumSize(new java.awt.Dimension(10, 1));
            panel.setPreferredSize(new java.awt.Dimension(0, 1));
            return panel;
        }

        private synchronized void makeInvisible() {
            scrollPane.setVisible(false);
            separator.setVisible(false);
            if (animationRunning) {
                animationRunning = false;
            }
            setTop(isTop);
        }

        private synchronized void makeVisible(boolean animate, final boolean top, final Item lastTop) {
            if (animationRunning) {
                return;
            }
            int height = top ? preferredHeight - tapPanelMinimumHeight : preferredHeight;
            if (!animate) {
                setTop(top);
                if (top && lastTop != null) {
                    lastTop.setTop(false);
                }
                scrollPane.setPreferredSize(new Dimension(0, height));
                outerPanel.setPreferredSize(new Dimension(0, height));
                scrollPane.setVisible(true);
                separator.setVisible(true);
            } else {
                scrollPane.setPreferredSize(new Dimension(0, 1));
                outerPanel.setPreferredSize(new Dimension(0, height));
                animationRunning = true;
                isTop = top;
                if (isTop && lastTop != null) {
                    lastTop.setTop(false);
                }
                topGapPanel.setVisible(!isTop);
                if (animationRunning) {
                    scrollPane.setVisible(true);
                    separator.setVisible(true);
                    tapPanel.revalidate();
                }
                if (isTop) {
                    tapPanel.setBackground(backgroundColor);
                }
                int delta = 1;
                int currHeight = 1;
                Timer animationTimer = new Timer(20, null);
                animationTimer.addActionListener(new AnimationTimerListener(animationTimer, delta, currHeight));
                animationTimer.setCoalesce(false);
                animationTimer.start();
            } // else
        }

        private class AnimationTimerListener implements ActionListener {

            private int delta;
            private int currHeight;
            private Timer animationTimer;
            private long time = 0l;

            public AnimationTimerListener(Timer animationTimer, int delta, int currHeight) {
                this.delta = delta;
                this.currHeight = currHeight;
                this.animationTimer = animationTimer;
            }

            public void actionPerformed(ActionEvent e) {
                long now = System.nanoTime();
                int step;
                if (time > 0) {
                    // Do bigger step if time is running out...
                    step = delta*((int) (now - time)/(animationTimer.getDelay()*1000000) + 1);
                    //System.err.println("interval = "+(now-time)/1000000+"step = "+step);
                } else {
                    step = delta;
                }
                time = now;
                currHeight += step;
                int height = isTop ? preferredHeight - tapPanelMinimumHeight : preferredHeight;
                if (currHeight > height) {
                    currHeight = height;
                }
                scrollPane.setPreferredSize(new Dimension(0, currHeight));
                //System.err.println("currHeight = "+currHeight);
                revalidate();
                doLayout();
                if (currHeight >= (isTop ? preferredHeight - tapPanelMinimumHeight : preferredHeight)) {
                    animationTimer.stop();
                    synchronized (Item.this) {
                        animationRunning = false;
                    }
                }
            }
        }

        private synchronized void setTop(boolean isTop) {
            this.isTop = isTop;
            if (isTop) {
                topGapPanel.setVisible(false);
                if (!animationRunning) {
                    outerPanel.setPreferredSize(new Dimension(0, preferredHeight - tapPanelMinimumHeight));
                    scrollPane.setPreferredSize(new Dimension(0, preferredHeight - tapPanelMinimumHeight));
                }
                tapPanel.setBackground(backgroundColor);
            } else {
                topGapPanel.setVisible(true);
                outerPanel.setPreferredSize(new Dimension(0, preferredHeight));
                scrollPane.setPreferredSize(new Dimension(0, preferredHeight));
            }
        }

    }

    private static class NoBorderToolBar extends JToolBar {

        public NoBorderToolBar() {
        }

        /** Creates a new instance of NoBorderToolbar
         * @param layout
         */
        public NoBorderToolBar( int layout ) {
            super( layout );
        }

        @Override
        protected void paintComponent(Graphics g) {
        }
    }

}
