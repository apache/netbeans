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

package org.netbeans.modules.quicksearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.quicksearch.ProviderModel.Category;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Quick search toolbar component
 * @author  Jan Becicka
 */
public abstract class AbstractQuickSearchComboBar extends javax.swing.JPanel {

    QuickSearchPopup displayer = new QuickSearchPopup(this);
    WeakReference<TopComponent> caller;

    Color origForeground;
    protected final KeyStroke keyStroke;

    protected JTextComponent command;

    public AbstractQuickSearchComboBar(KeyStroke ks) {
        keyStroke = ks;

        initComponents();

        setShowHint(true);

        command.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent arg0) {
                textChanged();
            }

            public void removeUpdate(DocumentEvent arg0) {
                textChanged();
            }

            public void changedUpdate(DocumentEvent arg0) {
                textChanged();
            }

            private void textChanged () {
                if (command.isFocusOwner()) {
                    displayer.maybeEvaluate(command.getText());
                }
            }

        });
        if (command.getDocument() instanceof AbstractDocument) {
            AbstractDocument ad = (AbstractDocument) command.getDocument();
            ad.setDocumentFilter(new InvalidSearchTextDocumentFilter());
        }
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    protected abstract JTextComponent createCommandField();

    protected abstract JComponent getInnerComponent();

    private void initComponents() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setName("Form"); // NOI18N
        setOpaque(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        command = createCommandField();
        String shortcutText = "";                                       //NOI18N
        if (!SearchResultRender.getKeyStrokeAsText(keyStroke).isEmpty()) {
            shortcutText = "(" + SearchResultRender.getKeyStrokeAsText( //NOI18N
                    keyStroke) + ")";                                   //NOI18N
        }
        command.setToolTipText(org.openide.util.NbBundle.getMessage(AbstractQuickSearchComboBar.class, "AbstractQuickSearchComboBar.command.toolTipText", new Object[] {shortcutText})); // NOI18N
        command.setName("command"); // NOI18N
        command.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                commandFocusGained(evt);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                commandFocusLost(evt);
            }
        });
        command.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                commandKeyPressed(evt);
            }
        });
        command.addMouseListener(new MouseAdapter() {
            public @Override void mouseClicked(MouseEvent e) {
                displayer.explicitlyInvoked();
            }
        });
    }

    private void formFocusLost(java.awt.event.FocusEvent evt) {
        displayer.setVisible(false);
    }

    private void commandKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
            displayer.selectNext();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            displayer.selectPrev();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            invokeSelectedItem();
        } else if ((evt.getKeyCode()) == KeyEvent.VK_ESCAPE) {
            returnFocus(true);
            displayer.clearModel();
        } else if (evt.getKeyCode() == KeyEvent.VK_F10 &&
                evt.isShiftDown()) {
            evt.consume();
            maybeShowPopup(null);
        }
    }

    /** Actually invokes action selected in the results list */
    public void invokeSelectedItem () {
        JList list = displayer.getList();
        ResultsModel.ItemResult ir = (ItemResult) list.getSelectedValue();

        // special handling of invocation of "more results item" (three dots)
        if (ir != null) {
            Runnable action = ir.getAction();
            if (action instanceof CategoryResult) {
                CategoryResult cr = (CategoryResult)action;
                evaluate(cr.getCategory());
                return;
            }
        }

        // #137259: invoke only some results were found
        if (list.getModel().getSize() > 0) {
            returnFocus(false);
            // #137342: run action later to let focus indeed be transferred
            // by previous returnFocus() call
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        displayer.invoke();
                    }
            });
        }
    }

    private void returnFocus (boolean force) {
        displayer.setVisible(false);
        if (caller != null) {
            TopComponent tc = caller.get();
            if (tc != null) {
                tc.requestActive();
                tc.requestFocus();
                return;
            }
        }
        if (force) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        }
    }


    private void commandFocusLost(java.awt.event.FocusEvent evt) {
        displayer.setVisible(false);
        setShowHint(true);
    }

    private void commandFocusGained(java.awt.event.FocusEvent evt) {
        caller = new WeakReference<TopComponent>(TopComponent.getRegistry().getActivated());
        setShowHint(false);
        CommandEvaluator.dropTemporaryCat();
    }

    protected void maybeShowPopup (MouseEvent evt) {
        if (evt != null && !SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }

        JPopupMenu pm = new JPopupMenu();
        final Set<ProviderModel.Category> evalCats =
                new LinkedHashSet<ProviderModel.Category>();
        evalCats.addAll(CommandEvaluator.getEvalCats());
        JMenuItem allCats = new AllMenuItem(evalCats);
        pm.add(allCats);

        for (ProviderModel.Category cat : ProviderModel.getInstance().getCategories()) {
            if (!CommandEvaluator.RECENT.equals(cat.getName())) {
                JCheckBoxMenuItem item = new CategoryCheckBoxMenuItem(cat,
                        evalCats);
                pm.add(item);
            }
        }

        pm.show(getInnerComponent(), 0, getInnerComponent().getHeight() - 1);
    }

    private void updateCats(Set<Category> evalCats) {
        CommandEvaluator.setEvalCats(evalCats);
        CommandEvaluator.dropTemporaryCat();
        // refresh hint
        setShowHint(!command.isFocusOwner());
    }

    private void updateCheckBoxes(Container container, Set<Category> evalCats) {
        Container parent = container.getParent();
        for (Component c : parent.getComponents()) {
            if (c instanceof CategoryCheckBoxMenuItem) {
                CategoryCheckBoxMenuItem ci = (CategoryCheckBoxMenuItem) c;
                ci.setSelected(evalCats.contains(ci.category));
                ci.setTooltipText();
            }
        }
    }

    /**
     * Runs evaluation. Possibly temporarily narrow to a specified category.
     *
     * @param tempCategory Temporary category. If set, only the specified
     * category will be evaluated. If null, all enabled categories will be
     * evaluated.
     */
    public void evaluate(Category tempCategory) {
        if (tempCategory != null) {
            CommandEvaluator.setTemporaryCat(tempCategory);
        } else {
            CommandEvaluator.dropTemporaryCat();
        }
        displayer.maybeEvaluate(command.getText());
    }

    public void setNoResults (boolean areNoResults) {
        // no op when called too soon
        if (command == null || origForeground == null) {
            return;
        }
        // don't alter color if showing hint already
        if (command.getForeground().equals(command.getDisabledTextColor())) {
            return;
        }
        command.setForeground(areNoResults ? Color.RED : origForeground);
    }

    private void setShowHint (boolean showHint) {
        // remember orig color on first invocation
        if (origForeground == null) {
            origForeground = command.getForeground();
        }
        if (showHint) {
            command.setForeground(command.getDisabledTextColor());
            Set<Category> evalCats = CommandEvaluator.getEvalCats();
            if (evalCats.size() < 3
                    && !CommandEvaluator.isTemporaryCatSpecified()) {
                Category bestFound = null;
                for (Category c : evalCats) {
                    if (bestFound == null || CommandEvaluator.RECENT.equals(
                            bestFound.getName())) {
                        bestFound = c;
                    }
                }
                command.setText(getHintText(bestFound));
            } else {
                command.setText(getHintText(null));
            }
        } else {
            command.setForeground(origForeground);
            command.setText("");
        }
    }

    private String getHintText (Category cat) {
        StringBuilder sb = new StringBuilder();
        if (cat != null) {
            sb.append(NbBundle.getMessage(AbstractQuickSearchComboBar.class,
                    "MSG_DiscoverabilityHint2", cat.getDisplayName())); //NOI18N
        } else {
            sb.append(NbBundle.getMessage(AbstractQuickSearchComboBar.class, "MSG_DiscoverabilityHint")); //NOI18N
        }
        String keyStrokeAsText = SearchResultRender.getKeyStrokeAsText(keyStroke);
        if (!keyStrokeAsText.isEmpty()) {
            sb.append(" (");                                            //NOI18N
            sb.append(keyStrokeAsText);
            sb.append(")");                                             //NOI18N
        }

        return sb.toString();
    }


    @Override
    public void requestFocus() {
        super.requestFocus();
        command.requestFocus();
    }

    public JTextComponent getCommand() {
        return command;
    }

    public int getBottomLineY () {
        return getInnerComponent().getY() + getInnerComponent().getHeight();
    }

    static Color getComboBorderColor () {
        Color shadow = UIManager.getColor(
                Utilities.isWindows() ? "Nb.ScrollPane.Border.color" : "TextField.shadow");
        return shadow != null ? shadow : getPopupBorderColor();
    }

    static Color getPopupBorderColor () {
        Color shadow = UIManager.getColor("controlShadow");
        return shadow != null ? shadow : Color.GRAY;
    }

    static Color getTextBackground () {
        Color textB = UIManager.getColor("TextPane.background");
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            textB = UIManager.getColor("NbExplorerView.background"); //NOI18N
        return textB != null ? textB : Color.WHITE;
    }

    static Color getResultBackground () {
        return getTextBackground();
    }

    static Color getCategoryTextColor () {
        Color shadow = UIManager.getColor("textInactiveText");
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) )
            shadow = UIManager.getColor("Table.foreground");
        return shadow != null ? shadow : Color.DARK_GRAY;
    }

    protected int computePrefWidth () {
        FontMetrics fm = command.getFontMetrics(command.getFont());
        ProviderModel pModel = ProviderModel.getInstance();
        int maxWidth = 0;
        for (Category cat : pModel.getCategories()) {
            // skip recent category
            if (CommandEvaluator.RECENT.equals(cat.getName())) {
                continue;
            }
            maxWidth = Math.max(maxWidth, fm.stringWidth(getHintText(cat)));
        }
        // don't allow width grow too much
        return Math.min(350, maxWidth);
    }

    private static String dispalyNameFor(Category category) {
            if (null != category.getCommandPrefix()) {
                return NbBundle.getMessage(AbstractQuickSearchComboBar.class,
                        "LBL_CategoryAndCommandPrefix", //NOI18N
                        category.getDisplayName(),
                        category.getCommandPrefix());
            } else {
                return category.getDisplayName();
            }
        }

    /**
     * Document filter that checks invalid input. See bug 217364.
     */
    static class InvalidSearchTextDocumentFilter extends DocumentFilter {

        private static final int SEARCH_TEXT_LENGTH_LIMIT = 256;
        private static final int SEARCH_NUM_WORDS_LIMIT = 20;

        @Override
        public void insertString(FilterBypass fb, int offset,
                String string, AttributeSet attr)
                throws BadLocationException {
            String normalized = normalizeWhiteSpaces(string);
            if (isLengthInLimit(normalized, fb, 0)) {
                super.insertString(fb, offset, normalized, attr);
            } else {
                warnAboutInvalidText();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                String text, AttributeSet attrs)
                throws BadLocationException {
            String normalized = text == null
                    ? null : normalizeWhiteSpaces(text); //NOI18N
            if (normalized == null || isLengthInLimit(normalized, fb, length)) {
                super.replace(fb, offset, length, normalized, attrs);
            } else {
                warnAboutInvalidText();
            }
        }

        /**
         * Check whether length limit or nubmer of words is exceeded when the
         * new string is inserted or replaced.
         *
         * @param newContent String to be inserted
         * @param fb Current FilterBypass
         * @param charsToBeRemoved Number of characters going to be replaced by
         * new string.
         */
        private boolean isLengthInLimit(String newContent, FilterBypass fb,
                int charsToBeRemoved) {
            return isLengthInLimit(newContent, SEARCH_TEXT_LENGTH_LIMIT
                    - fb.getDocument().getLength() + charsToBeRemoved);
        }

        /**
         * Check whether length limit or nubmer of words is exceeded when the
         * new string is inserted or replaced.
         *
         * @param newContent String to be inserted.
         * @param remainingChars Limit for characters to be inserted.
         */
        boolean isLengthInLimit(String newContent, int remainingChars) {
            return (newContent.length() <= remainingChars)
                    && (newContent.split(" ").length //NOI18N
                    <= SEARCH_NUM_WORDS_LIMIT);
        }

        /**
         * Replace all line breaks and multiple spaces with single space.
         */
        String normalizeWhiteSpaces(String s) {
            String replaced =  s.replaceAll("\\s+", " ");               //NOI18N
            return (replaced.length() > 1) ? replaced.trim() : replaced;
        }

        /**
         * Warn that search text would be invalid.
         */
        @NbBundle.Messages({
            "MSG_INVALID_SEARCH_TEST=Search text is too long."
        })
        private void warnAboutInvalidText() {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    Bundle.MSG_INVALID_SEARCH_TEST(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    /**
     * Show and select menu at a given path. Used to restore a menu after click.
     */
    private void showMenuPath(MenuElement[] selectedPath) {
        if (selectedPath != null && selectedPath.length > 1) {
            if (selectedPath[0] instanceof JPopupMenu) {
                ((JPopupMenu) selectedPath[0]).setVisible(true);
                MenuSelectionManager.defaultManager().setSelectedPath(
                        selectedPath);
            }
        }
    }

    /**
     * Menu item representing a single category.
     */
    private class CategoryCheckBoxMenuItem extends JCheckBoxMenuItem
            implements ActionListener {

        private MenuElement[] selectedPath = null;
        private Category category;
        private final Set<Category> evalCats;

        public CategoryCheckBoxMenuItem(final Category category,
                final Set<Category> evalCats) {
            super(dispalyNameFor(category), evalCats.contains(category));
            this.category = category;
            this.evalCats = evalCats;
            setTooltipText();
            getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (isShowing() && model.isArmed()) {
                        selectedPath = MenuSelectionManager.defaultManager()
                                .getSelectedPath();
                    }
                }
            });
            addActionListener(this);
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    mouseClickedOnItem(e);
                }
            });
        }

        @Override
        public void doClick(int pressTime) {
            super.doClick(pressTime);
            setTooltipText();
            showMenuPath(selectedPath);
        }

        private void mouseClickedOnItem(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                e.consume();
                if (isSelected()) {
                    Iterator<Category> iterator = evalCats.iterator();
                    while (iterator.hasNext()) {
                        Category c = iterator.next();
                        if (!CommandEvaluator.RECENT.equals(c.getName())) {
                            iterator.remove();
                        }
                    }
                    evalCats.add(category);
                } else {
                    evalCats.addAll(
                            ProviderModel.getInstance().getCategories());
                    evalCats.remove(category);
                }
                updateCheckBoxes(CategoryCheckBoxMenuItem.this, evalCats);
                updateCats(evalCats);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (this.isSelected()) {
                evalCats.add(category);
            } else {
                evalCats.remove(category);
            }
            updateCats(evalCats);
        }

        private void setTooltipText() throws MissingResourceException {

            boolean selected = evalCats.contains(category);
            String bundleKey = selected
                    ? "MSG_RightClickEnablesAllOthers" //NOI18N
                    : "MSG_RightClickDisablesOthers";  //NOI18N
            StringBuilder tooltip = new StringBuilder("<html>");        //NOI18N
            tooltip.append(NbBundle.getMessage(
                    AbstractQuickSearchComboBar.class, bundleKey));
            if (null != category.getCommandPrefix()) {
                tooltip.append("<br/>");
                tooltip.append(NbBundle.getMessage(
                        AbstractQuickSearchComboBar.class,
                        "LBL_TooltipCommandPrefix", //NOI18N
                        category.getCommandPrefix()));
            }
            tooltip.append("</html>");                                  //NOI18N
            setToolTipText(tooltip.toString());
        }

        @Override
        public Point getToolTipLocation(MouseEvent event) {
            Point p = new Point(((event.getX() - 25) / 5) * 5, // repaint every
                    ((event.getY() + 15) / 5) * 5);            // 5 pixels
            return p;
        }
    }

    /**
     * Menu item for enabling or disabling all categories.
     */
    private class AllMenuItem extends JMenuItem implements ActionListener {

        private Set<Category> evalCats;
        private int totalCount;
        private MenuElement[] selectedPath = null;

        public AllMenuItem(Set<Category> evalCats) {
            this.evalCats = evalCats;
            this.totalCount = ProviderModel.getInstance()
                    .getCategories().size();
            getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (isShowing() && model.isArmed()) {
                        selectedPath = MenuSelectionManager.defaultManager()
                                .getSelectedPath();
                    }
                }
            });
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (evalCats.size() == totalCount) {
                Iterator<Category> iterator = evalCats.iterator();
                while (iterator.hasNext()) {
                    Category c = iterator.next();
                    if (!CommandEvaluator.RECENT.equals(c.getName())) {
                        iterator.remove();
                    }
                }
            } else {
                evalCats.addAll(ProviderModel.getInstance().getCategories());
            }
            updateCats(evalCats);
            updateCheckBoxes(this, evalCats);
        }

        @Override
        public void doClick(int pressTime) {
            super.doClick(pressTime);
            showMenuPath(selectedPath);
        }

        @Override
        public String getText() {
            if (evalCats == null || evalCats.size() != totalCount) {
                return NbBundle.getMessage(getClass(),
                        "LBL_AllCategories");                           //NOI18N
            } else {
                return NbBundle.getMessage(getClass(),
                        "LBL_NoCategory");                              //NOI18N
            }
        }
    }
}
