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

package org.netbeans.core.output2;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.output2.Controller.ControllerOutputEvent;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOContainer;
import org.openide.windows.OutputListener;
import static org.netbeans.core.output2.OutputTab.ACTION.*;
import org.netbeans.core.output2.options.OutputOptions;
import org.netbeans.core.output2.ui.OutputKeymapManager;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.IOColors;
import org.openide.windows.OutputEvent;


/**
 * A component representing one tab in the output window.
 */
final class OutputTab extends AbstractOutputTab implements IOContainer.CallBacks {
    private static final RequestProcessor RP =
            new RequestProcessor("OutputTab");                          //NOI18N
    private final NbIO io;
    private OutWriter outWriter;
    private PropertyChangeListener optionsListener;
    private volatile boolean actionsLoaded = false;

    OutputTab(NbIO io) {
        this.io = io;
        if (Controller.LOG) Controller.log ("Created an output component for " + io);
        outWriter = ((NbWriter) io.getOut()).out();
        OutputDocument doc = new OutputDocument(outWriter);
        setDocument(doc);
        applyOptions();
        initOptionsListener();
        loadAndInitActions();
    }

    private void applyOptions() {
        Lines lines = getDocumentLines();
        if (lines != null) {
            OutputOptions opts = io.getOptions();
            lines.setDefColor(IOColors.OutputType.OUTPUT,
                    opts.getColorStandard());
            lines.setDefColor(IOColors.OutputType.ERROR,
                    opts.getColorError());
            lines.setDefColor(IOColors.OutputType.INPUT,
                    opts.getColorInput());
            lines.setDefColor(IOColors.OutputType.HYPERLINK,
                    opts.getColorLink());
            lines.setDefColor(IOColors.OutputType.HYPERLINK_IMPORTANT,
                    opts.getColorLinkImportant());
            Color bg = io.getOptions().getColorBackground();
            getOutputPane().getFoldingSideBar().setForeground(
                    opts.getColorStandard());
            setTextViewBackground(getOutputPane().getTextView(), bg);
            getOutputPane().setViewFont(
                    io.getOptions().getFont(getOutputPane().isWrapped()));
        }
    }

    /**
     * Set text view background color correctly. See bug #225829.
     */
    private void setTextViewBackground(JTextComponent textView, Color bg) {
        getOutputPane().getTextView().setBackground(bg);
        getOutputPane().getFoldingSideBar().setBackground(bg);
        if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) { //NOI18N
            UIDefaults defaults = new UIDefaults();
            defaults.put("EditorPane[Enabled].backgroundPainter", bg);  //NOI18N
            textView.putClientProperty("Nimbus.Overrides", defaults);   //NOI18N
            textView.putClientProperty(
                    "Nimbus.Overrides.InheritDefaults", true);          //NOI18N
            textView.setBackground(bg);
        }
    }

    private final TabAction action(ACTION a) {
        return actions.get(a);
    }

    private void installKBActions() {
        for (ACTION a : actionsToInstall) {
            installKeyboardAction(action(a));
        }
    }

    @Override
    public void setDocument (Document doc) {
        if (Controller.LOG) Controller.log ("Set document on " + this + " with " + io);
        assert SwingUtilities.isEventDispatchThread();
        Document old = getDocument();
        hasOutputListeners = false;
        super.setDocument(doc);
        if (old instanceof OutputDocument) {
            ((OutputDocument) old).dispose();
        }
        applyOptions();
    }

    public void reset() {
        if (origPane != null) {
            setFilter(null, false, false);
        }
        // get new OutWriter
        outWriter = io.out();

        // Workaround for bug 242979.
        Document actualDocument = getDocument();
        if (actualDocument instanceof OutputDocument) {
            OutputDocument od = (OutputDocument) actualDocument;
            if (od.getWriter() == outWriter) {
                return;
            }
        }
        setDocument(new OutputDocument(outWriter));
        applyOptions();
    }

    public OutputDocument getDocument() {
        Document d = getOutputPane().getDocument();
        if (d instanceof OutputDocument) {
            return (OutputDocument) d;
        }
        return null;
    }

    @Override
    protected AbstractOutputPane createOutputPane() {
        return new OutputPane(this);
    }

    @Override
    public void inputSent(String txt) {
        if (Controller.LOG) Controller.log("Input sent on OutputTab: " + txt);
        getOutputPane().lockScroll();
        NbIO.IOReader in = io.in();
        if (in != null) {
            if (Controller.LOG) Controller.log("Sending input to " + in);
            in.pushText(txt + "\n");
            outWriter.print(txt, null, false, null, null, OutputKind.IN, true);
        }
    }

    @Override
    protected void inputEof() {
        if (Controller.LOG) Controller.log ("Input EOF");
        NbIO.IOReader in = io.in();
        if (in != null) {
            in.eof();
        }
    }

    @Override
    public void hasSelectionChanged(boolean val) {
        if (isShowing() && actionsLoaded) {
            actions.get(ACTION.COPY).setEnabled(val);
            actions.get(ACTION.SELECT_ALL).setEnabled(!getOutputPane().isAllSelected());
        }
    }

    public NbIO getIO() {
        return io;
    }
    
    void requestActive() {
        io.getIOContainer().requestActive();
    }

    public void lineClicked(int line, int pos) {
        OutWriter out = getOut();
        int range[] = new int[2];
        OutputListener l = out.getLines().getListener(pos, range);
        if (l != null) {
            int size = out.getLines().getCharCount();
            assert range[1] < size : "Size: " + size + " range: " + range[0] + " " + range[1];
            ControllerOutputEvent oe = new ControllerOutputEvent(io, out, line);
            l.outputLineAction(oe);
            //Select the text on click if it is still visible
            if (getOutputPane().getLength() >= range[1]) { // #179768
                getOutputPane().sendCaretToPos(range[0], range[1], true, false);
            }
        }
    }

    void enterPressed(int caretMark, int caretDot) {
        int start, end;
        if (caretMark < caretDot) {
            start = caretMark;
            end = caretDot;
        } else {
            start = caretDot;
            end = caretMark;
        }
        if (getOut().getLines().isListener(start, end)) {
            int[] range = new int[2];
            OutputListener listener = getOut().getLines().nearestListener(
                    start, false, range);
            if (listener != null && range[0] <= start && range[1] >= end) {
                int line = getOut().getLines().getLineAt(start);
                OutputEvent oe = new ControllerOutputEvent(io, line);
                listener.outputLineAction(oe);
            }
        }
    }

    @Override
    public String toString() {
        return "OutputTab@" + System.identityHashCode(this) + " for " + io;
    }

    private boolean hasOutputListeners = false;

    public void documentChanged(OutputPane pane) {
        if (filtOut != null && pane == origPane) {
            filtOut.readFrom(outWriter);
        }
        boolean hadOutputListeners = hasOutputListeners;
        hasOutputListeners = getOut() != null && (getOut().getLines().firstListenerLine() >= 0 || getOut().getLines().firstImportantListenerLine() >= 0);
        if (hasOutputListeners != hadOutputListeners) {
            hasOutputListenersChanged(hasOutputListeners);
        }

        IOContainer ioContainer = io.getIOContainer();
        if (io.isFocusTaken()) {
	    // The following two lines pulled up from select per bug#185209
	    ioContainer.open();
	    // ioContainer.requestVisible();

            ioContainer.select(this);
            ioContainer.requestVisible();
        }
        Controller.getDefault().updateName(this);
        if (this == ioContainer.getSelected() && ioContainer.isActivated()) {
            updateActions();
        }
    }

    /**
     * Called when the output stream has been closed, to navigate to the
     * first line which shows an error (if any).
     */
    private void navigateToFirstErrorLine() {
        OutWriter out = getOut();
        if (out != null) {
            int line = out.getLines().firstImportantListenerLine();
            if (Controller.LOG) {
                Controller.log("NAV TO FIRST LISTENER LINE: " + line);
            }
            if (line >= 0) {
                getOutputPane().sendCaretToLine(line, false);
            }
        }
    }

     void hasOutputListenersChanged(boolean hasOutputListeners) {
        if (hasOutputListeners && getOutputPane().isScrollLocked()) {
            navigateToFirstErrorLine();
        }
    }

    @Override
    public void activated() {
        updateActions();
    }

    @Override
    public void closed() {
        io.setClosed(true);
        io.setStreamClosed(true);
        Controller.getDefault().removeTab(io);
        NbWriter w = io.writer();
        if (w != null && w.isClosed()) {
            //Will dispose the document
            setDocument(null);
            io.dispose();
        } else if (w != null) {
            //Something is still writing to the stream, but we're getting rid of the tab.  Don't dispose
            //the writer, just kill the tab's document
            if (getOut() != null) {
                getOut().setDisposeOnClose(true);
            }
            getDocument().disposeQuietly();
            NbIOProvider.dispose(io);
        }
    }

    @Override
    public void deactivated() {
    }

    @Override
    public void selected() {
    }

    /**
     * Determine if the new caret position is close enough that the scrollbar should be re-locked
     * to the end of the document.
     *
     * @param dot The caret position
     * @return if it should be locked
     */
    public boolean shouldRelock(int dot) {
        OutWriter w = getOut();
        if (w != null && !w.isClosed()) {
            int dist = Math.abs(w.getLines().getCharCount() - dot);
            return dist < 100;
        }
        return false;
    }

    /**
     * Flag used to block navigating the editor to the first error line when
     * selecting the error line in the output window after a build (or maybe
     * it should navigate the editor there?  Could be somewhat rude...)
     */
    boolean ignoreCaretChanges = false;

    /**
     * Called when the text caret has changed position - will call OutputListener.outputLineSelected if
     * there is a listener for that position.
     *
     * @param pos The line the caret is in
     */
    int lastCaretListenerRange[];
    void caretPosChanged(int pos) {
        if (!ignoreCaretChanges) {
            if (lastCaretListenerRange != null && pos >= lastCaretListenerRange[0] && pos < lastCaretListenerRange[1]) {
                return;
            }
            OutWriter out = getOut();
            if (out != null) {
                int[] range = new int[2];
                OutputListener l = out.getLines().getListener(pos, range);
                if (l != null) {
                    ControllerOutputEvent oe = new ControllerOutputEvent(io, out.getLines().getLineAt(pos));
                    l.outputLineSelected(oe);
                    lastCaretListenerRange = range;
                } else {
                    lastCaretListenerRange = null;
                }
            }
        }
    }

    private void sendCaretToError(boolean backward) {
        OutWriter out = getOut();
        if (out != null) {
            AbstractOutputPane op = getOutputPane();
            int selStart = op.getSelectionStart();
            int selEnd = op.getSelectionEnd();
            int pos = op.getCaretPos();

            // check if link is selected
            if (selStart != selEnd && pos == selStart && out.getLines().isListener(selStart, selEnd)) {
                pos = backward ? selStart - 1 : selEnd + 1;
            }
            int[] lpos = new int[2];
            OutputListener l = out.getLines().nearestListener(pos, backward, lpos);
            if (l != null) {
                op.sendCaretToPos(lpos[0], lpos[1], true);
                if (!io.getIOContainer().isActivated()) {
                    ControllerOutputEvent ce = new ControllerOutputEvent(io,  out.getLines().getLineAt(lpos[0]));
                    l.outputLineAction(ce);
                }
            }
        }
    }

    /**
     * Searching from current position
     * @param reversed true for reverse search
     * @return true if found
     */
    private boolean find(boolean reversed) {
        OutWriter out = getOut();
        if (out != null) {
            String lastPattern = FindDialogPanel.result();
            if (lastPattern == null) {
                return false;
            }
            int pos = reversed ? getOutputPane().getSelectionStart() : getOutputPane().getCaretPos();
            if (pos > getOutputPane().getLength() || pos < 0) {
                pos = 0;
            }
            boolean regExp = FindDialogPanel.regExp();
            boolean matchCase = FindDialogPanel.matchCase();
            int[] sel = reversed ? out.getLines().rfind(pos, lastPattern, regExp, matchCase)
                    : out.getLines().find(pos, lastPattern, regExp, matchCase);
            String appendMsg = null;
            if (sel == null) {
                sel = reversed ? out.getLines().rfind(out.getLines().getCharCount(), lastPattern, regExp, matchCase)
                        : out.getLines().find(0, lastPattern, regExp, matchCase);
                if (sel != null) {
                    appendMsg = NbBundle.getMessage(OutputTab.class, reversed ? "MSG_SearchFromEnd" : "MSG_SearchFromBeg");
                }
            }
            String msg;
            if (sel != null) {
                getOutputPane().unlockScroll();
                int line = out.getLines().getLineAt(sel[0]);
                ensureLineVisible(out, line);
                getOutputPane().setSelection(sel[0], sel[1]);
                int col = sel[0] - out.getLines().getLineStart(line);
                msg = NbBundle.getMessage(OutputTab.class, "MSG_Found", lastPattern, line + 1, col + 1);
                if (appendMsg != null) {
                    msg = msg + "; " + appendMsg;
                }
            } else {
                msg = NbBundle.getMessage(OutputTab.class, "MSG_NotFound", lastPattern);
            }
            StatusDisplayer.getDefault().setStatusText(msg);
            return sel != null;
        }
        return false;
    }

    /**
     * Ensure that a line is visible (not inside a collapsed fold). If a change
     * in the lines object is needed, fire immediately.
     */
    private void ensureLineVisible(OutWriter out, int line) {
        if (!out.getLines().isVisible(line)) {
            out.getLines().showFoldsForLine(line);
            if (out.getLines() instanceof ActionListener) {
                ((ActionListener) out.getLines()).actionPerformed(null);
            }
        }
    }

    /**
     * Holds the last written to directory for the save as file chooser.
     */
    private static String lastDir = null;

    /**
     * Invokes a file dialog and if a file is chosen, saves the output to that file.
     */
    void saveAs() {
        OutWriter out = getOut();
        if (out == null) {
            return;
        }
        File f = showFileChooser(this);
        if (f != null) {
            try {
                synchronized (out) {
                    out.getLines().saveAs(f.getPath());
                }
            } catch (IOException ioe) {
                NotifyDescriptor notifyDesc = new NotifyDescriptor(
                        NbBundle.getMessage(OutputTab.class, "MSG_SaveAsFailed", f.getPath()),
                        NbBundle.getMessage(OutputTab.class, "LBL_SaveAsFailedTitle"),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        new Object[]{NotifyDescriptor.OK_OPTION},
                        NotifyDescriptor.OK_OPTION);

                DialogDisplayer.getDefault().notify(notifyDesc);
            }
        }
    }

    /**
     * Shows a file dialog and an overwrite dialog if the file exists, returning
     * null if the user chooses not to overwrite.  Will use an AWT FileDialog for
     * Aqua, per Apple UI guidelines.
     *
     * @param owner A parent component for the dialog - the top level ancestor will
     *        actually be used so positioning is correct
     * @return A file to write to
     */
    private static File showFileChooser(JComponent owner) {
        File f = null;
        String dlgTtl = NbBundle.getMessage(Controller.class, "TITLE_SAVE_DLG"); //NOI18N

        boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

        if (isAqua) {
            //Apple UI guidelines recommend against ever using JFileChooser
            Container frameOrDialog = owner.getTopLevelAncestor();
            FileDialog fd;
            if (frameOrDialog instanceof Frame) {
                fd = new FileDialog((Frame) frameOrDialog, dlgTtl, FileDialog.SAVE);
            } else {
                fd = new FileDialog((Dialog) frameOrDialog, dlgTtl, FileDialog.SAVE);
            }
            if (lastDir != null && new File(lastDir).exists()) {
                fd.setDirectory(lastDir);
            }
            fd.setModal(true);
            fd.setVisible(true);
            if (fd.getFile() != null && fd.getDirectory() != null) {
                String s = fd.getDirectory() + fd.getFile();
                f = new File(s);
                if (f.exists() && f.isDirectory()) {
                    f = null;
                }
            }
        } else {
            JFileChooser jfc = new JFileChooser();
            if (lastDir != null && new File(lastDir).exists()) {
                File dir = new File(lastDir);
                if (dir.exists()) {
                    jfc.setCurrentDirectory(dir);
                }
            }
            jfc.setName(dlgTtl);
            jfc.setDialogTitle(dlgTtl);

            if (jfc.showSaveDialog(owner.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
                f = jfc.getSelectedFile();
            }
        }

        if (f != null && f.exists() && !isAqua) { //Aqua's file dialog takes care of this
            String msg = NbBundle.getMessage(Controller.class,
                    "FMT_FILE_EXISTS", new Object[]{f.getName()}); //NOI18N
            String title = NbBundle.getMessage(Controller.class,
                    "TITLE_FILE_EXISTS"); //NOI18N
            if (JOptionPane.showConfirmDialog(owner.getTopLevelAncestor(), msg, title,
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                f = null;
            }
        }
        if (f != null) {
            lastDir = f.getParent();
        }
        return f;
    }

    /**
     * Called when a line is clicked - if an output listener is listening on that
     * line, it will be sent <code>outputLineAction</code>.
     */
    private void openHyperlink() {
        OutWriter out = getOut();
        if (out != null) {
            int pos = getOutputPane().getCaretPos();
            int[] range = new int[2];
            OutputListener l = out.getLines().getListener(pos, range);
            if (l != null) {
                ignoreCaretChanges = true;
                getOutputPane().sendCaretToPos(range[0], range[1], true);
                ignoreCaretChanges = false;
                ControllerOutputEvent coe = new ControllerOutputEvent(io, out.getLines().getLineAt(pos));
                l.outputLineAction(coe);
            }
        }
    }

    /**
     * Post the output window's popup menu
     *
     * @param p The point clicked
     * @param src The source of the click event
     */
    @NbBundle.Messages({"STATUS_Initializing=Output Window is Initializing"})
    void postPopupMenu(Point p, Component src) {
        if (!actionsLoaded) {
            StatusDisplayer.getDefault().setStatusText(
                    Bundle.STATUS_Initializing());
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        Action[] a = getToolbarActions();
        if (a.length > 0) {
            boolean added = false;
            for (int i = 0; i < a.length; i++) {
                if (a[i].getValue(Action.NAME) != null) {
                    // add the proxy that doesn't show icons #67451
                    popup.add(new ProxyAction(a[i]));
                    added = true;
                }
            }
            if (added) {
                popup.addSeparator();
            }
        }

        List<TabAction> activeActions = new ArrayList<>(popupItems.length);
        for (int i = 0; i < popupItems.length; i++) {
            if (popupItems[i] == null) {
                popup.addSeparator();
            } else {
                TabAction ta = action(popupItems[i]);
                if (popupItems[i] == ACTION.WRAP) {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(ta);
                    item.setSelected(getOutputPane().isWrapped());
                    activeActions.add(ta);
                    popup.add(item);
                } else if (popupItems[i] == ACTION.FILTER) {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(ta);
                    item.setSelected(origPane != null);
                    activeActions.add(ta);
                    popup.add(item);
                } else {
                    if ((popupItems[i] == ACTION.CLOSE
                            && !io.getIOContainer().isCloseable(this))) {
                        continue;
                    }
                    JMenuItem item = popup.add(ta);
                    activeActions.add(ta);
                    if (popupItems[i] == ACTION.FIND) {
                        item.setMnemonic(KeyEvent.VK_F);
                    }
                }
            }
        }
        addFoldingActionsToPopUpMenu(popup, activeActions);
        // hack to remove the esc keybinding when doing popup..
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JComponent c = getOutputPane().getTextView();
        Object escHandle = c.getInputMap().get(esc);
        c.getInputMap().remove(esc);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(esc);

        popup.addPopupMenuListener(new PMListener(activeActions, escHandle));
        popup.show(src, p.x, p.y);

    }

    private void addFoldingActionsToPopUpMenu(JPopupMenu menu,
            List<TabAction> activeActions) {
        JMenu submenu = new JMenu(NbBundle.getMessage(
                OutputTab.class, "LBL_OutputFolds"));                   //NOI18N
        for (ACTION a : popUpFoldItems) {
            if (a == null) {
                submenu.addSeparator();
            } else {
                TabAction ta = action(a);
                activeActions.add(ta);
                submenu.add(new JMenuItem(ta));
            }
        }
        menu.addSeparator();
        menu.add(submenu);
    }

    void updateActions() {
        if (!actionsLoaded) {
            return;
        }
        OutputPane pane = (OutputPane) getOutputPane();
        int len = pane.getLength();
        boolean enable = len > 0;
        OutWriter out = getOut();
        action(SAVEAS).setEnabled(enable);
        action(SELECT_ALL).setEnabled(enable);
        action(COPY).setEnabled(pane.hasSelection());
        boolean hasErrors = out == null ? false : out.getLines().hasListeners();
        action(NEXT_ERROR).setEnabled(hasErrors);
        action(PREV_ERROR).setEnabled(hasErrors);
    }

    FilteredOutput filtOut;
    AbstractOutputPane origPane;

    private void setFilter(String pattern, boolean regExp, boolean matchCase) {
        if (pattern == null) {
            assert origPane != null;
            setOutputPane(origPane);
            origPane = null;
            filtOut.dispose();
            filtOut = null;
        } else {
            assert origPane == null;
            origPane = getOutputPane();
            filtOut = new FilteredOutput(pattern, regExp, matchCase);
            setOutputPane(filtOut.getPane());
            try {
                waitCursor(true);
                filtOut.readFrom(outWriter);
                installKBActions();
            } finally {
                waitCursor(false);
            }
        }
        validate();
        getOutputPane().repaint();
        requestFocus();
    }

    private void waitCursor(boolean enable) {
        RootPaneContainer root = ((RootPaneContainer) getTopLevelAncestor());
        Cursor cursor = Cursor.getPredefinedCursor(enable ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR);
        root.getGlassPane().setCursor(cursor);
        root.getGlassPane().setVisible(enable);
    }

    OutWriter getOut() {
        return origPane != null ? filtOut.getWriter() : outWriter;
    }

    private void initOptionsListener() {
        optionsListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Lines lines = getDocumentLines();
                if (lines != null) {
                    String pn = evt.getPropertyName();
                    OutputOptions opts = io.getOptions();
                    updateOptionsProperty(pn, lines, opts);
                    OutputTab.this.repaint();
                }
            }
        };
        this.io.getOptions().addPropertyChangeListener(
                WeakListeners.propertyChange(optionsListener, io.getOptions()));
    }

    private void updateOptionsProperty(String pn, Lines lines,
            OutputOptions opts) {

        if (OutputOptions.PROP_COLOR_STANDARD.equals(pn)) {
            lines.setDefColor(IOColors.OutputType.OUTPUT,
                    opts.getColorStandard());
            getOutputPane().getFoldingSideBar().setForeground(
                    opts.getColorStandard());
        } else if (OutputOptions.PROP_COLOR_ERROR.equals(pn)) {
            lines.setDefColor(IOColors.OutputType.ERROR,
                    opts.getColorError());
        } else if (OutputOptions.PROP_COLOR_INPUT.equals(pn)) {
            lines.setDefColor(IOColors.OutputType.INPUT,
                    opts.getColorInput());
        } else if (OutputOptions.PROP_COLOR_LINK.equals(pn)) {
            lines.setDefColor(IOColors.OutputType.HYPERLINK,
                    opts.getColorLink());
        } else if (OutputOptions.PROP_COLOR_LINK_IMPORTANT.equals(pn)) {
            lines.setDefColor(IOColors.OutputType.HYPERLINK_IMPORTANT,
                    opts.getColorLinkImportant());
        } else if (OutputOptions.PROP_COLOR_BACKGROUND.equals(pn)) {
            Color bg = opts.getColorBackground();
            setTextViewBackground(getOutputPane().getTextView(), bg);
        } else if (OutputOptions.PROP_FONT.equals(pn)) {
            if (!getOutputPane().isWrapped()) {
                getOutputPane().setViewFont(opts.getFont());
            }
        } else if (OutputOptions.PROP_FONT_SIZE_WRAP.equals(pn)) {
            if (getOutputPane().isWrapped()) {
                getOutputPane().setViewFont(opts.getFontForWrappedMode());
            }
        }
    }

    private void loadAndInitActions() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                createActions();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        installKBActions();
                        getActionMap().put("jumpPrev", //NOI18N
                                action(PREV_ERROR));
                        getActionMap().put("jumpNext", //NOI18N
                                action(NEXT_ERROR));
                        getActionMap().put(FindAction.class.getName(),
                                action(FIND));
                        getActionMap().put(
                                javax.swing.text.DefaultEditorKit.copyAction,
                                action(COPY));
                        actionsLoaded = true;
                        updateActions();
                        setInputVisible(isInputVisible()); // update action
                    }
                });
            }
        });
    }

    static enum ACTION { COPY, WRAP, SAVEAS, CLOSE, NEXT_ERROR, PREV_ERROR,
                         SELECT_ALL, FIND, FIND_NEXT, NAVTOLINE, POSTMENU,
                         FIND_PREVIOUS, CLEAR, NEXTTAB, PREVTAB, LARGER_FONT,
                         SMALLER_FONT, SETTINGS, FILTER, PASTE, COLLAPSE_FOLD,
                         EXPAND_FOLD, COLLAPSE_ALL, EXPAND_ALL, COLLAPSE_TREE,
                         EXPAND_TREE}

    private static final ACTION[] popupItems = new ACTION[] {
        COPY, PASTE, null, FIND, FIND_NEXT, FIND_PREVIOUS, FILTER, null,
        WRAP, LARGER_FONT, SMALLER_FONT, SETTINGS, null,
        SAVEAS, CLEAR, CLOSE,
    };

    private static final ACTION[] popUpFoldItems = new ACTION[]{
        COLLAPSE_FOLD, EXPAND_FOLD, null,
        COLLAPSE_ALL, EXPAND_ALL, null,
        COLLAPSE_TREE, EXPAND_TREE
    };

    private static final ACTION[] actionsToInstall = new ACTION[] {
            COPY, SELECT_ALL, FIND, FIND_NEXT, FIND_PREVIOUS, WRAP, LARGER_FONT,
            SMALLER_FONT, SAVEAS, CLOSE, COPY, NAVTOLINE, POSTMENU, CLEAR, FILTER,
            COLLAPSE_FOLD, EXPAND_FOLD, COLLAPSE_ALL, EXPAND_ALL, COLLAPSE_TREE,
            EXPAND_TREE
    };

    private final Map<ACTION, TabAction> actions = new EnumMap<>(ACTION.class);

    private void createActions() {
        KeyStrokeUtils.refreshActionCache();
        for (ACTION a : ACTION.values()) {
            TabAction action;
            switch(a) {
                case COPY:
                case PASTE:
                case WRAP:
                case SAVEAS:
                case CLOSE:
                case NEXT_ERROR:
                case PREV_ERROR:
                case SELECT_ALL:
                case FIND:
                case FIND_NEXT:
                case FIND_PREVIOUS:
                case FILTER:
                case LARGER_FONT:
                case SMALLER_FONT:
                case SETTINGS:
                case CLEAR:
                case COLLAPSE_FOLD:
                case EXPAND_FOLD:
                case COLLAPSE_ALL:
                case EXPAND_ALL:
                case COLLAPSE_TREE:
                case EXPAND_TREE:
                    action = new TabAction(a, "ACTION_"+a.name());
                    break;
                case NAVTOLINE:
                    action = new TabAction(a, "navToLine", //NOI18N
                                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
                    break;
                case POSTMENU:
                    action = new TabAction(a, "postMenu", //NOI18N
                                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK));
                    break;
                case NEXTTAB:
                    action = new TabAction(a, "NextViewAction", //NOI18N
                                (KeyStroke) null);
                    break;
                case PREVTAB:
                    action = new TabAction(a, "PreviousViewAction", //NOI18N
                                (KeyStroke) null);
                    break;
                default:
                    throw new IllegalStateException("Unhandled action "+a);
            }
            actions.put(a, action);
        }
    }

    /**
     * A stateless action which will find the owning OutputTab's controller and call
     * actionPerformed with its ID as an argument.
     */
    class TabAction extends AbstractAction {

        private ACTION action;

        /**
         * Create a ControllerAction with the specified action ID (constants defined in Controller),
         * using the specified bundle key.  Expects the following contents in the bundle:
         * <ul>
         * <li>A name for the action matching the passed key</li>
         * <li>An accelerator for the action matching [key].accel</li>
         * </ul>
         * @param id An action ID
         * @param bundleKey A key for the bundle associated with the Controller class
         * @see org.openide.util.Utilities#stringToKey
         */
        TabAction(ACTION action, String bundleKey) {
            if (bundleKey != null) {
                String name = NbBundle.getMessage(OutputTab.class, bundleKey);
                List<KeyStroke[]> accels = getAcceleratorsFor(action);
                this.action = action;
                putValue(NAME, name);
                if (accels != null && accels.size() > 0) {
                    List<KeyStroke> l = new ArrayList<KeyStroke>(accels.size());
                    for (KeyStroke[] ks : accels) {
                        if (ks.length == 1) { // ignore multi-key accelerators
                            l.add(ks[0]);
                        }
                    }
                    if (l.size() > 0) {
                        putValue(ACCELERATORS_KEY,
                                l.toArray(new KeyStroke[0]));
                        putValue(ACCELERATOR_KEY, l.get(0));
                    }
                }
            }
        }

        /**
         * Create a ControllerAction with the specified ID, name and keystroke.  Actions created
         * using this constructor will not be added to the popup menu of the component.
         *
         * @param id The ID
         * @param name A programmatic name for the item
         * @param stroke An accelerator keystroke
         */
        TabAction(ACTION action, String name, KeyStroke stroke) {
            this.action = action;
            putValue(NAME, name);
            putValue(ACCELERATOR_KEY, stroke);
        }

        void clearListeners() {
            PropertyChangeListener[] l = changeSupport.getPropertyChangeListeners();
            for (int i = 0; i < l.length; i++) {
                removePropertyChangeListener(l[i]);
            }
        }

        /**
         * Get a keyboard accelerator from the resource bundle, with special handling
         * for the mac keyboard layout.
         *
         * @param action Action to get accelerator for.
         * @return A keystroke
         */
        private List<KeyStroke[]> getAcceleratorsFor(ACTION action) {
            switch (action) {
                case COPY:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "copy-to-clipboard", null);                 //NOI18N
                case PASTE:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "paste-from-clipboard", null);              //NOI18N
                case SAVEAS:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.SAVE_AS_ACTION_ID, null);
                case CLOSE:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.CLOSE_ACTION_ID, null);
                case NEXT_ERROR:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "next-error", null);                        //NOI18N
                case PREV_ERROR:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "previous-error", null);                    //NOI18N
                case SELECT_ALL:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "select-all", null);                        //NOI18N
                case FIND:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "org.openide.actions.FindAction", null);    //NOI18N
                case FIND_NEXT:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "find-next", null);                         //NOI18N
                case FIND_PREVIOUS:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "find-previous", null);                     //NOI18N
                case FILTER:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.FILTER_ACTION_ID, null);
                case LARGER_FONT:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.LARGER_FONT_ACTION_ID, null);
                case SMALLER_FONT:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.SMALLER_FONT_ACTION_ID, null);
                case SETTINGS:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.OUTPUT_SETTINGS_ACTION_ID, null);
                case CLEAR:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.CLEAR_ACTION_ID, null);
                case WRAP:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            OutputKeymapManager.WRAP_ACTION_ID, null);
                case COLLAPSE_FOLD:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "collapse-fold", null);                     //NOI18N
                case EXPAND_FOLD:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "expand-fold", null);                       //NOI18N
                case COLLAPSE_ALL:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "collapse-all-folds", null);                //NOI18N
                case EXPAND_ALL:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "expand-all-folds", null);                  //NOI18N
                case COLLAPSE_TREE:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "collapse-fold-tree", null);                //NOI18N
                case EXPAND_TREE:
                    return KeyStrokeUtils.getKeyStrokesForAction(
                            "expand-fold-tree", null);                  //NOI18N
                default:
                    return null;
            }
        }

        public ACTION getAction() {
            return action;
        }

        @Override
        public boolean isEnabled() {
            if (getIO().isClosed()) {
                // Cached action for an already closed tab.
                // Try to find the appropriate action for a new active tab...
                JComponent selected = getIO().getIOContainer().getSelected();
                if (OutputTab.this != selected && selected instanceof OutputTab) {
                    OutputTab tab = (OutputTab) selected;
                    Action a = tab.action(action);
                    if (a != null) {
                        return a.isEnabled();
                    }
                }
                return false;
            }
            return super.isEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getIO().isClosed()) {
                // Cached action for an already closed tab.
                // Try to find the appropriate action for a new active tab...
                JComponent selected = getIO().getIOContainer().getSelected();
                if (OutputTab.this != selected && selected instanceof OutputTab) {
                    OutputTab tab = (OutputTab) selected;
                    Action a = tab.action(action);
                    if (a != null) {
                        a.actionPerformed(e);
                    }
                }
                return ;
            }
            switch (getAction()) {
                case COPY:
                    getOutputPane().copy();
                    break;
                case PASTE:
                    getOutputPane().paste();
                    break;
                case WRAP:
                    boolean wrapped = getOutputPane().isWrapped();
                    getOutputPane().setWrapped(!wrapped);
                    break;
                case SAVEAS:
                    saveAs();
                    break;
                case CLOSE:
                    io.getIOContainer().remove(OutputTab.this);
                    break;
                case NEXT_ERROR:
                    sendCaretToError(false);
                    break;
                case PREV_ERROR:
                    sendCaretToError(true);
                    break;
                case SELECT_ALL:
                    getOutputPane().selectAll();
                    break;
                case FIND:
                     {
                        String pattern = getFindDlgResult(getOutputPane().getSelectedText(), "LBL_Find_Title", "LBL_Find_What", "BTN_Find"); //NOI18N
                        if (pattern != null && find(false)) {
                            Action findNext = action(FIND_NEXT);
                            Action findPrev = action(FIND_PREVIOUS);
                            if (findNext != null) {
                                findNext.setEnabled(true);
                            }
                            if (findPrev != null) {
                                findPrev.setEnabled(true);
                            }
                            requestFocus();
                        }
                    }
                    break;
                case FIND_NEXT:
                    find(false);
                    break;
                case FIND_PREVIOUS:
                    find(true);
                    break;
                case NAVTOLINE:
                    openHyperlink();
                    break;
                case POSTMENU:
                    postPopupMenu(new Point(0, 0), OutputTab.this);
                    break;
                case CLEAR:
                    NbWriter writer = io.writer();
                    if (writer != null) {
                        try {
                            boolean vis = isInputVisible();
                            boolean closed = io.isStreamClosed();
                            writer.reset();
                            setInputVisible(vis);
                            io.setStreamClosed(closed);
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                    break;
                case SMALLER_FONT:
                    Controller.getDefault().changeFontSizeBy(-1, getOutputPane().isWrapped());
                    break;
                case LARGER_FONT:
                    Controller.getDefault().changeFontSizeBy(1, getOutputPane().isWrapped());
                    break;
                case SETTINGS:
                    OptionsDisplayer.getDefault().open(
                            "Advanced/OutputSettings");                 //NOI18N
                    break;
                case FILTER:
                    if (origPane != null) {
                        setFilter(null, false, false);
                    } else {
                        String pattern = getFindDlgResult(getOutputPane().getSelectedText(),
                                "LBL_Filter_Title", "LBL_Filter_What", "BTN_Filter"); //NOI18N
                        if (pattern != null) {
                            setFilter(pattern, FindDialogPanel.regExp(), FindDialogPanel.matchCase());
                        }
                    }
                    break;
                case COLLAPSE_FOLD:
                    getOutputPane().collapseFold();
                    break;
                case EXPAND_FOLD:
                    getOutputPane().expandFold();
                    break;
                case COLLAPSE_ALL:
                    getOutputPane().collapseAllFolds();
                    break;
                case EXPAND_ALL:
                    getOutputPane().expandAllFolds();
                    break;
                case COLLAPSE_TREE:
                    getOutputPane().collapseFoldTree();
                    break;
                case EXPAND_TREE:
                    getOutputPane().expandFoldTree();
                    break;
                default:
                    assert false;
            }
        }
    }

    @Override
    public void setInputVisible(boolean val) {
        super.setInputVisible(val);
        Action pasteAction = action(PASTE);
        if (pasteAction != null) {
            pasteAction.setEnabled(val);
        }
    }

    private boolean validRegExp(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException ex) {
            JOptionPane.showMessageDialog(getTopLevelAncestor(), 
                    NbBundle.getMessage(OutputTab.class, "FMT_Invalid_RegExp", pattern),
                    NbBundle.getMessage(OutputTab.class, "LBL_Invalid_RegExp"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String getFindDlgResult(String selection, String title, String label, String button) {
        String pattern = FindDialogPanel.getResult(selection, title, label, button); //NOI18N
        while (pattern != null && FindDialogPanel.regExp()) {
            if (validRegExp(pattern)) {
                break;
            }
            pattern = FindDialogPanel.getResult(pattern, title, label, button); //NOI18N
        }
        return pattern;
    }

    private static class ProxyAction implements Action {

        private Action orig;

        ProxyAction(Action original) {
            orig = original;
        }

        @Override
        public Object getValue(String key) {
            if (Action.SMALL_ICON.equals(key)) {
                return null;
            }
            return orig.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            orig.putValue(key, value);
        }

        @Override
        public void setEnabled(boolean b) {
            orig.setEnabled(b);
        }

        @Override
        public boolean isEnabled() {
            return orig.isEnabled();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            orig.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            orig.removePropertyChangeListener(listener);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            orig.actionPerformed(e);
        }
    }

    /**
     * #47166 - a disposed tab which has had its popup menu shown remains
     * referenced through PopupItems->JSeparator->PopupMenu->Invoker->OutputPane->OutputTab
     */
    private class PMListener implements PopupMenuListener {

        private List<TabAction> popupItems;
        private Object handle;

        PMListener(List<TabAction> popupItems, Object escHandle) {
            this.popupItems = popupItems;
            handle = escHandle;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            JPopupMenu popup = (JPopupMenu) e.getSource();
            popup.removeAll();
            popup.setInvoker(null);
            // hack
            KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            JComponent c = getOutputPane().getTextView();
            c.getInputMap().put(esc, handle);
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(esc, handle);

            //hack end
            popup.removePopupMenuListener(this);
            for (TabAction action : popupItems) {
                action.clearListeners();
            }
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenuWillBecomeInvisible(e);
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            //do nothing
        }
    }

    private class FilteredOutput {
        String pattern;
        OutWriter out;
        OutputPane pane;
        OutputDocument doc;
        int readCount;
        Pattern compPattern;
        boolean regExp;
        boolean matchCase;

        public FilteredOutput(String pattern, boolean regExp, boolean matchCase) {
            this.pattern = (regExp || matchCase) ? pattern : pattern.toLowerCase();
            this.regExp = regExp;
            this.matchCase = matchCase;
            out = new OutWriter();
            pane = new OutputPane(OutputTab.this);
            doc = new OutputDocument(out);
            pane.setDocument(doc);
        }

        boolean passFilter(String str) {
            if (regExp) {
                if (compPattern == null) {
                    compPattern = matchCase ? Pattern.compile(pattern) : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                }
                return compPattern.matcher(str).find();
            } else {
                return matchCase ? str.contains(pattern) : str.toLowerCase().contains(pattern);
            }
        }

        OutputPane getPane() {
            return pane;
        }

        OutWriter getWriter() {
            return out;
        }

        synchronized void readFrom(OutWriter orig) {
            AbstractLines lines = (AbstractLines) orig.getLines();
            // Last line is not guaranteed to be finished, see #219839.
            int lineCount = lines.getLineCount() - (orig.isClosed() ? 0 : 1);
            while (readCount < lineCount) {
                try {
                    int line = readCount++;
                    String str = lines.getLine(line);
                    if (!passFilter(str)) {
                        continue;
                    }
                    LineInfo info = lines.getExistingLineInfo(line);
                    out.print(str, info, lines.isImportantLine(line));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        void dispose() {
            out.dispose();
        }
    }

    private Lines getDocumentLines() {
        OutputDocument doc = getDocument();
        return doc == null ? null : doc.getLines();
    }
}
