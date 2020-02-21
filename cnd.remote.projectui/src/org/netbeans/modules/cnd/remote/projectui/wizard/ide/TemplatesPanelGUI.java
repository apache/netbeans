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
package org.netbeans.modules.cnd.remote.projectui.wizard.ide;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.ListView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public class TemplatesPanelGUI extends javax.swing.JPanel implements PropertyChangeListener {
    
    public static interface Builder extends ActionListener {

        public Children createCategoriesChildren (DataFolder folder);
        
        public Children createTemplatesChildren (DataFolder folder);
        
        public String getCategoriesName ();
        
        public String getTemplatesName ();
        
        public void fireChange ();
    }
    
    public static final String TEMPLATES_FOLDER = "templatesFolder";        //NOI18N
    public static final String TARGET_TEMPLATE = "targetTemplate";          //NOI18N
    private static final String ATTR_INSTANTIATING_DESC = "instantiatingWizardURL"; //NOI18N
    private static final Image PLEASE_WAIT_ICON = ImageUtilities.loadImage ("org/netbeans/modules/project/ui/resources/wait.gif"); // NOI18N
    
    private Builder firer;

    private String presetTemplateName = null;
    private Node pleaseWait;

    /** Creates new form TemplatesPanelGUI */
    public TemplatesPanelGUI (Builder firer) {
        assert firer != null : "Builder can not be null";  //NOI18N
        this.firer = firer;
        initComponents();
        postInitComponents ();
        setName (NbBundle.getMessage(TemplatesPanelGUI.class, "TXT_SelectTemplate")); // NOI18N
    }

    public void setTemplatesFolder (final FileObject folder) {
        final DataFolder dobj = DataFolder.findFolder (folder);
        dobj.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        TemplatesPanelGUI.this.setSelectedCategoryByName(OpenProjectListSettings.getInstance().getLastSelectedProjectCategory());
                    }
                });
            }
        });
        ((ExplorerProviderPanel)this.categoriesPanel).setRootNode(new FilterNode (
            dobj.getNodeDelegate(), this.firer.createCategoriesChildren(dobj)));
    }

    public void setSelectedCategoryByName (final String categoryName) {
        if (categoryName != null) {
            try {
                ((TemplatesPanelGUI.ExplorerProviderPanel) this.categoriesPanel).setSelectedNode(categoryName);
                //expand explicitly selected category
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Node[] sel = ((ExplorerProviderPanel)categoriesPanel).getSelectedNodes();
                        if( sel.length == 1 )
                            ((CategoriesPanel)categoriesPanel).btv.expandNode(sel[0]);
                    }
                });
            }
            catch (NodeNotFoundException ex) {
                // if categoryName is null then select first category leastwise
                ((CategoriesPanel)this.categoriesPanel).selectFirstCategory ();
            }
        } else {
            // if categoryName is null then select first category leastwise
            ((CategoriesPanel)this.categoriesPanel).selectFirstCategory ();
        }
    }
    
    public String getSelectedCategoryName () {
        return ((ExplorerProviderPanel)this.categoriesPanel).getSelectionPath();
    }
    
    public void setSelectedTemplateByName (final String templateName) {
        presetTemplateName = templateName;
        final TemplatesPanel tempExplorer = ((TemplatesPanel)this.projectsPanel);
    
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                if (templateName != null) {
                    try {
                        tempExplorer.setSelectedNode(templateName);
                    }
                    catch (NodeNotFoundException ex) {
                        //ignore here..
                    }
                    if (tempExplorer.getSelectionPath() == null) {
                        presetTemplateName = null;
                        tempExplorer.selectFirstTemplate();
                    }
                } else {
                    tempExplorer.selectFirstTemplate ();
                }
            }
        });

    }
    
    public String getSelectedTemplateName () {
        return ((TemplatesPanel)this.projectsPanel).getSelectionPath();
    }
    
    public FileObject getSelectedTemplate () {
        Node[] nodes = ((ExplorerProviderPanel) this.projectsPanel).getSelectedNodes();
        if (nodes != null && nodes.length == 1) {
            DataObject dobj = nodes[0].getCookie(DataObject.class);
            if (dobj != null) {
                while (dobj instanceof DataShadow) {
                    dobj = ((DataShadow)dobj).getOriginal();
                }
                return dobj.getPrimaryFile();
            }
        }
        return null;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        jScrollPane1.setViewportView(description);
        jLabel3.setLabelFor(description);
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        jScrollPane1.setViewportView(null);
        jLabel3.setLabelFor(null);
    }

    @Override
    public void propertyChange (PropertyChangeEvent event) {
        if (event.getSource() == this.categoriesPanel) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (event.getPropertyName ())) {
                Node[] selectedNodes = (Node[]) event.getNewValue();
                if (selectedNodes != null && selectedNodes.length == 1) {
                    assert pleaseWait == null || !pleaseWait.equals (selectedNodes[0]) : "Cannot be fired a propertyChange with PleaseWaitNode, but was " + selectedNodes[0]; 
                    try {
                        ((ExplorerProviderPanel)this.projectsPanel).setSelectedNodes(new Node[0]);
                    } catch (PropertyVetoException e) {
                        /*Ignore it*/
                    }
                    DataObject template = (DataObject) selectedNodes[0].getCookie(DataFolder.class);
                    if (template != null) {
                        if (!template.isValid()) {
                            // in ergonomics IDE, it is possible that dataObject
                            // no longer valid. If so, try to find it again
                            // (layer was replaced.)
                            FileObject fo = template.getPrimaryFile();
                            fo = FileUtil.getConfigFile(fo.getPath());
                            if (fo != null) {
                                try {
                                    template = DataObject.find(fo);
                                } catch (DataObjectNotFoundException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                        ((ExplorerProviderPanel)this.projectsPanel).setRootNode(
                            new FilterNode (selectedNodes[0], this.firer.createTemplatesChildren((DataFolder)template)));
                        // after change of root select the first template to make easy move in wizard
                        this.setSelectedTemplateByName (presetTemplateName);
                    }
                }
            }
        }
        else if (event.getSource() == this.projectsPanel) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (event.getPropertyName())) {
                Node[] selectedNodes = (Node[]) event.getNewValue ();
                if (selectedNodes != null && selectedNodes.length == 1) {
                    DataObject template = selectedNodes[0].getCookie(DataObject.class);
                    if (template != null) {
                        URL descURL = getDescription (template);
                        if (descURL != null) {
                            try {
                                //this.description.setPage (descURL);
                                // Set page does not work well if there are mutiple calls to that
                                // see issue #49067. This is a hotfix for the bug which causes                                
                                // synchronous loading of the content. It should be improved later 
                                // by doing it in request processor.
                                
                                //this.description.read( descURL.openStream(), descURL );
                                // #52801: handlig changed charset
                                String charset = findEncodingFromURL (descURL.openStream ());
                                ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "Url " + descURL + " has charset " + charset); // NOI18N
                                if (charset != null) {
                                    description.putClientProperty ("charset", charset); // NOI18N
                                }
                                this.description.read( descURL.openStream(), descURL );
                            } catch (ChangedCharSetException x) {
                                Document doc = description.getEditorKit ().createDefaultDocument ();
                                doc.putProperty ("IgnoreCharsetDirective", Boolean.valueOf (true)); // NOI18N
                                try {
                                    description.read (descURL.openStream (), doc);
                                } catch (IOException ioe) {
                                    ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
                                    this.description.setText (NbBundle.getBundle (TemplatesPanelGUI.class).getString ("TXT_NoDescription")); // NOI18N
                                }
                            } catch (IOException e) {
                                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
                                this.description.setText (NbBundle.getBundle (TemplatesPanelGUI.class).getString ("TXT_NoDescription")); // NOI18N
                            }
                        }
                        else {
                            this.description.setText (NbBundle.getBundle (TemplatesPanelGUI.class).getString ("TXT_NoDescription")); // NOI18N
                        }
                    }                    
                } else {
                    // bugfix #46738, Description in New Project dialog doesn't show description of selected categories
                    this.description.setText (NbBundle.getBundle (TemplatesPanelGUI.class).getString ("TXT_NoDescription")); // NOI18N
                }
                this.firer.fireChange ();
            }
        }
    }
        
    private void postInitComponents () {        
        Mnemonics.setLocalizedText(jLabel1, this.firer.getCategoriesName());
        Mnemonics.setLocalizedText(jLabel2, this.firer.getTemplatesName());
        this.description.setEditorKit(new HTMLEditorKit());
        description.setBackground(getBackground());
        description.setForeground(getForeground());
        description.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        // please wait node, see issue 52900
        pleaseWait = new AbstractNode (Children.LEAF) {
            @Override
            public Image getIcon (int ignore) {
                return PLEASE_WAIT_ICON;
            }
        };
        pleaseWait.setName (NbBundle.getBundle (TemplatesPanelGUI.class).getString ("LBL_TemplatesPanel_PleaseWait"));
        Children ch = new Children.Array ();
        ch.add (new Node[] {pleaseWait});
        final Node root = new AbstractNode (ch);
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                ((ExplorerProviderPanel)categoriesPanel).setRootNode (root);
            }
        });
        ((ExplorerProviderPanel)projectsPanel).addDefaultActionListener( firer );
        description.addHyperlinkListener(new ClickHyperlinks());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        categoriesPanel = new CategoriesPanel ();
        projectsPanel = new TemplatesPanel ();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        description = new javax.swing.JEditorPane();

        setPreferredSize(new java.awt.Dimension(500, 230));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(categoriesPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(TemplatesPanelGUI.class).getString("CTL_Categories")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(projectsPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(TemplatesPanelGUI.class).getString("CTL_Templates")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 6, 6);
        add(categoriesPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 6, 0);
        add(projectsPanel, gridBagConstraints);

        jLabel3.setLabelFor(description);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getBundle(TemplatesPanelGUI.class).getString("CTL_Description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(jLabel3, gridBagConstraints);

        description.setEditable(false);
        description.setText(org.openide.util.NbBundle.getBundle(TemplatesPanelGUI.class).getString("TXT_NoDescription")); // NOI18N
        description.setPreferredSize(new java.awt.Dimension(100, 66));
        jScrollPane1.setViewportView(description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private static final class ClickHyperlinks implements HyperlinkListener {
        public @Override void hyperlinkUpdate(HyperlinkEvent evt) {
            if (EventType.ACTIVATED == evt.getEventType() && evt.getURL() != null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(evt.getURL());
            }
        }
    }

    private URL getDescription (DataObject dobj) {
        //XXX: Some templates are using templateWizardURL others instantiatingWizardURL. What is correct?
        FileObject fo = dobj.getPrimaryFile();
        URL desc = (URL) fo.getAttribute(ATTR_INSTANTIATING_DESC);
        if (desc != null) {
            return desc;
        }
        desc = TemplateWizard.getDescription (dobj);
        return desc;
    }
    
    private static abstract class ExplorerProviderPanel extends JPanel implements ExplorerManager.Provider, PropertyChangeListener, VetoableChangeListener {
        
        private ExplorerManager manager;
        
        protected ExplorerProviderPanel () {           
            this.manager = new ExplorerManager ();
            this.manager.addPropertyChangeListener(this);
            this.manager.addVetoableChangeListener(this);
            this.initGUI ();
        }
                
        public void setRootNode (Node node) {
            this.manager.setRootContext(node);
        }
        
        public Node getRootNode () {
            return this.manager.getRootContext();
        }
        
        public Node[] getSelectedNodes () {
            return this.manager.getSelectedNodes();
        }
        
        public void setSelectedNodes (Node[] nodes) throws PropertyVetoException {
            this.manager.setSelectedNodes(nodes);
        }
        
        public void setSelectedNode (String path) throws NodeNotFoundException {
            if (path == null) {
                return;
            }
            StringTokenizer tk = new StringTokenizer (path,"/");    //NOI18N
            String[] names = new String[tk.countTokens()];
            for (int i=0;tk.hasMoreTokens();i++) {
                names[i] = tk.nextToken();
            }
            try {
                Node node = NodeOp.findPath(this.manager.getRootContext(),names);
                if (node != null) {
                    this.manager.setSelectedNodes(new Node[] {node});
                }
            } catch (PropertyVetoException e) {
                //Skip it, not important
            }
        }
        
        public String getSelectionPath () {
            Node[] selectedNodes = this.manager.getSelectedNodes();
            if (selectedNodes == null || selectedNodes.length != 1) {
                return null;
            }
            Node rootNode = this.manager.getRootContext();
            String[] path = NodeOp.createPath(selectedNodes[0],rootNode);
            StringBuilder builder = new StringBuilder ();
            for (int i=0; i< path.length; i++) {
                builder.append('/');        //NOI18N
                builder.append(path[i]);
            }
            return builder.substring(1);
        }
        
        @Override
        public ExplorerManager getExplorerManager() {
            return this.manager;
        }
        
     
        @Override
        public void propertyChange (final PropertyChangeEvent event) {
            // workaround of issue 43502, update of Help button set back the focus
            // to component which is active when this change starts
            //XXX: this workaround causes problems in the selection of templates
            // and should be removed, this workaround can be workarounded in the
            // setSelectedTemplateByName when template name is null
            // select the first template only if no template is already selected,
            // but nicer solution is to remove this workaround at all.
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    firePropertyChange(event.getPropertyName(),
                        event.getOldValue(), event.getNewValue());            
                     }
            });
        }
        
        
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName())) {
                Node[] newValue = (Node[]) evt.getNewValue();
                if (newValue == null || (newValue.length != 1 && newValue.length != 0)) {
                    throw new PropertyVetoException ("Invalid length",evt);      //NOI18N
                }
            }
        }
        
        @Override
        public void requestFocus () {
            this.createComponent().requestFocus();
        }
        
        protected abstract JComponent createComponent ();
        
        private void initGUI () {
            this.setLayout (new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints ();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.weighty = 1.0;
            JComponent component = this.createComponent ();
            ((GridBagLayout)this.getLayout()).setConstraints(component, c);
            this.add (component);
        }

        void addDefaultActionListener( ActionListener al ) {
            //do nothing by default
        }
    }


    private static class CategoriesBeanTreeView extends BeanTreeView {
        public CategoriesBeanTreeView () {
            super ();
            this.tree.setEditable(false);
        }
        public void selectFirstCategory () {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    tree.setSelectionRow (0);
                }
            });
        }
    }

    private static final class CategoriesPanel extends ExplorerProviderPanel {

        private CategoriesBeanTreeView btv;

        @Override
        protected synchronized JComponent createComponent () {
            if (this.btv == null) {
                this.btv = new CategoriesBeanTreeView ();
                this.btv.setRootVisible(false);
                this.btv.setPopupAllowed(false);
                this.btv.setFocusable(false);
                this.btv.setDefaultActionAllowed(false);
                this.btv.getAccessibleContext ().setAccessibleName (NbBundle.getMessage (TemplatesPanelGUI.class, "ACSN_CategoriesPanel")); // NOI18N
                this.btv.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (TemplatesPanelGUI.class, "ACSD_CategoriesPanel")); // NOI18N
                Border b = (Border)UIManager.get("Nb.ScrollPane.border"); // NOI18N
                if (b != null) {
                    this.btv.setBorder(b); 
                }
            }
            return this.btv;
        }
        
        public void selectFirstCategory () {
            btv.selectFirstCategory ();
        }

    }
    
    private static class TemplatesListView extends ListView implements ActionListener {
        public TemplatesListView () {
            super ();
            // bugfix #44717, Enter key must work regardless if TemplatesPanels is focused
            list.unregisterKeyboardAction (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0, false));
            setDefaultProcessor( this );
            ToolTipManager.sharedInstance ().unregisterComponent (list);
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            // Do nothing
        }
    }
    
    private static final class TemplatesPanel extends ExplorerProviderPanel {
        
        private ListView list;

        @Override
        protected synchronized JComponent createComponent () {            
            if (this.list == null) {
                this.list = new TemplatesListView ();
                this.list.setPopupAllowed(false);
                this.list.getAccessibleContext ().setAccessibleName (NbBundle.getMessage (TemplatesPanelGUI.class, "ACSN_TemplatesPanel")); // NOI18N
                this.list.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (TemplatesPanelGUI.class, "ACSD_TemplatesPanel")); // NOI18N
                Border b = (Border)UIManager.get("Nb.ScrollPane.border"); //NOI18N
                if (b != null) {
                    this.list.setBorder(b); // NOI18N
                }
            }
            
            return this.list;
        }
        
        public void selectFirstTemplate () {
            try {
                Children ch = getExplorerManager ().getRootContext ().getChildren ();
                if (ch.getNodesCount () > 0) {
                    getExplorerManager ().setSelectedNodes (new Node[] { ch.getNodes ()[0] });
                }
            } catch (PropertyVetoException pve) {
                // doesn't matter, can ignore it
            }
        }

        @Override
        void addDefaultActionListener( ActionListener al ) {
            createComponent();
            ((TemplatesListView)list).setDefaultProcessor( al );
        }
    }
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoriesPanel;
    private javax.swing.JEditorPane description;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel projectsPanel;
    // End of variables declaration//GEN-END:variables


    void warmUp (FileObject templatesFolder) {
        if (templatesFolder != null) {
            DataFolder df = DataFolder.findFolder (templatesFolder);
            if (df != null) {
                df.getChildren();
            }
        }
    }

    void doFinished (FileObject temlatesFolder, String category, String template) {
        assert temlatesFolder != null;
        
        this.categoriesPanel.addPropertyChangeListener(this);                        
        this.projectsPanel.addPropertyChangeListener(this);
        
        this.setTemplatesFolder (temlatesFolder);
        this.setSelectedCategoryByName (category);
        this.setSelectedTemplateByName (template);
        categoriesPanel.requestFocus ();
        if (description.getEditorKit() instanceof HTMLEditorKit) {
            // override the Swing default CSS to make the HTMLEditorKit use the
            // same font as the rest of the UI.

            // XXX the style sheet is shared by all HTMLEditorKits.  We must
            // detect if it has been tweaked by ourselves or someone else
            // (code completion javadoc popup for example) and avoid doing the
            // same thing again
            
            HTMLEditorKit htmlkit = (HTMLEditorKit) description.getEditorKit();
            StyleSheet css = htmlkit.getStyleSheet();
            if (css.getStyleSheets() != null)
                return;

            StyleSheet css2 = new StyleSheet();
            Font f = jLabel1.getFont();
            css2.addRule(new StringBuffer("body { font-size: ").append(f.getSize()) // NOI18N
                        .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css2.addStyleSheet(css);
            htmlkit.setStyleSheet(css2);
        }
    }

    // encoding support; copied from html/HtmlEditorSupport
    private static String findEncodingFromURL (InputStream stream) {
        try {
            byte[] arr = new byte[4096];
            int len = stream.read (arr, 0, arr.length);
            String txt = new String (arr, 0, (len>=0)?len:0).toUpperCase();
            // encoding
            return findEncoding (txt);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    /** Tries to guess the mime type from given input stream. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    private static String findEncoding (String txt) {
        int headLen = txt.indexOf ("</HEAD>"); // NOI18N
        if (headLen == -1) headLen = txt.length ();
        
        int content = txt.indexOf ("CONTENT-TYPE"); // NOI18N
        if (content == -1 || content > headLen) {
            return null;
        }
        
        int charset = txt.indexOf ("CHARSET=", content); // NOI18N
        if (charset == -1) {
            return null;
        }
        
        int charend = txt.indexOf ('"', charset);
        int charend2 = txt.indexOf ('\'', charset);
        if (charend == -1 && charend2 == -1) {
            return null;
        }

        if (charend2 != -1) {
            if (charend == -1 || charend > charend2) {
                charend = charend2;
            }
        }
        
        return txt.substring (charset + "CHARSET=".length (), charend); // NOI18N
    }
    
}
