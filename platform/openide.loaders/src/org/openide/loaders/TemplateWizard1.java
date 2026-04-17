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

package org.openide.loaders;

import java.awt.Component;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.openide.WizardDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.AsyncGUIJob;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/** Dialog that can be used in create from template.
*
* @author  Jaroslav Tulach
*/
final class TemplateWizard1 extends javax.swing.JPanel implements DataFilter,
    ExplorerManager.Provider, java.beans.PropertyChangeListener, AsyncGUIJob {
    /** See org.openide.WizardDescriptor.PROP_CONTENT_SELECTED_INDEX
     */
    private static final String PROP_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_DATA
     */
    private static final String PROP_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    /** listener to changes in the wizard */
    private ChangeListener listener;
    /** selected template */
    private DataObject template;
    /** templates root */
    private DataFolder templatesRoot;
    /** manager for templates tree view */
    private ExplorerManager manager;
    
    /** Initialization data structure for passing data between
     * asynchronous background initialization and UI update */
    private static final class InitData {
        HtmlBrowser browser;
        String noDescMsg;
        Border noDescBorder;
    }; // end of InitData
    
    /** holds init data for async initialization */
    private InitData initData;
    
    /** Creates new form NewFromTemplatePanel */
    public TemplateWizard1 () {
        initComponents ();

        treeView = new TemplatesTreeView();
        treeView.setDefaultActionAllowed(false);
        treeView.setPopupAllowed(false);
        treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        java.awt.GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 11, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(treeView, gridBagConstraints1);

        ResourceBundle bundle = org.openide.util.NbBundle.getBundle(TemplateWizard1.class);
        
        setName (bundle.getString("LAB_TemplateChooserPanelName"));

        putClientProperty(PROP_CONTENT_SELECTED_INDEX, 0);
        putClientProperty(PROP_CONTENT_DATA, new String[] {getName(), "..."}); // NOI18N
        
        // Fix of #19667 - those values will be retreived in addNotify
        putClientProperty("LAB_SelectTemplateBorder", // NOI18N
            bundle.getString("LAB_SelectTemplateBorder")); 
        putClientProperty("LAB_TemplateDescriptionBorder", // NOI18N
            bundle.getString("LAB_TemplateDescriptionBorder"));
        putClientProperty("ACSD_TemplatesTree", // NOI18N
            bundle.getString("ACSD_TemplatesTree"));
        putClientProperty("ACSD_TemplateWizard1", // NOI18N
            bundle.getString("ACSD_TemplateWizard1"));
        // bugfix #19667 end
        
        updateRootNode (null);
        
        templatesLabel.setLabelFor(treeView);
        
        noBrowser.setText(bundle.getString("MSG_InitDescription"));
        java.awt.CardLayout card = (java.awt.CardLayout)browserPanel.getLayout();
        card.show (browserPanel, "noBrowser"); // NOI18N
        // for asynchnonous lazy init of this component
        Utilities.attachInitJob(this, this);
    }

    @Override
    public void addNotify() {
        // overriden to set the labels later than in constructor
        // in order to fix #19667
        Mnemonics.setLocalizedText(templatesLabel, 
                (String)getClientProperty("LAB_SelectTemplateBorder") // NOI18N
        );
        Mnemonics.setLocalizedText(browserLabel,
            (String)getClientProperty("LAB_TemplateDescriptionBorder") // NOI18N
        );

        treeView.getAccessibleContext().setAccessibleDescription(
            (String)getClientProperty("ACSD_TemplatesTree") // NOI18N
        );
        getAccessibleContext().setAccessibleDescription(
            (String)getClientProperty("ACSD_TemplateWizard1") // NOI18N
        );
        
        super.addNotify();
    }
    
    /** Explorer manager for templates tree view */
    public ExplorerManager getExplorerManager() {
        if (manager == null) {
            manager = new ExplorerManager();
            manager.addPropertyChangeListener(this);
        }
        return manager;
    }

    /** Forward focus to tree view. */
    @SuppressWarnings("deprecation")
    @Override
    public boolean requestDefaultFocus() {
        return treeView.requestDefaultFocus();
    }

    /** Preffered size */
    @Override
    public java.awt.Dimension getPreferredSize() {
        return TemplateWizard.PREF_DIM;
    }
    
    /** Updates the root of templates.
     * @param root the root folder
     */
    private void updateRootNode (DataFolder root) {
        if (root == null) {
            FileObject fo = FileUtil.getConfigFile("Templates"); // NOI18N
            if (fo != null && fo.isFolder ())
                root = DataFolder.findFolder (fo);
        }

        if (root == null || root.equals(templatesRoot))
            return;

        templatesRoot = root;

        Children ch = new DataShadowFilterChildren(root.getNodeDelegate());
        getExplorerManager().setRootContext(new DataShadowFilterNode (root.getNodeDelegate(), ch, root.getNodeDelegate().getDisplayName ()));
    }

    private class DataShadowFilterChildren extends FilterNode.Children {
        
        public DataShadowFilterChildren (Node or) {
            super (or);
        }
        
        /** Creates nodes for nodes.
         */
        @Override
        protected Node[] createNodes(Node key) {
            Node n = key;
            String nodeName = null;
            
            DataObject obj = null;
            DataShadow shadow = n.getCookie(DataShadow.class);
            if (shadow != null) {
                // I need DataNode here to get localized name of the
                // shadow, but without the ugly "(->)" at the end
                DataNode dn = new DataNode(shadow, Children.LEAF);
                nodeName = dn.getDisplayName();
                obj = shadow.getOriginal();
                n = obj.getNodeDelegate();
            }
            
            if (obj == null)
                obj = n.getCookie(DataObject.class);
            
            if (obj != null) {
                if (obj.isTemplate ()) {
                    // on normal nodes stop recursion
                    return new Node[] { new DataShadowFilterNode (n, Children.LEAF, nodeName) };
                }
            
                if (acceptDataObject (obj)) {
                    // on folders use normal filtering
                    return new Node[] { new DataShadowFilterNode (n, new DataShadowFilterChildren (n), nodeName) };
                }
            }
            return new Node[] {};
        }

    }
    

    private static class DataShadowFilterNode extends FilterNode {
        
        private String name;
        
        public DataShadowFilterNode (Node or, org.openide.nodes.Children children, String name) {
            super (or, children);
            this.name = name;
            disableDelegation(FilterNode.DELEGATE_SET_DISPLAY_NAME);
        }
        
        public String getDisplayName() {
            return name != null ? name : super.getDisplayName();
        }
        
        // issue 29867, rename should be prohibited
        public boolean canRename () {
            return false;
        }
        
    }
    

    /** Updates description to reflect the one associated with given object.
    * @param obj object
    */
    private void updateDescription (DataObject obj) {
        java.net.URL url = null;
        if (obj != null) {
            url = TemplateWizard.getDescription (obj);
        }
        java.awt.CardLayout card = (java.awt.CardLayout)browserPanel.getLayout();
        if (url != null && 
            getExplorerManager().getSelectedNodes().length != 0) {
            if (browser != null) {
                browser.setURL(url);
                if (!browser.isVisible()) {
                    card.show (browserPanel, "browser"); // NOI18N
                }
            }
        } else {
            card.show (browserPanel, "noBrowser"); // NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        browserPanel = new javax.swing.JPanel();
        noBrowser = new javax.swing.JLabel();
        templatesLabel = new javax.swing.JLabel();
        browserLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(0, 0));
        browserPanel.setLayout(new java.awt.CardLayout());

        // same background as html browser to avoid flicking
        noBrowser.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        noBrowser.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        // bigger minimum size then usual to behave well in card
        // layout together with HtmlBrowser
        noBrowser.setMinimumSize(new java.awt.Dimension(0, 25));
        noBrowser.setOpaque(true);
        browserPanel.add(noBrowser, "noBrowser");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.5;
        add(browserPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(templatesLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(browserLabel, gridBagConstraints);

    }//GEN-END:initComponents


    private void nameFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFocusGained
    }//GEN-LAST:event_nameFocusGained
    private void templatesTreeValueChanged (javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_templatesTreeValueChanged
    }//GEN-LAST:event_templatesTreeValueChanged

    /** Handles explorer manager property changes. */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName() == ExplorerManager.PROP_SELECTED_NODES && listener != null) {
            listener.stateChanged (new ChangeEvent (this));
            
            updateDescription (template);
        }
    }


    private void packagesListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_packagesListValueChanged
    }//GEN-LAST:event_packagesListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel browserLabel;
    private javax.swing.JPanel browserPanel;
    private javax.swing.JLabel noBrowser;
    private javax.swing.JLabel templatesLabel;
    // End of variables declaration//GEN-END:variables
    private TemplatesTreeView treeView;
    private HtmlBrowser browser;

    /** Should the data object be displayed or not?
    * @param obj the data object
    * @return <CODE>true</CODE> if the object should be displayed,
    *    <CODE>false</CODE> otherwise
    */
    public boolean acceptDataObject(DataObject obj) {
        if (obj instanceof DataFolder) {
            Object o = obj.getPrimaryFile ().getAttribute ("simple"); // NOI18N
            return o == null || Boolean.TRUE.equals (o);
        } else {
            return obj.isTemplate();
        }
    }

    /** Prepares decription area with html browser inside.
     * Executed in other then event dispatch thread.
     */
    public void construct() {
        initData = new InitData();
        initData.browser = new HtmlBrowser(false, false);
        initData.browser.setName("browser");
        initData.noDescMsg = NbBundle.getBundle(TemplateWizard1.class).
                            getString("MSG_NoDescription");
        initData.noDescBorder = new EtchedBorder();

        // override the Swing default CSS to make the HTMLEditorKit use the
        // same font as the rest of the UI
        
        Component comp = initData.browser.getBrowserComponent();
        if (! (comp instanceof javax.swing.JEditorPane))
            return;

        javax.swing.text.EditorKit kit = ((javax.swing.JEditorPane) comp).getEditorKitForContentType("text/html"); // NOI18N
        if (! (kit instanceof javax.swing.text.html.HTMLEditorKit))
            return;

        javax.swing.text.html.HTMLEditorKit htmlkit = (javax.swing.text.html.HTMLEditorKit) kit;

        // XXX the style sheet is shared by all HTMLEditorKits.  We must
        // detect if it has been tweaked by ourselves or someone else
        // (template description for example) and avoid doing the same
        // thing again
        
        if (htmlkit.getStyleSheet().getStyleSheets() != null)
            return;
        
        javax.swing.text.html.StyleSheet css = new javax.swing.text.html.StyleSheet();
        java.awt.Font f = new javax.swing.JTextArea().getFont();
        css.addRule(new StringBuffer("body { font-size: ").append(f.getSize()) // NOI18N
                    .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
        css.addStyleSheet(htmlkit.getStyleSheet());
        htmlkit.setStyleSheet(css);
    }

    /** Fills description area using constructed data. Executed in event dispatch thread.
     */
    public void finished() {
        browser = initData.browser;
        browserLabel.setLabelFor(browser);
        browser.getAccessibleContext().setAccessibleName(browserLabel.getText());
        browserPanel.add(browser, "browser");
        updateDescription(template);
        // change loading text to no description text                
        // install same border as html browser have
        noBrowser.setText(initData.noDescMsg);
        noBrowser.setBorder(initData.noDescBorder);
        // we don't need initData anymore, make gc'able
        initData = null;
    }
    
    /** Helper implementation of WizardDescription.Panel for TemplateWizard.Panel1.
    * Provides the wizard panel with the current data--either
    * the default data or already-modified settings, if the user used the previous and/or next buttons.
    * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
    * @param settings the object representing wizard panel state, as originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}
    */
    void implReadSettings (Object settings) {
        TemplateWizard wizard = (TemplateWizard)settings;
        wizard.setTitle(org.openide.util.NbBundle.getBundle(TemplateWizard.class).getString("CTL_TemplateTitle"));
        updateRootNode (wizard.getTemplatesFolder ());

        template = wizard.getTemplate ();
        if (template != null && !template.isValid() ) {
            template = null;
        } 

        // now try to find out the path
        // a bit ugly code to do that
        DataObject obj = template;
        DataObject stop = wizard.getTemplatesFolder ();
        final LinkedList<String> names = new LinkedList<String>();
        for (;;) {
            if (obj == null) {
                // seems that the template is not one of templates
                break;
            }

            if (obj == stop) {
                // the last object found
                break;
            }

            String key = obj.getNodeDelegate().getName ();
            names.addFirst(key);
            obj = obj.getFolder();
        }

        RequestProcessor.getDefault ().post (new Runnable () {
            private Node selection;
            
            public void run () { 
                if (selection == null) {
                    // go thru all the nodes and find
                    Node node = getExplorerManager().getRootContext();
                    for (String name : names) {
                        node = node.getChildren ().findChild (name);
                        if (node == null) {
                            // end it
                            node = getExplorerManager().getRootContext();
                            break;
                        }
                    }
                    
                    selection = node;
                    
                    // execute the second pass
                    SwingUtilities.invokeLater (this);
                } else {
                    // second pass, executed in AWT thread
                    try {
                        getExplorerManager().setSelectedNodes(new Node[] {selection});
                    } catch (java.beans.PropertyVetoException evt) {
                        // ignore
                    }
                }
            }
        }, 300, Thread.MIN_PRIORITY);
    }

    /** Helper implementation of WizardDescription.Panel for TemplateWizard.Panel1.
    * Provides the wizard panel with the opportunity to update the
    * settings with its current customized state.
    * Rather than updating its settings with every change in the GUI, it should collect them,
    * and then only save them when requested to by this method.
    * Also, the original settings passed to {@link #readSettings} should not be modified (mutated);
    * rather, the (copy) passed in here should be mutated according to the collected changes.
    * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
    * @param settings the object representing a settings of the wizard
    */
    void implStoreSettings (Object settings) {
        if (template != null) {
            TemplateWizard wizard = (TemplateWizard)settings;
            if (wizard.getTemplate() != template) {
                // if template has changed then set target step names and index to default
                Component c = wizard.targetChooser().getComponent();
                if (c instanceof JComponent) {
                    ((JComponent)c).putClientProperty(PROP_CONTENT_DATA, new String[] { c.getName() });
                    ((JComponent)c).putClientProperty(PROP_CONTENT_SELECTED_INDEX, 0);
                }
            } else {
                // bugfix #27939, if template isn't changed and PROP_CONTENT_DATA no set => set it
                Component c = wizard.targetChooser().getComponent();
                if (c instanceof JComponent) {
                    if (((JComponent)c).getClientProperty (PROP_CONTENT_DATA) == null) {
                        ((JComponent)c).putClientProperty(PROP_CONTENT_DATA, new String[] { c.getName() });
                        ((JComponent)c).putClientProperty(PROP_CONTENT_SELECTED_INDEX, 0);
                    }
                }
            }
            wizard.setTemplateImpl (template, false);
        }
    }

    /** Helper implementation of WizardDescription.Panel for TemplateWizard.Panel1.
    * Test whether the panel is finished and it is safe to proceed to the next one.
    * If the panel is valid, the "Next" (or "Finish") button will be enabled.
    * @return <code>true</code> if the user has entered satisfactory information
    */
    boolean implIsValid () {
        boolean enable = false;
        Node[] n = getExplorerManager().getSelectedNodes();
        if (n.length == 1) {
            template = n[0].getCookie(DataObject.class);
            enable = template != null && template.isTemplate();
        }
        return enable;
    }

    /** Add a listener to changes of the panel's validity.
    * @param l the listener to add
    * @see #isValid
    */
    void addChangeListener (ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();
        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
    * @param l the listener to remove
    */
    void removeChangeListener (ChangeListener l) {
        listener = null;
    }

    
    /** Model for displaying only objects till template.
    */
    private static final class TemplatesModel extends NodeTreeModel {
        TemplatesModel() {}
        
        public int getChildCount (Object o) {
            Node n = Visualizer.findNode(o);
            DataObject obj = n.getCookie(DataObject.class);

            return obj == null || obj.isTemplate () ? 0 : super.getChildCount (o);
        }

        public boolean  isLeaf (Object o) {
            Node n = Visualizer.findNode(o);
            DataObject obj = n.getCookie(DataObject.class);

            return obj == null || obj.isTemplate ();
        }
    }

    /** Specialized tree view for templates, non editable and with proper
     * border */
    private static final class TemplatesTreeView extends BeanTreeView {
        TemplatesTreeView() {
            tree.setEditable(false);
            //#219709 - workaround for JDK bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8003400
            tree.setLargeModel( false );
            // install proper border
            setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        }
        
        protected NodeTreeModel createModel() {
            return new TemplatesModel();
        }
    }
}
