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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.View;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.lib.api.CssColor;
import org.netbeans.modules.web.common.ui.api.WebUIUtils;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.ui.DomTC;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.MatchedStyles;
import org.netbeans.modules.web.webkit.debugging.api.css.Media;
import org.netbeans.modules.web.webkit.debugging.api.css.Property;
import org.netbeans.modules.web.webkit.debugging.api.css.PropertyInfo;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.ListView;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.w3c.dom.Document;

/**
 * Selection section of CSS Styles view.
 *
 * @author Jan Stola
 */
public class CSSStylesSelectionPanel extends JPanel {
    
    /** Is Mac L&F? */
    private static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N 
    /** Request processor used by this class. */
    static final RequestProcessor RP = new RequestProcessor(CSSStylesSelectionPanel.class);
    /** Lookup of this panel. */
    private transient Lookup lookup;
    /** The current inspected page. */
    private transient WebKitPageModel pageModel;
    /** The current inspected node. */
    private transient Node inspectedNode;
    /** Page model listener. */
    private transient Listener listener;
    /** Property Summary view. */
    private TreeTableView propertyPane;
    /** Explorer manager for Property Summary. */
    private ExplorerManager propertyPaneManager;
    /** Style Cascade view. */
    private ListView rulePane;
    /** Explorer manager for Style Cascade. */
    private ExplorerManager rulePaneManager;
    /** Edit CSS Rules action used in rule pane. */
    private EditCSSRulesAction editCSSRulesAction;
    /** Panel for messages. */
    private JPanel messagePanel;
    /** Label for messages. */
    private JLabel messageLabel;
    /** Header of Property Summary section. */
    private JLabel propertySummaryLabel;
    /** Component showing the style information for the current selection. */
    private final JComponent selectionView;
    /** Mapping of {@code Resource} to the corresponding {@code FileObject}. */
    private final Map<Resource,FileObject> resourceCache = new WeakHashMap<Resource, FileObject>();

    /**
     * Creates a new {@code CSSStylesSelectionPanel}.
     */
    CSSStylesSelectionPanel() {
        setLayout(new BorderLayout());
        JSplitPane splitPane = createSplitPane();
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(initPropertyPane());
        splitPane.setBottomComponent(initRulePane());
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        selectionView = splitPane;
        initMessagePanel();
        initSelectionOfOwningRule();
        add(selectionView, BorderLayout.CENTER);
        updateContent(null, false);
    }

    private JSplitPane createSplitPane() {
        return new JSplitPane() {

            @Override
            public String getUIClassID() {
                if( AQUA && UIManager.get("Nb.SplitPaneUI.clean") != null ) { //NOI18N
                    return "Nb.SplitPaneUI.clean"; //NOI18N
                } else {
                    return super.getUIClassID();
                }
            }
        };
    }
    
    /**
     * Initializes Property Summary section.
     *
     * @return Property Summary panel.
     */
    private JPanel initPropertyPane() {
        propertyPane = new PropertyPaneView();
        String valueTitle =  NbBundle.getMessage(CSSStylesSelectionPanel.class, "CSSStylesSelectionPanel.value"); // NOI18N
        propertyPane.setProperties(new Node.Property[] {
            new PropertySupport.ReadOnly<String>(MatchedPropertyNode.PROPERTY_VALUE, String.class, valueTitle, null) {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return null;
                }
            }
        });
        ExplorerManagerProviderPanel propertyPanePanel = new ExplorerManagerProviderPanel();
        propertyPanePanel.setLayout(new BorderLayout());
        propertyPanePanel.add(propertyPane, BorderLayout.CENTER);
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        propertySummaryLabel = new JLabel();
        propertySummaryLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        propertySummaryLabel.setMinimumSize(new Dimension(0,0));
        titlePanel.add(propertySummaryLabel);
        titlePanel.add(Box.createHorizontalGlue());
        JToggleButton pseudoClassToggle = new JToggleButton();
        pseudoClassToggle.setFocusPainted(false);
        pseudoClassToggle.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/web/inspect/resources/elementStates.png", true)); // NOI18N
        pseudoClassToggle.setToolTipText(NbBundle.getMessage(CSSStylesSelectionPanel.class, "CSSStylesSelectionPanel.pseudoClasses")); // NOI18N
        CustomToolbar toolBar = new CustomToolbar();
        toolBar.addButton(pseudoClassToggle);
        titlePanel.add(toolBar);
        headerPanel.add(titlePanel, BorderLayout.PAGE_START);
        headerPanel.add(createPseudoClassPanel(pseudoClassToggle), BorderLayout.CENTER);
        propertyPanePanel.add(headerPanel, BorderLayout.PAGE_START);
        propertyPaneManager = propertyPanePanel.getExplorerManager();
        propertyPanePanel.setMinimumSize(new Dimension(0,0)); // allow shrinking in JSplitPane
        return propertyPanePanel;
    }

    /**
     * Creates a panel that allows forcing of pseudo-classes.
     * 
     * @param pseudoClassToggle toggle-button used to show the panel.
     * @return panel that allows forcing of pseudo-classes.
     */
    private JPanel createPseudoClassPanel(JToggleButton pseudoClassToggle) {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,2));
        ResourceBundle bundle = NbBundle.getBundle(CSSStylesSelectionPanel.class);
        panel.add(createPseudoCheckBox(
                CSS.PseudoClass.ACTIVE,
                bundle.getString("CSSStylesSelectionPanel.pseudoClass.active"))); // NOI18N
        panel.add(createPseudoCheckBox(
                CSS.PseudoClass.HOVER,
                bundle.getString("CSSStylesSelectionPanel.pseudoClass.hover"))); // NOI18N
        panel.add(createPseudoCheckBox(
                CSS.PseudoClass.FOCUS,
                bundle.getString("CSSStylesSelectionPanel.pseudoClass.focus"))); // NOI18N
        panel.add(createPseudoCheckBox(
                CSS.PseudoClass.VISITED,
                bundle.getString("CSSStylesSelectionPanel.pseudoClass.visited"))); // NOI18N
        panel.setVisible(false);
        pseudoClassToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JToggleButton source = (JToggleButton)e.getSource();
                panel.setVisible(source.isSelected());
            }
        });
        return panel;
    }

    /** Name of the client property that holds a pseudo-class that is affected by the check-box. */
    private static final String PSEUDO_CLASS = "pseudoClass"; // NOI18N
    /** Check-boxes that can be used to force pseudo-classes. */
    private final List<JCheckBox> pseudoClassCheckBoxes = new ArrayList<JCheckBox>(4);

    /**
     * Creates a check-box that can be used to force the specified pseudo-class.
     * 
     * @param pseudoClass pseudo-class affected by the check-box.
     * @param title title of the check-box.
     * @return check-box that can be used to force the specified pseudo-class.
     */
    private JCheckBox createPseudoCheckBox(final CSS.PseudoClass pseudoClass, String title) {
        JCheckBox checkbox = new JCheckBox(title);
        checkbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WebKitPageModel model = pageModel;
                Node selectedNode = inspectedNode;
                if ((model != null) && (selectedNode != null)) {
                    org.netbeans.modules.web.webkit.debugging.api.dom.Node node =
                        selectedNode.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
                    JCheckBox source = (JCheckBox)e.getSource();
                    boolean checked = source.isSelected();
                    if (checked) {
                        model.addPseudoClass(node, pseudoClass);
                    } else {
                        model.removePseudoClass(node, pseudoClass);
                    }
                    updateMatchedRules(model, selectedNode, true);
                }
            }
        });
        checkbox.putClientProperty(PSEUDO_CLASS, pseudoClass);
        pseudoClassCheckBoxes.add(checkbox);
        return checkbox;
    }

    /**
     * Updates the panel used to force pseudo-classes.
     * 
     * @param pageModel inspected page.
     * @param node inspected/selected node.
     */
    private void updatePseudoClassPanel(WebKitPageModel pageModel,
            org.netbeans.modules.web.webkit.debugging.api.dom.Node node) {
        CSS.PseudoClass[] pseudoClasses = pageModel.getPseudoClasses(node);
        EnumSet<CSS.PseudoClass> set = EnumSet.noneOf(CSS.PseudoClass.class);
        set.addAll(Arrays.asList(pseudoClasses));
        for (JCheckBox checkbox : pseudoClassCheckBoxes) {
            CSS.PseudoClass pseudoClass = (CSS.PseudoClass)checkbox.getClientProperty(PSEUDO_CLASS);
            boolean selected = set.contains(pseudoClass);
            checkbox.setSelected(selected);
        }
    }

    /**
     * Initializes Style Cascade section.
     *
     * @return Style Cascade section.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value="SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION", justification="The instances are never serialized.") // NOI18N
    private JPanel initRulePane() {
        rulePane = new ListView() {
            {
                final StylesRenderer renderer = new StylesRenderer(resourceCache);
                list.setCellRenderer(renderer);
                MouseAdapter adapter = new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if (isLink(e)) {
                            list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            list.setCursor(Cursor.getDefaultCursor());
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        list.setCursor(Cursor.getDefaultCursor());
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (isLink(e)) {
                            Point p = e.getPoint();
                            int index = list.locationToIndex(p);
                            Object value = list.getModel().getElementAt(index);
                            Node node = Visualizer.findNode(value);
                            node.getPreferredAction().actionPerformed(new ActionEvent(node, 0, null));
                        }
                    }

                    /**
                     * Determines whether there is a link under the mouse cursor.
                     *
                     * @param event event describing the mouse state.
                     */
                    private boolean isLink(MouseEvent event) {
                        Point p = event.getPoint();
                        int index = list.locationToIndex(p);
                        if (index == -1) {
                            return false;
                        }
                        Rectangle cellBounds = list.getCellBounds(index, index);
                        p.translate(-cellBounds.x, -cellBounds.y);
                        Object value = list.getModel().getElementAt(index);
                        renderer.getListCellRendererComponent(list, value, index, false, false);
                        return renderer.isLink(p);
                    }
                };
                list.addMouseMotionListener(adapter);
                list.addMouseListener(adapter);
            }

            @Override
            protected void showSelection(int[] indexes) {
                super.showSelection(indexes);
                // Issue 226899
                if (indexes != null && indexes.length > 0) {
                    list.ensureIndexIsVisible(indexes[0]);
                }
            }
        };
        rulePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ExplorerManagerProviderPanel rulePanePanel = new ExplorerManagerProviderPanel();
        rulePanePanel.setLayout(new BorderLayout());
        rulePanePanel.add(rulePane, BorderLayout.CENTER);
        
        JPanel northPanel = new JPanel();        
        northPanel.setLayout(new BorderLayout());
        
        //add the info label
        JLabel rulePaneSummaryLabel = new JLabel();
        rulePaneSummaryLabel.setText(NbBundle.getMessage(
                CSSStylesSelectionPanel.class, "CSSStylesSelectionPanel.rulePaneHeader")); // NOI18N
        rulePaneSummaryLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        northPanel.add(rulePaneSummaryLabel, BorderLayout.CENTER);
        
        //add toolbar
        CustomToolbar toolbar = new CustomToolbar();
        final JToggleButton createRuleToggleButton = new JToggleButton();
        editCSSRulesAction = new EditCSSRulesAction();
        createRuleToggleButton.setAction(editCSSRulesAction);
        org.openide.awt.Mnemonics.setLocalizedText(createRuleToggleButton, null);
        createRuleToggleButton.setToolTipText((String)editCSSRulesAction.getValue(Action.NAME));
        createRuleToggleButton.setFocusable(false);
        createRuleToggleButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createRuleToggleButton.setSelected(false); //disable selected as it's a toggle button
            }
        });
        
        toolbar.addButton(createRuleToggleButton);
        northPanel.add(toolbar, BorderLayout.EAST);
        rulePanePanel.add(northPanel, BorderLayout.NORTH);
        
        rulePanePanel.setMinimumSize(new Dimension(0,0)); // allow shrinking in JSplitPane
        rulePaneManager = rulePanePanel.getExplorerManager();
        lookup = ExplorerUtils.createLookup(rulePaneManager, getActionMap());
        return rulePanePanel;
    }

    /**
     * Initializes the listener responsible for selection of the owning
     * rule (in Style Cascade) when the selection in Property Summary changes.
     */
    private void initSelectionOfOwningRule() {
        propertyPaneManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(propertyName)) {
                    Node[] selection = propertyPaneManager.getSelectedNodes();
                    if (selection.length == 1) {
                        Lookup lookup = selection[0].getLookup();
                        Rule rule = lookup.lookup(Rule.class);
                        if (rule != null) {
                            selectRule(rule, rulePaneManager.getRootContext());
                        }
                    }
                }
            }
            /**
             * Attempts to select the specified rule in the given sub-tree
             * of Style Cascade view.
             * 
             * @param rule rule to select.
             * @param root root of a sub-tree in Style Cascade to search.
             * @return {@code true} when the rule was selected successfully,
             * returns {@code false} otherwise.
             */
            private boolean selectRule(Rule rule, Node root) {
                Lookup lookup = root.getLookup();
                Rule otherRule = lookup.lookup(Rule.class);
                if (rule == otherRule) {
                    try {
                        rulePaneManager.setSelectedNodes(new Node[] { root });
                    } catch (PropertyVetoException ex) {
                        Logger.getLogger(CSSStylesSelectionPanel.class.getName()).log(Level.FINEST, null, ex);
                    }
                    return true;
                }
                for (Node subNode : root.getChildren().getNodes()) {
                    if (selectRule(rule, subNode)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Initializes the panel used to display messages.
     */
    private void initMessagePanel() {
        messageLabel = new JLabel();
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(false);
        messageLabel.setBackground(new BeanTreeView().getViewport().getView().getBackground());
        messageLabel.setOpaque(true);

        messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.add(messageLabel, BorderLayout.CENTER);
    }

    /**
     * Updates the content of and sets a new page model to this panel.
     *
     * @param pageModel page model to use by this panel.
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    final void updateContent(WebKitPageModel pageModel, boolean keepSelection) {
        if (this.pageModel != null) {
            this.pageModel.removePropertyChangeListener(getListener());
        }
        this.pageModel = pageModel;
        if (this.pageModel != null) {
            this.pageModel.addPropertyChangeListener(getListener());
        }
        updateContentImpl(pageModel, keepSelection);
    }

    /**
     * Updates the content of this panel.
     *
     * @param pageModel page model to use by this panel.
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    void updateContentImpl(final WebKitPageModel pageModel, final boolean keepSelection) {        
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateContentImpl(pageModel, keepSelection);
                }
            });
            return;
        }
        inspectedNode = null;
        if (pageModel == null) {
            setDummyRoots();
            editCSSRulesAction.setActiveNode(null);
        } else {
            List<Node> selection = pageModel.getSelectedNodes();
            int selectionSize = 0;
            boolean containsUnkownNode = false;
            Node knownNode = null;
            for (Node node : selection) {
                Object webKitNode = node.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
                if (webKitNode == null) {
                    containsUnkownNode = true;
                } else {
                    knownNode = node;
                    selectionSize++;
                }
            }
            if (selectionSize == 0) {
                setDummyRoots();
                String key;
                if (containsUnkownNode) {
                    TopComponent tc = WindowManager.getDefault().findTopComponent(DomTC.ID);
                    if (tc.isOpened()) {
                        key = "CSSStylesSelectionPanel.selectInDOMTree"; // NOI18N
                    } else {
                        key = "CSSStylesSelectionPanel.unknownElementSelected"; // NOI18N
                    }
                } else {
                    key = "CSSStylesSelectionPanel.noElementSelected"; // NOI18N
                }
                showLabel(key);
            } else if (selectionSize > 1) {
                setDummyRoots();
                showLabel("CSSStylesSelectionPanel.multipleElementsSelected"); // NOI18N
            } else {
                inspectedNode = knownNode;
                final Node selectedNode = knownNode;
                final org.netbeans.modules.web.webkit.debugging.api.dom.Node node =
                    selectedNode.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
                if (node.getNodeType() == Document.ELEMENT_NODE) {
                    String name = selectedNode.getHtmlDisplayName();
                    if (name.startsWith("<html>")) { // NOI18N
                        name = name.substring(6);
                    }
                    String header = NbBundle.getMessage(CSSStylesSelectionPanel.class, "CSSStylesSelectionPanel.propertySummaryHeader", name); // NOI18N
                    propertySummaryLabel.setText("<html><div>" + header + "</div>"); // NOI18N
                    // Hack that avoids line wrapping
                    int width = propertySummaryLabel.getPreferredSize().width;
                    propertySummaryLabel.setText("<html><div width=\""+width+"\">" + header + "</div>"); // NOI18N
                    showLabel(null);
                    updatePseudoClassPanel(pageModel, node);
                    updateMatchedRules(pageModel, selectedNode, keepSelection);
                } else {
                    setDummyRoots();
                    showLabel("CSSStylesSelectionPanel.noElementSelected"); // NOI18N
                }
            }
            editCSSRulesAction.setActiveNode((selectionSize == 1) ? knownNode : null);
        }
        revalidate();
        repaint();
    }

    /**
     * Updates the list of displayed matched rules.
     * 
     * @param pageModel inspected page.
     * @param selectedNode inspected/selected node.
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    void updateMatchedRules(final WebKitPageModel pageModel, final Node selectedNode, final boolean keepSelection) {
        if (EventQueue.isDispatchThread()) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    updateMatchedRules(pageModel, selectedNode, keepSelection);
                }
            });
            return;
        }
        org.netbeans.modules.web.webkit.debugging.api.dom.Node node =
            selectedNode.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
        WebKitDebugging webKit = pageModel.getWebKit();
        CSS css = webKit.getCSS();
        CSS.PseudoClass[] pseudoClasses = pageModel.getPseudoClasses(node);
        css.forcePseudoState(node, pseudoClasses);
        MatchedStyles matchedStyles = css.getMatchedStyles(node, pseudoClasses, true, true);
        Map<String,PropertyInfo> propertyInfos = css.getSupportedCSSProperties();
        if (matchedStyles != null) {
            Project project = pageModel.getProject();
            final Node rulePaneRoot = new MatchedRulesNode(project, selectedNode, matchedStyles);
            final Node propertyPaneRoot = new MatchedPropertiesNode(project, matchedStyles, propertyInfos);
            updateResourceCache(rulePaneRoot);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Node[] selectedRules = rulePaneManager.getSelectedNodes();
                    Node[] selectedProperties = propertyPaneManager.getSelectedNodes();
                    rulePaneManager.setRootContext(rulePaneRoot);
                    propertyPaneManager.setRootContext(propertyPaneRoot);
                    if (keepSelection) {
                        if (selectedProperties.length > 0) {
                            Node selectedProperty = selectedProperties[0];
                            Property property = selectedProperty.getLookup().lookup(Property.class);
                            if (property != null) {
                                String propertyName = property.getName();
                                for (Node candidate : propertyPaneRoot.getChildren().getNodes()) {
                                    Property candProperty = candidate.getLookup().lookup(Property.class);
                                    if (candProperty != null && propertyName.equals(candProperty.getName())) {
                                        try {
                                            propertyPaneManager.setSelectedNodes(new Node[] {candidate});
                                        } catch (PropertyVetoException pvex) {}
                                        break;
                                    }
                                }
                            }
                        }
                        if (selectedRules.length > 0) {
                            Node selectedRuleNode = selectedRules[0];
                            Rule selectedRule = selectedRuleNode.getLookup().lookup(Rule.class);
                            if (selectedRule != null) {
                                Node newSelectedRuleNode = Utilities.findRule(rulePaneRoot, selectedRule);
                                if (newSelectedRuleNode != null) {
                                    try {
                                        rulePaneManager.setSelectedNodes(new Node[] {newSelectedRuleNode});
                                    } catch (PropertyVetoException pvex) {}
                                }
                            }
                        }
                        Node[] nodes = rulePaneManager.getSelectedNodes();
                        if (nodes.length == 0) {
                            // The previous selection was either empty
                            // or is no longer valid => pre-select the most
                            // specific rule
                            preselectRule();
                        }
                    } else {
                        preselectRule();
                    }
                }
            });
        }
    }

    /**
     * Updates the {@code resourceCache} map for the children of the given node.
     * 
     * @param parent parent of the nodes for which the {@code resourceCache}
     * should be updated.
     */
    void updateResourceCache(Node parent) {
        Children children = parent.getChildren();
        for (Node node : children.getNodes(true)) {
            Resource ruleOrigin = node.getLookup().lookup(Resource.class);
            if (ruleOrigin != null) {
                resourceCache.put(ruleOrigin, ruleOrigin.toFileObject());
            }
        }
    }

    /**
     * Pre-selects the first property in the property pane (and
     * the corresponding rule in the rule pane) or just the first
     * rule in the rule pane.
     */
    void preselectRule() {
        Node propertyPaneRoot = propertyPaneManager.getRootContext();
        Node[] nodes = propertyPaneRoot.getChildren().getNodes();
        if (nodes.length == 0) {
            Node rulePaneRoot = rulePaneManager.getRootContext();
            nodes = rulePaneRoot.getChildren().getNodes();
            if (nodes.length > 0) {
                try {
                    rulePaneManager.setSelectedNodes(new Node[] { nodes[0] });
                } catch (PropertyVetoException pvex) {}                                
            }
        } else {
            try {
                propertyPaneManager.setSelectedNodes(new Node[] { nodes[0] });
            } catch (PropertyVetoException pvex) {}
        }
    }

    /**
     * Shows a label with the message that corresponds to the given bundle key.
     *
     * @param key key that corresponds to the message to show. If it is
     * {@code null} then the message label is hidden and the regular selection
     * view is shown instead.
     */
    private void showLabel(String key) {
        if ((key == null) != (selectionView.getParent() != null)) {
            if (key == null) {
                remove(messagePanel);
                add(selectionView);
            } else {
                remove(selectionView);
                add(messagePanel);
            }
        }
        if (key != null) {
            String message = NbBundle.getMessage(CSSStylesSelectionPanel.class, key);
            messageLabel.setText(message);
        }
    }

    /**
     * Sets dummy roots to tree views to release the currently displayed nodes.
     */
    private void setDummyRoots() {
        Node rulePaneRoot = new AbstractNode(Children.LEAF);
        Node propertyPaneRoot = new AbstractNode(Children.LEAF);
        // Workaround for a bug in TreeTableView
        rulePaneRoot.setDisplayName(NbBundle.getMessage(CSSStylesSelectionPanel.class, "MatchedRulesNode.displayName")); // NOI18N
        propertyPaneRoot.setDisplayName(NbBundle.getMessage(CSSStylesSelectionPanel.class, "MatchedPropertiesNode.displayName")); // NOI18N
        rulePaneManager.setRootContext(rulePaneRoot);
        propertyPaneManager.setRootContext(propertyPaneRoot);
    }

    /**
     * Returns the lookup of this panel.
     *
     * @return lookup of this panel.
     */
    Lookup getLookup() {
        return lookup;
    }

    /**
     * Returns a node selection listener.
     *
     * @return node selection listener.
     */
    private synchronized Listener getListener() {
        if (listener == null) {
            listener = new Listener();
        }
        return listener;
    }

    /**
     * Node selection listener.
     */
    class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (PageModel.PROP_SELECTED_NODES.equals(propertyName)) {
                updateContentImpl(pageModel, false);
            }
        }

    }

    /**
     * Panel that provides explorer manager and the corresponding lookup.
     */
    static class ExplorerManagerProviderPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
        /** Explorer manager provided by this panel. */
        private final ExplorerManager manager = new ExplorerManager();
        /** Lookup provided by this panel. */
        private final Lookup lookup = ExplorerUtils.createLookup(manager, getActionMap());

        @Override
        public final ExplorerManager getExplorerManager() {
            return manager;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }
    }

    /**
     * Custom {@code TreeTableView} used to display style information
     * for the selected element.
     */
    static class PropertyPaneView extends TreeTableView {

        /**
         * Creates a new {@code PropertyPaneView}.
         */
        PropertyPaneView() {
            setRootVisible(false);
            setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            final TreeCellRenderer renderer = tree.getCellRenderer();
            ((NodeRenderer) renderer).setShowIcons(false);
            hideTreeLines();
            if (Boolean.getBoolean("netbeans.plaf.dark.theme") // NOI18N
                    || "Nimbus".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
                // Issue 231547
                treeTable.getParent().setBackground(new Color(treeTable.getBackground().getRGB()));
            } else {
                Color bgColor = UIManager.getColor("Label.background"); // NOI18N
                treeTable.setBackground(bgColor);            
                treeTable.getParent().setBackground(bgColor);
            }
            final TableCellRenderer defaultRenderer = HtmlRenderer.createRenderer();
            treeTable.setDefaultRenderer(Node.Property.class, new TableCellRenderer() {
                // Text rendered in the first column of tree-table (i.e. in the tree)
                // is not baseline-aligned with the text in the other columns for some reason.
                // This border attempts to work around this problem.
                private final Border border[] = {
                    BorderFactory.createEmptyBorder(1,0,0,0),
                    BorderFactory.createEmptyBorder(1,3,0,0),
                };
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    String toolTip = null;
                    boolean isColor = false;
                    if (value instanceof Node.Property) {
                        Node.Property property = (Node.Property)value;
                        toolTip = property.getShortDescription();
                        try {
                            value = property.getValue();
                        } catch (IllegalAccessException ex) {
                        } catch (InvocationTargetException ex) {
                        }
                        Object color = property.getValue(MatchedPropertyNode.COLOR_PROPERTY);
                        isColor = (color == Boolean.TRUE);
                    }
                    Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (component instanceof JComponent) {
                        JComponent jcomponent = ((JComponent)component);
                        jcomponent.setBorder(border[column]);
                        jcomponent.setToolTipText(toolTip);
                    }
                    if (isColor && (component instanceof JLabel)) {
                        String colorCode = value.toString();
                        CssColor color = CssColor.getColor(colorCode);
                        if (color != null) {
                            colorCode = color.colorCode();
                        }
                        if (colorCode.startsWith("#") || (color != null)) { // NOI18N
                            JLabel label = (JLabel)component;
                            label.setIcon(WebUIUtils.createColorIcon(colorCode));
                        }
                    }
                    return component;
                }
            });
            treeTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (component instanceof JLabel) {
                        JLabel label = (JLabel)component;
                        label.setText("<html><b>"+label.getText()+"<b>"); // NOI18N
                        label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEtchedBorder(),
                            BorderFactory.createEmptyBorder(0, (column == 0) ? 17 : 1, 0, 0)));
                    }
                    return component;
                }
            });
        }

        /**
         * A hack that disables painting of tree lines.
         */
        private void hideTreeLines() {
            TreeUI treeUI = tree.getUI();
            if (treeUI instanceof BasicTreeUI) {
                try {
                    // The following code is equivalent to
                    // ((BasicTreeUI)tree.getUI()).paintLines = false;
                    Field paintLines = BasicTreeUI.class.getDeclaredField("paintLines"); // NOI18N
                    paintLines.setAccessible(true);
                    paintLines.setBoolean(treeUI, false);
                } catch (IllegalArgumentException ex) {
                } catch (IllegalAccessException ex) {
                } catch (NoSuchFieldException ex) {
                } catch (SecurityException ex) {
                }
            }
        }

    }

    /**
     * Renderer for the Styles (i.e., the middle) section of CSS Styles view.
     */
    static class StylesRenderer extends DefaultListCellRenderer {
        /** Component used for rendering. */
        private final JPanel renderer = new JPanel();
        /** Label showing information about the matched node. */
        private final JLabel matchedNodeLabel = new JLabel();
        /** Label showing the selector. */
        private final JLabel selectorLabel = new JLabel();
        /** Label showing the media query. */
        private final JLabel mediaLabel = new JLabel();
        /** Label showing the location of the rule. */
        private final JLabel ruleLocationLabel = new JLabel();
        /** Panel showing the location of the rule. */
        private final JPanel ruleLocationPanel = new JPanel();
        /** HTML renderer used to obtain background color. */
        private final ListCellRenderer htmlRenderer = HtmlRenderer.createRenderer();
        /** Mapping of {@code Resource} to the corresponding {@code FileObject}. */
        private final Map<Resource,FileObject> resourceCache;

        /**
         * Creates a new {@code StylesRenderer}.
         * 
         * @param resourceCache mapping of {@code Resource} to the corresponding
         * {@code FileObject}.
         */
        StylesRenderer(Map<Resource,FileObject> resourceCache) {
            this.resourceCache = resourceCache;
            ruleLocationPanel.setOpaque(false);
            ruleLocationPanel.setLayout(new BorderLayout());
            ruleLocationPanel.add(ruleLocationLabel, BorderLayout.LINE_START);
        }

        /**
         * Builds the layout of the rendered component.
         */
        private void buildLayout() {
            GroupLayout layout = new GroupLayout(renderer);
            GroupLayout.Group hGroup = layout.createSequentialGroup()
                    .addComponent(selectorLabel, 1, 1, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup()
                        .addComponent(matchedNodeLabel, 1, 1, Short.MAX_VALUE)
                        .addComponent(ruleLocationPanel, 1, 1, Short.MAX_VALUE)
                    );
            GroupLayout.Group vGroup = layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(selectorLabel)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(matchedNodeLabel)
                            .addComponent(ruleLocationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        ))
                    .addGap(0, 0, Short.MAX_VALUE);
            hGroup = layout.createParallelGroup()
                    .addComponent(mediaLabel, 1, 1, Short.MAX_VALUE)
                    .addGroup(hGroup);
            vGroup = layout.createSequentialGroup()
                    .addComponent(mediaLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(vGroup);
            layout.setHorizontalGroup(hGroup);
            layout.setVerticalGroup(vGroup);
            renderer.setLayout(layout);
            Color borderColor = UIManager.getColor("Label.background"); // NOI18N
            renderer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        @Override
        @NbBundle.Messages({
            "CSSStylesSelectionPanel.generatedStylesheet=Generated Style Sheet" // NOI18N
        })
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JComponent component = (JComponent)super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus); // NOI18N
            JLabel htmlLabel = (JLabel)htmlRenderer.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus); // NOI18N
            Color bgColor = color(htmlLabel, false);
            if (bgColor == null) {
                bgColor = component.getBackground();
            }
            renderer.setOpaque(isSelected);
            renderer.setBackground(bgColor);
            renderer.setBorder(component.getBorder());
            Color foreground = color(htmlLabel, true);
            if (foreground == null) {
                foreground = component.getForeground();
            }
            matchedNodeLabel.setForeground(foreground);
            selectorLabel.setForeground(foreground);
            mediaLabel.setForeground(foreground);
            Node node = Visualizer.findNode(value);
            Rule rule = node.getLookup().lookup(Rule.class);
            if (rule != null) {
                // Using HTML labels to allow wrapping of their content
                String matchedNode = node.getDisplayName();
                matchedNodeLabel.setText("<html>"+matchedNode); // NOI18N
                String ruleLocation = null;
                RuleInfo ruleInfo = node.getLookup().lookup(RuleInfo.class);
                if (ruleInfo != null && ruleInfo.getMetaSourceFile() != null && ruleInfo.getMetaSourceLine() != -1) {
                    ruleLocation = ruleInfo.getMetaSourceFile();
                    int slashIndex = Math.max(ruleLocation.lastIndexOf('/'), ruleLocation.lastIndexOf('\\'));
                    ruleLocation = ruleLocation.substring(slashIndex+1);
                    ruleLocation += ":" + ruleInfo.getMetaSourceLine(); // NOI18N
                } else {
                    Resource ruleOrigin = node.getLookup().lookup(Resource.class);
                    if (ruleOrigin != null) {
                        FileObject fob = resourceCache.get(ruleOrigin);
                        if (fob == null) {
                            ruleLocation = rule.getSourceURL();
                        } else {
                            ruleLocation = fob.getNameExt();
                        }
                        if (ruleLocation != null) {
                            if (ruleLocation.isEmpty()) {
                                ruleLocation = Bundle.CSSStylesSelectionPanel_generatedStylesheet();
                            }
                            // Source line seems to be 0-based (i.e. is 0 for the first line).    
                            int sourceLine = rule.getSourceLine() + 1;
                            ruleLocation += ":" + sourceLine; // NOI18N
                        }
                    }
                }
                ruleLocationLabel.setVisible(ruleLocation != null);
                if (ruleLocation != null) {
                    ruleLocation = "<html><u>" + ruleLocation; // NOI18N
                }
                ruleLocationLabel.setText(ruleLocation);
                ruleLocationPanel.doLayout();
                String selector = rule.getSelector();
                selectorLabel.setText("<html>"+selector); // NOI18N
                String mediaQuery = null;
                for (Media media : rule.getMedia()) {
                    mediaQuery = media.getText();
                }
                mediaLabel.setText(mediaQuery);
                mediaLabel.setVisible(mediaQuery != null);
                Color fg;
                if (isSelected) {
                    fg = foreground;
                } else if (UIManager.getLookAndFeel().getID().equals("GTK")) { // NOI18N
                    fg = UIManager.getColor("Label.foreground"); // NOI18N
                } else {
                    fg = UIManager.getColor("Label.disabledForeground"); // NOI18N
                }
                ruleLocationLabel.setForeground(fg);
                mediaLabel.setForeground(fg);
                mediaLabel.setEnabled(isSelected);
            }
            String toolTip = node.getShortDescription();
            renderer.setToolTipText(toolTip);

            // This tricky section tries to avoid problems with HTML labels
            // that don't have a reasonable preferred size.

            // Lay out the renderer for its expected width => this sets
            // the correct width to HTML labels.
            int width = list.getWidth()-list.getInsets().left-list.getInsets().right;
            renderer.setSize(width, 1);
            buildLayout();
            renderer.doLayout();

            // The labels have the corrent width now but they have an incorrect
            // height (they are laid out as if the whole content was on one line).
            // We resize their view according to their current width. This results
            // in relayout of HTML content. The labels should return their correct
            // preferred size after this step.
            resizeViewToMatchTheCurrentSize(matchedNodeLabel);
            resizeViewToMatchTheCurrentSize(selectorLabel);

            // Now (when the labels return their correct preferred size) we can
            // re-layout the container to get the desired result.
            // Unfortunately, GroupLayout is caching some values in a problematic
            // way. Hence, it is not sufficient to call doLayout(). We have
            // to rebuild the whole layout to get rid of the incorrect cached values.
            buildLayout();
            renderer.doLayout();

            return renderer;
        }

        /**
         * Resizes the {@code View} object used by this label to match
         * the current size of the label. Resizing of the {@code View}
         * causes re-layout of HTML label (which affects its preferred size).
         * 
         * @param label label whose {@code View} should be resized.
         */
        private void resizeViewToMatchTheCurrentSize(JLabel label) {
            Object view = label.getClientProperty("html"); // NOI18N
            if (view instanceof View) {
                ((View)view).setSize(label.getWidth(), label.getHeight());
            }
        }

        /**
         * Returns the background or foreground color of the given label.
         * We cannot use {@code getBackground()} or {@code getForeground()}
         * method because the given label is {@code HTMLRendererImpl} that
         * handles its background and foreground in a non-standard way.
         *
         * @param label label whose background or foreground should be returned.
         * @param foreground if true then the method returns the foreground
         * color, it returns background color otherwise.
         * @return background or foreground color of the given label.
         */
        private Color color(JLabel label, boolean foreground) {
            Color color = null;
            Object htmlUI = label.getUI();
            try {
                String methodName = foreground ? "getForegroundFor" : "getBackgroundFor"; // NOI18N
                Method method = htmlUI.getClass().getDeclaredMethod(methodName, htmlRenderer.getClass()); // NOI18N
                method.setAccessible(true);
                Object result = method.invoke(null, htmlRenderer);
                if (result instanceof Color) {
                    color = (Color)result;
                }
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            } catch (NoSuchMethodException ex) {
            }
            return color;
        }

        /**
         * Determines whether there is a link on the specified point of the renderer.
         *
         * @param point point to check.
         * @return {@code true} when there is a link, returns {@code false} otherwise.
         */
        boolean isLink(Point point) {
            boolean link = false;
            Rectangle bounds = ruleLocationPanel.getBounds();
            if (bounds.contains(point)) {
                point.translate(-bounds.x, -bounds.y);
                bounds = ruleLocationLabel.getBounds();
                link = bounds.contains(point);
            }
            return link;
        }

    }

}
