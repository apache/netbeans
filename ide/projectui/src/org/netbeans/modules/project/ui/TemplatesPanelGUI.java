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

package org.netbeans.modules.project.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
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
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.TreePath;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.awt.QuickSearch;
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
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

import static org.netbeans.modules.project.ui.Bundle.*;

/**
 *
 * @author  tom
 */
public class TemplatesPanelGUI extends javax.swing.JPanel implements PropertyChangeListener {
    
    public static interface Builder extends ActionListener {

        public Children createCategoriesChildren (DataFolder folder, String filterText);
        
        public Children createTemplatesChildren (DataFolder folder, String filterText);
        
        public String getCategoriesName ();
        
        public String getTemplatesName ();
        
        public void fireChange ();
    }
    
    public static final String TEMPLATES_FOLDER = "templatesFolder";        //NOI18N
    public static final String TARGET_TEMPLATE = "targetTemplate";          //NOI18N
    private static final String ATTR_INSTANTIATING_DESC = "instantiatingWizardURL"; //NOI18N
    private static final @StaticResource String WAIT = "org/netbeans/modules/project/ui/resources/wait.gif";
    private static final Image PLEASE_WAIT_ICON = ImageUtilities.loadImage (WAIT); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(TemplatesPanelGUI.class);
    
    private Builder firer;

    private String presetTemplateName = null;
    private Node pleaseWait;
    private WizardDescriptor wiz;

    private String filterText;

    @Messages("TXT_SelectTemplate=Select Project")
    public TemplatesPanelGUI (Builder firer) {
        assert firer != null : "Builder can not be null";  //NOI18N
        this.firer = firer;
        initComponents();
        postInitComponents ();
        setName(TXT_SelectTemplate());

        QuickSearch quickSearch = QuickSearch.attach( panelFilter, BorderLayout.CENTER, createQuickSearchCallback(), true );
        adjustQuickSearch( quickSearch );
//                    @Override
//                    public void run() {
//                    }
//                });
//            }
//        });
        

        //Hack to add a text label to the quicksearch
        Component qsComponent = panelFilter.getComponent( 0 );
        if( qsComponent instanceof JComponent ) {
            for( Component c : ((JComponent)qsComponent).getComponents() ) {
                if( c instanceof JLabel ) {
                    JLabel jLabel = (JLabel) c;
                    String text = org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "LBL_TemplateChooserPanelGUI_QuicksearchLabel");
                    Mnemonics.setLocalizedText(jLabel, text);
    }
            }
        }
    }

    public void setTemplatesFolder (final FileObject folder) {
        final DataFolder dobj = DataFolder.findFolder (folder);
        dobj.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        setSelectedCategoryByName(OpenProjectListSettings.getInstance().getLastSelectedProjectCategory());
                    }
                });
            }
        });
        ((ExplorerProviderPanel)this.categoriesPanel).setRootNode(new FilterNode (
            dobj.getNodeDelegate(), this.firer.createCategoriesChildren(dobj, filterText)));
    }

    private FileObject getCurrentTemplatesFolder() {
        FileObject res = null;
        Node rootNode = ((ExplorerProviderPanel)categoriesPanel).getExplorerManager().getRootContext();
        if( null != rootNode ) {
            DataObject dob = rootNode.getLookup().lookup( DataObject.class );
            if( null != dob ) {
                res = dob.getPrimaryFile();
            }
        }
        return res;
    }

    public void setSelectedCategoryByName (final String categoryName) {
        if (categoryName != null) {
            ((org.netbeans.modules.project.ui.TemplatesPanelGUI.ExplorerProviderPanel) this.categoriesPanel).setSelectedNode(categoryName);
            //expand explicitly selected category
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    Node[] sel = ((ExplorerProviderPanel)categoriesPanel).getSelectedNodes();
                    if (sel.length == 1) {
                        ((CategoriesPanel) categoriesPanel).btv.expandNode(sel[0]);
                    }
                }
            });
        } else {
            // if categoryName is null then select first category leastwise
            ((CategoriesPanel)this.categoriesPanel).selectFirst ();
        }
    }
    
    public String getSelectedCategoryName () {
        return ((ExplorerProviderPanel)this.categoriesPanel).getSelectionPath();
    }
    
    public void setSelectedTemplateByName (final String templateName) {
        presetTemplateName = templateName;
        final TemplatesPanel tempExplorer = ((TemplatesPanel)this.projectsPanel);
    
        SwingUtilities.invokeLater (new Runnable () {
            @Override public void run () {
                if (templateName != null) {
                    tempExplorer.setSelectedNode(templateName);
                    if (tempExplorer.getSelectionPath() == null) {
                        presetTemplateName = null;
                        tempExplorer.selectFirst();
                    }
                } else {
                    tempExplorer.selectFirst ();
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
            DataObject dobj = nodes[0].getLookup().lookup(DataObject.class);
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

    void setWizardDescriptor(WizardDescriptor wiz) {
        this.wiz = wiz;
    }

    @Messages("TemplatesPanelGUI_note_samples=<html>Note that samples are instructional and may not include all<br>security mechanisms required for a production environment.</html>")
    @Override public void propertyChange (PropertyChangeEvent event) {
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
                    DataObject template = (DataObject) selectedNodes[0].getLookup().lookup(DataFolder.class);
                    if (template != null) {
                        FileObject fo = template.getPrimaryFile();
                        String templatePath = fo.getPath();
                        if (!template.isValid()) {
                            // in ergonomics IDE, it is possible that dataObject
                            // no longer valid. If so, try to find it again
                            // (layer was replaced.)
                            fo = FileUtil.getConfigFile(templatePath);
                            if (fo != null) {
                                try {
                                    template = DataObject.find(fo);
                                } catch (DataObjectNotFoundException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                        ((ExplorerProviderPanel)this.projectsPanel).setRootNode(
                            new FilterNode (selectedNodes[0], this.firer.createTemplatesChildren((DataFolder)template, filterText)));
                        // after change of root select the first template to make easy move in wizard
                        this.setSelectedTemplateByName (presetTemplateName);
                        if (wiz != null) {
                            if (templatePath.matches("Templates/Project/Samples($|/.+)")) {
                                wiz.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, TemplatesPanelGUI_note_samples());
                            } else {
                                wiz.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
                            }
                        }
                    }
                }
            }
        }
        else if (event.getSource() == this.projectsPanel) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (event.getPropertyName())) {
                Node[] selectedNodes = (Node[]) event.getNewValue ();
                if (selectedNodes != null && selectedNodes.length == 1) {
                    DataObject template = selectedNodes[0].getLookup().lookup(DataObject.class);
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
                                    this.description.setText(null);
                                }
                            } catch (IOException e) {
                                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
                                this.description.setText(null);
                            }
                            description.setCaretPosition(0);
                        }
                        else {
                            this.description.setText(null);
                        }
                    }                    
                } else {
                    // bugfix #46738, Description in New Project dialog doesn't show description of selected categories
                    this.description.setText(null);
                }
                this.firer.fireChange ();
            }
        }
    }
        
    @Messages("LBL_TemplatesPanel_PleaseWait=Please wait...")
    private void postInitComponents () {        
        Mnemonics.setLocalizedText(jLabel1, this.firer.getCategoriesName());
        Mnemonics.setLocalizedText(jLabel2, this.firer.getTemplatesName());
        this.description.setEditorKit(new HTMLEditorKit());
        description.putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );

        // please wait node, see issue 52900
        pleaseWait = new AbstractNode (Children.LEAF) {
            @Override
            public Image getIcon (int ignore) {
                return PLEASE_WAIT_ICON;
            }
        };
        pleaseWait.setName(LBL_TemplatesPanel_PleaseWait());
        Children ch = new Children.Array ();
        ch.add (new Node[] {pleaseWait});
        final Node root = new AbstractNode (ch);
        SwingUtilities.invokeLater (new Runnable () {
            @Override public void run() {
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
        panelFilter = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(500, 230));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(categoriesPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TemplatesPanelGUI.class, "CTL_Categories")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(projectsPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TemplatesPanelGUI.class, "CTL_Templates")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 6, 6);
        add(categoriesPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 6, 0);
        add(projectsPanel, gridBagConstraints);

        jLabel3.setLabelFor(description);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TemplatesPanelGUI.class, "CTL_Description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(jLabel3, gridBagConstraints);

        description.setEditable(false);
        description.setPreferredSize(new java.awt.Dimension(100, 66));
        jScrollPane1.setViewportView(description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        panelFilter.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(panelFilter, gridBagConstraints);
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
    
    private abstract static class ExplorerProviderPanel extends JPanel implements ExplorerManager.Provider, PropertyChangeListener, VetoableChangeListener {
        
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
        
        public void setSelectedNode (String path) {
            if (path == null) {
                return;
            }
            StringTokenizer tk = new StringTokenizer (path,"/");    //NOI18N
            final String[] names = new String[tk.countTokens()];
            for (int i=0;tk.hasMoreTokens();i++) {
                names[i] = tk.nextToken();
            }
            RP.post(new Runnable() {
                @Override public void run() {
                    try {
                        Node node = NodeOp.findPath(manager.getRootContext(), names);
                        if (node != null) {
                            setSelectedNodes(new Node[] {node});
                        }
                    } catch (PropertyVetoException e) {
                        //Skip it, not important
                    } catch (NodeNotFoundException x) {
                        // OK, never mind
                    }
                }
            });
        }
        
        public String getSelectionPath () {
            Node[] selectedNodes = this.manager.getSelectedNodes();
            if (selectedNodes == null || selectedNodes.length != 1) {
                return null;
            }
            Node rootNode = this.manager.getRootContext();
            String[] path = NodeOp.createPath(selectedNodes[0],rootNode);
            StringBuilder builder = new StringBuilder();
            for (int i=0; i< path.length; i++) {
                builder.append('/');        //NOI18N
                builder.append(path[i]);
            }
            assert builder.length() > 1 : "NodeOp.createPath() returned empty path for node " + selectedNodes[0] + " with root node " + rootNode; // NOI18N
            return builder.length() > 1 ? builder.substring(1) : null;
        }
        
        @Override public ExplorerManager getExplorerManager() {
            return this.manager;
        }
        
     
        @Override public void propertyChange(final PropertyChangeEvent event) {
            // workaround of issue 43502, update of Help button set back the focus
            // to component which is active when this change starts
            //XXX: this workaround causes problems in the selection of templates
            // and should be removed, this workaround can be workarounded in the
            // setSelectedTemplateByName when template name is null
            // select the first template only if no template is already selected,
            // but nicer solution is to remove this workaround at all.
            SwingUtilities.invokeLater (new Runnable () {
                @Override public void run() {
                    firePropertyChange(event.getPropertyName(),
                        event.getOldValue(), event.getNewValue());            
                     }
            });
        }
        
        
        @Override public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
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

        public void selectFirst() {
            RP.post(new Runnable() {
                @Override public void run() {
                    final Children ch = getRootNode().getChildren();
                    // XXX what is the best way to wait for >0 node to appear without necessarily waiting for them all?
                    if (ch.getNodesCount(true) > 0) { // blocks
                        EventQueue.invokeLater(new Runnable() { // #210326
                            @Override public void run() {
                                if (getSelectedNodes().length == 0) { // last minute
                                    try {
                                        getExplorerManager().setSelectedNodes(new Node[] {ch.getNodeAt(0)});
                                    } catch (PropertyVetoException x) {
                                        Logger.getLogger(TemplatesPanelGUI.class.getName()).log(Level.INFO, "race condition while selecting first of " + getRootNode(), x);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }

    }


    private static class CategoriesBeanTreeView extends BeanTreeView {
        CategoriesBeanTreeView() {
            this.tree.setEditable(false);
            //#219709 - workaround for JDK bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8003400
            tree.setLargeModel( false );
        }

        @Override
        protected void showSelection(TreePath[] treePaths) {
            //#240260 - need to ajust path bounds to show the selected node completely.
            //Most likely the tree model includes 'please wait' node that the path bounds are calculated.
            //So when the wait node is removed later on the bounds are actually invalid.
            tree.getSelectionModel().setSelectionPaths(treePaths);

            if (treePaths.length == 1) {
                showPathWithoutExpansion(treePaths[0]);
            }
        }

        /** Make a path visible.
        * @param path the path
        */
        private void showPathWithoutExpansion(TreePath path) {
            Rectangle rect = tree.getPathBounds(path);
            if (rect != null) { //PENDING
                TreeUI tmp = tree.getUI();
                int correction = 0;
                if (tmp instanceof BasicTreeUI) {
                    correction = ((BasicTreeUI) tmp).getLeftChildIndent();
                    correction += ((BasicTreeUI) tmp).getRightChildIndent();
                }
                rect.x = Math.max(0, rect.x - correction);
                rect.y += rect.height;
                if (rect.y >= 0) { //#197514 - do not scroll to negative y values
                    tree.scrollRectToVisible(rect);
                }
            }
        }
        
    }

    private static final class CategoriesPanel extends ExplorerProviderPanel {

        private CategoriesBeanTreeView btv;

        @Messages({
            "ACSN_CategoriesPanel=Categories of types new objects",
            "ACSD_CategoriesPanel=List of categories of new objects which can be choosen"
        })
        @Override protected synchronized JComponent createComponent() {
            if (this.btv == null) {
                this.btv = new CategoriesBeanTreeView ();
                this.btv.setRootVisible(false);
                this.btv.setPopupAllowed(false);
                this.btv.setFocusable(false);
                this.btv.setDefaultActionAllowed(false);
                this.btv.getAccessibleContext().setAccessibleName(ACSN_CategoriesPanel());
                this.btv.getAccessibleContext().setAccessibleDescription(ACSD_CategoriesPanel());
                this.btv.setDragSource(false);
                this.btv.setDropTarget(false);
                Border b = (Border)UIManager.get("Nb.ScrollPane.border"); // NOI18N
                if (b != null) {
                    this.btv.setBorder(b); 
                }
            }
            return this.btv;
        }
        
    }
    
    private static class TemplatesListView extends ListView implements ActionListener {
        TemplatesListView() {
            super ();
            // bugfix #44717, Enter key must work regardless if TemplatesPanels is focused
            list.unregisterKeyboardAction (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0, false));
            setDefaultProcessor( this );
            ToolTipManager.sharedInstance ().unregisterComponent (list);
        }

        @Override public void actionPerformed(ActionEvent e) {
            // Do nothing
        }
    }
    
    private static final class TemplatesPanel extends ExplorerProviderPanel {
        
        private ListView list;

        @Messages({
            "ACSN_TemplatesPanel=Types of new objects",
            "ACSD_TemplatesPanel=List of types of new objects which can be choosen"
        })
        @Override protected synchronized JComponent createComponent() {
            if (this.list == null) {
                this.list = new TemplatesListView ();
                this.list.setPopupAllowed(false);
                this.list.getAccessibleContext().setAccessibleName(ACSN_TemplatesPanel());
                this.list.getAccessibleContext().setAccessibleDescription(ACSD_TemplatesPanel());
                Border b = (Border)UIManager.get("Nb.ScrollPane.border");
                if (b != null) {
                    this.list.setBorder(b); // NOI18N
                }
            }
            
            return this.list;
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
    private javax.swing.JPanel panelFilter;
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
            if (css.getStyleSheets() != null) {
                description.setFont( jLabel1.getFont() );
                return;
            }

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
            String txt = new String(arr, 0, (len >= 0 ) ? len : 0, StandardCharsets.ISO_8859_1).toUpperCase(Locale.ENGLISH);
            // encoding
            return findEncoding (txt);
        } catch (IOException x) {
            Logger.getLogger(TemplatesPanelGUI.class.getName()).log(Level.INFO, null, x);
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
        if (headLen == -1) {
            headLen = txt.length();
        }
        
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

    private QuickSearch.Callback createQuickSearchCallback() {
        return new QuickSearch.Callback() {

            @Override
            public void quickSearchUpdate( String searchText ) {
                if( null != searchText )
                    searchText = searchText.toLowerCase();
                filterText = searchText;
                refreshContent();
            }

            @Override
            public void showNextSelection( boolean forward ) {
            }

            @Override
            public String findMaxPrefix( String prefix ) {
                return prefix;
            }

            @Override
            public void quickSearchConfirmed() {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        ((CategoriesPanel)categoriesPanel).btv.requestFocus();
                    }
                });
            }

            @Override
            public void quickSearchCanceled() {
                filterText = null;
                refreshContent();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        clearQuickSearchField();
                        ((CategoriesPanel)categoriesPanel).btv.requestFocus();
                    }
                });
            }
        };
    }

    private void refreshContent() {
        FileObject folder = getCurrentTemplatesFolder();
        if( null != folder ) {
            setTemplatesFolder( folder );
            setSelectedCategoryByName( null );
        }
    }

    private void adjustQuickSearch( QuickSearch qs ) {
        qs.setAlwaysShown( true );
        Component qsComponent = panelFilter.getComponent( 0 );
        if( qsComponent instanceof JComponent ) {
            ((JComponent)qsComponent).setBorder( BorderFactory.createEmptyBorder() );
        }
        JTextField textField = getQuickSearchField();
        if( null != textField )
            textField.setMaximumSize( null );
    }
    
    private JTextField getQuickSearchField() {
        Component qsComponent = panelFilter.getComponent( 0 );
        if( qsComponent instanceof JComponent ) {
            for( Component c : ((JComponent)qsComponent).getComponents() ) {
                if( c instanceof JTextField ) {
                    return ( JTextField ) c;
                }
            }
        }
        return null;
    }

    private void clearQuickSearchField() {
        JTextField textField = getQuickSearchField();
        if( null != textField )
            textField.setText( null );
    }
}
