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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormLAF;
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormModelEvent;
import org.netbeans.modules.form.FormModelListener;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADComponentNode;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.netbeans.modules.form.actions.TestAction;
import org.netbeans.modules.form.fakepeer.FakePeerContainer;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AddGapsAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.RemoveGapsAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DesignContainerAction;
import org.openide.awt.AcceleratorBinding;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Grid designer.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class GridDesigner extends JPanel {
    /** Color of the selection. */
    public static final Color SELECTION_COLOR = FormLoaderSettings.getInstance().getSelectionBorderColor();
    /** Image of the resizing handle. */
    public static final Image RESIZE_HANDLE = ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/resize_handle.png", false).getImage(); // NOI18N
    /** The "main" panel of the designer. */
    private JPanel innerPane;
    /** Glass pane of the designer. */
    private GlassPane glassPane;
    /** Replicator used to clone components for the designer. */
    private VisualReplicator replicator;
    /** GridManager. */
    private GridManager gridManager;
    /** Property sheet. */
    private PropertySheet sheet;
    /** Grid customizer (part of the panel on the left side). */
    private GridCustomizer customizer;
    /** Form model. */
    private FormModel formModel;
    
    /** Default gap column width */
    private static final int DEFAULT_GAP_WIDTH = 5;
    /** Default gap row height */
    private static final int DEFAULT_GAP_HEIGHT = 5;
    /** Gap support switch. */
    private JToggleButton gapButton;
    /** Spinner allowing to set gap width if gap support is enabled. */
    private JSpinner gapWidthSpinner;
    /** Spinner allowing to set gap Height if gap support is enabled. */
    private JSpinner gapHeightSpinner;
    /** gapWidthSpinner icon label. */
    private JLabel gapWidthSpinnerLabel;
    /** gapHeightSpinner icon label. */
    private JLabel gapHeightSpinnerLabel;
    /** gapWidthSpinner rigid area. */
    private Component gapWidthSpinnerBox;
    /** gapHeightSpinner rigid area. */
    private Component gapHeightSpinnerBox;
    
    /**
     * Sets the designer container.
     * 
     * @param metaContainer designer container.
     */
    public void setDesignedContainer(RADVisualContainer metaContainer) {
        removeAll();
        formModel = metaContainer.getFormModel();
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane();
        innerPane = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        innerPane.setLayout(new OverlayLayout(innerPane));
        glassPane = new GlassPane(this);
        glassPane.setOpaque(false);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        UndoRedoSupport support = UndoRedoSupport.getSupport(formModel);
        support.reset(glassPane);
        JButton undoButton = initUndoRedoButton(toolBar.add(support.getUndoAction()));
        toolBar.add(undoButton);
        JButton redoButton = initUndoRedoButton(toolBar.add(support.getRedoAction()));
        toolBar.add(redoButton);
        setupUndoRedoShortcut(true, support.getUndoAction());
        setupUndoRedoShortcut(false, support.getRedoAction());
        toolBar.add(Box.createRigidArea(new Dimension(15,10)));

        gapButton = initGapButton();
        toolBar.add(gapButton);
        toolBar.add(Box.createRigidArea(new Dimension(10,10)));
        gapWidthSpinnerLabel = new JLabel();
        gapWidthSpinnerLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gap_width.png", false)); // NOI18N
        toolBar.add(gapWidthSpinnerLabel);
        gapWidthSpinner = initGapWidthSpinner();
        toolBar.add(gapWidthSpinner);
        gapWidthSpinnerBox = Box.createRigidArea(new Dimension(5,10));
        toolBar.add(gapWidthSpinnerBox);
        gapHeightSpinnerLabel = new JLabel();
        gapHeightSpinnerLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gap_height.png", false)); // NOI18N
        toolBar.add(gapHeightSpinnerLabel);
        gapHeightSpinner = initGapHeightSpinner();
        toolBar.add(gapHeightSpinner);
        gapHeightSpinnerBox = Box.createRigidArea(new Dimension(15,10));
        toolBar.add(gapHeightSpinnerBox);
        
        JToggleButton padButton = initPaddingButton();
        toolBar.add(padButton);
        toolBar.add(Box.createRigidArea(new Dimension(10,10)));
        initPreviewButton(toolBar, metaContainer);
        toolBar.add(Box.createRigidArea(new Dimension(10,10)));
        toolBar.add(Box.createGlue());
        rightPanel.add(toolBar, BorderLayout.PAGE_START);
        // Estimate of the size of the header
        Dimension headerDim = new JLabel("99").getPreferredSize(); // NOI18N
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportView(innerPane);
        scrollPane.setPreferredSize(new Dimension(500,500));
        int unitIncrement = headerDim.height;
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(unitIncrement);
        rightPanel.add(scrollPane);
        splitPane.setRightComponent(rightPanel);
        add(splitPane);
        replicator = new VisualReplicator(true, FormUtils.getViewConverters(), FormEditor.getBindingSupport(formModel));
        replicator.setTopMetaComponent(metaContainer);
        final Object[] bean = new Object[1];
        // Create the cloned components in the correct look and feel setup
        FormLAF.executeWithLookAndFeel(formModel, new Runnable() {
            @Override
            public void run() {
                bean[0] = (Container)replicator.createClone();
            } 
        });        
        Container container = metaContainer.getContainerDelegate(bean[0]);
        if (container.isPreferredSizeSet()) { // Issue 211635
            container.setPreferredSize(null);
        }
        innerPane.removeAll();
        JPanel mainPanel = new JPanel();
        Color defaultBackground = UIManager.getColor( "Tree.background" ); //NOI18N
        if( null == defaultBackground )
            defaultBackground = Color.white;
        mainPanel.setBackground(defaultBackground);
        GroupLayout layout = new GroupLayout(mainPanel);
        layout.setHonorsVisibility(false);
        GroupLayout.Group hGroup = layout.createSequentialGroup()
                .addGap(3*GlassPane.HEADER_GAP+headerDim.width)
                .addComponent(container, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setHorizontalGroup(hGroup);
        GroupLayout.Group vGroup = layout.createSequentialGroup()
                .addGap(2*GlassPane.HEADER_GAP+headerDim.height)
                .addComponent(container, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setVerticalGroup(vGroup);
        mainPanel.setLayout(layout);
        glassPane.setPanes(innerPane, container);
        configureGridManager();
        splitPane.setLeftComponent(initLeftColumn());
        innerPane.add(glassPane);
        FakePeerContainer fakePeerContainer = new FakePeerContainer();
        fakePeerContainer.setLayout(new BorderLayout());
        fakePeerContainer.setBackground(mainPanel.getBackground());
        fakePeerContainer.setFont(FakePeerSupport.getDefaultAWTFont());
        fakePeerContainer.add(mainPanel);
        innerPane.add(fakePeerContainer);
        
        boolean gapSupport = gridManager.getGridInfo().hasGaps();
        updateGapProperties(gapSupport);
        updateGapControls(gapSupport);

        addFormModelListener();

        HelpCtx.setHelpIDString(this, "gui.layouts.griddesigner"); // NOI18N
    }

    private void setupUndoRedoShortcut(boolean undo, Action ourAction) {
        // try to obtain the same shortcut for undo/redo as defined in the IDE
        KeyStroke ks = null;
        FileObject undoRedoFO = FileUtil.getConfigFile(undo ? "Menu/Edit/org-openide-actions-UndoAction.shadow" : "Menu/Edit/org-openide-actions-RedoAction.shadow"); // NOI18N
        if (undoRedoFO != null) {
            Class<? extends SystemAction> undoClass = undo ? org.openide.actions.UndoAction.class : org.openide.actions.RedoAction.class;
            ContextAwareAction undoRedoAction = (ContextAwareAction)SystemAction.get(undoClass);
            Action a = undoRedoAction.createContextAwareInstance(Lookup.EMPTY);
            AcceleratorBinding.setAccelerator(a, undoRedoFO);
            Object ksObj = a.getValue(Action.ACCELERATOR_KEY);
            if (ksObj instanceof KeyStroke) {
                ks = (KeyStroke) ksObj;
            }
        }
        if (ks == null) { // fallback to Ctrl-Z/Ctrl-Y
            ks = Utilities.stringToKey(undo ? "D-Z" : "D-Y"); // NOI18N
        }
        String actionId = ourAction.getClass().getName();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, actionId);
        getActionMap().put(actionId, ourAction);
    }

    public void cleanup() {
        setSelectedNodes(Collections.EMPTY_LIST);
        removeFormModelListener();
    }

    /**
     * Updates the gap toggle button and gap width/height spinners state.
     */
    private void updateGapControls(boolean gapSupport) {
        gapButton.setSelected(gapSupport);
        gapWidthSpinner.setEnabled(gapSupport);
        gapHeightSpinner.setEnabled(gapSupport);
        gapWidthSpinnerLabel.setEnabled(gapSupport);
        gapHeightSpinnerLabel.setEnabled(gapSupport);
        gapWidthSpinnerBox.setEnabled(gapSupport);
        gapHeightSpinnerBox.setEnabled(gapSupport);
        if(gapSupport) {
            gapButton.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.gapSupportDisable")); // NOI18N
        } else {
            gapButton.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.gapSupportEnable")); // NOI18N
        }
    }
 
    /**
     * Configures the appropriate {@code GridManager}.
     */
    private void configureGridManager() {
        gridManager = new GridBagManager(replicator);
        glassPane.setGridManager(gridManager);
        customizer = gridManager.getCustomizer(glassPane);
    }

    /**
     * Updates or initializes gap related settings.
     */
    private void updateGapProperties(boolean gapSupport) {
        if(gapSupport) {
            int gapWidth = gridManager.getGridInfo().getGapWidth();
            int gapHeight = gridManager.getGridInfo().getGapHeight();
            if(gapWidth < 0) {
                gapWidth = FormLoaderSettings.getInstance().getGapWidth();
            }
            if(gapHeight < 0) {
                gapHeight = FormLoaderSettings.getInstance().getGapHeight();
            }
            if(gapWidth < 0) {
                gapWidth = (Integer)gapWidthSpinner.getValue();
            }
            if(gapHeight < 0) {
                gapHeight = (Integer)gapHeightSpinner.getValue();
            }
            FormLoaderSettings.getInstance().setGapWidth(gapWidth);
            FormLoaderSettings.getInstance().setGapHeight(gapHeight);
            gridManager.updateGaps(true);
            gapWidthSpinner.setValue(gapWidth);
            gapHeightSpinner.setValue(gapHeight);
        } else {
            if(FormLoaderSettings.getInstance().getGapWidth() < 0) {
                FormLoaderSettings.getInstance().setGapWidth(DEFAULT_GAP_WIDTH);
            }
            if(FormLoaderSettings.getInstance().getGapHeight() < 0) {
                FormLoaderSettings.getInstance().setGapHeight(DEFAULT_GAP_HEIGHT);
            }
        }
    }

    /**
     * Creates and initializes components on the left side.
     * 
     * @return component that represents the left side.
     */
    private JComponent initLeftColumn() {
        sheet = new PropertySheet();
        sheet.setPreferredSize(new Dimension(310, 300));
        JPanel leftPanel;
        if (customizer == null) {
            leftPanel = sheet;
        } else {
            leftPanel = new JPanel();
            leftPanel.setLayout(new BorderLayout());
            leftPanel.add(sheet);
            leftPanel.add(customizer.getComponent(), BorderLayout.PAGE_START);
        }
        return leftPanel;
    }

    /**
     * Creates and initializes "enable gap support" button.
     * 
     * @return "enable gap support" button.
     */
    private JToggleButton initGapButton() {
        JToggleButton button = new JToggleButton();
        ImageIcon image = ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gaps.png", false); // NOI18N
        button.setIcon(image);
        button.setFocusPainted(false);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean gapSupport = ((JToggleButton)e.getSource()).isSelected();
                gapWidthSpinner.setEnabled(gapSupport);
                gapHeightSpinner.setEnabled(gapSupport);
                gapWidthSpinnerLabel.setEnabled(gapSupport);
                gapHeightSpinnerLabel.setEnabled(gapSupport);
                gapWidthSpinnerBox.setEnabled(gapSupport);
                gapHeightSpinnerBox.setEnabled(gapSupport);
                if(gapSupport) {
                    assert !gridManager.getGridInfo().hasGaps();
                    ((JToggleButton)e.getSource()).setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.gapSupportDisable")); // NOI18N
                    int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
                    int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
                    gapWidthSpinner.setValue(gapWidth);
                    gapHeightSpinner.setValue(gapHeight);
                    glassPane.performAction(new AddGapsAction());
                } else {
                    assert gridManager.getGridInfo().hasGaps();
                    ((JToggleButton)e.getSource()).setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.gapSupportEnable")); // NOI18N
                    glassPane.performAction(new RemoveGapsAction());
                }
            }
        });
        return button;
    }
    
    /**
     * Creates and initializes spinner allowing gap column width modification.
     * 
     * @return "gap column width" spinner.
     */
    private JSpinner initGapWidthSpinner() {
        SpinnerNumberModel gapWidthModel = new SpinnerNumberModel(DEFAULT_GAP_WIDTH, 0, 100, 1);
        JSpinner spinner = new JSpinner(gapWidthModel);
        spinner.setEditor(new JSpinner.NumberEditor(spinner));
        spinner.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.gapColumnSpinner")); // NOI18N
        Dimension labelPrefSize = new JLabel("999").getPreferredSize();
        Dimension spinnerMinSize = spinner.getMinimumSize();
        spinner.setMaximumSize(new Dimension(labelPrefSize.width + spinnerMinSize.width, Short.MAX_VALUE));
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner)e.getSource();
                int gapWidth = (Integer)spinner.getValue();
                FormLoaderSettings.getInstance().setGapWidth(gapWidth);
                glassPane.updateLayout();
            }
        });
        return spinner;
    }
    
    /**
     * Creates and initializes spinner allowing gap row height modification.
     * 
     * @return "gap row height" spinner.
     */
    private JSpinner initGapHeightSpinner() {
        SpinnerNumberModel gapHeightModel = new SpinnerNumberModel(DEFAULT_GAP_HEIGHT, 0, 100, 1);
        JSpinner spinner = new JSpinner(gapHeightModel);
        spinner.setEditor(new JSpinner.NumberEditor(spinner));
        spinner.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.gapRowSpinner")); // NOI18N
        Dimension labelPrefSize = new JLabel("999").getPreferredSize();
        Dimension spinnerMinSize = spinner.getMinimumSize();
        spinner.setMaximumSize(new Dimension(labelPrefSize.width + spinnerMinSize.width, Short.MAX_VALUE));
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner)e.getSource();
                int gapHeight = (Integer)spinner.getValue();
                FormLoaderSettings.getInstance().setGapHeight(gapHeight);
                glassPane.updateLayout();
            }
        });
        return spinner;
    }
    
    /**
     * Creates and initializes "pad empty rows/columns" button.
     * 
     * @return "pad empty rows/columns" button.
     */
    private JToggleButton initPaddingButton() {
        JToggleButton button = new JToggleButton();
        ImageIcon image = ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/pad_empty.png", false); // NOI18N
        button.setIcon(image);
        button.setFocusPainted(false);
        boolean padEmptyCells = FormLoaderSettings.getInstance().getPadEmptyCells();
        button.setSelected(padEmptyCells);
        if(padEmptyCells) {
            button.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.padEmptyCellsHide")); // NOI18N
        } else {
            button.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.padEmptyCellsShow")); // NOI18N
        }
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean padEmptyCells = ((JToggleButton)e.getSource()).isSelected();
                FormLoaderSettings.getInstance().setPadEmptyCells(padEmptyCells);
                if(padEmptyCells) {
                    ((JToggleButton)e.getSource()).setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.padEmptyCellsHide")); // NOI18N
                } else {
                    ((JToggleButton)e.getSource()).setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.padEmptyCellsShow")); // NOI18N
                }
                glassPane.updateLayout();
            }
        });
        return button;
    }
    
    /**
     * Creates and initializes preview/test layout button.
     * 
     * @param toolBar toolbar where the button should be added.
     * @param metaComp meta-component to preview.
     */
    private void initPreviewButton(JToolBar toolBar, final RADVisualComponent metaComp) {
        TestAction testAction = SystemAction.get(TestAction.class);
        JButton button = toolBar.add(testAction);
        button.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.previewLayout")); // NOI18N
        button.setFocusPainted(false);
    }

    /**
     * Initializes undo/redo toolbar button.
     * 
     * @param button button to initialize.
     * @return initialized button.
     */
    private JButton initUndoRedoButton(JButton button) {
        String text = (String)button.getAction().getValue(Action.NAME);
        Mnemonics.setLocalizedText(button, text);
        text = button.getText();
        button.setText(null);
        button.setToolTipText(text);
        button.setFocusPainted(false);
        return button;
    }

    /** Selected meta-components. */
    private Set<RADVisualComponent> metaSelection = new HashSet<RADVisualComponent>();
    
    /**
     * Sets selection.
     * 
     * @param selection new selection.
     */
    public void setSelection(Set<Component> selection) {
        metaSelection.clear();
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            Component comp = (Component)replicator.getClonedComponent(metacomp);
            if (selection.contains(comp)) {
                metaSelection.add(metacomp);
            }
        }
        updatePropertySheet();
        updateCustomizer();
    }

    /** Listener for property changes on selected nodes. */
    private PropertyChangeListener selectedNodeListener;

    /**
     * Returns listener for property changes on selected nodes.
     * 
     * @return listener for property changes on selected nodes.
     */
    private PropertyChangeListener getSelectedNodeListener() {
        if (selectedNodeListener == null) {
            selectedNodeListener = createSelectedNodeListener();
        }
        return selectedNodeListener;
    }

    /** Determines whether update of glassPane has been scheduled. */
    boolean updateScheduled = false;
    
    /**
     * Creates {@code selectedNodeListner}.
     * 
     * @return {@code selectedNodeListner}.
     */
    private PropertyChangeListener createSelectedNodeListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isShowing() && !glassPane.isUserActionInProgress()) {
                    if (!updateScheduled) {
                        // This method is called several times when a change
                        // is done to some property (in property sheet)
                        // when several components are selected.
                        // Avoiding partial refresh - waiting till
                        // other invocations/property modifications
                        // are finished.
                        updateScheduled = true;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateScheduled = false;
                                glassPane.updateLayout();
                                updateCustomizer();
                            }
                        });
                    }
                }
            }
        };
    }

    /** Nodes selected in the property sheet. */
    private List<Node> selectedNodes = new ArrayList<Node>();
    
    /** Updates the property sheet according to the current selection. */
    private void updatePropertySheet() {
        List<Node> nodes = new ArrayList<Node>(metaSelection.size());
        for (RADVisualComponent metacomp : metaSelection) {
            RADComponentNode node = metacomp.getNodeReference();
            if (node == null) {
                // "metacomp" was just added and the node reference is not initialized yet
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        List<Node> nodes = new ArrayList<Node>(metaSelection.size());
                        for (RADVisualComponent metacomp : metaSelection) {
                            nodes.add(new LayoutConstraintsNode(metacomp.getNodeReference()));
                        }
                        setSelectedNodes(nodes);
                        sheet.setNodes(nodes.toArray(new Node[0]));
                    }
                });
                return;
            } else {
                nodes.add(new LayoutConstraintsNode(node));
            }
        }
        setSelectedNodes(nodes);
        sheet.setNodes(nodes.toArray(new Node[0]));
    }

    /**
     * Sets the selected nodes in the property sheet.
     * 
     * @param nodes new selection in the property sheet.
     */
    void setSelectedNodes(List<Node> nodes) {
        for (Node node : selectedNodes) {
            node.removePropertyChangeListener(getSelectedNodeListener());
        }
        this.selectedNodes = nodes;
        for (Node node : selectedNodes) {
            node.addPropertyChangeListener(getSelectedNodeListener());
        }
    }

    /**
     * Updates the grid customizer (part of the left side of the designer)
     * according to the current selection.
     */
    void updateCustomizer() {
        if (customizer != null) {
            DesignerContext context = glassPane.currentContext();
            customizer.setContext(context);
        }
    }

    /**
     * Updates context menu of the designer. It adds special
     * (non-{@code GridAction}) actions like Design Parent/This Container.
     * 
     * @param context the current designer context.
     * @param menu menu to update.
     */
    void updateContextMenu(DesignerContext context, JPopupMenu menu) {
        if ((context.getFocusedColumn() == -1) == (context.getFocusedRow() == -1)) {
            RADVisualContainer root = (RADVisualContainer)replicator.getTopMetaComponent();
            RADVisualContainer parent = root.getParentContainer();
            if (haveIdenticalLayoutDelegate(root, parent)) {
                // Design Parent action
                menu.add(new DesignContainerAction(this, parent, true));
            }
        }
        if (metaSelection.size() == 1) {
            RADVisualContainer root = (RADVisualContainer)replicator.getTopMetaComponent();
            RADVisualComponent comp = metaSelection.iterator().next();
            if (comp instanceof RADVisualContainer) {
                RADVisualContainer cont = (RADVisualContainer)comp;
                if (haveIdenticalLayoutDelegate(root, cont)) {
                    // Design This Container action
                    menu.add(new DesignContainerAction(this, cont, false));
                }
            }
        }
    }

    /** Listener for form model property changes. */
    private FormModelListener formModelListener;

    /**
     * Returns listener for form model changes.
     * 
     * @return listener for form model changes.
     */
    private FormModelListener getFormModelListener() {
        if (formModelListener == null) {
            formModelListener = createFormModelListener();
        }
        return formModelListener;
    }

    /**
     * Creates {@code FormModelListener}.
     * Used here to listen on layout property changes
     * affecting the state of gap support controls.
     * 
     * @return {@code FormModelListener}.
     */
    private FormModelListener createFormModelListener() {
        return new FormModelListener() {
            @Override
            public void formChanged(FormModelEvent[] events) {
                for(FormModelEvent event : events) {
                    if( gridManager.getGridInfo().isGapEvent(event) ) {
                        boolean gapSupport = gridManager.getGridInfo().hasGaps();
                        updateGapControls(gapSupport);
                    }
                }
            }
        };
    }

    /**
     * Adds form model listener.
     */
    void addFormModelListener() {
        formModel.addFormModelListener(getFormModelListener());
    }

    /**
     * Removes form model listener.
     */
    void removeFormModelListener() {
        formModel.removeFormModelListener(getFormModelListener());
    }

    /**
     * Checks whether two containers have the same layout delegate.
     * 
     * @param cont1 the first container to check.
     * @param cont2 the second container to check.
     * @return {@code true} if the given containers have the same layout delegate,
     * returns {@code false} otherwise.
     */
    private boolean haveIdenticalLayoutDelegate(RADVisualContainer cont1, RADVisualContainer cont2) {
        if ((cont1 == null) || (cont2 == null)) {
            return false;
        }
        LayoutSupportManager support1 = cont1.getLayoutSupport();
        if (support1 == null) {
            return false;
        }
        String delegate1 = support1.getLayoutDelegate().getClass().getName();
        LayoutSupportManager support2 = cont2.getLayoutSupport();
        if (support2 == null) {
            return false;
        }
        String delegate2 = support2.getLayoutDelegate().getClass().getName();
        return delegate1.equals(delegate2);
    }

    /**
     * Node that shows just layout constraints of the given {@code RADComponentNode}.
     */
    static class LayoutConstraintsNode extends FilterNode {

        /**
         * Creates a new {@code LayoutConstraintsNode} based on the given node.
         * 
         * @param original the original node this node should be based on.
         */
        LayoutConstraintsNode(Node original) {
            super(original);
        }

        @Override
        public Node.PropertySet[] getPropertySets() {
            for (Node.PropertySet pSet : super.getPropertySets()) {
                String name = pSet.getName();
                if ("layout".equals(name)) { // NOI18N
                    final Node.PropertySet set = pSet;
                    String displayName = NbBundle.getMessage(GridDesigner.class, "GridDesigner.layoutConstraints"); // NOI18N
                    return new Node.PropertySet[] {new PropertySet(set.getName(), displayName, set.getShortDescription()) {
                        @Override
                        public Property<?>[] getProperties() {
                            return set.getProperties();
                        }
                    }};
                }
            }
            return new Node.PropertySet[0];
        }
    }

}
