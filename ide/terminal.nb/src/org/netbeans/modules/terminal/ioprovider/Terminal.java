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

package org.netbeans.modules.terminal.ioprovider;

import org.netbeans.modules.terminal.support.OpenInEditorAction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Keymap;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

import org.openide.util.NbPreferences;
import org.openide.windows.IOContainer;

import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.Term;

import org.netbeans.lib.terminalemulator.support.DefaultFindState;
import org.netbeans.lib.terminalemulator.support.FindState;
import org.netbeans.lib.terminalemulator.support.TermOptions;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.ui.IOVisibility;
import org.netbeans.modules.terminal.api.ui.IOVisibilityControl;

import org.netbeans.modules.terminal.nb.TermAdvancedOption;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.TermAdapter;
import org.netbeans.lib.terminalemulator.TermListener;
import org.netbeans.lib.terminalemulator.TermStream;
import org.netbeans.modules.terminal.nb.actions.ActionFactory;
import org.netbeans.modules.terminal.nb.actions.PinTabAction;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.ui.TerminalContainer;
import org.netbeans.modules.terminal.spi.ui.ExternalCommandActionProvider;
import org.netbeans.modules.terminal.support.TerminalPinSupport;
import org.netbeans.modules.terminal.support.TerminalPinSupport.DetailsStateListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.*;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * A {@link org.netbeans.lib.terminalemulator.Term}-based terminal component for
 * inside NetBeans.
 * <p>
 * The most straightforward way of using it is as follows:
 * <pre>
    import org.netbeans.lib.richexecution.Command;
    import org.netbeans.lib.richexecution.Program;
 *
    public void actionPerformed(ActionEvent evt) {
        // Ask user what command they want to run
        String cmd = JOptionPane.showInputDialog("Command");
        if (cmd == null || cmd.trim().equals(""))
            return;

        TerminalProvider terminalProvider = TerminalProvider.getDefault();
        Terminal terminal = terminalProvider.createTerminal("command: " + cmd);
        Program program = new Command(cmd);
        terminal.startProgram(program, true);
    }
 * </pre>
 * @author ivan
 */
public final class Terminal extends JComponent {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final IOContainer ioContainer;
    private final TerminalInputOutput tio;	// back pointer
    private final String name;
    private final MouseAdapter mouseAdapter;

    private final CallBacks callBacks = new CallBacks();

    // Not final so we can dispose of them
    private final ActiveTerm term;
    private final TermListener termListener;
    private FindState findState;

    private static final Preferences prefs =
        NbPreferences.forModule(TermAdvancedOption.class);
    private final TermOptions termOptions;
    private final TermOptionsPCL termOptionsPCL = new TermOptionsPCL();
    
    private final FileObject shortcutsDir = FileUtil.getConfigFile("Terminal/Shortcuts"); //NOI18N
    private final ShortcutsListener shortcutsListener = new ShortcutsListener();

    private String title;
    private boolean customTitle;

    // AKA ! weak closed
    private boolean visibleInContainer;

    // AKA ! stream closed
    private boolean outConnected;
    private boolean errConnected;
    private boolean extConnected;

    // AKA strong closed
    private boolean disposed;

    // properties managed by IOvisibility
    private boolean closable = true;

    private boolean pinned = false;
    private String cwd;

    private boolean closedUnconditionally;

    private class TermOptionsPCL implements PropertyChangeListener {
	@Override
        public void propertyChange(PropertyChangeEvent evt) {
            applyTermOptions(false);
        }
    }

    /**
     * These are messages from IOContainer we are obligated to handle.
     */
    private class CallBacks implements IOContainer.CallBacks, Lookup.Provider {

	private final Lookup lookup = Lookups.fixed(new MyIOVisibilityControl());

	@Override
	public Lookup getLookup() {
	    return lookup;
	}

	@Override
        public void closed() {
            // System.out.printf("Terminal.CallBacks.closed()\n");
	    // Causes assertion error in IOContainer/IOWindow.
            // OLD close();
	    setVisibleInContainer(false);
        }

	@Override
        public void selected() {
            // System.out.printf("Terminal.CallBacks.selected()\n");
        }

	@Override
        public void activated() {
            // System.out.printf("Terminal.CallBacks.activated()\n");
        }

	@Override
        public void deactivated() {
            // System.out.printf("Terminal.CallBacks.deactivated()\n");
        }

	private class MyIOVisibilityControl extends IOVisibilityControl {

	    @Override
	    protected boolean okToClose() {
		if (Terminal.this.isClosedUnconditionally())
		    return true;
		return Terminal.this.okToHide();
	    }

	    @Override
	    protected boolean isClosable() {
		if (Terminal.this.isClosedUnconditionally())
		    return true;
		return Terminal.this.isClosable();
	    }
	}
    }

    /**
     * Adapter to forward Term size change events as property changes.
     */
    private class MyTermListener implements TermListener {

        private final static int MAX_TITLE_LENGTH = 35;
        private final static String PREFIX = "..."; // NOI18N
        private final static String INFIX = " - "; // NOI18N

        @Override
        public void sizeChanged(Dimension cells, Dimension pixels) {
            IOResizable.Size size = new IOResizable.Size(cells, pixels);
            tio.pcs().firePropertyChange(IOResizable.PROP_SIZE, null, size);
        }

        @Override
        public void cwdChanged(String cwd) {
            if (!customTitle) {
                int newLength = PREFIX.length() + INFIX.length() + cwd.length();
                String newTitle = name.concat(INFIX).concat(cwd);
                updateTooltopText(newTitle);
                if (newLength > MAX_TITLE_LENGTH) {
                    newTitle = name
                            .concat(INFIX)
                            .concat(PREFIX)
                            .concat(cwd.substring(newLength - MAX_TITLE_LENGTH));
                }
                updateName(newTitle);
            }
        }

        @Override
        public void titleChanged(String title) {
            if (!customTitle) {
                String newTitle = title;
                updateTooltopText(newTitle);
                if (title.length() > MAX_TITLE_LENGTH) {
                    newTitle = PREFIX.concat(title.substring(title.length() - MAX_TITLE_LENGTH));
                }
                updateName(newTitle);
            }
        }

        @Override
        public void externalToolCalled(String command) {
            if (!command.startsWith(Term.ExternalCommandsConstants.COMMAND_PREFIX)) {
                return;
            }
            command = command.substring(Term.ExternalCommandsConstants.COMMAND_PREFIX.length());
            if (!command.startsWith(Term.ExternalCommandsConstants.IDE_OPEN)) {
                return;
            }
            
            ExternalCommandActionProvider.getProvider(command).handle(command, Lookups.fixed(getCwd(), term));
        }
    }

    private static class TerminalOutputEvent extends OutputEvent {
        private final String text;

        public TerminalOutputEvent(InputOutput io, String text) {
            super(io);
            this.text = text;
        }

        @Override
        public String getLine() {
            return text;
        }
    }

    /* package */ Terminal(IOContainer ioContainer, TerminalInputOutput tio, String name) {
	if (ioContainer == null)
	    throw new IllegalArgumentException("ioContainer cannot be null");	// NOI18N

        this.ioContainer = ioContainer;
	this.tio = tio;
	this.name = name;

        termOptions = TermOptions.getDefault(prefs);

        // this.term = new StreamTerm();
        this.term = createActiveTerminal();

	applyDebugFlags();

        this.term.setCursorVisible(true);

        findState = new DefaultFindState(term);

        term.setHorizontallyScrollable(false);
        term.setEmulation("xterm");	// NOI18N
        term .setBackground(Color.white);
        term.setHistorySize(4000);
        term.setRenderingHints(getRenderingHints());

        term.addListener(new TermAdapter() {
            @Override
            public void cwdChanged(String aCwd) {
                cwd = aCwd;
            }
        });

	shortcutsDir.addFileChangeListener(shortcutsListener);
        termOptions.addPropertyChangeListener(termOptionsPCL);
        applyTermOptions(true);
	
	final Set<Action> actions = new HashSet<Action>();
	final Set<Action> awareActions = new HashSet<Action>();
	actions.add(newTabAction);
	actions.add(copyAction);
	actions.add(pasteAction);
	actions.add(findAction);
	actions.add(wrapAction);
	actions.add(largerFontAction);
	actions.add(smallerFontAction);
	actions.add(setTitleAction);
	actions.add(pinTabAction);
	actions.add(clearAction);
	actions.add(dumpSequencesAction);
	actions.add(closeAction);
        actions.add(switchTabAction);
	
	for (Action action : actions) {
	    if (action instanceof ContextAwareAction) {
		action = ((ContextAwareAction) action).createContextAwareInstance(Lookups.fixed(this));
	    }
	    awareActions.add(action);
	}
	
	final TerminalPinSupport support = TerminalPinSupport.getDefault();
	TerminalPinSupport.DetailsStateListener listener = new TerminalPinSupport.DetailsStateListener() {

	    @Override
	    public void detailsAdded(Term notifiedTerm) {
		if (notifiedTerm != term) {
		    return;
		}
		TerminalPinSupport.TerminalPinningDetails pinDetails = support.findPinningDetails(notifiedTerm);
		if (pinDetails != null) {
		    boolean customTitle = pinDetails.isCustomTitle();
		    setCustomTitle(customTitle);
		    if (customTitle) {
			setTitle(pinDetails.getTitle());
		    }
		    pin(true);
		}
	    }
	};

	support.addDetailsStateListener(WeakListeners.create(DetailsStateListener.class, listener, support));
	
	setupKeymap(awareActions);

        mouseAdapter = new MouseAdapter() {
            @Override
	    // On UNIX popup on press
	    // On Windows popup on release.
	    // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4119064
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                                                          e.getPoint(),
                                                          term.getScreen());
                    postPopupMenu(p);
                }
            }
	    @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                                                          e.getPoint(),
                                                          term.getScreen());
                    postPopupMenu(p);
                }
         }


	};
        term.getScreen().addMouseListener(mouseAdapter);

        term.getScreen().getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() { //NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                Point p = getPopupPosition();
                postPopupMenu(p);
            }
        }
        );

        term.getScreen().addMouseWheelListener(new MouseWheelListener() {

	    @Override
	    public void mouseWheelMoved(final MouseWheelEvent e) {
		if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown()) {
		    int change = -e.getWheelRotation();
		    changeFontSizeBy(change);
		    e.consume();
		}
	    }	
	});

	termListener = new MyTermListener();
	term.addListener(termListener);
	
//	final SupportStream supportStream = new SupportStream();
//	term.pushStream(supportStream);
//	term.setTransferHandler(new TransferHandlerImpl(supportStream));

        // Set up to convert clicks on active regions, created by OutputWriter.
        // println(), to outputLineAction notifications.
        term.setActionListener(new ActiveTermListener() {
            @Override
            public void action(ActiveRegion r, InputEvent e) {
                final Object userObject = r.getUserObject();
                if (userObject == null) {
                    return;
                }
                if (r.isLink() && userObject instanceof String) {
                    String text = (String) userObject;

                    int lineNumber = -1;
                    String filePath = text;

                    int colonIdx = text.lastIndexOf(':');
                    // Shortest file path
                    if (colonIdx > 2) {
                        try {
                            lineNumber = Integer.parseInt(text.substring(colonIdx + 1));
                            filePath = text.substring(0, colonIdx);
                        } catch (NumberFormatException x) {
                        }
                    }
                    OpenInEditorAction.post(filePath, lineNumber);
                } 
                else if (userObject instanceof OutputListener) {
                    OutputListener ol = (OutputListener) r.getUserObject();
                    if (ol == null) {
                        return;
                    }
                    Extent extent = r.getExtent();
                    String text = term.textWithin(extent.begin, extent.end);
                    OutputEvent oe = new TerminalOutputEvent(Terminal.this.tio, text);
                    ol.outputLineAction(oe);
                }
            }
        });

        // Tell term about keystrokes we use for menu accelerators so
        // it passes them through.
        /* LATER
         * A-V brings up the main View menu.
        term.getKeyStrokeSet().add(copyAction.getValue(Action.ACCELERATOR_KEY));
        term.getKeyStrokeSet().add(pasteAction.getValue(Action.ACCELERATOR_KEY))
;
        term.getKeyStrokeSet().add(closeAction.getValue(Action.ACCELERATOR_KEY))
;
        */

        this.setLayout(new BorderLayout());
        add(term, BorderLayout.CENTER);
	setFocusable(false);
    }

    void dispose() {
	if (disposed)
	    return;
	disposed = true;

        term.getScreen().removeMouseListener(mouseAdapter);
	term.removeListener(termListener);
	term.setActionListener(null);
	findState = null;
	shortcutsDir.removeFileChangeListener(shortcutsListener);
        termOptions.removePropertyChangeListener(termOptionsPCL);
	tio.dispose();
    }

    boolean isDisposed() {
	return disposed;
    }

    public IOContainer.CallBacks callBacks() {
        return callBacks;
    }

    public String name() {
	return name;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void requestFocus() {
	// redirect focus into terminal's screen
	term.getScreen().requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
	// redirect focus into terminal's screen
	return term.getScreen().requestFocusInWindow();
    }

    private void applyDebugFlags() {
	String value = System.getProperty("Term.debug");
	if (value == null)
	    return;

	int flags = 0;
	StringTokenizer st = new StringTokenizer(value, ",");	// NOI18N
	while (st.hasMoreTokens()) {
	    String s = st.nextToken();
	    if (s.toLowerCase().equals("ops"))			// NOI18N
		flags |= Term.DEBUG_OPS;
	    else if (s.toLowerCase().equals("keys"))		// NOI18N
		flags |= Term.DEBUG_KEYS;
	    else if (s.toLowerCase().equals("input"))		// NOI18N
		flags |= Term.DEBUG_INPUT;
	    else if (s.toLowerCase().equals("output"))		// NOI18N
		flags |= Term.DEBUG_OUTPUT;
	    else if (s.toLowerCase().equals("wrap"))		// NOI18N
		flags |= Term.DEBUG_WRAP;
	    else if (s.toLowerCase().equals("margins"))		// NOI18N
		flags |= Term.DEBUG_MARGINS;
	    else if (s.toLowerCase().equals("sequences"))	// NOI18N
		term.setSequenceLogging(true);
	    else
		;
	}
	term.setDebugFlags(flags);
    }

    private void applyTermOptions(boolean initial) {
        term.setFixedFont(true);
        term.setFont(termOptions.getFont());

        term.setBackground(termOptions.getBackground());
        term.setForeground(termOptions.getForeground());
        term.setHighlightColor(termOptions.getSelectionBackground());
        term.setHistorySize(termOptions.getHistorySize());
        term.setTabSize(termOptions.getTabSize());
	term.setSelectByWordDelimiters(termOptions.getSelectByWordDelimiters());

        term.setClickToType(termOptions.getClickToType());
        term.setScrollOnInput(termOptions.getScrollOnInput());
        term.setScrollOnOutput(termOptions.getScrollOnOutput());
        term.setAltSendsEscape(termOptions.getAltSendsEscape());
        if (initial) {
            term.setHorizontallyScrollable(!termOptions.getLineWrap());
	}
	
	applyShortcuts();

        // If we change the font from smaller to bigger, the size
        // calculations go awry and the last few lines are forever hidden.
        setSize(getPreferredSize());
        validate();

    }

    /**
     * Return the underlying Term.
     * @return the underlying StreamTerm.
     */
    public ActiveTerm term() {
        return term;
    }
    
    public String getCwd() {
	return cwd;
    }

    public void setTitle(String title) {
	customTitle = true;
	updateName(title);
    }

    public void resetTitle() {
	customTitle = false;
	updateName("");	//NOI18N
    }

    public String getTitle() {
        return title;
    }

    public FindState getFindState() {
        return findState;
    }

    public void activateSearch() {
        if (findState.isVisible()) {
            return;
        }
        findState.setVisible(true);
        Container ancestor = SwingUtilities.getAncestorOfClass(TerminalContainer.class, this);
        if (ancestor != null && ancestor instanceof TerminalContainer) {
            Task t = new Task.ActivateSearch((TerminalContainer) ancestor, this);
            t.post();
        }
    }

    public void changeFontSizeBy(final int d) {
	int oldFontSize = termOptions.getFontSize();
	
	int newFontSze = oldFontSize + d;
	if (newFontSze <= TermOptions.MIN_FONT_SIZE) {
	    newFontSze = TermOptions.MIN_FONT_SIZE;
	} else if (newFontSze >= TermOptions.MAX_FONT_SIZE) {
	    newFontSze = TermOptions.MAX_FONT_SIZE;
	} 
	
	TermOptions.getDefault(prefs).setFontSize(newFontSze);
    }

    public boolean isPinned() {
	return pinned;
    }

    public void setPinned(boolean pinned) {
	this.pinned = pinned;
    }
    
    public boolean isCustomTitle() {
	return customTitle;
    }

    public void setCustomTitle(boolean customTitle) {
	this.customTitle = customTitle;
    }

    // Ideally IOContainer.remove() would be unconditional and we could check
    // the isClosable() and vetoing here. However, Closing a tab via it's 'X'
    // is internal to IOContainer implementation and it calls IOCOntainer.remove()
    // directly. So we're stuck with it being conditional.
    //
    // But we can trick it into being unconditional by having MyIOVisibilityControl,
    // which gets called from IOCOntainer.remove(), return true if we're
    // closing unconditionally.

    /* package */ void setClosedUnconditionally(boolean closedUnconditionally) {
	this.closedUnconditionally = closedUnconditionally;
    }

    /* package */ boolean isClosedUnconditionally() {
	return closedUnconditionally;
    }

    public void closeUnconditionally() {
	setClosedUnconditionally(true);
	close();
    }

    public void close() {
	if (!isVisibleInContainer()) {
	    return;
	}

	if (isPinnable()) {
	    /* Will be enabled after delegating actions from TerminalContainerTabber will 
	     * be implemented. Enabling this now will cause inconsistensy or copy-pasting.
	     */
	    final AtomicBoolean canceled = new AtomicBoolean(false);
	    if (pinned && !closedUnconditionally) {
		final int CLOSE_AND_UNPIN = 0;
		final int CLOSE = 1;
		final int CANCEL = 2;

		final String CMD_CLOSE_AND_UNPIN = "CloseAndUnpin"; //NOI18N
		final String CMD_CLOSE = "Close"; //NOI18N
		final String CMD_CANCEL = "Cancel"; //NOI18N

		JButton[] options = new JButton[]{
		    new JButton(NbBundle.getMessage(Terminal.class, "TXT_CloseAndUnpin", getTitle())),
		    new JButton(NbBundle.getMessage(Terminal.class, "TXT_Close")),
		    new JButton(NbBundle.getMessage(Terminal.class, "TXT_Cancel"))
		};

		ActionListener commandListener = new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();

			if (CMD_CLOSE_AND_UNPIN.equals(command)) {
			    Action pinTabAction = ActionFactory.forID(ActionFactory.PIN_TAB_ACTION_ID);
			    if (pinTabAction != null) {
				pinTabAction.actionPerformed(null);
			    }
			} else if (CMD_CANCEL.equals(command)) {
			    canceled.set(true);
			    return;
			} else if (CMD_CLOSE.equals(command)){
			    return;
			}
		    }
		};

		options[CLOSE_AND_UNPIN].setActionCommand(CMD_CLOSE_AND_UNPIN);
		options[CLOSE].setActionCommand(CMD_CLOSE);
		options[CANCEL].setActionCommand(CMD_CANCEL);

		options[CLOSE_AND_UNPIN].addActionListener(commandListener);
		options[CLOSE].addActionListener(commandListener);
		options[CANCEL].addActionListener(commandListener);

		String message = NbBundle.getMessage(Terminal.class, "LBL_CloseTerminal", getTitle());

		DialogDescriptor dd = new DialogDescriptor(
			message,
			NbBundle.getMessage(Terminal.class, "TXT_CloseTerminalTitle"),
			true,
			options,
			options[CLOSE_AND_UNPIN],
			DialogDescriptor.DEFAULT_ALIGN,
			null,
			null
		);

		dd.setClosingOptions(options); // all are closing

		Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

		try {
		    dialog.setVisible(true);
		} catch (Throwable th) {
		    if (!(th.getCause() instanceof InterruptedException)) {
			throw new RuntimeException(th);
		    }
		    dd.setValue(options[CANCEL]);
		} finally {
		    dialog.dispose();
		}

		if (dd.getValue().equals(DialogDescriptor.DEFAULT_OPTION)) {
		    canceled.set(true);
		}
	    }

	    if (canceled.get()) {
		return;
	    }
	}

	ioContainer.remove(this);
    }

    public void setVisibleInContainer(boolean visible) {
	boolean wasVisible = this.visibleInContainer;
	this.visibleInContainer = visible;
	if (visible != wasVisible)
	    tio.pcs().firePropertyChange(IOVisibility.PROP_VISIBILITY, wasVisible, visible);
    }

    public boolean isVisibleInContainer() {
	return visibleInContainer;
    }

    public void setOutConnected(boolean outConnected) {
	boolean wasConnected = isConnected();
	this.outConnected = outConnected;

	// closing out implies closing err.
	if (outConnected == false)
	    this.errConnected = false;

	if (isConnected() != wasConnected) {
	    updateName();
	    tio.pcs().firePropertyChange(IOConnect.PROP_CONNECTED, wasConnected, isConnected());
	}
    }

    public void setErrConnected(boolean errConnected) {
	boolean wasConnected = isConnected();
	this.errConnected = errConnected;
	if (isConnected() != wasConnected) {
	    updateName();
	    tio.pcs().firePropertyChange(IOConnect.PROP_CONNECTED, wasConnected, isConnected());
	}
    }

    public void setExtConnected(boolean extConnected) {
	boolean wasConnected = isConnected();
	this.extConnected = extConnected;
	if (isConnected() != wasConnected) {
	    updateName();
	    tio.pcs().firePropertyChange(IOConnect.PROP_CONNECTED, wasConnected, isConnected());
	}
    }

    public boolean isConnected() {
	return outConnected || errConnected || extConnected;
    }

    private boolean okToHide() {
	try {
	    tio.vcs().fireVetoableChange(IOVisibility.PROP_VISIBILITY, true, false);
	} catch (PropertyVetoException ex) {
	    return false;
	}
	return true;
    }

    public void setClosable(boolean closable) {
	this.closable = closable;
	putClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON, ! closable);
    }

    public boolean isClosable() {
	return closable;
    }

    public void updateName(final String name) {
	this.title = name;
	updateName();
    }

    private void updateName() {
	Task task = new Task.UpdateName(ioContainer, this);
	task.post();
    }
    
    private void updateTooltopText(String text) {
	Task task = new Task.SetToolTipText(ioContainer, this, text);
	task.post();
    }

    private void setIcon(Icon icon) {
	Task task = new Task.SetIcon(ioContainer, this, icon);
	task.post();
    }

    private final ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/terminal/support/pin.png", false); //NOI18N

    public void pin(boolean newState) {
	if (newState) {
	    setIcon(icon);
	} else {
	    setIcon(null);
	}
	TerminalPinSupport.getDefault().findPinningDetails(term).setPinned(newState);

	setPinned(newState);
	
	pinTabAction.putValue("Name", PinTabAction.getMessage(newState)); //NOI18N
    }
    
    private boolean isPinnable() {
	Object clientProperty = this.getClientProperty("pinAction"); //NOI18N
	return clientProperty != null && clientProperty.equals("enabled"); //NOI18N
    }

    /*
    private static final String BOOLEAN_STATE_ACTION_KEY = "boolean_state_action";	// NOI18N
    private static final String BOOLEAN_STATE_ENABLED_KEY = "boolean_state_enabled";	// NOI18N

    private boolean isBooleanStateAction(Action a) {
	Boolean isBooleanStateAction = (Boolean) a.getValue(BOOLEAN_STATE_ACTION_KEY);	//
	return isBooleanStateAction != null && isBooleanStateAction;
    }

    // not used
    private void addMenuItem(JPopupMenu menu, Object o) {
	if (o instanceof JSeparator) {
	    menu.add((JSeparator) o);
	} else if (o instanceof Action) {
	    Action a = (Action) o;
	    if (isBooleanStateAction(a)) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setSelected((Boolean) a.getValue(BOOLEAN_STATE_ENABLED_KEY));
		menu.add(item);
	    } else {
		menu.add((Action) o);
	    }
	}
    }
    */
    
    private Action copyAction = ActionFactory.forID(ActionFactory.COPY_ACTION_ID);
    private Action pasteAction = ActionFactory.forID(ActionFactory.PASTE_ACTION_ID);
    private Action findAction = ActionFactory.forID(ActionFactory.FIND_ACTION_ID);
    private Action wrapAction = ActionFactory.forID(ActionFactory.WRAP_ACTION_ID);
    private Action largerFontAction = ActionFactory.forID(ActionFactory.LARGER_FONT_ACTION_ID);
    private Action smallerFontAction = ActionFactory.forID(ActionFactory.SMALLER_FONT_ACTION_ID);
    private Action setTitleAction = ActionFactory.forID(ActionFactory.SET_TITLE_ACTION_ID);
    private Action pinTabAction = ActionFactory.forID(ActionFactory.PIN_TAB_ACTION_ID);
    private Action clearAction = ActionFactory.forID(ActionFactory.CLEAR_ACTION_ID);
    private Action dumpSequencesAction = ActionFactory.forID(ActionFactory.DUMP_SEQUENCE_ACTION_ID);
    private Action closeAction = ActionFactory.forID(ActionFactory.CLOSE_ACTION_ID);
    private Action switchTabAction = ActionFactory.forID(ActionFactory.SWITCH_TAB_ACTION_ID);
    private Action newTabAction = ActionFactory.forID(ActionFactory.NEW_TAB_ACTION_ID);

    private void setupKeymap(Set<Action> actions) {
	// We need to do two things.
	// 1) bind various Actions' keystrokes via InputMap/ActionMap
	// 2_ Tell Term to ignore said keystrokes and not consume them.
	JComponent comp = term.getScreen();

	ActionMap actionMap = comp.getActionMap();
	ActionMap newActionMap = new ActionMap();
	newActionMap.setParent(actionMap);

	InputMap inputMap = comp.getInputMap();
	InputMap newInputMap = new InputMap();
	newInputMap.setParent(inputMap);

	Set<KeyStroke> passKeystrokes = new HashSet<KeyStroke>();

	for (Action a : actions) {
            String n = (String) a.getValue(Action.NAME);
            Object key = a.getValue(Action.ACCELERATOR_KEY);
            if (key == null) {
                continue;
            }
            if (key instanceof KeyStroke) {
                KeyStroke accelerator = (KeyStroke) key;
                newInputMap.put(accelerator, n);
                newActionMap.put(n, a);
                passKeystrokes.add(accelerator);
            } else if (key instanceof KeyStroke[]) {
                for (KeyStroke accelerator : (KeyStroke[]) key) {
                    newInputMap.put(accelerator, n);
                    newActionMap.put(n, a);
                    passKeystrokes.add(accelerator);
                }
            }
        }

	/* LATER
	 * unsuccessful attempt at fixing bug #185483
	 * getBoundKeyStrokes is not implemented.

	// get global keymap
	Collection<? extends Keymap> c = Lookup.getDefault().lookupAll(Keymap.class);
	System.out.printf("Terminal.setupKeymap() ... lookup returns %d hits\n", c.size());
	Keymap globalKeymap = Lookup.getDefault().lookup(Keymap.class);
	if (globalKeymap == null) {
	    System.out.printf("\tCouldn't findPinningDetails keymap\n");
	} else {
	    KeyStroke[] keyStrokes = globalKeymap.getBoundKeyStrokes();
	    for (KeyStroke ks : keyStrokes) {
		System.out.printf("\tks %s\n", ks);
	    }

	}

	// passKeystrokes.add(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
	// passKeystrokes.add(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
	 */
	
	comp.setActionMap(newActionMap);
	comp.setInputMap(JComponent.WHEN_FOCUSED, newInputMap);
        term.setKeyStrokeSet((HashSet) passKeystrokes);
    }

    private Point getPopupPosition() {
        final int offset = 25;

        return new Point(offset, offset);
    }

    private void postPopupMenu(Point p) {
	findAction.setEnabled(!findState.isVisible());

	// TMP Find is not operation so keep it disabled
	// TODO IG fix
	findAction.setEnabled(false);
        
        Container container = SwingUtilities.getAncestorOfClass(TerminalContainer.class, this);
        boolean isTerminalContainer = (container != null && container instanceof TerminalContainer);
	
	JPopupMenu menu = Utilities.actionsToPopup(
		new Action[]{
                    newTabAction,
                    null,
		    copyAction,
		    pasteAction,
		    null,
		    isTerminalContainer ? findAction: null,
		    null,
		    wrapAction,
		    largerFontAction,
		    smallerFontAction,
		    null,
		    setTitleAction,
		    isPinnable() ? pinTabAction : null, //NOI18N
		    null,
		    clearAction,
		    isClosable() ? closeAction : null, // it's ok to have null as last element,
		    (System.getProperty("Term.debug") != null) ? dumpSequencesAction : null
		}, Lookups.fixed(this)
	);
	menu.putClientProperty("container", ioContainer); // NOI18N
	menu.putClientProperty("component", this);             // NOI18N

	/* LATER?
	 * NB IO APIS don't add sidebar actions to menu
        Action[] acts = getActions();
        if (acts.length > 0) {
            for (Action a : acts) {
                if (a.getValue(Action.NAME) != null)
                    menu.add(a);
            }
            if (menu.getSubElements().length > 0)
                menu.add(new JSeparator());
        }
	 */
	
        menu.addPopupMenuListener(new PopupMenuListener() {
	    @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
	    @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }
	    @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        } );
        menu.show(term.getScreen(), p.x, p.y);
    }

    private Map<?, ?> getRenderingHints() {
        Map<?, ?> renderingHints = null;
        // init hints if any
        Lookup lookup = MimeLookup.getLookup("text/plain"); // NOI18N
        if (lookup != null) {
            FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
            if (fcs != null) {
                AttributeSet attributes = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
                if (attributes != null) {
                    renderingHints = (Map<?, ?>) attributes.getAttribute(EditorStyleConstants.RenderingHints);
                }
            }
        }
	return renderingHints;
    }

    /**
     * Callback for when a hyperlink in a Terminal is clicked.
     * <p>
     * A hyperlink can be created by outputting a sequence like this:
     * <br>
     * <b>ESC</b>]10;<i>clientData</i>;<i>text</i><b>BEL</b>
     * @author ivan
     */
    public interface HyperlinkListener {
	public void action(String clientData);
    }

    public void setHyperlinkListener(final HyperlinkListener hyperlinkListener) {
	term.setActionListener(new ActiveTermListener() {
	    public void action(ActiveRegion r, InputEvent e) {
		if (r.isLink()) {
		    String url = (String) r.getUserObject();
		    hyperlinkListener.action(url);
		}
	    }
	});
    }

    void scrollTo(Coord coord) {
        term.possiblyNormalize(coord);
    }
    
    private void applyShortcuts() {
	if (!termOptions.getIgnoreKeymap()) {
	    Set<String> actions = new HashSet<String>();
	    for (FileObject def : shortcutsDir.getChildren()) {
		try {
		    DataObject dobj = DataObject.find(def);
		    InstanceCookie ic = dobj.getLookup().lookup(InstanceCookie.class);
		    if (ic != null) {
			// put class names in the map,
			// otherwise we may end with several instances of the action
			actions.add(ic.instanceCreate().getClass().getName());
		    }
		} catch (Exception e) {
		    Exceptions.printStackTrace(e);
		}
	    }
	    term.setKeymap(Lookup.getDefault().lookup(Keymap.class), actions);
	    // needed for Ctrl+Tab, Ctrl+Shift+Tab switching
	    term.getScreen().setFocusTraversalKeysEnabled(false);
	} else {
	    term.setKeymap(null, null);
	    term.getScreen().setFocusTraversalKeysEnabled(true);
	}
    }
    
    private class ShortcutsListener extends FileChangeAdapter {
	@Override
	public void fileAttributeChanged(FileAttributeEvent fe) {
	    applyShortcuts();
	}

	@Override
	public void fileChanged(FileEvent fe) {
	    applyShortcuts();
	}

	@Override
	public void fileDataCreated(FileEvent fe) {
	    applyShortcuts();
	}

	@Override
	public void fileDeleted(FileEvent fe) {
	    applyShortcuts();
	}

	@Override
	public void fileFolderCreated(FileEvent fe) {
	    applyShortcuts();
	}

	@Override
	public void fileRenamed(FileRenameEvent fe) {
	    applyShortcuts();
	}
    }
    
    private ActiveTerm createActiveTerminal() {
	Clipboard aSystemClipboard = Lookup.getDefault().lookup(Clipboard.class);
	if (aSystemClipboard == null) {
	    aSystemClipboard = getToolkit().getSystemClipboard();
	}
	return new MyActiveTerm(aSystemClipboard);
    }
    
    private static final class MyActiveTerm extends ActiveTerm {
	private final Clipboard systemClipboard;
	
	private MyActiveTerm(Clipboard systemClipboard) {
	    this.systemClipboard = systemClipboard;
	}
	
	@Override
	public void copyToClipboard() {
	    String text = getSelectedText();
	    if (text != null) {
		StringSelection ss = new StringSelection(text);
		systemClipboard.setContents(ss, ss);
	    }
	}
    }


    private static class SupportStream extends TermStream {

	@Override
	public void flush() {
	    if (toDCE == toDTE) {
		toDCE.flush();
	    } else {
		toDTE.flush();
		toDCE.flush();
	    }
	}

	@Override
	public void putChar(char c) {
	    toDTE.putChar(c);
	}

	@Override
	public void putChars(char[] buf, int offset, int count) {
	    toDTE.putChars(buf, offset, count);
	}

	@Override
	public void sendChar(char c) {
	    toDCE.sendChar(c);
	}

	@Override
	public void sendChars(char[] c, int offset, int count) {
	    toDCE.sendChars(c, offset, count);
	}

    }

    private static class TransferHandlerImpl extends TransferHandler {

	private DataFlavor dataObjectDnd = null;
	private DataFlavor multiTransferObject = null;
	private final SupportStream stream;

	public TransferHandlerImpl(SupportStream stream) {
	    /*
	     * Trying to load data flavor for drag'n'drop operations . 
	     * So in this case just don't enable drag'n'drop feature for FileObjects.
	     */
	    try {
		this.dataObjectDnd = new DataFlavor("application/x-java-openide-dataobjectdnd;class=org.openide.loaders.DataObject;mask={0}");//NOI18N
	    } catch (ClassNotFoundException ex) {
	    }
	    try {
		this.multiTransferObject = new DataFlavor("application/x-java-openide-multinode;class=org.openide.util.datatransfer.MultiTransferObject;mask={0}");//NOI18N
	    } catch (ClassNotFoundException ex) {
	    }
	    
	    this.stream = stream;
	}

	private void display(List<String> strings) {
	    StringBuilder sb = new StringBuilder();
	    for (String string : strings) {
		sb.append('\'');
		sb.append(string);
		sb.append('\'');
		sb.append(' ');
	    }

	    final String str = sb.toString();
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    stream.sendChars(str.toCharArray(), 0, str.length());
		    }
	    });
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
	    boolean canHandleDO = (dataObjectDnd != null && support.isDataFlavorSupported(dataObjectDnd));
	    boolean canHandleMTO = (multiTransferObject != null && support.isDataFlavorSupported(multiTransferObject));
	    boolean canHandleList = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	    
	    if (canHandleDO || canHandleList) {
		return true;
	    } else if (canHandleMTO) {
		try {
		    MultiTransferObject mto = (MultiTransferObject) support.getTransferable().getTransferData(multiTransferObject);
		    for (int i = 0; i < mto.getCount(); i++) {
			if (mto.isDataFlavorSupported(i, dataObjectDnd)) {
			    return true;
			}
		    }
		} catch (UnsupportedFlavorException ex) {
		} catch (IOException ex) {
		}
	    }

	    return false;
	}

	/**
	 * Drops a list of objects to the Terminal, single quoted, space as
	 * a delimiter. Terminal TC won't gain focus.
	 * Order: 
	 * 1. List of FileObject 
	 * 2. Single FileObject 
	 * 3. List of File. 
	 * FO stands before File because list of RemoteFO is recognized as list 
	 * of File but can't be correctly handled.
	 *
	 */
	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
	    Transferable transferable = support.getTransferable();

	    try {
		if (multiTransferObject != null && support.isDataFlavorSupported(multiTransferObject)) {
		    MultiTransferObject mto = (MultiTransferObject) transferable.getTransferData(multiTransferObject);
		    List<String> strings = new ArrayList<String>();
		    for (int i = 0; i < mto.getCount(); i++) {
			if (mto.isDataFlavorSupported(i, dataObjectDnd)) {
			    DataObject dObj = (DataObject) mto.getTransferData(i, dataObjectDnd);
			    FileObject fObj = dObj.getLookup().lookup(FileObject.class);
			    if (fObj != null) {
				strings.add(fObj.getPath());
			    }
			}
		    }
		    display(strings);
		    return true;
		} else if (dataObjectDnd != null && support.isDataFlavorSupported(dataObjectDnd)) {
		    DataObject dObj = (DataObject) transferable.getTransferData(dataObjectDnd);
		    FileObject fObj = dObj.getLookup().lookup(FileObject.class);
		    if (fObj != null) {
			String str = fObj.getPath();
			display(Arrays.asList(str));
			return true;
		    }
		} else if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		    List<File> list = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
		    List<String> strings = new ArrayList<String>();
		    for (File file : list) {
			strings.add(file.getAbsolutePath());
		    }

		    display(strings);
		    return true;
		}
	    } catch (UnsupportedFlavorException ex) {
	    } catch (IOException ex) {
	    }

	    return false;
	}
    }
}
