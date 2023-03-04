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

package org.netbeans.modules.bugtracking.ui.issue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Stola
 */
class FindSupport {
    private static final String FIND_NEXT_ACTION = "find-next"; // NOI18N
    private static final String FIND_PREVIOUS_ACTION = "find-previous"; // NOI18N
    private TopComponent tc;
    private FindBar bar;
    // Highlighters
    private  Highlighter.HighlightPainter highlighterAll;
    private  Highlighter.HighlightPainter highlighterCurrent;
    // Current search details
    private Pattern pattern;
    private JTextComponent currentComp;
    private int currentStart;
    private int currentEnd;

    private FindSupport(TopComponent tc) {
        this.tc = tc;
        bar = new FindBar(this);
        ActionMap actionMap = tc.getActionMap();
        CallbackSystemAction a = SystemAction.get(org.openide.actions.FindAction.class);
        actionMap.put(a.getActionMapKey(), new FindAction(true));
        actionMap.put(FIND_NEXT_ACTION, new FindAction(true));
        actionMap.put(FIND_PREVIOUS_ACTION, new FindAction(false));
        // Hack ensuring the same shortcuts as editor
        JEditorPane pane = new JEditorPane();
        for (Action action : pane.getEditorKitForContentType("text/x-java").getActions()) { // NOI18N
            Object name = action.getValue(Action.NAME);
            if (FIND_NEXT_ACTION.equals(name) || FIND_PREVIOUS_ACTION.equals(name)) {
                reuseShortcut(action);
            }
        }
        // PENDING the colors below should not be hardcoded
        highlighterAll = new DefaultHighlighter.DefaultHighlightPainter(new Color(255,180,66));
        highlighterCurrent = new DefaultHighlighter.DefaultHighlightPainter(new Color(176,197,227));
        pattern = Pattern.compile("$^"); // NOI18N
    }

    private void reuseShortcut(Action action) {
        Object key = action.getValue(Action.ACCELERATOR_KEY);
        if (key instanceof KeyStroke) {
            InputMap inputMap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.put((KeyStroke)key, action.getValue(Action.NAME));
        }
    }

    public static FindSupport create(TopComponent tc) {
        return new FindSupport(tc);
    }

    public JComponent getFindBar() {
        return bar;
    }

    void reset() {
        highlight(tc, true);
        currentComp = null;
    }

    void updatePattern() {
        reset();
        String p = bar.getPattern();
        if (!bar.getRegularExpression()) {
            p = Pattern.quote(p);
            if (bar.getWholeWords()) {
                p="\\b"+p+"\\b"; // NOI18N
            }
        }
        int flags = Pattern.MULTILINE;
        if (!bar.getMatchCase()) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        try {
            pattern = Pattern.compile(p, flags);
        } catch (PatternSyntaxException psex) {
            String message = NbBundle.getMessage(FindSupport.class, "FindBar.invalidExpression"); // NOI18N
            StatusDisplayer.getDefault().setStatusText(message, StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        }
        findNext();
        if (bar.getHighlightResults()) {
            highlight(tc, false);
        }
    }

    void findNext() {
        boolean found = false;
        if (currentComp != null && currentComp.isVisible()) {
            highlight(tc, true);
            found = findNext(tc);
        }
        if (!found) {
            currentComp = null;
            findNext(tc);
        }
        if (currentComp != null && currentComp.isVisible() && bar.getHighlightResults()) {
            highlight(tc, false);
        }
    }

    private boolean findNext(Component comp) {
        if (comp == bar) {
            return false;
        }
        if (comp instanceof JTextPane) {
            if (currentComp == null || currentComp == comp) {
                if(!comp.isVisible()) {
                    return false;
                }
                JTextPane tcomp = (JTextPane)comp;
                String txt = tcomp.getText();
                Matcher matcher = pattern.matcher(txt);
                int idx = (currentComp==null) ? 0 : currentEnd;
                if (matcher.find(idx)) {
                    currentComp = tcomp;
                    currentStart = matcher.start();
                    currentEnd = matcher.end();
                    if (currentStart == currentEnd) {
                        currentComp = null;
                    } else {
                        try {
                            Highlighter highlighter = tcomp.getHighlighter();
                            highlighter.addHighlight(currentStart, currentEnd, highlighterCurrent);
                            scrollToCurrent();
                        } catch (BadLocationException blex) {
                            BugtrackingManager.LOG.log(Level.INFO, blex.getMessage(), blex);
                        }
                        return true;
                    }
                } else {
                    currentComp = null;
                }
            }
        } else if (comp instanceof Container) {
            Container cont = (Container)comp;
            for (Component subComp : cont.getComponents()) {
                if (findNext(subComp)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void scrollToCurrent() {
        try {
            Rectangle r1 = currentComp.modelToView(currentStart);
            Rectangle r2 = currentComp.modelToView(currentStart);
            Rectangle r = r1.union(r2);
            currentComp.scrollRectToVisible(r);
        } catch (BadLocationException blex) {
            BugtrackingManager.LOG.log(Level.INFO, blex.getMessage(), blex);
        }
    }

    void findPrevious() {
        boolean found = false;
        if (currentComp != null) {
            highlight(tc, true);
            found = findPrevious(tc);
        }
        if (!found) {
            currentComp = null;
            findPrevious(tc);
        }
        if (currentComp != null && bar.getHighlightResults()) {
            highlight(tc, false);
        }
    }

    private boolean findPrevious(Component comp) {
        if (comp == bar) {
            return false;
        }
        if (comp instanceof JTextPane) {
            if (currentComp == null || currentComp == comp) {
                JTextPane tcomp = (JTextPane)comp;
                String txt = tcomp.getText();
                Matcher matcher = pattern.matcher(txt);
                Highlighter highlighter = tcomp.getHighlighter();
                int lastStart = -1;
                int lastEnd = -1;
                while (true) {
                    boolean found = matcher.find((lastEnd==-1) ? 0 : lastEnd);
                    if (found && ((currentComp == null) || (matcher.end()<=currentStart))) {
                        lastStart = matcher.start();
                        lastEnd = matcher.end();
                        if (lastStart == lastEnd) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (lastEnd == -1 || lastStart == lastEnd) {
                    currentComp = null;
                } else {
                    currentComp = tcomp;
                    currentStart = lastStart;
                    currentEnd = lastEnd;
                    try {
                        highlighter.addHighlight(currentStart, currentEnd, highlighterCurrent);
                        scrollToCurrent();
                    } catch (BadLocationException blex) {
                        BugtrackingManager.LOG.log(Level.INFO, blex.getMessage(), blex);
                    }
                    return true;
                }
            }
        } else if (comp instanceof Container) {
            Container cont = (Container)comp;
            Component[] comps = cont.getComponents();
            for (int i=comps.length-1; i>=0; i--) {
                if (findPrevious(comps[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    void cancel() {
        if (currentComp != null) {
            currentComp.requestFocus();
        }
        reset();
        bar.setVisible(false);
    }

    void switchHighlight(boolean on) {
        if (!on) {
            highlight(tc, true);
        }
        if (currentComp != null) {
            try {
                currentComp.getHighlighter().addHighlight(currentStart, currentEnd, highlighterCurrent);
            } catch (BadLocationException blex) {
                BugtrackingManager.LOG.log(Level.INFO, blex.getMessage(), blex);
            }
        }
        if (on) {
            highlight(tc, false);
        }
    }

    private void highlight(Component comp, boolean cancel) {
        if (comp == bar) {
            return;
        }
        if (comp instanceof JTextPane) {
            JTextPane tcomp = (JTextPane)comp;
            if(!tcomp.isVisible()) {
                return;
            }
            String txt = tcomp.getText();
            Matcher matcher = pattern.matcher(txt);
            Highlighter highlighter = tcomp.getHighlighter();
            if (cancel) {
                highlighter.removeAllHighlights();
            } else {
                int idx = 0;
                while (matcher.find(idx)) {
                    int start = matcher.start();
                    int end = matcher.end();
                    if (start == end) {
                        break;
                    }
                    try {
                        highlighter.addHighlight(start, end, highlighterAll);
                    } catch (BadLocationException blex) {
                        BugtrackingManager.LOG.log(Level.INFO, blex.getMessage(), blex);
                    }
                    idx = matcher.end();
                }
            }
        } else if (comp instanceof Container) {
            Container cont = (Container)comp;
            for (Component subComp : cont.getComponents()) {
                highlight(subComp, cancel);
            }
        }
    }

    private class FindAction extends AbstractAction {
        private boolean forward;

        FindAction(boolean forward) {
            this.forward = forward;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (bar.isVisible()) {
                if (forward) {
                    findNext();
                } else {
                    findPrevious();
                }
            } else {
                bar.setVisible(true);
                updatePattern();
            }
            bar.requestFocusInWindow();
        }

    }

}
