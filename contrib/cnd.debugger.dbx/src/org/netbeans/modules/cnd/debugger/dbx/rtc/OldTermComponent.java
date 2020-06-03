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

import java.io.InputStream;
import java.io.OutputStream;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JComponent;

// popumenu support:
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.prefs.Preferences;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;

import org.openide.awt.MouseUtils;
import org.openide.windows.TopComponent;
import org.openide.util.NbPreferences;

import org.netbeans.modules.terminal.api.ui.IOTopComponent;

import org.netbeans.lib.terminalemulator.support.TermOptions;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.io.TermComponentFactory;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.utils.NotifyingInputSteam;


//
// OldTermComponent used to implement TermComponent and was used for
// Pio and Console. But NewTermComponent deals with those and all
// OldTermComponent is used for is RtcComponent
//
// It's only ever called with TermComponentFactory.ACTIVE so we SHOULD
// be able to vastly simplify it and eventually merge it with RtcComponent.
//

/* package */ class OldTermComponent
	extends JComponent implements PropertyChangeListener {

    private final IOTopComponent owner;
    private final int flags;

    /**
     * The terminal emulator - either ActiveTerm, StreamTerm.
     */
    protected final StreamTerm term;


    public OldTermComponent(TopComponent owner, int flags) {
	assert owner instanceof IOTopComponent :
	       "owner of OldTermComponent must be IOTopComponent";	// NOI18N
	this.owner = (IOTopComponent) owner;
	this.flags = flags;
	TermComponentFactory.ckFlags(flags);

	if ((flags & TermComponentFactory.ACTIVE) == TermComponentFactory.ACTIVE) {
	    term = new ActiveTerm();
	} else {
	    term = new StreamTerm();
	}
	initCommon();
    }

    private void initCommon() {
	setupTerm();

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = new Insets(2, 2, 0, 0);

	setLayout(new GridBagLayout());
	add(term, gbc);

	termOptions().addPropertyChangeListener(this);
	initializeA11y();

	setVisible(true);
    }


    public boolean isPty() {
	return TermComponentFactory.isPty(flags);
    }

    public final boolean isRaw() {
	return TermComponentFactory.isRaw(flags);
    }

    public final boolean isActive() {
	return TermComponentFactory.isActive(flags);
    }

    public final boolean isPacketMode() {
	return TermComponentFactory.isPacketMode(flags);
    }

    protected void initializeA11y() {
	// super.initializeA11y();
    }


    // no-one calls this!
    /* OLD
    public void cleanup() {
	if (pty != null) {
	    try {
		pty.close();
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}
	termOptions().removePropertyChangeListener(this);
    }
     */

    // interface PropertyChangeListener
    public void propertyChange(PropertyChangeEvent e) {
	applyTermOptions();
    }

    private InputStream interposeActivityDetector(InputStream out) {
	// 4896262, for non-remote PIO
	final NotifyingInputSteam notifier = new NotifyingInputSteam(out);
	notifier.setListener(new NotifyingInputSteam.Listener () {
	    public void activity () {
		if (DebuggerOption.FRONT_PIO.isEnabled(
			    NativeDebuggerManager.get().globalOptions()))
		    OldTermComponent.this.requestVisible();
	    }
	});
	notifier.arm();

	out = notifier;
	return out;
    }

    public void connectIO(OutputStream in, InputStream out) {
	out = interposeActivityDetector(out);
	term.connect(in, out, null);
    }

    public Term getTerm() {
	return term;
    }

    public ActiveTerm getActiveTerm() {
	assert term instanceof ActiveTerm;
	return (ActiveTerm) term;
    }

    private static TermOptions termOptions = null;

    private static final Preferences prefs =
        NbPreferences.forModule(TermOptions.class);

    private static TermOptions termOptions() {
	if (termOptions == null) {
	    termOptions = TermOptions.getDefault(prefs);
	}
	return termOptions;
    } 

    @Override
    public void requestFocus() {
	/*
	 * Pass on focus request to term.
	 */
	if (term != null) {
	    //term.requestActive(); // Not implemented yet
	    // TEMPORARY
	    // boolean result = term.requestFocusInWindow();
	    term.requestFocus();
	}
    }

    public void requestVisible() {
	owner.topComponent().requestVisible();
    }

    public void bringUp() {
	owner.ioContainer().add(this, null);
    }

    public void bringDown() {
	owner.ioContainer().remove(this);
    }

    public void open() {
	owner.topComponent().open();
    }

    public void switchTo() {
	// using 'this' as a parameter doesn't work for
	// rtc ... should use RtcView.
	owner.ioContainer().select(this);
    }

    private void setupTerm() {
	term.setRowsColumns(7, 80);
	term.setEmulation("dtterm"); // NOI18N
	term.setClickToType(false); // Term has a weird default
	// term.setDebugFlags(Term.DEBUG_INPUT);
	// SHOULD setup keystrokes passthrough
	applyTermOptions();
    }

    /**
     * Apply TermOptions' properties to our Term.
     */
    private void applyTermOptions() {
	if (term == null)
	    return;

        term.setFixedFont(true);
        term.setFont(termOptions().getFont());

	term.setBackground(termOptions().getBackground());
	term.setForeground(termOptions().getForeground());
	term.setHighlightColor(termOptions().getSelectionBackground());
	term.setHistorySize(termOptions().getHistorySize());
	term.setTabSize(termOptions().getTabSize());

	term.setClickToType(termOptions().getClickToType());
	term.setScrollOnInput(termOptions().getScrollOnInput());
	term.setScrollOnOutput(termOptions().getScrollOnOutput());
	term.setHorizontallyScrollable(!termOptions().getLineWrap());

	// If we change the font from smaller to bigger, the size
	// calculations go awry and the last few lines are forever hidden. 
	//
	// One usually uses 'pack()' but it doesn't work because it
	// applies only to JFrames an under vulcan in MDI mode it had
	// catastrophic results (the whole window gets maximized, etc.)
	//
	// The following is a workaround used in the Rtc windows' 
	// componentShown() as a fix for
	// 4652678 (Memory Usage window and Access Errors window are ...)

	setSize(getPreferredSize());
	validate();
    }


    // 
    // Popup menu code
    //

    protected void populateMenu(Object source, Point pt, JPopupMenu popup) {
	// OLD addSystemActionsToMenu(popup);
    }

    protected void singleClicked(Point p, Object source) {
    }
    
    protected void doubleClicked(Point p, Object source) {
    }
        


    private void postPopupMenu(Point pt, Component source) {
	int xpos = pt.x;
	int ypos = pt.y;

	JPopupMenu popup = new JPopupMenu(); // Was JPopupMenuPlus
	populateMenu(source, pt, popup);

	if (popup.getSubElements().length > 0) {
	    JViewport viewport = null;
	    if (source instanceof JViewport) {
		viewport = (JViewport)source;
	    } else if ((source instanceof Component) &&
		       (source.getParent() != null) &&
		       (source.getParent() instanceof JViewport)) {
		viewport = (JViewport) source.getParent();
	    }
	    if (viewport != null) {
		java.awt.Point psp = viewport.getViewPosition();
		xpos -= psp.x;
		ypos -= psp.y;
	    }
	    
	    // For tables, the header also takes up room which the viewport
	    // doesn't account for.
	    if (source instanceof JTable) {
		JTableHeader jt = ((JTable)source).getTableHeader();
		if (jt != null) {
		    ypos += jt.getHeight();
		}
	    }

//		if (inView) {
		// Add the space taken up by the content label as well...
//		    ypos += contentLabel.getHeight();
//		}
	    
	    Point p = new Point(xpos, ypos);
	    SwingUtilities.convertPointToScreen (p, OldTermComponent.this);
	    Dimension popupSize = popup.getPreferredSize ();
	    Dimension screenSize =
		Toolkit.getDefaultToolkit ().getScreenSize ();

	    if (p.x + popupSize.width > screenSize.width) {
		p.x = screenSize.width - popupSize.width;
	    }
	    if (p.y + popupSize.height > screenSize.height) {
		p.y = screenSize.height - popupSize.height;
	    }
	    SwingUtilities.convertPointFromScreen (p, OldTermComponent.this);
	    popup.show(OldTermComponent.this, p.x, p.y);
	}
    }

    class PopupAdapter extends MouseUtils.PopupMouseAdapter {
        protected void showPopup (MouseEvent e) {
	    Point pt = new Point(e.getX(), e.getY());
	    Component source = (Component) e.getSource();
	    postPopupMenu(pt, source);
        }

        @Override
	public void mouseClicked(MouseEvent e){
	    super.mouseClicked(e);
	    // Double click
	    if (e.getClickCount() == 1) {
		singleClicked(e.getPoint(), e.getSource());
	    } else if (e.getClickCount() == 2) {
		doubleClicked(e.getPoint(), e.getSource());
	    }
	}
    }

    private PopupAdapter popupMenuListener = null;

    protected void addPopupListener(JComponent jc) {
	if (popupMenuListener == null) {
	    popupMenuListener = new PopupAdapter ();
	}
	jc.addMouseListener(popupMenuListener);
    }

    protected void removePopupListener(JComponent jc) {
	if (popupMenuListener != null) {
	    jc.removeMouseListener(popupMenuListener);
	}
    }

// OLD
//    protected void addSystemActionsToMenu(JPopupMenu popup) {
//        // constructs pop-up menu from actions of selected component
//        // OLD SystemAction[] compActions = getSystemActions();
//	if (muxableOwner /* OLD owner() */ == null)
//	    return;
//        Action[] compActions = muxableOwner /* owner() */.getActions();
//        for (int i = 0; i < compActions.length; i++) {
//            if (compActions[i] == null)
//                popup.addSeparator();
//            else if (compActions[i] instanceof CallableSystemAction) {
//                popup.add(((CallableSystemAction)compActions[i]).
//		    getPopupPresenter());
//	    /* LATER
//            //add FileSystemAction to pop-up menu
//            } else if (compActions[i] instanceof FileSystemAction) {
//                popup.add(((FileSystemAction)compActions[i]).
//                          getPopupPresenter());
//	    */
//	    }
//        }
//	/*
//        if (compActions.length != 0) {
//            popup.addSeparator();
//        }
//	*/
//    }

    public HyperlinkKeyProcessor setupHyperlinkProcessing() {
	ActiveTerm at = getActiveTerm();
        HyperlinkKeyProcessor p = new HyperlinkKeyProcessor(at);
        at.getScreen().addKeyListener(p);
	return p;
    }

    //------------------------------------------------------------
    // End of Hyperlink processing via keyboard
    //------------------------------------------------------------
}
