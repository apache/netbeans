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
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;
import org.openide.util.Lookup;

/** Operator for Output tab. It resides in output top component.
 * <p>
 * Usage:<br>
 * <pre>
 *      // find output tab with given name
 *      OutputTabOperator oto = new OutputTabOperator("compile-single");
 *      // wait for a message appears in output
 *      oto.waitText("my message");
 *      // get the text
 *      String wholeOutput = oto.getText();
 *      // close this output
 *      oto.close();
 * </pre>
 *
 * @author Jiri Skrivanek 
 * @see OutputOperator
 */
public class OutputTabOperator extends JComponentOperator {

    // operator of OutputPane component
    ComponentOperator outputPaneOperator;
    // actions used only in OutputTabOperator
    private static final Action findNextAction =
            new Action(null,
            Bundle.getString("org.netbeans.core.output2.Bundle",
            "ACTION_FIND_NEXT"),
            null,
            KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
    private static final Action wrapTextAction =
            new Action(null,
            Bundle.getString("org.netbeans.core.output2.Bundle",
            "ACTION_WRAP"),
            null,
            System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
    private static final ActionNoBlock saveAsAction =
            new ActionNoBlock(null,
            Bundle.getString("org.netbeans.core.output2.Bundle",
            "ACTION_SAVEAS"),
            null,
            System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    private static final Action nextErrorAction =
            new Action(null, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
    private static final Action previousErrorAction =
            new Action(null, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.SHIFT_MASK));
    private static final Action closeAction =
            new Action(null,
            Bundle.getString("org.netbeans.core.output2.Bundle",
            "ACTION_CLOSE"),
            null,
            System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK));
    private static final Action clearAction =
            new Action(null,
            Bundle.getString("org.netbeans.core.output2.Bundle",
            "ACTION_CLEAR"),
            null,
            System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
    private static final CopyAction copyAction = new CopyAction();
    private static final FindAction findAction = new FindAction();
    /**
     * Buttons in the tool bar to the left of the currently active output tab.
     * They apply to depend on the currently open tab, so they're placed
     * here instead of OutputOperator. This way it's also more useful.
     */
    private JButtonOperator btnReRun;
    private JButtonOperator btnReRunWithDifferentParameters;
    private JButtonOperator btnStop;
    private JButtonOperator btnAntSettings;

    /** Create new instance of OutputTabOperator from given component.
     * @param source JComponent source
     */
    public OutputTabOperator(JComponent source) {
        // used in OutputOperator
        super(source);
    }

    /** Waits for output tab with given name.
     * It is activated by default.
     * @param name name of output tab to look for
     */
    public OutputTabOperator(String name) {
        this(name, 0);
    }

    /** Waits for index-th output tab with given name.
     * It is activated by default.
     * @param name name of output tab to look for
     * @param index index of requested output tab with given name
     */
    public OutputTabOperator(String name, int index) {
        super((JComponent) new OutputOperator().waitSubComponent(new OutputTabSubchooser(name), index));
        makeComponentVisible();
    }

    /**
     * Returns operator for the Re-Run button in the tool bar on the left of the tab.
     * The button is inside the parent of the output tab, but applies (depends) to the
     * currently active tab, so the methods are here instead of
     * OutputOperator.
     *
     * @return JButtonOperator for Re-run button
     */
    public JButtonOperator btnReRun() {
        if (btnReRun == null) {
            btnReRun = new JButtonOperator((JButton) JButtonOperator.waitJComponent((Container) new OutputOperator().getSource(), "Re-run", true, true));
        }
        return btnReRun;
    }

    /**
     * Returns operator for the "Re-run with Different Parameters" button
     * in the tool bar on the left of the tab. The button is inside the parent
     * of the output tab, but applies (depends) to the currently active tab,
     * so the methods are here instead of OutputOperator.
     *
     * @return JButtonOperator for Re-run with Different Parameters button
     */
    public JButtonOperator btnReRunWithDifferentParameters() {
        if (btnReRunWithDifferentParameters == null) {
            btnReRunWithDifferentParameters = new JButtonOperator((JButton) JButtonOperator.waitJComponent((Container) new OutputOperator().getSource(), "Re-run with Different Parameters", true, true));
        }
        return btnReRunWithDifferentParameters;
    }

    /**
     * Returns operator for the Stop button in the tool bar on the left of the tab.
     * The button is inside the parent of the output tab, but applies (depends) to the
     * currently active tab, so the methods are here instead of
     * OutputOperator.
     *
     * @return JButtonOperator for Stop button
     */
    public JButtonOperator btnStop() {
        if (btnStop == null) {
            btnStop = new JButtonOperator((JButton) JButtonOperator.waitJComponent((Container) new OutputOperator().getSource(), "Stop", true, true));
        }
        return btnStop;
    }

    /**
     * Returns operator for the Ant Settings button in the tool bar on the left of the tab.
     * The button is inside the parent of the output tab, but applies (depends) to the
     * currently active tab, so the methods are here instead of
     * OutputOperator.
     *
     * @return JButtonOperator for Ant Settings button
     */
    public JButtonOperator btnAntSettings() {
        if (btnAntSettings == null) {
            btnAntSettings = new JButtonOperator((JButton) JButtonOperator.waitJComponent((Container) new OutputOperator().getSource(), "Ant Settings", true, true));
        }
        return btnAntSettings;
    }

    /** Activates this output tab. If this output tab is in tabbed pane, it is selected. If
     * it is only tab in the Output top component, the Output top component 
     * is activated.
     */
    @Override
    public final void makeComponentVisible() {
        if (getParent() instanceof JTabbedPane) {
            super.makeComponentVisible();
            // output tab is a tab of JTabbedPane
            new JTabbedPaneOperator((JTabbedPane) getParent()).setSelectedComponent(getSource());
        } else {
            // output tab is sub component of Output top component
            new OutputOperator().makeComponentVisible();
        }
    }

    /** Returns length of written text. It is a number of written characters.
     * @return length of already written text
     */
    public int getLength() {
        // ((OutputTab)getSource()).getDocument().getLength();
        return runMapping(new MapIntegerAction("getLength") {

            @Override
            public int map() {
                Document document = documentForTab(getSource());
                try {
                    if (getOutputDocumentClass().isInstance(document)) {
                        Method getLengthMethod = getOutputDocumentClass().getDeclaredMethod("getLength", (Class[]) null);
                        getLengthMethod.setAccessible(true);
                        return ((Integer) getLengthMethod.invoke(document, (Object[]) null)).intValue();
                    }
                } catch (Exception e) {
                    throw new JemmyException("getLength() by reflection failed.", e);
                }
                return 0;
            }
        });
    }

    /** Finds a line number by text.
     * @param lineText String line text
     * @return line number of specified text starting at 0; -1 if text not found
     */
    public int findLine(String lineText) {
        int lineCount = getLineCount();
        if (lineCount < 1) {
            // no line yet
            return -1;
        }
        for (int i = 0; i < lineCount; i++) {
            if (getComparator().equals(getLine(i), lineText)) {
                return i;
            }
        }
        return -1;
    }

    /** Returns text from this output tab.
     * @return text from this output tab.
     */
    public String getText() {
        final int length = getLength();
        return (String) runMapping(new MapAction("getText") {

            @Override
            public Object map() {
                Document document = documentForTab(getSource());
                try {
                    if (getOutputDocumentClass().isInstance(document)) {
                        Method getTextMethod = getOutputDocumentClass().getDeclaredMethod("getText", new Class[]{int.class, int.class});
                        getTextMethod.setAccessible(true);
                        return getTextMethod.invoke(document, new Object[]{Integer.valueOf(0), Integer.valueOf(length)}).toString();
                    }
                } catch (Exception e) {
                    throw new JemmyException("Getting text by reflection failed.", e);
                }
                return "";
            }
        });
    }

    /** Get text between <code>startLine</code> and <code>endLine</code> from this output tab.
     * Both <code>startLine</code> and <code>endLine</code> are included.
     * @param startLine first line to be included (starting at 0)
     * @param endLine last line to be included
     * @return text between <code>startLine</code> and <code>endLine</code> from this output tab
     */
    public String getText(int startLine, int endLine) {
        StringBuilder result = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            result.append(getLine(i));
            result.append('\n');
        }
        return result.toString();
    }

    /** Waits for text to be displayed in this output tab.
     * @param text text to wait for
     */
    public void waitText(final String text) {
        getOutput().printLine("Wait \"" + text + "\" text in component \n    : " + toStringSource());
        getOutput().printGolden("Wait \"" + text + "\" text");
        waitState(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return (findLine(text) > -1);
            }

            @Override
            public String getDescription() {
                return ("\"" + text + "\" text");
            }
        });
    }

    /** Returns count of filled lines of this output tab.
     * @return count of filled lines of this output tab.
     */
    public int getLineCount() {
        return ((Integer) runMapping(new MapAction("getLineCount") {

            @Override
            public Object map() {
                Document document = documentForTab(getSource());
                try {
                    if (getOutputDocumentClass().isInstance(document)) {
                        Method getElementCountMethod = getOutputDocumentClass().getDeclaredMethod("getElementCount", (Class[]) null);
                        getElementCountMethod.setAccessible(true);
                        return (Integer) getElementCountMethod.invoke(document, (Object[]) null);
                    }
                } catch (Exception e) {
                    throw new JemmyException("getElementCount() by reflection failed.", e);
                }
                return 0;
            }
        })).intValue();
    }

    private Class getOutputDocumentClass() throws ClassNotFoundException {
        ClassLoader scl = Lookup.getDefault().lookup(ClassLoader.class);
        return Class.forName("org.netbeans.core.output2.OutputDocument", true, scl);
    }

    /** Returns operator for OutputPane component.
     * All events should be dispatched to this component.
     * @return operator for OutputPane component
     */
    public ComponentOperator outputPaneOperator() {
        // first make component visible because tab must be visible to dispatch events
        makeComponentVisible();
        if (outputPaneOperator == null) {
            outputPaneOperator = ComponentOperator.createOperator(outputPaneForTab(getSource()));
            outputPaneOperator.copyEnvironment(this);
            // #217765 - wait for lazy loaded actions
            waitState(new ComponentChooser() {
                @Override
                public boolean checkComponent(Component comp) {
                    try {
                        Field actionsLoadedField = getSource().getClass().getDeclaredField("actionsLoaded");
                        actionsLoadedField.setAccessible(true);
                        return actionsLoadedField.getBoolean(getSource());
                    } catch (Exception ex) {
                        throw new JemmyException("Reflection failed: " + ex, ex);
                    }
                }

                @Override
                public String getDescription() {
                    return "Output tab actions loaded";
                }
            });
        }
        return outputPaneOperator;
    }

    /** Returns text from specified line.
     * @param line line number to get text from
     * @return text from the specified line (starting at 0)
     */
    public String getLine(final int line) {
        return (String) runMapping(new MapAction("getText") {

            @Override
            public Object map() {
                Document document = documentForTab(getSource());
                try {
                    if (getOutputDocumentClass().isInstance(document)) {
                        Class<?> clazz = getOutputDocumentClass();
                        Method getLineStartMethod = clazz.getDeclaredMethod("getLineStart", new Class[]{int.class});
                        getLineStartMethod.setAccessible(true);
                        Integer lineStart = (Integer) getLineStartMethod.invoke(document, new Object[]{Integer.valueOf(line)});
                        Method getLineEndMethod = clazz.getDeclaredMethod("getLineEnd", new Class[]{int.class});
                        getLineEndMethod.setAccessible(true);
                        Integer lineEnd = (Integer) getLineEndMethod.invoke(document, new Object[]{Integer.valueOf(line)});
                        if (lineStart.intValue() == lineEnd.intValue()) {
                            // line is empty
                            return "";
                        }
                        Method getTextMethod = clazz.getDeclaredMethod("getText", new Class[]{int.class, int.class});
                        getTextMethod.setAccessible(true);
                        return getTextMethod.invoke(document, new Object[]{lineStart, Integer.valueOf(lineEnd.intValue() - lineStart.intValue())}).toString();
                    }
                } catch (Exception e) {
                    throw new JemmyException("Getting text by reflection failed.", e);
                }
                return "";
            }
        });
    }

    private static Component outputPaneForTab(Component tab) {
        try {
            return (Component) tab.getClass().getMethod("getOutputPane").invoke(tab);
        } catch (Exception x) {
            throw new JemmyException("Reflection failed: " + x, x);
        }
    }

    private static Document documentForTab(Component tab) {
        Component pane = outputPaneForTab(tab);
        try {
            return (Document) pane.getClass().getMethod("getDocument").invoke(pane);
        } catch (Exception x) {
            throw new JemmyException("Reflection failed: " + x, x);
        }
    }

    /** SubChooser to determine OutputTab component
     * Used in findTopComponent method.
     */
    protected static final class OutputTabSubchooser implements ComponentChooser {

        /** Name of OutputTab to search for. */
        private String tabName = null;

        public OutputTabSubchooser() {
        }

        public OutputTabSubchooser(String tabName) {
            this.tabName = tabName;
        }

        @Override
        public boolean checkComponent(Component comp) {
            if (comp.getClass().getName().endsWith("OutputTab")) {  // NOI18N
                return Operator.getDefaultStringComparator().equals(comp.getName(), tabName);
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return "org.netbeans.core.output2.OutputTab" + // NOI18N
                    ((tabName != null) ? " with \"" + tabName + "\" name" : "");  // NOI18N
        }
    }

    /** Performs verification by accessing all sub-components */
    public void verify() {
        outputPaneOperator();
    }

    /****************************** Actions *****************************/
    /** Performs copy action. */
    public void copy() {
        copyAction.perform(outputPaneOperator());
    }

    /** Performs find action. */
    public void find() {
        findAction.perform(outputPaneOperator());
    }

    /** Performs find next action. */
    public void findNext() {
        findNextAction.perform(outputPaneOperator());
    }

    /** Performs next error action. */
    public void nextError() {
        nextErrorAction.perform(outputPaneOperator());
    }

    /** Performs next error action. */
    public void previousError() {
        previousErrorAction.perform(outputPaneOperator());
    }

    /** Performs wrap text action. */
    public void wrapText() {
        wrapTextAction.perform(outputPaneOperator());
    }

    /** Performs save as action. */
    public void saveAs() {
        saveAsAction.perform(outputPaneOperator());
    }

    /** Performs close action. */
    public void close() {
        closeAction.perform(outputPaneOperator());
    }

    /** Performs clear action. */
    public void clear() {
        clearAction.perform(outputPaneOperator());
    }

    /** Performs select all action. */
    public void selectAll() {
        new JEditorPaneOperator(this).selectAll();
    }
}
