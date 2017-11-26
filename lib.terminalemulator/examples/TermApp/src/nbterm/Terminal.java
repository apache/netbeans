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
package nbterm;

import org.netbeans.lib.terminalemulator.support.LineFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.richexecution.PtyProcess;

import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.support.DefaultFindState;
import org.netbeans.lib.terminalemulator.support.FindBar;
import org.netbeans.lib.terminalemulator.support.FindState;
import org.netbeans.lib.terminalemulator.support.TermOptions;
import org.netbeans.lib.terminalemulator.support.TermOptionsPanel;

class Terminal extends JFrame implements Runnable {

    private final TermExecutor executor;
    private final Program program;
    private final ActiveTerm term;
    private final FindState findState;
    private final FindBar findBar;
    private PtyProcess ptyProcess;

    private static final Preferences prefs =
        Preferences.userNodeForPackage(Terminal.class);
    private static final TermOptions termOptions = TermOptions.getDefault(prefs);

    private static final String BOOLEAN_STATE_ACTION_KEY = "boolean_state_action";
    private static final String BOOLEAN_STATE_ENABLED_KEY = "boolean_state_enabled";


    /**
     * Print something into the terminal in the bold font.
     * @param fmt
     * @param args
     */
    private void tprintf(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        term.setAttribute(1);   // bold
        term.appendText(msg, true);
        term.setAttribute(0);   // default
    }

    Terminal(TermExecutor executor, String termType, Program program, boolean processErrors, int rows, int cols) {
        super();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.executor = executor;
        this.program = program;

        term = new ActiveTerm();
        term.setCursorVisible(true);
        term.setRowsColumns(rows, cols);

        term.setHorizontallyScrollable(false);
        term.setEmulation(termType);
        if (!term.getEmulation().equals(termType))
            tprintf("nbterm: Terminal type '%s' not supported -- reverting to '%s'\n", termType, term.getEmulation());
        term.setBackground(Color.white);
        term.setHistorySize(4000);

        applyTermOptions(true);

	final Set<Action> actions = new HashSet<Action>();
	actions.add(newAction);
        actions.add(newAction);
        actions.add(copyAction);
        actions.add(pasteAction);
        actions.add(findAction);
        actions.add(wrapAction);
        actions.add(logSequencesAction);
        actions.add(dumpSequencesAction);
        actions.add(clearAction);
        actions.add(optionsAction);
	setupKeymap(actions);

        if (processErrors) {
            term.setActionListener(new ActiveTermListener() {
                public void action(ActiveRegion r, InputEvent e) {
                    if (r.isLink()) {
                        String cookie = (String) r.getUserObject();
                        int colonx = cookie.indexOf(':');
                        String file = cookie.substring(0, colonx);
                        String line = cookie.substring(colonx+1);
                        // tprintf("clicked '%s' -> '%s' '%s'\n", cookie, file, line);
                        int lineno = Integer.parseInt(line);

                        Main.showInEditor(file, lineno);
                    }
                }
            });
            LineFilter.pushInto(new ErrorProcessor(), term, 100);
        }

        this.findState = new DefaultFindState(term);
        this.findBar = new FindBar(new FindBar.Owner() {

            public void close(FindBar fb) {
                findBar.getState().setVisible(false);
                getContentPane().remove(findBar);
                validate();
            }
        });
        getContentPane().setLayout(new BorderLayout());
        term.getScreen().addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), term.getScreen());
                    postPopupMenu(p);
                }
            }
        });

        getContentPane().add(term, BorderLayout.CENTER);
        pack();
    }

    public Term term() {
        return term;
    }

    private void applyTermOptions(boolean initial) {
        Font font = term.getFont();
        /* OLD
        if (font != null) {
            Font newFont = new Font(font.getName(),
                                    font.getStyle(),
                                    termOptions.getFontSize());
            term.setFont(newFont);
        } else {
            Font newFont = new Font("monospaced",
                                    java.awt.Font.PLAIN,
                                    termOptions.getFontSize());
            term.setFont(newFont);
        }
        */
        term.setFixedFont(true);
        term.setFont(termOptions.getFont());

        term.setBackground(termOptions.getBackground());
        term.setForeground(termOptions.getForeground());
        term.setHighlightColor(termOptions.getSelectionBackground());
        term.setHistorySize(termOptions.getHistorySize());
        term.setTabSize(termOptions.getTabSize());

        term.setClickToType(termOptions.getClickToType());
        term.setScrollOnInput(termOptions.getScrollOnInput());
        term.setScrollOnOutput(termOptions.getScrollOnOutput());
        if (initial)
            term.setHorizontallyScrollable(!termOptions.getLineWrap());

        // If we change the font from smaller to bigger, the size
        // calculations go awry and the last few lines are forever hidden.
        setSize(getPreferredSize());
        validate();

    }

    private final class NewAction extends AbstractAction {

        public NewAction() {
            super("New");
            /* LATER
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_MASK);
            System.out.printf("Accelerator for Find: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
            */
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            Main.start();
        }
    }

    private final class ClearAction extends AbstractAction {

        public ClearAction() {
            super("Clear");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.clear();
        }
    }

    private final class OptionsAction extends AbstractAction {

        public OptionsAction() {
            super("Options");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;

            TermOptions clonedTermOptions = termOptions.makeCopy();
            TermOptionsPanel subPanel = new TermOptionsPanel();
            subPanel.setTermOptions(clonedTermOptions);

            JOptionPane optionPane = new JOptionPane(subPanel,
                                                     JOptionPane.PLAIN_MESSAGE,
                                                     JOptionPane.OK_CANCEL_OPTION
                                                     );
                JDialog dialog = optionPane.createDialog(Terminal.this,
                                                         "NBTerm Options");
                dialog.setVisible(true);      // WILL BLOCK!

                if (optionPane.getValue() == null)
                    return;     // was closed at the window level

                switch ((Integer) optionPane.getValue()) {
                    case JOptionPane.OK_OPTION:
                        System.out.printf("Dialog returned OK\n");
                        termOptions.assign(clonedTermOptions);
                        applyTermOptions(false);
                        termOptions.storeTo(prefs);
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        System.out.printf("Dialog returned CANCEL\n");
                        break;
                    case JOptionPane.CLOSED_OPTION:
                        System.out.printf("Dialog returned CLOSED\n");
                        break;
                    default:
                        System.out.printf("Dialog returned OTHER: %s\n",
                                          optionPane.getValue());
                        break;
                }
        }
    }

    private final class CopyAction extends AbstractAction {

        public CopyAction() {
            super("Copy");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK);
            System.out.printf("Accelerator for Copy: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.copyToClipboard();
        }
    }

    private final class PasteAction extends AbstractAction {

        public PasteAction() {
            super("Paste");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_MASK);
            System.out.printf("Accelerator for Paste: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.pasteFromClipboard();
        }
    }

    private final class FindAction extends AbstractAction {

        public FindAction() {
            super("Find");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_MASK);
            System.out.printf("Accelerator for Find: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;

            if (findState.isVisible())
                return;
            findState.setVisible(true);
            findBar.setState(findState);
            add(findBar, BorderLayout.SOUTH);
            validate();
        }
    }

    private final class WrapAction extends AbstractAction {

        public WrapAction() {
            super("Wrap text");
            // LATER KeyStroke accelerator = Utilities.stringToKey("A-R");
            putValue(BOOLEAN_STATE_ACTION_KEY, true);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            boolean hs = term.isHorizontallyScrollable();
            term.setHorizontallyScrollable(!hs);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(BOOLEAN_STATE_ENABLED_KEY)) {
                return !term.isHorizontallyScrollable();
            } else {
                return super.getValue(key);
            }
        }
    }

    private final class LogSequencesAction extends AbstractAction {

        public LogSequencesAction() {
            super("Log Sequences");
            putValue(BOOLEAN_STATE_ACTION_KEY, true);
        }

        public void actionPerformed(ActionEvent e) {
            if (term.isSequenceLogging())
                term.setSequenceLogging(false);
            else
                term.setSequenceLogging(true);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(BOOLEAN_STATE_ENABLED_KEY)) {
                return term.isSequenceLogging();
            } else {
                return super.getValue(key);
            }
        }
    }

    private final class DumpSequencesAction extends AbstractAction {

        public DumpSequencesAction() {
            super("Dump Sequences");
        }

        private void dump(String title, Set<String> set) {
            File file = new File(String.format("/tmp/term-sequences-%s", title));
            PrintStream ps;
            try {
                ps = new PrintStream(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Terminal.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }

            if (set != null) {
                for (String s : set)
                    ps.printf("%s\n", s);
            }

            ps.close();
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            dump("completed", term.getCompletedSequences());
            dump("unrecognized", term.getUnrecognizedSequences());
        }
    }

    private final class PrintStatsAction extends AbstractAction {

        public PrintStatsAction() {
            super("Print Stats");
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.printStats("Stats");
        }
    }

    private boolean isBooleanStateAction(Action a) {
        Boolean isBooleanStateAction = (Boolean) a.getValue(BOOLEAN_STATE_ACTION_KEY);
        return isBooleanStateAction != null && isBooleanStateAction;
    }

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

    private void printActionMap(ActionMap actionMap) {
	System.out.printf("-------- Original ActionMap -----------\n");
	Object[] allkeys = actionMap.allKeys();
	if (allkeys == null) {
	    System.out.printf("\t<empty>\n");
	} else {
	    for (Object k : allkeys) {
		Action a = actionMap.get(k);
		System.out.printf("\t%s %s\n", k, a);
	    }
	}
	System.out.printf("%s\n", actionMap);
    }

    private void setupKeymap(Set<Action> actions) {
	JComponent comp = term.getScreen();

	ActionMap actionMap = comp.getActionMap();
	ActionMap newActionMap = new ActionMap();
	newActionMap.setParent(actionMap);

	printActionMap(actionMap);

	InputMap inputMap = comp.getInputMap();
	InputMap newInputMap = new InputMap();
	newInputMap.setParent(inputMap);

	Set<KeyStroke> passKeystrokes = new HashSet<KeyStroke>();

	for (Action a : actions) {
	    String name = (String) a.getValue(Action.NAME);
            KeyStroke accelerator = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
	    System.out.printf("Registering %s for %s\n", accelerator, name);
	    if (accelerator == null)
		continue;
	    newInputMap.put(accelerator, name);
	    newActionMap.put(name, a);
	    passKeystrokes.add(accelerator);
	}

	comp.setActionMap(newActionMap);
	comp.setInputMap(JComponent.WHEN_FOCUSED, newInputMap);

        term.setKeyStrokeSet((HashSet) passKeystrokes);
    }

    private void postPopupMenu(Point p) {
        JPopupMenu menu = new JPopupMenu();
        addMenuItem(menu, newAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, copyAction);
        addMenuItem(menu, pasteAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, findAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, wrapAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, clearAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, optionsAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, logSequencesAction);
        addMenuItem(menu, dumpSequencesAction);
        addMenuItem(menu, printStatsAction);

        findAction.setEnabled(!findState.isVisible());

        menu.addPopupMenuListener(new PopupMenuListener() {

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        menu.show(term.getScreen(), p.x, p.y);
    }

    private final Action newAction = new NewAction();
    private final Action copyAction = new CopyAction();
    private final Action pasteAction = new PasteAction();
    private final Action findAction = new FindAction();
    private final Action wrapAction = new WrapAction();
    private final Action logSequencesAction = new LogSequencesAction();
    private final Action dumpSequencesAction = new DumpSequencesAction();
    private final Action printStatsAction = new PrintStatsAction();
    private final Action clearAction = new ClearAction();
    private final Action optionsAction = new OptionsAction();

    public void run() {
        try {
            run2();
        } catch (Exception x) {
            System.out.printf("Exception in thread\n%s\n", x);
        }
    }

    private void run2() {
        //
        // Start process
        //
        ptyProcess = executor.start(program, term);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    ptyProcess.hangup();
                } catch (IllegalStateException x) {
                }
            }
        });

        // Make main window visible
        setVisible(true);

        //
        // Wait for process to exit
        //
        ptyProcess.waitFor();

        dispose();
    }
}
