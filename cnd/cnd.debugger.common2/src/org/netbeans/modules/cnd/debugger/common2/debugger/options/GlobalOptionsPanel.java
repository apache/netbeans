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


package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionCustomizerNode;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionCustomizerPanel;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.TreeSelectionModel;
import java.beans.*;
import org.netbeans.modules.cnd.utils.ui.CndUIConstants;
import org.netbeans.spi.options.OptionsPanelController;

import org.openide.util.NbCollections;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;

// roughly modelled on
// org.netbeans.modules.cnd.execution.profiles.ui.ProfileCustomizer

@OptionsPanelController.Keywords(keywords={"c/c++ debugging", "#KW_DebuggerOptionsPanel"}, location=CndUIConstants.TOOLS_OPTIONS_CND_CATEGORY_ID, tabTitle= "#TAB_DebuggerOptionsPanelTitle")
public class GlobalOptionsPanel extends JPanel {

    private OptionSet options;

    private JPanel customizerPanel;
    private JPanel categoryPanel;
    private CategoryView categoryView;

    private Component currentCustomizer;

    private GridBagConstraints fillConstraints;


    public GlobalOptionsPanel() {
	initComponents();
	setName("TAB_DebuggerOptionsPanelTitle"); // NOI18N
    }

    public void setOptions(OptionSet options) {
	this.options = options;

	String preselectedNodeName = "SessionStartup"; 	// NOI18N
	categoryView.selectNode( preselectedNodeName );

	initValues();
    }

    public OptionSet getOptions() {
	return options;
    }


    /** Initializes the dialog box */
    protected void initComponents() {

        fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;

	GridBagConstraints gridBagConstraints;

	setLayout(new GridBagLayout());

	Catalog.setAccessibleName(this, "ACSN_OptionCustomizer"); // NOI18N
	Catalog.setAccessibleDescription(this, "ACSD_OptionCustomizer"); // NOI18N

	JTextArea header = new JTextArea() {
            @Override
		public boolean isFocusable() {
			return false;
		}
	};
	    header.setRequestFocusEnabled(false);
	    header.setWrapStyleWord(true);
	    header.setLineWrap(true);
	    header.setEditable(false);
	    header.setBorder(new EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));	
	    header.setText(Catalog.get("DialogDescription")); // NOI18N
	    header.setBackground((Color) javax.swing.UIManager.getDefaults().get("Label.background")); // NOI18N
            header.setMinimumSize(new Dimension(100, 62));
            header.setPreferredSize(new Dimension(100, 62));

	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	    gridBagConstraints.fill = GridBagConstraints.BOTH;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
	    add(header, gridBagConstraints);

	categoryPanel = new JPanel();
	    categoryPanel.setLayout(new GridBagLayout());
	    categoryPanel.setBorder(new javax.swing.border.EtchedBorder());
	    categoryPanel.setPreferredSize(new java.awt.Dimension(200, 4));
	    categoryPanel.setMinimumSize(new java.awt.Dimension(200, 4));
	    Catalog.setAccessibleName(categoryPanel,
		"ACSN_OptionCustomizer_categoryPanel"); // NOI18N
	    Catalog.setAccessibleDescription(categoryPanel,
		"ACSD_OptionCustomizer_categoryPanel"); // NOI18N
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 1;
	    gridBagConstraints.gridheight = GridBagConstraints.REMAINDER;
	    gridBagConstraints.fill = GridBagConstraints.BOTH;
	    gridBagConstraints.weighty = 1.0;
	    gridBagConstraints.insets = new Insets(8, 8, 8, 8);
	    add(categoryPanel, gridBagConstraints);

	customizerPanel = new JPanel();
	    customizerPanel.setLayout(new GridBagLayout());
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.gridy = 1;
	    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	    gridBagConstraints.gridheight = GridBagConstraints.REMAINDER;
	    gridBagConstraints.fill = GridBagConstraints.BOTH;
	    gridBagConstraints.weightx = 1.0;
	    gridBagConstraints.weighty = 1.0;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 8);
	    add(customizerPanel, gridBagConstraints);


	categoryView = new CategoryView(createRootNode());
	    fillConstraints = new GridBagConstraints();
	    fillConstraints.gridwidth = GridBagConstraints.REMAINDER;
	    fillConstraints.gridheight = GridBagConstraints.REMAINDER;
	    fillConstraints.fill = GridBagConstraints.BOTH;
	    fillConstraints.weightx = 1.0;
	    fillConstraints.weighty = 1.0;
	    categoryPanel.add(categoryView, fillConstraints);
    }

    private static String ICON =
	"org/netbeans/modules/cnd/debugger/common2/resources/general";// NOI18N

    private static Node createRootNode() {
	OptionCustomizerNode [] children = new OptionCustomizerNode[] {
	    new OptionCustomizerNode(
		"SessionStartup",	// NOI18N
		Catalog.get("SessionStartup"), // NOI18N
		ICON,
		new GlobalOptionsSubPanel.SessionStartup(),
		null
		),

	    new OptionCustomizerNode(
		"WindowProperties",	// NOI18N
		Catalog.get("WindowProperties"), // NOI18N
		ICON,
		new GlobalOptionsSubPanel.WindowProperties(),
		null
		),
/*
	    new OptionCustomizerNode(
		"Persistence",	// NOI18N
		Catalog.get("Persistence"),
		ICON,
		new GlobalOptionsSubPanel.Persistence(),
		null
		),

	    new OptionCustomizerNode(
		"DebuggingBehaviour",	// NOI18N
		Catalog.get("DebuggingBehaviour"),
		ICON,
		new GlobalOptionsSubPanel.DebuggingBehaviour(),
		null
		),
 
 */
	};

	OptionCustomizerNode root = new OptionCustomizerNode(
	    "root PCN", // NOI18N
	    "root PCN", // NOI18N
	    null, // "icon base",
	    null,
	    children
	    );

        return new ConfigurationNode(root);
    }

    private static JLabel createEmptyLabel( String text ) {
        
        JLabel label;
        if ( text == null ) {
            label = new JLabel();
        }
        else {
            label = new JLabel( text );        
            label.setHorizontalAlignment( JLabel.CENTER );
        }
                
        return label;        
    }

    /**
     * Node to be used for configuration
     */
    private static class ConfigurationNode extends AbstractNode {
        
        private OptionCustomizerPanel customizer;
        
        public ConfigurationNode(OptionCustomizerNode description) {
            super( description.children == null ?
		   Children.LEAF :
		   new ConfigurationChildren( description.children ) );
            setName(description.name);
            setDisplayName(description.displayName);
            this.customizer = description.customizer;
        }
        
        @Override
        public boolean hasCustomizer() {
            return true;
        }
        
        @Override
        public Component getCustomizer() {
            return (Component) customizer;
        }
        
    }

    private static class ConfigurationChildren extends Children.Keys<OptionCustomizerNode> {
        
        private Collection<OptionCustomizerNode> descriptions;
        
        public ConfigurationChildren(OptionCustomizerNode[] descriptions) {
            this.descriptions = Arrays.asList(descriptions);
        }
        
        // Children.Keys impl --------------------------------------------------
        
        @Override
        public void addNotify() {
            setKeys(descriptions);
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.<OptionCustomizerNode>emptyList());
        }
        
        @Override
        protected Node[] createNodes(OptionCustomizerNode key) {
            return new Node[] {
		new ConfigurationNode(key)
	    };
        }
    }


    private void storeValues() {
	if (currentCustomizer == null)
	    return;
	if (currentCustomizer instanceof OptionCustomizerPanel) {
	    ((OptionCustomizerPanel)currentCustomizer).storeValues();
	}
    }
    
    private void initValues() {
	if (currentCustomizer == null)
	    return;
	if (currentCustomizer instanceof OptionCustomizerPanel) {
	    ((OptionCustomizerPanel)currentCustomizer).initValues(options);
	} 
    }

    /**
     * Called when Cancel is pressed
     */
    protected void cancelChanges() {
	// noop
    }


    /**
     * Called when OK is pressed
     */
    public void applyChanges() {
	storeValues();
    }


    //
    // inner classes
    // 
    private class CategoryView
	extends JPanel implements ExplorerManager.Provider {

        private ExplorerManager manager;
        private BeanTreeView btv;
	
	CategoryView(Node rootNode) {
            // See #36315
            manager = new ExplorerManager();

	    setLayout(new BorderLayout());

	    Dimension size = new Dimension( 220, 4 );
	    btv = new BeanTreeView();    // Add the BeanTreeView
	    Catalog.setAccessibleName(btv,
		"ACSN_BeanTreeView");		// NOI18N
	    Catalog.setAccessibleDescription(btv,
		"ACSD_BeanTreeView");		// NOI18N
	    btv.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
	    btv.setPopupAllowed( false );
	    btv.setRootVisible( false );
	    btv.setDefaultActionAllowed( false );
	    btv.setMinimumSize( size );
	    btv.setPreferredSize( size );
	    btv.setMaximumSize( size );
	    btv.setDragSource (false);
	    this.add( btv, BorderLayout.CENTER );
	    manager.setRootContext( rootNode );
	    ManagerChangeListener managerChangeListener =
		new ManagerChangeListener();
	    manager.addPropertyChangeListener(managerChangeListener);
	    //btv.expandAll();
	    expandCollapseTree(rootNode, btv);
	}

	private void expandCollapseTree(Node rootNode, BeanTreeView btv) {
	    Children children = rootNode.getChildren();
	    Node[] nodes1 = children.getNodes();
	    for (int i = 0; i < nodes1.length; i++) {
		btv.expandNode(nodes1[i]);
		Node[] nodes2 = nodes1[i].getChildren().getNodes();
		for (int j = 0; j < nodes2.length; j++) {
		    btv.collapseNode(nodes2[j]);
		}
	    }
	}
        
        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        @Override
        public void addNotify() {
            super.addNotify();
            //btv.expandAll();
        }
        
        public void selectNode( String name ) {
            
            Children ch = manager.getRootContext().getChildren();
            if ( ch != null ) {
                Node nodes[] = ch.getNodes( true );
                
                if ( nodes != null && nodes.length > 0 ) {
                    try {                   
                        Node node = nodes[0];
                        
                        if ( name != null  ) {
                            // Find the node
                            try {
				StringTokenizer st = new StringTokenizer( name, "/" ); // NOI18N
                                node = NodeOp.findPath( manager.getRootContext(), NbCollections.checkedEnumerationByFilter(st, String.class, true) ); // NOI18N
                            }
                            catch ( NodeNotFoundException e ) {
                                // First node will be selected
                            }
                        }
                                                
                        manager.setSelectedNodes( new Node[] { node } );
                    }
                    catch ( PropertyVetoException e ) {
                        // No node will be selected
                    }
                }
            }
            
        }
                
        
        
        /**
	 * Listens to selection change and shows the customizers as
         *  panels
         */
        
        private class ManagerChangeListener implements PropertyChangeListener {
	    private void nodeSelected() {
		Node nodes[] = manager.getSelectedNodes(); 
		if ( nodes == null || nodes.length <= 0 ) {
		    return;
		}
		Node node = nodes[0];

		if ( currentCustomizer != null ) {
		    customizerPanel.remove( currentCustomizer );
		}
		if ( node.hasCustomizer() ) {
		    storeValues();
		    currentCustomizer = node.getCustomizer();
		    if (currentCustomizer == null) {
			currentCustomizer = createEmptyLabel(null);
		    }
		    initValues();
		    customizerPanel.add(currentCustomizer, fillConstraints);
		    currentCustomizer.validate();
		    currentCustomizer.repaint();
		    customizerPanel.validate();
		    customizerPanel.repaint();
		    /* LATER
		    if ( MakeCustomizer.this.dialogDescriptor != null ) {
			MakeCustomizer.this.dialogDescriptor.setHelpCtx( MakeCustomizer.this.getHelpCtx() );
		    }
		    */
		}
		else {
		    currentCustomizer = null;
		}
		btv.requestFocus();
	    }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getSource() != manager) {
                    return;
                }

                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
		    nodeSelected();
                }
            }
        }
    }
}
