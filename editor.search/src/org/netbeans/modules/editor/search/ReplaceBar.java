/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.editor.search;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.GuardedException;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public final class ReplaceBar extends JPanel implements PropertyChangeListener {

    private static ReplaceBar replacebarInstance = null;
    private static final Logger LOG = Logger.getLogger(ReplaceBar.class.getName());
    private SearchBar searchBar;
    private final JComboBox<String> replaceComboBox;
    private final JTextComponent replaceTextField;
    private final JButton replaceButton;
    private final JButton replaceAllButton;
    private final JLabel replaceLabel;
    private final JCheckBox preserveCaseCheckBox;
    private final JCheckBox backwardsCheckBox;
    private final FocusTraversalPolicy searchBarFocusTraversalPolicy;
    private final List<Component> focusList = new ArrayList<>();
    private boolean popupMenuWasCanceled = false;

    public static ReplaceBar getInstance(SearchBar searchBar) {
        if (replacebarInstance == null) {
            replacebarInstance = new ReplaceBar(searchBar);
        }
        if (replacebarInstance.getSearchBar() != searchBar) {
            replacebarInstance.setSearchBar(searchBar);
        }
        return replacebarInstance;
    }

    private ReplaceBar(SearchBar searchBar) {
        setSearchBar(searchBar);
        addEscapeKeystrokeFocusBackTo(this);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setFocusCycleRoot(true);
        setForeground(UIManager.getColor("textText")); //NOI18N

        // padding at the end of the toolbar
        add(Box.createHorizontalStrut(8)); //spacer in the beginnning of the toolbar
        SearchComboBox<String> scb = new SearchComboBox<>();
        replaceComboBox = scb;
        replaceComboBox.addPopupMenuListener(new ReplacePopupMenuListener());
        replaceTextField = scb.getEditorPane();
        replaceTextField.setToolTipText(NbBundle.getMessage(ReplaceBar.class, "TOOLTIP_ReplaceText")); // NOI18N
        replaceTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                getSearchBar().lostFocusOnTextField();
            }
        });
        addEnterKeystrokeReplaceTo(replaceTextField);
        addShiftEnterReplaceAllTo(replaceTextField);

        replaceLabel = new JLabel();
        Mnemonics.setLocalizedText(replaceLabel, NbBundle.getMessage(ReplaceBar.class, "CTL_Replace")); // NOI18N
        replaceLabel.setLabelFor(replaceTextField);
        add(replaceLabel);
        add(replaceComboBox);

        final JToolBar.Separator leftSeparator = new JToolBar.Separator();
        leftSeparator.setOrientation(SwingConstants.VERTICAL);
        add(leftSeparator);

        replaceButton = SearchButton.createButton("org/netbeans/modules/editor/search/resources/replace.png", "CTL_ReplaceNext"); // NOI18N
        replaceButton.setToolTipText(NbBundle.getMessage(ReplaceBar.class, "TOOLTIP_ReplaceText")); // NOI18N
        replaceButton.setEnabled(!getSearchBar().getIncSearchTextField().getText().isEmpty());
        replaceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replace();
            }
        });
        add(replaceButton);

        replaceAllButton = SearchButton.createButton("org/netbeans/modules/editor/search/resources/replace_all.png", "CTL_ReplaceAll"); // NOI18N
        replaceAllButton.setToolTipText(NbBundle.getMessage(ReplaceBar.class, "TOOLTIP_ReplaceText")); // NOI18N
        replaceAllButton.setEnabled(!getSearchBar().getIncSearchTextField().getText().isEmpty());
        replaceAllButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replaceAll();
            }
        });
        add(replaceAllButton);

        changeButtonsSizeAsSearchBarButtons();
        searchBar.addCheckBoxesActions(replaceTextField);

        final JToolBar.Separator rightSeparator = new JToolBar.Separator();
        rightSeparator.setOrientation(SwingConstants.VERTICAL);
        add(rightSeparator);

        backwardsCheckBox = searchBar.createCheckBox("CTL_BackwardsReplace", EditorFindSupport.FIND_BACKWARD_SEARCH); // NOI18N
        add(backwardsCheckBox);
        preserveCaseCheckBox = searchBar.createCheckBox("CTL_PreserveCase", EditorFindSupport.FIND_PRESERVE_CASE); // NOI18N
        preserveCaseCheckBox.setToolTipText(NbBundle.getMessage(ReplaceBar.class, "TOOLTIP_PreserveCase")); // NOI18N
        add(preserveCaseCheckBox);

        backwardsCheckBox.setSelected(searchBar.getFindSupportValue(EditorFindSupport.FIND_BACKWARD_SEARCH));
        preserveCaseCheckBox.setSelected(searchBar.getFindSupportValue(EditorFindSupport.FIND_PRESERVE_CASE));
        preserveCaseCheckBox.setEnabled(!searchBar.getRegExp() && !searchBar.getFindSupportValue(EditorFindSupport.FIND_MATCH_CASE));
        
        // padding at the end of the toolbar
        add(Box.createHorizontalGlue());

        focusList.clear();
        focusList.add(searchBar.getIncSearchTextField());
        focusList.add(replaceTextField);
        searchBarFocusTraversalPolicy = new ListFocusTraversalPolicy(focusList);

        EditorFindSupport.getInstance().addPropertyChangeListener(WeakListeners.propertyChange(this, EditorFindSupport.getInstance()));
        setVisible(false);
    }

    private SearchBar getSearchBar() {
        return searchBar;
    }

    private void setSearchBar(SearchBar searchBar) {
        this.searchBar = searchBar;
    }

    public JButton getReplaceButton() {
        return replaceButton;
    }

    public JButton getReplaceAllButton() {
        return replaceAllButton;
    }
    
    public JTextComponent getReplaceTextField() {
        return replaceTextField;
    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    private void changeButtonsSizeAsSearchBarButtons() {
        int replaceBarButtonsSize = replaceButton.getPreferredSize().width + replaceAllButton.getPreferredSize().width;
        int searchBarButtonsSize = searchBar.getFindNextButton().getPreferredSize().width + searchBar.getFindPreviousButton().getPreferredSize().width;
        int diffButtonsSize = (searchBarButtonsSize - replaceBarButtonsSize) / 2;
        int diffEven = diffButtonsSize % 2 == 0 ? 0 : 1;
        if (diffButtonsSize > 0) {
            replaceButton.setPreferredSize(new Dimension(replaceButton.getPreferredSize().width + diffButtonsSize + diffEven, replaceButton.getPreferredSize().height));
            replaceAllButton.setPreferredSize(new Dimension(replaceAllButton.getPreferredSize().width + diffButtonsSize, replaceAllButton.getPreferredSize().height));
        } else {
            searchBar.getFindNextButton().setPreferredSize(new Dimension(searchBar.getFindNextButton().getPreferredSize().width - diffButtonsSize + diffEven, searchBar.getFindNextButton().getPreferredSize().height));
            searchBar.getFindPreviousButton().setPreferredSize(new Dimension(searchBar.getFindPreviousButton().getPreferredSize().width - diffButtonsSize, searchBar.getFindPreviousButton().getPreferredSize().height));
        }
    }

    private void addEnterKeystrokeReplaceTo(JTextComponent replaceTextField) {
        replaceTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                "replace-next"); // NOI18N
        replaceTextField.getActionMap().put("replace-next", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!popupMenuWasCanceled && !searchBar.isPopupMenuWasCanceled()) {
                    replace();
                } else {
                    popupMenuWasCanceled = false;
                    searchBar.setPopupMenuWasCanceled(false);
                }
            }
        });
    }

    private void addShiftEnterReplaceAllTo(JTextComponent textField) {
        textField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, true),
                "replace-all"); // NOI18N
        textField.getActionMap().put("replace-all", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replaceAll();
            }
        });
    }

    private void addEscapeKeystrokeFocusBackTo(JPanel jpanel) {
        jpanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                "loose-focus"); // NOI18N
        jpanel.getActionMap().put("loose-focus", new AbstractAction() { // NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!popupMenuWasCanceled && !searchBar.isPopupMenuWasCanceled()) {
                    looseFocus();
                } else {
                    popupMenuWasCanceled = false;
                    searchBar.setPopupMenuWasCanceled(false);
                }

            }
        });
    }

    void updateReplaceComboBoxHistory(String incrementalSearchText) {
        EditorFindSupport.getInstance().addToReplaceHistory(new EditorFindSupport.RP(incrementalSearchText, preserveCaseCheckBox.isSelected()));
        // Add the text to the top of the list
        for (int i = replaceComboBox.getItemCount() - 1; i >= 0; i--) {
            String item = replaceComboBox.getItemAt(i);
            if (item.equals(incrementalSearchText)) {
                replaceComboBox.removeItemAt(i);
            }
        }
        ((MutableComboBoxModel<String>) replaceComboBox.getModel()).insertElementAt(incrementalSearchText, 0);
        replaceComboBox.setSelectedIndex(0);
    }

    private ActionListener closeButtonListener;

    private ActionListener getCloseButtonListener() {
        if (closeButtonListener == null) {
            closeButtonListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    looseFocus();
                }
            };
        }
        return closeButtonListener;
    }

    private void unchangeSearchBarToBeOnlySearchBar() {
        searchBar.getCloseButton().removeActionListener(getCloseButtonListener());
        Mnemonics.setLocalizedText(searchBar.getFindLabel(), NbBundle.getMessage(ReplaceBar.class, "CTL_Find")); // NOI18N
        Dimension oldDimensionForFindLabel = searchBar.getFindLabel().getUI().getMinimumSize(searchBar.getFindLabel());
        searchBar.getFindLabel().setMinimumSize(oldDimensionForFindLabel);
        searchBar.getFindLabel().setPreferredSize(oldDimensionForFindLabel);
        searchBar.addEscapeKeystrokeFocusBackTo(searchBar);
        searchBar.setFocusTraversalPolicy(null);
        searchBar.looseFocus();
        searchBar.setSearchProperties(SearchPropertiesSupport.getSearchProperties());
    }

    private void changeSearchBarToBePartOfReplaceBar() {
        searchBar.getCloseButton().addActionListener(getCloseButtonListener());
        Mnemonics.setLocalizedText(searchBar.getFindLabel(), NbBundle.getMessage(ReplaceBar.class, "CTL_Replace_Find")); // NOI18N
        Dimension newDimensionForFindLabel = new Dimension(replaceLabel.getPreferredSize().width, searchBar.getFindLabel().getPreferredSize().height);
        searchBar.getFindLabel().setMinimumSize(newDimensionForFindLabel);
        searchBar.getFindLabel().setPreferredSize(newDimensionForFindLabel);
        this.addEscapeKeystrokeFocusBackTo(searchBar);
        searchBar.setFocusTraversalPolicy(searchBarFocusTraversalPolicy);
        setFocusTraversalPolicy(searchBarFocusTraversalPolicy);
        searchBar.getPreferredSize();
        searchBar.setSearchProperties(SearchPropertiesSupport.getReplaceProperties());
    }

    public void looseFocus() {
        if (!isVisible()) {
            return;
        }

        unchangeSearchBarToBeOnlySearchBar();
        setVisible(false);
    }

    public void gainFocus(boolean persistanceStatus) {
        if (!isVisible()) {
            String lastReplace = replaceTextField.getText();
            changeSearchBarToBePartOfReplaceBar();
            SearchComboBoxEditor.changeToOneLineEditorPane((JEditorPane) replaceTextField);
            addEnterKeystrokeReplaceTo(replaceTextField);
            MutableComboBoxModel<String> comboBoxModelIncSearch = ((MutableComboBoxModel<String>) replaceComboBox.getModel());
            for (int i = comboBoxModelIncSearch.getSize() - 1; i >= 0; i--) {
                comboBoxModelIncSearch.removeElementAt(i);
            }
            for (EditorFindSupport.RP rp : EditorFindSupport.getInstance().getReplaceHistory()) {
                comboBoxModelIncSearch.addElement(rp.getReplaceExpression());
            }
            replaceTextField.setText(lastReplace);
            setVisible(true);
        }
        searchBar.gainFocus(persistanceStatus);
        searchBar.getIncSearchTextField().requestFocusInWindow();
    }

    private void replace() {
        replace(false);
    }

    private void replaceAll() {
        replace(true);
    }

    private void replace(boolean replaceAll) {
        searchBar.updateIncSearchComboBoxHistory(searchBar.getIncSearchTextField().getText());
        this.updateReplaceComboBoxHistory(replaceTextField.getText());

        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        Map<String, Object> findProps = new HashMap<>();
        findProps.putAll(searchBar.getSearchProperties());
        findProps.put(EditorFindSupport.FIND_REPLACE_WITH, replaceTextField.getText());
        findProps.put(EditorFindSupport.FIND_BACKWARD_SEARCH, backwardsCheckBox.isSelected());
        findProps.put(EditorFindSupport.FIND_PRESERVE_CASE, preserveCaseCheckBox.isSelected() && preserveCaseCheckBox.isEnabled());
        findSupport.putFindProperties(findProps);
        if (replaceAll) {
            findSupport.replaceAll(findProps);
        } else {
            try {
                findSupport.replace(findProps, false);
                findSupport.find(findProps, false);
            } catch (GuardedException ge) {
                LOG.log(Level.FINE, null, ge);
                Toolkit.getDefaultToolkit().beep();
            } catch (BadLocationException ble) {
                LOG.log(Level.WARNING, null, ble);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (isShowing()) {
            preserveCaseCheckBox.setEnabled(!(Boolean) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_REG_EXP) && !(Boolean) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_MATCH_CASE));
        }
    }

    private class ReplacePopupMenuListener implements PopupMenuListener {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenuWasCanceled = true;
        }
    }
}
