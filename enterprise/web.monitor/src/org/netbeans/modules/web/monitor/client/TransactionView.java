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

/**
 * @author Ana von Klopp
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;     
import javax.swing.JPanel;     
import javax.swing.JScrollPane;      
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;    
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.CompoundBorder;     
import javax.swing.border.EmptyBorder;     
import javax.swing.border.EtchedBorder;     
import javax.swing.event.ChangeListener;    
import javax.swing.event.ChangeEvent;    
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.web.monitor.data.DataRecord;
import org.openide.util.ImageUtilities;

/**
 * Update title does not work like it should. Maybe there is a getName
 * method for this that I can override.
 */
class TransactionView extends TopComponent implements ExplorerManager.Provider,
				     PropertyChangeListener, ChangeListener {

    // Handles all the files etc. 
    private static transient TransactionView instance = null; 
    private static transient Controller controller = null;

    // Misc
    private transient JToggleButton timeAButton, 	timeDButton,
	alphaButton; 
    
    private transient JToggleButton timestampButton;

    // Sizing and stuff...
    private transient  Dimension logD = new Dimension(250, 400);
    private transient  Dimension dataD = new Dimension(500, 400);
    private transient  Dimension tabD = new Dimension(500,472);
    
    // Display stuff 
    private static transient ExplorerManager mgr = null;
    private transient JPanel logPanel = null; 
    private transient JPanel dataPanel = null; 
    private transient JSplitPane splitPanel = null; 
    private transient double dividerRatio = .35;
    private transient BeanTreeView tree = null;
    private transient AbstractNode selected = null;

    private transient RequestDisplay requestDisplay = null;
    private transient CookieDisplay  cookieDisplay = null;
    private transient SessionDisplay sessionDisplay = null;
    private transient ContextDisplay contextDisplay = null;
    private transient ClientDisplay  clientDisplay = null;
    private transient HeaderDisplay  headerDisplay = null;

    // Handle resizing for larger fonts
    boolean fontChanged = true;

    // Data display tables 
    private int displayType = 0;

    // Need to override requestFocusInWindow to call requestFocusInWindow
    // on some internal component for F1 help to work correctly
    public boolean requestFocusInWindow() {
        if (tree != null) {
            return tree.requestFocusInWindow();
        } else {
            return false;
        }
    }
    
    public HelpCtx getHelpCtx() {
	String helpID = NbBundle.getBundle(TransactionView.class).getString("MON_Transaction_View_F1_Help_ID"); // NOI18N
	return new HelpCtx( helpID );
    }

    /**
     * Creates the display and the nodes that are present all the
     * time. Because all this is done at startup, we don't actually
     * retrieve any data until the Monitor is opened.
     */
    private TransactionView() {
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/web/monitor/client/icons/menuitem.gif"));
        setToolTipText(NbBundle.getMessage(TransactionView.class, "MON_Window_Tooltip"));
	controller = Controller.getInstance();
	initialize();
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_monitorDesc"));
	this.getAccessibleContext().setAccessibleName(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_monitorName"));

    }

    static synchronized TransactionView getInstance() { 
	if(instance == null) 
	    instance = new TransactionView(); 
	return instance; 
    }

    private void initialize() {

	mgr = new ExplorerManager();
	mgr.addPropertyChangeListener(this);
	mgr.setRootContext(controller.getRoot());

        // following line tells the top component which lookup should be associated with it
        associateLookup (ExplorerUtils.createLookup (mgr, getActionMap ()));

        setLayout(new java.awt.BorderLayout());
	tree = new BeanTreeView();
	tree.setDefaultActionAllowed(true);
	tree.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_treeName"));
	tree.getAccessibleContext().setAccessibleName(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_treeDesc"));

	createLogPanel(); 
	createDataPanel(); 
	splitPanel = 
	    new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPanel, dataPanel); 
	splitPanel.setDividerLocation((int)(logD.getWidth()));
	splitPanel.setResizeWeight(dividerRatio);
	splitPanel.setDividerSize(1); 
	splitPanel.setOneTouchExpandable(true); 
	this.add(splitPanel);
	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));
    }
    
    /**
     * Open the transaction nodes (i.e. first level children of the root).
     */
    void openTransactionNodes() {

	// Post the request for later in case there are timing issues
	// going on here. 

	OpenTransactionNodesRequest req = new
	    OpenTransactionNodesRequest();
	
	RequestProcessor.Task t = 
	    RequestProcessor.postRequest(req, 500); // wait a sec...
    }

    public int getPersistenceType() { 
	return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    class OpenTransactionNodesRequest implements Runnable {
	
	public void run() {
	    openTransactionNodes();
	}

	void openTransactionNodes() {
	    NavigateNode root = controller.getRoot();
	    Children ch = root.getChildren();
	    Node [] nodes = ch.getNodes();
	    CurrNode cn = (CurrNode)nodes[0];
	    SavedNode sn = (SavedNode)nodes[1];
	    
	    
	    // If there are any current nodes, then select the most
	    // recent (i.e. the last?) one. 

	    Children currCh = cn.getChildren();
	    Node [] currChNodes = currCh.getNodes();
	    int numCN = currChNodes.length;
	    if (numCN > 0) {
		int selectThisOne = 0;
		if (timeAButton.isSelected()) {
		    selectThisOne = numCN - 1;
		}
		selectNode(currChNodes[selectThisOne]);
	    } else {
		Children savedCh = sn.getChildren();
		Node [] savedChNodes = savedCh.getNodes();
		int numSN = savedChNodes.length;
		if (numSN > 0) {
		    selectNode(savedChNodes[0]);
		}
	    }
	}
    }

    void selectNode(Node n) {

	try {
	    mgr.setSelectedNodes(n != null ? new Node[] {n} : new Node[] {});
	    
	} catch (Exception exc) {
            Logger.getLogger(TransactionView.class.getName()).log(Level.INFO, "selectNode", exc);
	} // safely ignored
    }
    
    /**
     * Loads the transactions into the monitor on opening. */
    private boolean openedOnceAlready = false;
    public void open() {
	super.open();
	//setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));	
	if (!openedOnceAlready) {
	    openedOnceAlready = true;
	    controller.getTransactions();
	    openTransactionNodes();
	    //this.revalidate(); 
	    //this.repaint(); 
	}
	//PENDING ...
	controller.checkServer(false);
    }
    
    /**
     * Returns true if the monitor is already initialized - the old transactions 
     * were loaded from disk, false otherwise.
     */
    public boolean isInitialized() {
        return openedOnceAlready;
    }

    protected void updateTitle() {
	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));	
    }
    
    /**
     * Do not serialize this component, substitute null instead.
     */
    public Object writeReplace() throws ObjectStreamException {
        return new ResolvableHelper();
    }


    /**
     * Invoked at startup, creates the display GUI.
     */
    private void createLogPanel() {

	JToolBar buttonPanel = new JToolBar();
	buttonPanel.setBorder
	    (new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
				new EmptyBorder (4, 4, 4, 4)
				    ));
	buttonPanel.setFloatable (false);

	JButton updateButton = new JButton(ImageUtilities.loadIcon(
                "org/netbeans/modules/web/monitor/client/icons/update.gif")); // NOI18N
	updateButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Reload_all_17"));
	updateButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    controller.getTransactions();
		}});

	timeAButton = new JToggleButton(ImageUtilities.loadIcon(
                "org/netbeans/modules/web/monitor/client/icons/timesortA.gif"), false);
	timeAButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Order_transactions_15"));

	timeAButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    if(!((JToggleButton)e.getSource()).isSelected())
			return;
		    else {
			timeDButton.setSelected(false);
			alphaButton.setSelected(false);
			controller.setComparator
			    (controller.new CompTime(false));
		    }
		}});

	timeDButton = new JToggleButton(ImageUtilities.loadIcon(
                "org/netbeans/modules/web/monitor/client/icons/timesortB.gif"), true);
	timeDButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Order_transactions_16"));
	timeDButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    if(!((JToggleButton)e.getSource()).isSelected())
			return;
		    else {
			timeAButton.setSelected(false);
			alphaButton.setSelected(false);
			controller.setComparator
			    (controller.new CompTime(true));
		    }

		}});

	alphaButton = new JToggleButton(ImageUtilities.loadIcon(
                "org/netbeans/modules/web/monitor/client/icons/a2z.gif"), false);
	alphaButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Order_transactions_14"));
	alphaButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    if(!((JToggleButton)e.getSource()).isSelected())
			return;
		    else {
			timeAButton.setSelected(false);
			timeDButton.setSelected(false);
			controller.setComparator
			    (controller.new CompAlpha());
		    }

		}});


	timestampButton = new JToggleButton(
            ImageUtilities.loadIcon("org/netbeans/modules/web/monitor/client/icons/timestamp.gif"),
            TransactionNode.showTimeStamp());
	timestampButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Show_time_25"));
	timestampButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    TransactionNode.toggleTimeStamp();
		    // PENDING - should find a way to repaint
		    // the tree. tree.repaint() does not work. 
		    controller.updateNodeNames();
		}});

	buttonPanel.add(updateButton);
	buttonPanel.add(timeDButton);
	buttonPanel.add(timeAButton);
	buttonPanel.add(alphaButton);
	JPanel sep = new JPanel() { // PENDING proper insets should do the same spacing job
		public float getAlignmentX() {
		    return 0;
		}
		public float getAlignmentY() {
		    return 0;
		}
	    };
	sep.setMaximumSize(new Dimension(10, 10));
	buttonPanel.add(sep);
	buttonPanel.add(timestampButton);

	logPanel = new JPanel();
	logPanel.setLayout(new BorderLayout());

	JPanel p = new JPanel (new BorderLayout ());
	//p.setBorder (new EtchedBorder (EtchedBorder.LOWERED));
	p.add(BorderLayout.NORTH, buttonPanel);
	p.add(BorderLayout.CENTER, tree);
	logPanel.add(BorderLayout.CENTER, p);
	logPanel.setPreferredSize(logD);		  
    }


    /**
     * Invoked at startup, creates the display GUI.
     */
    private void createDataPanel() {

	JTabbedPane jtp = new JTabbedPane();
        jtp.getAccessibleContext().setAccessibleName(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_Transaction_dataName"));
        jtp.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_Transaction_dataDesc"));

	jtp.setPreferredSize(tabD);
	jtp.setMaximumSize(tabD);

        // compute scrolling unit increment
        FontMetrics metrics = getFontMetrics(jtp.getFont());
        int scrollingUnitIncrement = metrics.getHeight();
        
	requestDisplay = new RequestDisplay(); 
	JScrollPane p = new JScrollPane(requestDisplay);
        p.getVerticalScrollBar().setUnitIncrement(scrollingUnitIncrement);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Request_19_Tab"), p);

	cookieDisplay = new CookieDisplay(); 
	p = new JScrollPane(cookieDisplay);
        p.getVerticalScrollBar().setUnitIncrement(scrollingUnitIncrement);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Cookies_4_Tab"), p);

	sessionDisplay = new SessionDisplay(); 
	p = new JScrollPane(sessionDisplay);
        p.getVerticalScrollBar().setUnitIncrement(scrollingUnitIncrement);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Session_24"), p); 

	contextDisplay = new ContextDisplay(); 
	p = new JScrollPane(contextDisplay);
        p.getVerticalScrollBar().setUnitIncrement(scrollingUnitIncrement);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Context_23"), p);

	clientDisplay = new ClientDisplay(); 
	p = new JScrollPane(clientDisplay);
        p.getVerticalScrollBar().setUnitIncrement(scrollingUnitIncrement);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Client_Server"), p);

	headerDisplay = new HeaderDisplay(); 
	p = new JScrollPane(headerDisplay);
        p.getVerticalScrollBar().setUnitIncrement(scrollingUnitIncrement);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Header_19"), p);

	jtp.addChangeListener(this);

	dataPanel = new JPanel();
	dataPanel.setLayout(new BorderLayout());
	dataPanel.add(BorderLayout.CENTER, jtp);
	dataPanel.setPreferredSize(dataD);
    }


    /**
     * Invoked by DisplayAction. Displays monitor data for the selected
     * node. 
     * PENDING - register this as a listener for the display action
     */
    void displayTransaction(Node node) {
	if (node == null)
	    return;

	if(node instanceof TransactionNode || 
	   node instanceof NestedNode) {
	    try {
		selected = (AbstractNode)node;
	    } 
	    catch (ClassCastException ex) {
		selected = null;
		selectNode(null);
	    }
	}
	else {
	    selected = null;
	    selectNode(null);
	}
	
	showData(); 
    }

    void saveTransaction(Node[] nodes) {
	if((nodes == null) || (nodes.length == 0)) return;
	controller.saveTransaction(nodes);
	selected = null;
	selectNode(null);
	showData(); 
    }
    
    /**
     * Listens to events from the tab pane, displays different
     * categories of data accordingly. 
     */
    public void stateChanged(ChangeEvent e) {

	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));

	JTabbedPane p = (JTabbedPane)e.getSource();
	displayType = p.getSelectedIndex();
	showData();
    }
    

    void showData() {
	 
	DataRecord dr = null;	    
	try {
	    if (selected != null) {
		dr = controller.getDataRecord(selected);
	    }
	}
	catch(Exception ex) {
	    ex.printStackTrace();
	}
	
	if (displayType == 0)
	    requestDisplay.setData(dr);
	else if (displayType == 1)
	    cookieDisplay.setData(dr);
	else if (displayType == 2)
	    sessionDisplay.setData(dr);
	else if (displayType == 3)
	    contextDisplay.setData(dr);
	else if (displayType == 4)
	    clientDisplay.setData(dr);
	else if (displayType == 5)
	    headerDisplay.setData(dr);

	this.repaint();
	
    }

    /**
     * Display the data for a node if it's selected. This should
     * probably be done by checking if you can get the DisplayAction
     * from the Node, and then calling it if it's enabled.
     */
    public void propertyChange(PropertyChangeEvent evt) {

	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));
	//updateTitle();

	if(evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {

	    if(evt.getNewValue() instanceof Node[]) {
		try {
		    Node[] ns = (Node[])evt.getNewValue();
		    if(ns.length == 1) {
			displayTransaction(ns[0]); 
		    }
		}
		// Do nothing, this was not a proper node
		catch(Exception e) {
                    Logger.getLogger(TransactionView.class.getName()).log(Level.INFO, "", e);
		    selected = null;
		    showData();
		    return;
		}
	    }
	}
    }

    /**
     * Blanks out the displays - this is used by the delete actions
     */
    void blank() {
	selected = null;
	selectNode(null);
	showData(); 
    }

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    public void paint(Graphics g) {
	if(fontChanged) {
	    super.paint(g);
	    return; 
	}

	FontMetrics fm = g.getFontMetrics(getFont());
	fontChanged = false;
	
	double logWidth = fm.stringWidth(NbBundle.getBundle(TransactionView.class).getString("MON_Transactions_27")) * 1.1; 

	if(logWidth > logD.getWidth()) { 
	    double factor = logWidth/logD.getWidth(); 
	    logD.setSize(logWidth, factor * logD.getHeight());


	    dataD.setSize(factor * dataD.getWidth(), 
			  factor * dataD.getHeight()); 
	}

	logPanel.setPreferredSize(logD);
	dataPanel.setPreferredSize(dataD);
	splitPanel.resetToPreferredSizes(); 
	splitPanel.setDividerLocation((int)(logD.getWidth()));

	try { 
	    Container o = this.getParent(); 
	    while(true) { 
		if(o instanceof JFrame) { 
		    JFrame parent = (JFrame)o; 
		    parent.pack(); 
		    break; 
		} 
		o = o.getParent(); 
	    } 
	}
	catch(Throwable t) {
	    // Do nothing, we can't resize the component
	    // invalidate on this component does not work. 
	}
	//super.paint(g);
	return;
    }

    public static final class ResolvableHelper implements Serializable {
        static final long serialVersionUID = 1234546018839457544L;
        Object readResolve() {
	    Controller.getInstance().getTransactions();
            return TransactionView.getInstance(); 
        }
    }
    
    protected String preferredID() {
        return "TransactionView"; //NOI18N
    }
    
    // methods needed for binding with context menu ------
    void setTimestampButtonSelected(boolean state) {        
        timestampButton.setSelected(state);
    }
    
    boolean isTimestampButtonSelected() {        
        return timestampButton.isSelected();
    }
    
    void addTimestampButtonActionListener(ActionListener l) {
        timestampButton.addActionListener(l);
    }
    
    boolean isAscButtonSelected() {   
        return timeAButton.isSelected();
    }

    boolean isDescButtonSelected() {   
        return timeDButton.isSelected();
    }

    boolean isAlphButtonSelected() {         
        return alphaButton.isSelected();
    }

    void toggleTaskbarButtons(boolean asc, boolean desc, boolean alph) {
        timeAButton.setSelected(asc);
        timeDButton.setSelected(desc);
        alphaButton.setSelected(alph);    
    }
    
    // EOF: methods needed for binding with context menu ------

    public ExplorerManager getExplorerManager() {
        return mgr;
    }
    
    protected void componentActivated() {
        ExplorerUtils.activateActions(mgr, true);
    }
    
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(mgr, false);
    }
}
