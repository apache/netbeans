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
package org.netbeans.modules.search;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import org.netbeans.api.search.SearchHistory;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.modules.search.ui.FormLayoutHelper;
import org.netbeans.modules.search.ui.UiUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Base class for pattern sandboxes.
 *
 * @author jhavlin
 */
public abstract class PatternSandbox extends JPanel
        implements HierarchyListener {

    private static final Logger LOG = Logger.getLogger(
            PatternSandbox.class.getName());
    private static final RequestProcessor RP
            = new RequestProcessor(PatternSandbox.class);

    protected JComboBox<String> cboxPattern;
    private JLabel lblPattern;
    protected JLabel lblHint;
    private JLabel lblOptions;
    private JPanel pnlOptions;
    protected JTextPane textPane;
    private JButton btnApply;
    private JButton btnCancel;
    private JScrollPane textScrollPane;
    protected Highlighter highlighter;
    protected Highlighter.HighlightPainter painter;
    protected BasicSearchCriteria searchCriteria;
    private Color cboxPatternForegroundStd = null;
    private static final Color errorColor = chooseErrorColor();

    /**
     * Initialize UI components.
     */
    protected void initComponents() {

        cboxPattern = new JComboBox<>();
        cboxPattern.setEditor(new BasicSearchForm.MultiLineComboBoxEditor(cboxPattern));
        cboxPattern.setEditable(true);
        cboxPattern.setRenderer(new ShorteningCellRenderer());
        lblPattern = new JLabel();
        lblPattern.setLabelFor(cboxPattern);
        lblHint = new JLabel();
        lblHint.setEnabled(false);
        lblOptions = new JLabel();
        textPane = new JTextPane();
        textScrollPane = new JScrollPane();
        textScrollPane.setViewportView(textPane);
        textScrollPane.setPreferredSize(new Dimension(400, 100));
        textScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        searchCriteria = new BasicSearchCriteria();
        initSpecificComponents();
        pnlOptions = createOptionsPanel();
        btnApply = new JButton();
        btnCancel = new JButton();
        cboxPatternForegroundStd = cboxPattern.getEditor().getEditorComponent().
                getForeground();

        initTextPaneContent();
        initHighlighter();

        setMnemonics();
        layoutComponents();
        initInteraction();
        this.addHierarchyListener(this);
        highlightMatchesLater();
    }

    /**
     * Add listeners to buttons.
     */
    private void initButtonsInteraction() {
        btnCancel.addActionListener((ActionEvent e) -> {
            closeDialog();
        });
        btnApply.addActionListener((ActionEvent e) -> {
            apply();
            closeDialog();
        });
    }

    /**
     * Add listeners to textual components.
     */
    private void initTextInputInteraction() {
        cboxPattern.getEditor().getEditorComponent().addKeyListener(
                new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (!e.isActionKey()) {
                            highlightMatchesLater();
                        }
                    }
                });
        cboxPattern.addActionListener((ActionEvent e) -> highlightMatchesLater());

        textPane.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                highlightMatchesLater();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                highlightMatchesLater();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                highlightMatchesLater();
            }
        });
    }

    /**
     * Set dialog layout and place components.
     */
    private void layoutComponents() {
        FormLayoutHelper mainHelper =
                new FormLayoutHelper(this, FormLayoutHelper.EAGER_COLUMN);
        mainHelper.setAllGaps(true);

        JPanel form = createFormPanel();
        JPanel buttonsPanel = createButtonsPanel();

        mainHelper.addRow(GroupLayout.DEFAULT_SIZE,
                200,
                Short.MAX_VALUE,
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, form, textScrollPane));

        mainHelper.addRow(
                GroupLayout.DEFAULT_SIZE,
                buttonsPanel.getPreferredSize().height,
                buttonsPanel.getPreferredSize().height,
                buttonsPanel);
    }

    /**
     * Create panel for buttons.
     */
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        FormLayoutHelper buttonsHelper = new FormLayoutHelper(buttonsPanel,
                FormLayoutHelper.EAGER_COLUMN,
                FormLayoutHelper.DEFAULT_COLUMN,
                FormLayoutHelper.DEFAULT_COLUMN);
        buttonsHelper.setInlineGaps(true);
        buttonsHelper.addRow(getExtraButton(), btnApply, btnCancel);
        return buttonsPanel;
    }

    /**
     * Create panel for form components.
     */
    private JPanel createFormPanel() {
        JPanel form = new JPanel();
        FormLayoutHelper formHelper = new FormLayoutHelper(form,
                FormLayoutHelper.DEFAULT_COLUMN,
                FormLayoutHelper.EAGER_COLUMN);
        formHelper.setInlineGaps(true);
        formHelper.addRow(
                GroupLayout.DEFAULT_SIZE,
                cboxPattern.getPreferredSize().height,
                Short.MAX_VALUE,
                lblPattern, cboxPattern);
        
        if (lblHint.getText() != null
                && !"".equals(lblHint.getText())) {                     //NOI18N
            formHelper.addRow(new JLabel(), lblHint);
        }

        formHelper.addRow(
                GroupLayout.DEFAULT_SIZE,
                0,
                pnlOptions.getPreferredSize().height,
                lblOptions, pnlOptions);
        return form;
    }

    /**
     * Set localized text and accessible keys.
     */
    private void setMnemonics() {
        Mnemonics.setLocalizedText(lblPattern, getPatternLabelText());
        Mnemonics.setLocalizedText(lblHint, getHintLabelText());
        Mnemonics.setLocalizedText(btnCancel,
                getText("PatternSandbox.btnCancel.text"));              //NOI18N
        Mnemonics.setLocalizedText(btnApply,
                getText("PatternSandbox.btnApply.text"));               //NOI18N
    }

    /**
     * Initialize highlighter and painter.
     */
    private void initHighlighter() {
        highlighter = new DefaultHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(
                chooseHighlightColor());
        textPane.setHighlighter(highlighter);
    }

    /**
     * Initialize listeners and add them to components.
     */
    private void initInteraction() {
        initTextInputInteraction();
        initButtonsInteraction();
    }

    /**
     * Set key events for the dialog - enter for applying and escape for
     * closing.
     */
    private void setKeys() {
        KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Object actionKey = "cancel"; // NOI18N
        getRootPane().getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                k, actionKey);

        Action cancelAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                closeDialog();
            }
        };

        getRootPane().getActionMap().put(actionKey, cancelAction);

        // pressing enter is equal to clicking Apply button
        getRootPane().setDefaultButton(btnApply);
    }

    /**
     * Schedule highlighting of matches.
     */
    protected void highlightMatchesLater() {
        EventQueue.invokeLater(this::highlightMatches);
    }

    /**
     * Highlight matches in the text pane.
     */
    @NbBundle.Messages({
        "MSG_PatternSansboxTimout=Pattern Matching took too long and was cancelled."})
    protected void highlightMatches() {

        highlighter.removeAllHighlights();

        Object value = cboxPattern.getEditor().getItem();
        if (value == null || value.toString().isEmpty()) {
            return;
        }
        String regex = value.toString();
        Pattern p;
        try {
            p = getPatternForHighlighting(regex);
            if (p == null) {
                throw new NullPointerException();
            }
            cboxPattern.getEditor().getEditorComponent().
                    setForeground(cboxPatternForegroundStd);
        } catch (Throwable e) {
            cboxPattern.getEditor().getEditorComponent().
                    setForeground(errorColor);
            return;
        }
        try {
            highlightIndividualMatches(p);
        } catch (TimeoutExeption e) {
            DialogDisplayer.getDefault().notifyLater(
                    new NotifyDescriptor.Message(
                    Bundle.MSG_PatternSansboxTimout(),
                    NotifyDescriptor.Message.ERROR_MESSAGE));
            LOG.log(Level.INFO, e.getMessage(), e);
        }
    }

    /**
     * Get localized text from the bundle by key.
     */
    private static String getText(String key) {
        return NbBundle.getMessage(PatternSandbox.class, key);
    }

    /**
     * Reverse items in a list. Create a new list, original list is untouched.
     */
    private static <T> List<T> reverse(List<T> list) {
        LinkedList<T> ll = new LinkedList<>();
        for (T t : list) {
            ll.add(0, t);
        }
        return ll;
    }

    /**
     * Close the dialog.
     */
    private void closeDialog() {
        saveTextPaneContent();
        Window w = (Window) SwingUtilities.getAncestorOfClass(
                Window.class, this);
        if (w != null) {
            w.dispose();
        }
    }

    /**
     * Get current pattern as a string. Never returns null.
     */
    private static String getSelectedItemAsString(JComboBox<String> cbox) {
        if (cbox.getSelectedItem() != null) {
            return cbox.getSelectedItem().toString();
        } else {
            return "";
        }
    }

    /**
     * Set keys when the panel is attached to a window.
     */
    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
            setKeys();
        }
    }

    /**
     * Get extra button (or a component) that will be displazed in the left
     * bottom corner of the dialog.
     */
    protected JComponent getExtraButton() {
        return new JLabel();
    }

    /**
     * Get label for Combo Box with pattern.
     */
    protected abstract String getPatternLabelText();

    /**
     * Get hint text for the pattern.
     */
    protected abstract String getHintLabelText();

    /**
     * Create panel containing controls for special options.
     */
    protected abstract JPanel createOptionsPanel();

    /**
     * Init components that are specific for concrete subtype. Should be called
     * when the object is constructed.
     */
    protected abstract void initSpecificComponents();

    /**
     * Apply pattern and options. Called when apply button is clicked or enter
     * key is pressed.
     */
    protected abstract void apply();

    /**
     * Returns a pattern that is used for highligting of the text pane. It
     * should correspond to current state of pattern combo box and associated
     * options.
     */
    protected abstract Pattern getPatternForHighlighting(String patternExpr);

    /**
     * Highlight matches in the text pane, using a valid patter object.
     */
    protected abstract void highlightIndividualMatches(Pattern p);

    /**
     * Load text pane content from find dialog history.
     */
    protected abstract void initTextPaneContent();

    /**
     * Save text pane content to find dialog history.
     */
    protected abstract void saveTextPaneContent();

    protected abstract String getTitle();

    /**
     * Sandox for find text pattern.
     *
     */
    static class TextPatternSandbox extends PatternSandbox
            implements ItemListener {

        private static final String LINE_SEP = "pattern.sandbox.line.separator"; //NOI18N
        private JCheckBox chkMatchCase;
        private String regexp;
        private boolean matchCase;
        private LineEnding lineEnding = null;

        public TextPatternSandbox(String regexp, boolean matchCase) {
            this.regexp = regexp;
            this.matchCase = matchCase;
            initComponents();
            searchCriteria.setMatchType(SearchPattern.MatchType.REGEXP);
        }

        @Override
        protected void initSpecificComponents() {

            chkMatchCase = new JCheckBox();
            chkMatchCase.addItemListener(this);
            setSpecificMnemonics();
            chkMatchCase.setSelected(matchCase);

            cboxPattern.setSelectedItem(regexp);
            SearchHistory history = SearchHistory.getDefault();
            for (SearchPattern sp : history.getSearchPatterns()) {
                cboxPattern.addItem(sp.getSearchExpression());
            }
        }

        private void setSpecificMnemonics() {
            Mnemonics.setLocalizedText(chkMatchCase,
                    getText("BasicSearchForm.chkCaseSensitive.text"));  //NOI18N
        }

        @Override
        protected String getPatternLabelText() {
            return getText(
                    "BasicSearchForm.lblTextToFind.text");              //NOI18N
        }

        @Override
        protected JPanel createOptionsPanel() {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            p.add(chkMatchCase);
            return p;
        }

        @Override
        protected final void apply() {
            onApply(getSelectedItemAsString(cboxPattern),
                    chkMatchCase.isSelected());
        }

        protected void onApply(String regexpExpr, boolean matchCase) {
        }

        @Override
        protected void initTextPaneContent() {
            String c = FindDialogMemory.getDefault().getTextSandboxContent();
            textPane.setText(c);
        }

        @Override
        protected void saveTextPaneContent() {
            String c = textPane.getText();
            FindDialogMemory.getDefault().setTextSandboxContent(c);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            ItemSelectable is = e.getItemSelectable();
            if (is == chkMatchCase) {
                searchCriteria.setCaseSensitive(chkMatchCase.isSelected());
            }
            highlightMatchesLater();
        }

        @Override
        protected Pattern getPatternForHighlighting(String patternExpr) {
            searchCriteria.onOk();
            searchCriteria.setTextPattern(patternExpr);
            return searchCriteria.getTextPattern();
        }

        @Override
        protected void highlightIndividualMatches(Pattern p) {
            String text = textPane.getText();
            Matcher m = p.matcher(new TimeLimitedCharSequence(text));
            int correction = 0; // count removed \r characters
            int lastCorrected = 0;
            while (m.find()) {
                try {
                    correction += countCRs(text, lastCorrected, m.start());
                    int start = m.start() - correction;
                    correction += countCRs(text, m.start(), m.end());
                    int end = m.end() - correction;
                    lastCorrected = m.end();
                    highlighter.addHighlight(start, end, painter);
                } catch (BadLocationException ex) {
                    Logger.getLogger(
                            this.getClass().getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
            textPane.repaint();
        }

        /**
         * Count carriage return characters (CR, \r), that are in the input
         * text, but are not in the JTextPane.
         */
        private int countCRs(String text, int from, int to) {
            if (LineEnding.CRLF != lineEnding) { //NOI18N
                return 0;
            }
            int count = 0;
            for (int i = from; i < to; i++) {
                if (text.charAt(i) == '\r') {
                    count++;
                }
            }
            return count;
        }

        @Override
        protected String getTitle() {
            return getText("TextPatternSandbox.title");                 //NOI18N
        }

        @Override
        protected String getHintLabelText() {
            return "";                                                  //NOI18N
        }

        @NbBundle.Messages({
            "LBL_LineEnding=Line endin&g: ",
            "LBL_LineEnding.tooltip=Line ending sequence that is used in the text pane",
            "LBL_LineEnding.accName=Line ending sequence"
        })
        @Override
        protected JComponent getExtraButton() {
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, Bundle.LBL_LineEnding());
            final JComboBox<LineEnding> cbox = new JComboBox<>(new LineEnding[]{});
            cbox.getAccessibleContext().setAccessibleName(Bundle.LBL_LineEnding_accName());
            cbox.setToolTipText(Bundle.LBL_LineEnding_tooltip());
            label.setLabelFor(cbox);
            panel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            panel.add(label);
            panel.add(cbox);
            loadLineEnding(cbox);
            return panel;
        }

        /**
         * Update line ending in the text pane and its highlighting.
         */
        private void updateLineEnding() {
            if (lineEnding != null) {
                textPane.getDocument().putProperty(
                        DefaultEditorKit.EndOfLineStringProperty,
                        lineEnding.getSequence());
                highlightMatchesLater();
            }
        }

        /**
         * Load last used value of lineEnding or use the default one, add items
         * to combo box with available line endings, and select the appropriate
         * item.
         */
        private void loadLineEnding(final JComboBox<LineEnding> comboBox) {
            RP.post(() -> {
                String typeStr = NbPreferences.forModule(
                        PatternSandbox.class).get(LINE_SEP, null);
                if (typeStr != null) {
                    try {
                        lineEnding = LineEnding.valueOf(typeStr);
                    } catch (IllegalArgumentException e) {
                        LOG.log(Level.FINE, "Unknown LEType {0}", typeStr); //NOI18N
                    }
                }
                if (lineEnding == null) {
                    lineEnding = Utilities.isWindows()
                            ? LineEnding.CRLF : LineEnding.LF;
                }
                EventQueue.invokeLater(() -> fillLineEndingComboBox(comboBox));
            });
        }

        /**
         * Fill line-ending combo box with all available values, select
         * appropriate last-used or default value, and initialize action
         * listener.
         */
        private void fillLineEndingComboBox(final JComboBox<LineEnding> comboBox) {
            comboBox.addItem(LineEnding.CRLF);
            comboBox.addItem(LineEnding.LF);
            comboBox.addItem(LineEnding.CR);
            comboBox.setSelectedItem(lineEnding);
            comboBox.addActionListener((ActionEvent e) -> {
                lineEnding = (LineEnding) comboBox.getSelectedItem();
                updateLineEnding();
                saveLineEnding();
            });
            updateLineEnding();
        }

        /**
         * Persist selected line ending.
         */
        private void saveLineEnding() {
            RP.post(() -> {
                if (lineEnding != null) {
                    NbPreferences.forModule(PatternSandbox.class).put(
                            LINE_SEP, lineEnding.name());
                }
            });
        }

        /**
         * LineEnding Type
         */
        @NbBundle.Messages({
            "LBL_Windows=\\r\\n: Windows",
            "LBL_Unix=\\n: Unix (Linux, Mac)",
            "LBL_MacOld=\\r: Old Mac",})
        private static enum LineEnding {

            CRLF("\r\n", Bundle.LBL_Windows()), //NOI18N
            LF("\n", Bundle.LBL_Unix()), //NOI18N
            CR("\r", Bundle.LBL_MacOld()); //NOI18N

            private final String sequence;
            private final String name;

            private LineEnding(String sequence, String name) {
                this.sequence = sequence;
                this.name = name;
            }

            @Override
            public String toString() {
                return name;
            }

            public String getSequence() {
                return sequence;
            }
        }
    }

    public static class PathPatternSandbox extends PatternSandbox {

        protected boolean pathRegexp;
        protected String value;

        public PathPatternSandbox(String value) {
            this.value = value;
            initComponents();
            searchCriteria.setFileNameRegexp(true);
        }

        @Override
        protected void initSpecificComponents() {

            cboxPattern.setSelectedItem(value);

            FindDialogMemory memory = FindDialogMemory.getDefault();
            for (String s : reverse(memory.getFileNamePatterns())) {
                cboxPattern.addItem(s);
            }
        }

        @Override
        protected String getPatternLabelText() {
            return getText(
                    "BasicSearchForm.lblFileNamePattern.text");         //NOI18N
        }

        @Override
        protected String getHintLabelText() {
            return "";                                                  //NOI18N
        }

        @Override
        protected JPanel createOptionsPanel() {
            return new JPanel();
        }

        @Override
        protected void apply() {
            onApply(getSelectedItemAsString(cboxPattern));
        }

        protected void onApply(String regexp) {
        }

        @Override
        protected void initTextPaneContent() {
            String c = FindDialogMemory.getDefault().getPathSandboxContent();
            textPane.setText(c);
        }

        @Override
        protected void saveTextPaneContent() {
            String c = textPane.getText();
            FindDialogMemory.getDefault().setPathSandboxContent(c);
        }

        @Override
        protected Pattern getPatternForHighlighting(String patternExpr) {
            searchCriteria.setFileNamePattern(patternExpr);
            searchCriteria.onOk();
            return searchCriteria.getFileNamePattern();
        }

        @Override
        protected void highlightIndividualMatches(Pattern p) {

            String text = textPane.getText().replace("\r\n", "\n");  //NOI18N

            Pattern sep = Pattern.compile("\n");                        //NOI18N
            Matcher m = sep.matcher(new TimeLimitedCharSequence(text));
            int lastStart = 0;
            while (m.find()) {
                matchLine(text, p, lastStart, m.start());
                lastStart = m.end();
            }
            matchLine(text, p, lastStart, text.length());
            textPane.repaint();
        }

        /**
         * Highlight matches in a line. Line is a substring of text withing
         * start and end positions. Matching is done differently for standard
         * and regexp patterns.
         */
        private void matchLine(String text, Pattern p, int start, int end) {

            boolean matches;
            if (searchCriteria.isFileNameRegexp()) {
                Matcher m = p.matcher(text.substring(start, end));
                matches = m.find();
            } else {
                int fileNameStart; // start and end for pattern matching
                int lastSlash = text.lastIndexOf("/", end);             //NOI18N
                if (lastSlash == -1 || lastSlash < start) {
                    lastSlash = text.lastIndexOf("\\", end);            //NOI18N
                }
                if (lastSlash == -1 || lastSlash < start) {
                    fileNameStart = start;
                } else {
                    fileNameStart = lastSlash + 1;
                }
                Matcher m = p.matcher(text.substring(fileNameStart, end));
                matches = m.matches();
            }
            if (matches) {
                try {
                    highlighter.addHighlight(start, end, painter);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        /**
         * Add Browse button.
         */
        @Override
        protected JComponent getExtraButton() {
            JPanel jp = new JPanel();
            jp.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            final JButton b = new JButton();
            jp.add(b);
            Mnemonics.setLocalizedText(b,
                    getText("PathPatternSandbox.browseButton.text"));   //NOI18N
            b.addActionListener((ActionEvent e) -> {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooser.setMultiSelectionEnabled(true);
                jFileChooser.showOpenDialog(b);
                if (jFileChooser.getSelectedFiles() == null) {
                    return;
                }
                for (File f : jFileChooser.getSelectedFiles()) {
                    textPane.setText(textPane.getText() + "\n" //NOI18N
                            + f.getAbsolutePath());
                }
            });
            return jp;
        }

        @Override
        protected String getTitle() {
            return getText("PathPatternSandbox.title");                 //NOI18N
        }
    }

    /**
     * Sandbox for file path pattern.
     *
     */
    static class PathPatternComposer extends PathPatternSandbox
            implements ItemListener {

        private JCheckBox chkFileRegexp;

        public PathPatternComposer(String value, boolean pathRegexp) {
            super(value);
            this.pathRegexp = pathRegexp;
            initComponents();
            if (pathRegexp) {
                searchCriteria.setFileNameRegexp(true);
            }
        }

        @Override
        protected void initSpecificComponents() {
            super.initSpecificComponents();
            chkFileRegexp = new JCheckBox();
            chkFileRegexp.addItemListener(this);
            chkFileRegexp.addItemListener(new RegexpModeListener());
            chkFileRegexp.setSelected(pathRegexp);

            Mnemonics.setLocalizedText(chkFileRegexp,
                    getText("BasicSearchForm.chkFileNameRegex.text"));  //NOI18N
        }

        @Override
        protected JPanel createOptionsPanel() {
            JPanel jp = new JPanel();
            jp.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
            jp.add(chkFileRegexp);
            return jp;
        }

        @Override
        protected final void apply() {
            onApply(getSelectedItemAsString(cboxPattern),
                    chkFileRegexp.isSelected());
        }

        protected void onApply(String pattern, boolean regexp) {
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            ItemSelectable is = e.getItemSelectable();
            if (is == chkFileRegexp) {
                searchCriteria.setFileNameRegexp(chkFileRegexp.isSelected());
                highlightMatchesLater();
            }
        }

        @Override
        protected String getHintLabelText() {
            return UiUtils.getFileNamePatternsExample(pathRegexp);
        }
    }

    /**
     * Listener that modifies visibility of simple-pattern hint text if regexp
     * mode changes.
     */
    protected class RegexpModeListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            lblHint.setText(UiUtils.getFileNamePatternsExample(
                    e.getStateChange() == ItemEvent.SELECTED));
        }
    }

    public static void openDialog(PatternSandbox sandbox, JComponent baseComponent) {

        JDialog jd = new JDialog(
                (JDialog) SwingUtilities.getAncestorOfClass(
                JDialog.class, baseComponent));

        jd.add(sandbox);
        jd.setTitle(sandbox.getTitle());
        jd.setModal(true);
        // try to reuse location of basecomponent
        // else 3 cascades of subdialogs does not fit to the screen
        jd.setLocation(baseComponent.getLocationOnScreen());
        jd.pack();
        sandbox.cboxPattern.requestFocusInWindow();
        jd.setVisible(true);
    }

    private static class TimeLimitedCharSequence implements CharSequence {

        private final CharSequence content;
        private final long dateCreated;
        int counter = 0;

        public TimeLimitedCharSequence(CharSequence content) {
            this(content, System.currentTimeMillis());
        }

        public TimeLimitedCharSequence(CharSequence content, long dateCreated) {
            this.content = content == null ? "" : content;              //NOI18N
            this.dateCreated = dateCreated;
        }

        @Override
        public int length() {
            return content.length();
        }

        @Override
        public char charAt(int index) {
            if (counter++ % 1024 == 0
                    && System.currentTimeMillis() - dateCreated > 1000) {
                throw new TimeoutExeption();
            }
            return content.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new TimeLimitedCharSequence(
                    content.subSequence(start, end), dateCreated);
        }
    }

    private static class TimeoutExeption extends RuntimeException {
    }

    private static class ShorteningCellRenderer extends DefaultListCellRenderer {

        private static final int MAX_LENGTH = 50;
        private static final String THREE_DOTS = "...";                 //NOI18N

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            if (value instanceof String && component instanceof JLabel
                    && value.toString().length() > MAX_LENGTH) {
                ((JLabel) component).setText(value.toString().substring(
                        0, MAX_LENGTH - THREE_DOTS.length()) + THREE_DOTS);
            }
            return component;
        }
    }

    private static Color chooseErrorColor() {
        return chooseColor("nb.search.sandbox.regexp.wrong", //NOI18N
                Color.RED);
    }

    private static Color chooseHighlightColor() {
        return chooseColor("nb.search.sandbox.highlight", //NOI18N
                Color.ORANGE);
    }

    private static Color chooseColor(String uiManagerKey, Color defaultColor) {
        Color colorFromManager = UIManager.getColor(uiManagerKey);
        return colorFromManager == null ? defaultColor : colorFromManager;
    }
}
