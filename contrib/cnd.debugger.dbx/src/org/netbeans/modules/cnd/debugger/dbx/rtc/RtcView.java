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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

/**
 * Main presentation of an RTC "Experiment".
 */

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;


import org.openide.ErrorManager;

import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import org.openide.windows.TopComponent;
import org.openide.cookies.SaveCookie;

import org.openide.text.Line;

import org.netbeans.modules.cnd.debugger.common2.utils.FlyweightAction;
import org.netbeans.modules.cnd.debugger.common2.utils.FlyweightBooleanStateAction;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.terminal.api.ui.IOTopComponent;


/**
 * General interface to the view of an RTC model/experiment independent of
 * where it's located which is one of two places:
 * <ul>
 * <li> In batch mode as an RTC experiment document viewer, under an RtcView
 * which appears in the editor mode.
 * <li> In interactive mode under a MuxabeTopComponent which appears in the
 * output mode.
  </ul>
 *
 */
public class RtcView extends JPanel
		     implements ChangeListener, SaveCookie {

    private class NonInteractiveController implements RtcController {
	public OptionSet optionSet() {
	    return null;
	}

	public boolean isInteractive() {
	    return false;
	}

	public void setChecking(boolean access, boolean memuse) { }

	public void setAccessChecking(boolean enable) { }

	public boolean isAccessCheckingEnabled() {
	    return false;
	}

	public void setMemuseChecking(boolean enable) { }

	public boolean isMemuseEnabled() {
	    return false;
	}

	public void setLeaksChecking(boolean enable) { }

	public void skipLoadobjs(Loadobjs loadobjs) { }

	public boolean isLeaksEnabled() {
	    return false;
	}

	public void setMelAtExit(boolean enable) { }
	public boolean isMelAtExit() {
	    return false;
	}

	public void setBiuAtExit(boolean enable) { }
	public boolean isBiuAtExit() {
	    return false;
	}

	public void suppressLastError() { }

	public void showLeaks(boolean all, boolean detailed) { }

	public void showBlocks(boolean all, boolean detailed) { }

	public void showErrorInEditor(String fileName, int lineNumber) {
	    Line line = EditorBridge.getLine(fileName, lineNumber, NativeDebuggerManager.get().currentDebugger());

	    if (line != null) {
		EditorBridge.showInEditor(line);
		RtcMarker.getDefaultError().setLine(RtcView.this, line);
	    } else {
		RtcMarker.getDefaultError().clearLine(RtcView.this);
		RtcMarker.getDefaultFrame().clearLine(RtcView.this);
	    }
	}

	public void showFrameInEditor(String fileName, int lineNumber) {
	    Line line = EditorBridge.getLine(fileName, lineNumber, NativeDebuggerManager.get().currentDebugger());

	    if (line != null) {
		EditorBridge.showInEditor(line);
		RtcMarker.getDefaultFrame().setLine(RtcView.this, line);
	    } else {
		RtcMarker.getDefaultError().clearLine(RtcView.this);
		RtcMarker.getDefaultFrame().clearLine(RtcView.this);
	    }
	}
    }

    private class Resolver implements Hyperlink.Resolver {
	// OLD static final int afterFirstColon = 7; // length of 'editor:'

	public void activate(Object source, Hyperlink hyperlink) {
	    // Format of url:
	    // editor:<filename>:<linenumber>:{error|frame}

	    final String url = hyperlink.getUrl();
	    if (url.startsWith("editor:")) { // NOI18N
		// Parse out the components
		int firstColon = url.indexOf(':', 0);
		int secondColon = url.indexOf(':', firstColon+1);
		int thirdColon = url.indexOf(':', secondColon+1);
		String fileName = url.substring(firstColon+1, secondColon);
		String linenoStr = url.substring(secondColon+1, thirdColon);
		String lineContents = url.substring(thirdColon+1);
		if (Log.Rtc.hyperlink) {
		    System.out.printf("Link activated from %s\n", source); // NOI18N
		    System.out.printf("Filename = \"%s\"\n", fileName); // NOI18N
		    System.out.printf("Linenumber = '%s'\n", linenoStr);// NOI18N
		    System.out.printf("Linecontents = '%s'\n", lineContents);// NOI18N
		}

		if (fileName.equals("")) {
		    ErrorManager.getDefault().log("Sorry, no filename in link ..."); // NOI18N
		    return;
		}

		int lineNumber = Integer.parseInt(linenoStr);
		if (lineNumber <= 0) {
		    ErrorManager.getDefault().log("The line number is invalid... (" + lineNumber + " from string " + linenoStr + ") - the url was " + url); // NOI18N
		    return;
		}
		
		if (controller == null) {
		    ErrorManager.getDefault().log("Sorry, no controller ..."); // NOI18N
		    return;
		}

		if (IpeUtils.isEmpty(lineContents))
		    controller.showFrameInEditor(fileName, lineNumber);	// SS12
		else if (lineContents.equals("error")) // NOI18N
		    controller.showErrorInEditor(fileName, lineNumber);
		else if (lineContents.equals("frame")) // NOI18N
		    controller.showFrameInEditor(fileName, lineNumber);
	    }
	}
    }

    private final RtcController defaultController =
	new NonInteractiveController();

    private RtcController controller;
    private final Hyperlink.Resolver resolver = new Resolver();
    private RtcModel model;

    private JTabbedPane tabbedPane;
    private RtcComponent front;

    private final TopComponent owner;
    private AccessComponent access;
    private MemuseComponent memuse;
    private LeaksComponent leaks;

    private RtcState state;

    private final Lookup lookup;

    private JTextField statusText;
    private JLabel statusLabel_access;
    private JLabel statusLabel_memuse;
    private JLabel statusLabel_leaks;

    private FlyweightAction saveAction;
    private FlyweightAction refreshAction;

    // package private so sub-components can access:
    FlyweightAction saveAsTextAction;
    FlyweightAction clearAction;
    FlyweightAction clearAllAction;
    FlyweightAction suppressAction;

    FlyweightAction newBlocksAction;
    FlyweightAction allBlocksAction;
    FlyweightAction newLeaksAction;
    FlyweightAction allLeaksAction;
    FlyweightAction incrementalMemoryAction;
    FlyweightAction allMemoryAction;

    FlyweightBooleanStateAction detailedReportAction;
    FlyweightBooleanStateAction summaryReportAction;

    FlyweightBooleanStateAction detailedStackAction;
    FlyweightBooleanStateAction summaryStackAction;

    private final Option frontAccessOption;
    private final Option frontMemuseOption;

    RtcModel.Listener listener;


    public RtcView(TopComponent owner,
		   Option frontAccessOption,
		   Option frontMemuseOption) {

	this.owner = owner;
	this.frontAccessOption = frontAccessOption;
	this.frontMemuseOption = frontMemuseOption;

	lookup = Lookups.singleton(this);
	initComponents();
	initializeA11y();
	// setController will update state and actions
	setController(defaultController);
    }

    protected void initializeA11y() {
	// super.initializeA11y();

	/* OLD
	Action popupAction = new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
		Point pt = new Point(0, 0);
		Component c = front;
		front.postPopupMenu(pt, c);
	    }
	};
	 */

	//
	// Mnemonics for tabs
	// 
	tabbedPane.setMnemonicAt(tabbedPane.indexOfComponent(access),
				 KeyEvent.VK_A);
	tabbedPane.setMnemonicAt(tabbedPane.indexOfComponent(memuse),
				 KeyEvent.VK_U);
	tabbedPane.setMnemonicAt(tabbedPane.indexOfComponent(leaks),
				 KeyEvent.VK_L);


	/* LATER

	No accelerators for any RTC actions

	ActionMap am = getActionMap();
	InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

	am.put(popupAction, popupAction);
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, Event.SHIFT_MASK),
	       popupAction);
	am.put(suppressAction, suppressAction);
	im.put(suppressAction.getAccelerator(),
	       suppressAction);
	am.put(saveAction, saveAction);
	im.put(saveAction.getAccelerator(),
	       saveAction);
	am.put(incrementalMemoryAction, incrementalMemoryAction);
	im.put(incrementalMemoryAction.getAccelerator(),
	       incrementalMemoryAction);
	am.put(allMemoryAction, allMemoryAction);
	im.put(allMemoryAction.getAccelerator(),
	       allMemoryAction);
	am.put(clearAllAction, clearAllAction);
	im.put(clearAllAction.getAccelerator(), clearAllAction);
	*/
    }

    @Override
    protected void finalize() {
	RtcMarker.getDefaultError().relinquish(this);
	RtcMarker.getDefaultFrame().relinquish(this);
    }

    // RtcView can be the sole component of an RtcViewer or a child of
    // a MuxableTopComponent hence the instanceof and cast HACK's below.
    // To clean up SHOULD make RtcViewer be a degenarate MuxableTopComponent.

    public void bringDown() {
	if (owner() instanceof IOTopComponent)
	    ((IOTopComponent)owner()).ioContainer().remove(this);
    }

    public void switchTo() {
	if (owner() instanceof IOTopComponent)
	    ((IOTopComponent)owner()).ioContainer().select(this);
    }

    Option frontAccessOption() {
	return frontAccessOption;
    };

    Option frontMemuseOption() {
	return frontMemuseOption;
    }

    TopComponent owner() {
	return owner;
    }

    private void initStatusArea(JToolBar toolbar) {
	javax.swing.Icon icon;
	java.awt.Image image;

	final String iconRoot =
	    "org/netbeans/modules/cnd/debugger/dbx/resources/icons/rtc/";	// NOI18N

	image = ImageUtilities.loadImage(iconRoot +
				    "memory_access_16.png");	// NOI18N
	icon = new ImageIcon(image);
	statusLabel_access = new JLabel(icon);
	Catalog.setAccessibleDescription(statusLabel_access,
					 "DESC_Status_access");	// NOI18N
	statusLabel_access.
	    setToolTipText(Catalog.get("TIP_Status_access"));	// NOI18N
	toolbar.add(statusLabel_access);


	image = ImageUtilities.loadImage(iconRoot +
				    "memory_usage_16.png");	// NOI18N
	icon = new ImageIcon(image);
	statusLabel_memuse = new JLabel(icon);
	Catalog.setAccessibleDescription(statusLabel_memuse,
					 "DESC_Status_memuse");	// NOI18N
	statusLabel_memuse.
	    setToolTipText(Catalog.get("TIP_Status_memuse"));	// NOI18N
	toolbar.add(statusLabel_memuse);


	image = ImageUtilities.loadImage(iconRoot +
				    "memory_leaks_16.png");	// NOI18N
	icon = new ImageIcon(image);
	statusLabel_leaks = new JLabel(icon);
	Catalog.setAccessibleDescription(statusLabel_leaks,
					 "DESC_Status_leaks");	// NOI18N
	statusLabel_leaks.
	    setToolTipText(Catalog.get("TIP_Status_leaks"));	// NOI18N
	toolbar.add(statusLabel_leaks);
    }

    private void initComponents() {
	refreshAction = new RefreshAction(this);
	suppressAction = new SuppressAction(this);
	newBlocksAction = new NewBlocksAction(this);
	allBlocksAction = new AllBlocksAction(this);
	newLeaksAction = new NewLeaksAction(this);
	allLeaksAction = new AllLeaksAction(this);
	incrementalMemoryAction = new IncrementalMemoryAction(this);
	allMemoryAction = new AllMemoryAction(this);
	detailedReportAction = new DetailedReportAction(this);
	summaryReportAction = new SummaryReportAction(this); 
	detailedStackAction = new DetailedStackAction(this);
	summaryStackAction = new SummaryStackAction(this); 
	clearAction = new ClearAction(this);
	clearAllAction = new ClearAllAction(this);
	saveAction = new SaveAction(this);
	saveAsTextAction = new SaveAsTextAction(this);

	detailedReportAction.setBooleanState(detailedReportView);
	summaryReportAction.setBooleanState(!detailedReportView);

	detailedStackAction.setBooleanState(detailedStackView);
	summaryStackAction.setBooleanState(!detailedStackView);


	setLayout(new BorderLayout());

	JToolBar toolbar = new JToolBar();
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	toolbar.setBorderPainted(true);

	this.add(toolbar, BorderLayout.NORTH);
		toolbar.add(suppressAction.getToolbarPresenter());

		if (Log.Rtc.godmode)
		    toolbar.add(refreshAction.getToolbarPresenter());

		toolbar.add(saveAction.getToolbarPresenter());

		/* 
		toolbar.add(newBlocksAction.getToolbarPresenter());
		toolbar.add(allBlocksAction.getToolbarPresenter());
		toolbar.add(newLeaksAction.getToolbarPresenter());
		toolbar.add(allLeaksAction.getToolbarPresenter());
		*/
		toolbar.add(incrementalMemoryAction.getToolbarPresenter());
		toolbar.add(allMemoryAction.getToolbarPresenter());
		toolbar.add(clearAllAction.getToolbarPresenter());

		toolbar.add(Box.createHorizontalGlue());

		initStatusArea(toolbar);


	tabbedPane = new JTabbedPane();
	this.add(tabbedPane, BorderLayout.CENTER);

	access = new AccessComponent(this, resolver);
	memuse = new MemuseComponent(this, resolver);
	leaks = new LeaksComponent(this, resolver);

	tabbedPane.addTab(Catalog.get("TITLE_AccessWindow"), access);
	tabbedPane.addTab(Catalog.get("TITLE_MemuseWindow"), memuse);
	tabbedPane.addTab(Catalog.get("TITLE_LeaksWindow"), leaks);

	tabbedPane.addChangeListener(this);
	front = (RtcComponent) tabbedPane.getSelectedComponent();

	listener = new RtcModel.Listener() {
	    public void profileChanged() {
		updateState();
		updateActions();
	    }

	    public void modelChanged() {
		refresh();
	    }

	    public void runBegin(RtcModel.Run run) {
		updateActions();
	    }
	    public void runEnd() {
		updateActions();
	    }

	    public void accessStateChanged(RtcState state) {
		RtcView.this.state = state;
		updateState();
		updateActions();
	    };
	    public void memuseStateChanged(RtcState state) {
		RtcView.this.state = state;
		updateState();
		updateActions();
	    };

	    public void accessItem(RtcModel.AccessError item) {}
	    public void memuseBegin(RtcModel.MemoryReportHeader header) {}
	    public void memuseItem(RtcModel.MemoryReportItem item) {}
	    public void memuseEnd() {} 
	    public void memuseInterrupted() {}

	    public void leaksBegin(RtcModel.MemoryReportHeader header) {}
	    public void leakItem(RtcModel.MemoryReportItem item) {}
	    public void leaksEnd() {}
	    public void leaksInterrupted() {}
	};
    }

    // interface ChangeListener
    public void stateChanged(ChangeEvent e) {
	// Called when a different tab is moved to the front
	// System.out.printf("RtcView.stateChanged(): %s\n", e);
	front = (RtcComponent) tabbedPane.getSelectedComponent();
	updateActions();
    }

    public boolean isInteractive() {
	assert controller != null :
	       "RtcView.isInteractive(): no defaultController";	// NOI18N
	return controller.isInteractive();
    }


    /**
     * Return whether "showLeaks/showBlocks will succeed.
     * They won't if the process is done running.
     */

    public boolean isUpdatable() {
	return isInteractive() && model != null && model.currentRun() != null;
    }

    RtcController getController() {
	return controller;
    }

    public RtcModel getModel() {
	return model;
    }


    public boolean isLeaksVisible() {
	return front == leaks;
    }

    public boolean isMemuseVisible() {
	return front == memuse;
    }

    public boolean isAccessVisible() {
	return front == access;
    }


    private void setModelHelp(RtcModel newModel) {

	// out with the old ...
	if (model != null)
	    model.removeListener(listener);

	// ... in with the new
	this.model = newModel;

	if (model != null)
	    model.addListener(listener);
    }

    public void setControllerHelp(RtcController newController) {

	// out with the old ...

	// ... in with the new
	if (newController == null)
	    newController = defaultController;

	this.controller = newController;
    }

    private void setModelControllerHelp() {
	access.listenTo(model, controller);
	memuse.listenTo(model, controller);
	leaks.listenTo(model, controller);
    }

    public void setController(RtcController newController) {
	setControllerHelp(newController);
	setModelControllerHelp();
	updateActions();
	updateState();
    }

    public void setModel(RtcModel newModel) {
	setModelHelp(newModel);
	setModelControllerHelp();
	updateActions();
	updateState();
	// Perhaps SHOULD refresh too
	refresh();
    }

    public void setModelController(RtcModel newModel,
				   RtcController newController) {
	setModelHelp(newModel);
	setControllerHelp(newController);
	setModelControllerHelp();
	updateActions();
	updateState();
    }

    // interface MuxableComponent
    public void componentActivated() {
	focusToFirstTab();
    }

    // interface MuxableComponent
    public void componentClosed() {
	RtcMarker.getDefaultError().relinquish(this);
	RtcMarker.getDefaultFrame().relinquish(this);
    }

    // interface MuxableComponent
    public void componentOpened() {
	access.componentOpened();
	memuse.componentOpened();
	leaks.componentOpened();
    }

    // interface MuxableComponent
    public void componentShowing() {
    }

    // interface MuxableComponent
    public void componentHidden() {
    }

    // interface MuxableComponent extends Lookup.Provider
    public Lookup getLookup() {
	return lookup;
    }

    void requestVisible(RtcComponent rc) {
	// Make the TC we're in visible
	if (owner() != null) {
	    owner().open();
	    owner().requestVisible();
	    // Don't make it active because if we're issuing 'step's in
	    // the cmdline we don't want focus to go away.
	    // owner().requestActive();
	}

	// Bring the tab corresponding to 'rc' to front.
	tabbedPane.setSelectedComponent(rc);
    }

    void focusToFirstTab() {
	// tabbedPane.setSelectedIndex(0);
	tabbedPane.requestFocus();
    }

    // interface JComponent
    @Override
    public void requestFocus() {
	super.requestFocus();
	System.out.printf("RtcView.requestFocus()\n"); // NOI18N
	focusToFirstTab();
    }

    // interface JComponent
    @Override
    public boolean requestFocusInWindow() {
	boolean gotFocus = super.requestFocusInWindow();
	System.out.printf("RtcView.requestFocusInWindow(): %s\n", gotFocus); // NOI18N
	if (gotFocus)
	    focusToFirstTab();
	return gotFocus;
    }

    void focusToTab(RtcComponent origin) {
	tabbedPane.setSelectedComponent(origin);
    }

    void nextTab() {
	int nextIndex = tabbedPane.getSelectedIndex() + 1;
	if (nextIndex >= 3)
	    nextIndex = 0;
	tabbedPane.setSelectedIndex(nextIndex);
    }

    void previousTab() {
	int previousIndex = tabbedPane.getSelectedIndex() - 1;
	if (previousIndex < 0)
	    previousIndex = 0;
	tabbedPane.setSelectedIndex(previousIndex);
    }

    private void updateActions() {
	if (Log.Rtc.debug)
	    System.out.printf("RtcView.updateActions()\n"); // NOI18N

	allMemoryAction.update();
	incrementalMemoryAction.update();

	newBlocksAction.update();
	allBlocksAction.update();
	newLeaksAction.update();
	allLeaksAction.update();

	saveAsTextAction.setEnabled(true);
	detailedReportAction.setEnabled(true);
	summaryReportAction.setEnabled(true);

	/* TMP
	// The renderer currently only supports stack detail for access errors.
	detailedStackAction.setEnabled(isAccessVisible());
	summaryStackAction.setEnabled(isAccessVisible());
	*/
	// NOTE we don't render stacks in summary memory reports very well yet
	// So these are only enabled under Log.Rtc.godmode for experimentation.
	detailedStackAction.setEnabled(true);
	summaryStackAction.setEnabled(true);

	clearAction.setEnabled(isInteractive());
	clearAllAction.setEnabled(isInteractive());

	if (getController() == null) {
	    suppressAction.setEnabled(false);
	    saveAction.setEnabled(false);
	    return;
	}

	if (front == access &&
	    isInteractive() &&
	    state != null &&
	    state.accessOn) {
	    suppressAction.setEnabled(true);
	} else {
	    suppressAction.setEnabled(false);
	}

	// We can only save if we have a model, and we have options to
	// tell us where to save to.
	// SHOULD we disallow saving until the experiment is "complete"?

	if (model != null && isInteractive() && model.getProfile() != null)
	    saveAction.setEnabled(true);
	else
	    saveAction.setEnabled(false);
    }

    private void updateState() {
	if (statusText != null) {
	    String text = "";		// NOI18N
	    if (state != null) {
		if (state.accessOn)
		    text += Catalog.get("STATUS_access") + " ";
		if (state.memuseOn)
		    text += Catalog.get("STATUS_memuse") + " ";
		if (state.leaksOn)
		    text += Catalog.get("STATUS_leaks");
	    }
	    statusText.setText(text);
	} else {
	    if (state != null) {
		statusLabel_access.setEnabled(state.accessOn);
		statusLabel_memuse.setEnabled(state.memuseOn);
		statusLabel_leaks.setEnabled(state.leaksOn);
	    } else {
		statusLabel_access.setEnabled(false);
		statusLabel_memuse.setEnabled(false);
		statusLabel_leaks.setEnabled(false);
	    }
	}
    }

    public void refresh() {
	access.refresh();
	memuse.refresh();
	leaks.refresh();
    }

    // interface SaveCookie
    public void save() {

	if (getModel() == null) {
	    if (Log.Rtc.debug)
		System.out.printf("RtcView.save(): no model\n"); // NOI18N
	    return;
	}
	getModel().save();
    }

    @Override
    public String getName() {
	if (model != null) {
	    return model.getName();
	} else {
	    return Catalog.get("NoName");
	}
    }

    /**
     * Controlls whether detailed or summary memory reports are generated.
     * This flag is used for _both_ leaks and memuse reports.
     */
    private boolean detailedReportView = false;

    public boolean isDetailedReportView() {
	return detailedReportView;
    }

    /**
     * Controlls whether detailed or summary stack traces are generated.
     */
    private boolean detailedStackView = false;

    public boolean isDetailedStackView() {
	return detailedStackView;
    }

    void showBlocks(boolean all) {
	// SHOULD front the memuse tab
	if (controller != null) {
	    controller.showBlocks(all, true);		// detailed view
	    controller.showBlocks(all, false);		// summary view
	}
    }

    void showLeaks(boolean all) {
	// SHOULD front the leaks tab
	if (controller != null) {
	    controller.showLeaks(all, true);		// detailed view
	    controller.showLeaks(all, false);	// summary view
	}
    }

    void showMemory(boolean all) {
	if (controller != null) {
	    if (front == memuse) {
		controller.showBlocks(all, true);	// detailed view
		controller.showBlocks(all, false);	// summary view
	    } else if (front == leaks) {
		controller.showLeaks(all, true);	// detailed view
		controller.showLeaks(all, false);	// summary view
	    }
	}
    }

    void showReportDetails(boolean detailedReportView) {
	this.detailedReportView = detailedReportView;

	// no need to refresh access since it's not sensitive to
	// detailedReportView
	memuse.refresh();
	leaks.refresh();
    }

    void showStackDetails(boolean detailedStackView) {
	this.detailedStackView = detailedStackView;

	access.renderer().setDetailedStack(detailedStackView);

	access.refresh();
	memuse.refresh();
	leaks.refresh();
    }

    void clear() {
	if (front == access)
	    model.clearAccess();
	else if (front == memuse)
	    model.clearMemuse();
	else if (front == leaks)
	    model.clearLeaks();
    }

    void clearAll() {
	model.clearAll();
    }

    void saveAsText() {
	front.saveAs();
    }
}
