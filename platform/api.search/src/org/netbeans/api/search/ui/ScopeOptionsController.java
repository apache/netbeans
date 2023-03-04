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
package org.netbeans.api.search.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.modules.search.BasicSearchProvider;
import org.netbeans.modules.search.IgnoreListPanel;
import org.netbeans.modules.search.PatternSandbox;
import org.netbeans.modules.search.ui.CheckBoxWithButtonPanel;
import org.netbeans.modules.search.ui.FormLayoutHelper;
import org.netbeans.modules.search.ui.UiUtils;

/**
 * Component controller for setting search scope options.
 *
 * Use {@link ComponentUtils} to create instances of this class.
 *
 * @author jhavlin
 */
public final class ScopeOptionsController extends ComponentController<JPanel> {

    private FileNameController fileNameComboBox;
    private boolean replacing;
    protected JPanel ignoreListOptionPanel;
    private JButton btnEditIgnoreList;
    protected JCheckBox chkUseIgnoreList;
    private JCheckBox chkFileNameRegex;
    private JButton btnTestFileNamePattern;
    private JCheckBox chkArchives;
    private JCheckBox chkGenerated;
    private ItemListener checkBoxListener;
    private JPanel fileNameComponent;

    /**
     * Create settings panel that can be used in search dialog.
     *
     * @param component Component to adjust.
     * @param fileNameComboBox File name combo box that will be bound to the
     * regular-expression check box in the panel.
     * @param replacing Replace mode flag.
     */
    ScopeOptionsController(JPanel component,
            FileNameController fileNameComboBox, boolean replacing) {
        this(component, null, fileNameComboBox, replacing);
    }

    /**
     * Create two settings panels that can be used in search dialog. The first
     * panel will contain controls for setting search scope options, the second
     * panel controls for setting file name pattern options.
     *
     * @param scopeComponent Component to adjust for search scope setting.
     * @param fileNameComponent Component to adjust for file name settings.
     * @param fileNameController File name controller tath will be bound to the
     * regular-expression check box in the file name settings panel.
     * @param replacing Replace mode flag.
     * @since api.search/1.12
     */
    ScopeOptionsController(JPanel scopeComponent, JPanel fileNameComponent,
            FileNameController fileNameController, boolean replacing) {
        super(scopeComponent);
        this.fileNameComponent = fileNameComponent;
        this.fileNameComboBox = fileNameController;
        this.replacing = replacing;
        init();
    }

    /**
     * Return search scope options reflecting the actual state of the panel.
     *
     * Modifying returned object will not affect this panel.
     */
    public SearchScopeOptions getSearchScopeOptions() {
        SearchScopeOptions sso = SearchScopeOptions.create();
        if (fileNameComboBox != null) {
            sso.setPattern(fileNameComboBox.getFileNamePattern());
        }
        sso.setRegexp(isFileNameRegExp());
        sso.setSearchInArchives(isSearchInArchives());
        sso.setSearchInGenerated(isSearchInGenerated());
        if (isUseIgnoreList()) {
            sso.addFilter(BasicSearchProvider.getIgnoreListFilter());
        }
        return sso;
    }

    private void init() {
        btnTestFileNamePattern = new JButton();
        chkFileNameRegex = new JCheckBox();
        chkFileNameRegex.setToolTipText(UiUtils.getText(
                "BasicSearchForm.chkFileNameRegex.tooltip"));           //NOI18N

        if (!replacing) {
            chkArchives = new JCheckBox();
            chkGenerated = new JCheckBox();
        }
        chkUseIgnoreList = new JCheckBox();
        btnEditIgnoreList = new JButton();
        checkBoxListener = new CheckBoxListener();

        component.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setMnemonics();
        initIgnoreListControlComponents();
        initScopeOptionsRow(replacing);
        initInteraction();
    }

    /**
     * Initialize ignoreListOptionPanel and related control components.
     */
    private void initIgnoreListControlComponents() {
        ignoreListOptionPanel = new CheckBoxWithButtonPanel(chkUseIgnoreList,
                btnEditIgnoreList);
    }

    /**
     * Initialize panel for controls for scope options and add it to the form
     * panel.
     */
    private void initScopeOptionsRow(boolean searchAndReplace) {

        JPanel regexpPanel = new CheckBoxWithButtonPanel(
                chkFileNameRegex, btnTestFileNamePattern);
        if (fileNameComponent != null) {
            fileNameComponent.setLayout(
                    new FlowLayout(FlowLayout.LEADING, 0, 0));
            fileNameComponent.add(ignoreListOptionPanel);
            fileNameComponent.add(regexpPanel);
            if (!searchAndReplace) {
                component.add(chkArchives);
                component.add(chkGenerated);
            }
        } else {
            JPanel jp = new JPanel();
            if (searchAndReplace) {
                jp.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
                jp.add(ignoreListOptionPanel);
                jp.add(regexpPanel);
                jp.setMaximumSize(jp.getMinimumSize());
            } else {
                FormLayoutHelper flh = new FormLayoutHelper(jp,
                        FormLayoutHelper.DEFAULT_COLUMN,
                        FormLayoutHelper.DEFAULT_COLUMN);
                flh.addRow(chkArchives, chkGenerated);
                flh.addRow(ignoreListOptionPanel,
                        new CheckBoxWithButtonPanel(
                        chkFileNameRegex, btnTestFileNamePattern));
                jp.setMaximumSize(jp.getMinimumSize());
            }
            component.add(jp);
        }
    }

    private void initInteraction() {
        btnTestFileNamePattern.addActionListener((ActionEvent e) -> openPathPatternSandbox());
        btnEditIgnoreList.addActionListener((ActionEvent e) -> IgnoreListPanel.openDialog(btnEditIgnoreList));
        if (!replacing) {
            chkArchives.addItemListener(checkBoxListener);
            chkGenerated.addItemListener(checkBoxListener);
        }
        chkUseIgnoreList.addItemListener(checkBoxListener);
        if (fileNameComboBox != null) {
            chkFileNameRegex.addActionListener((ActionEvent e) -> fileNameComboBox.setRegularExpression(chkFileNameRegex.isSelected()));
        } else {
            chkFileNameRegex.addItemListener(checkBoxListener);
        }
    }

    private void openPathPatternSandbox() {

        PatternSandbox.openDialog(new PatternSandbox.PathPatternSandbox(
                fileNameComboBox.getComponent().getSelectedItem() == null
                ? "" : fileNameComboBox.getFileNamePattern()) { //NOI18N
            @Override
            protected void onApply(String pattern) {
                if (pattern.isEmpty()) {
                    if (!fileNameComboBox.isAllFilesInfoDisplayed()) {
                        fileNameComboBox.getComponent().setSelectedItem(pattern);
                        fileNameComboBox.displayAllFilesInfo();
                    }
                } else {
                    if (fileNameComboBox.isAllFilesInfoDisplayed()) {
                        fileNameComboBox.hideAllFilesInfo();
                    }
                    fileNameComboBox.getComponent().setSelectedItem(pattern);
                }
            }
        }, btnTestFileNamePattern);
    }

    private void setMnemonics() {

        UiUtils.lclz(chkFileNameRegex,
                "BasicSearchForm.chkFileNameRegex.text");               //NOI18N
        btnTestFileNamePattern.setText(UiUtils.getHtmlLink(
                "BasicSearchForm.btnTestFileNamePattern.text"));        //NOI18N
        btnEditIgnoreList.setText(UiUtils.getHtmlLink(
                "BasicSearchForm.btnEditIgnoreList.text"));             //NOI18N
        UiUtils.lclz(chkUseIgnoreList,
                "BasicSearchForm.chkUseIgnoreList.text");               //NOI18N
        if (!replacing) {
            UiUtils.lclz(chkArchives,
                    "BasicSearchForm.chkArchives.text");                //NOI18N
            UiUtils.lclz(chkGenerated,
                    "BasicSearchForm.chkGenerated.text");               //NOI18N
        }
    }

    /**
     * State of checkbox for enabling searching in archives.
     *
     * @return True if searching in archives is enabled, false if it is
     * disabled.
     */
    public boolean isSearchInArchives() {
        return isOn(chkArchives);
    }

    /**
     * State of checkbox for enabling searching in generated sources.
     *
     * Generated sources include class files or web service stubs generated for
     * WSDL files. These files are usualy filtered out by SharabilityQuery.
     *
     * @see SharabilityQuery
     * @return True if searching in generated sources is enabled, false
     * otherwise.
     */
    public boolean isSearchInGenerated() {
        return isOn(chkGenerated);
    }

    /**
     * State of checkbox for using ignore list.
     *
     * If this method returns true, {@link SearchScopeOptions} object returned
     * from {@link #getSearchScopeOptions()} includes filter for ignored files.
     *
     * @return True if ignore list is enabled, false otherwise.
     */
    public boolean isUseIgnoreList() {
        return isOn(chkUseIgnoreList);
    }

    /**
     * @return True if file name pattern is set to be used as regular expression
     * for matching the whole file path, false is it should be used as simple
     * pattern for file names.
     */
    public boolean isFileNameRegExp() {
        return isOn(chkFileNameRegex);
    }

    /**
     * @return True if and only if checkbox is not null, is enabled and
     * selected.
     */
    private boolean isOn(JCheckBox chbox) {
        return chbox != null && chbox.isEnabled() && chbox.isSelected();
    }

    /**
     * Enable/disable searching in archives.
     */
    public void setSearchInArchives(boolean searchInArchives) {
        if (chkArchives == null) {
            if (searchInArchives) {
                throw new IllegalArgumentException(
                        "Searching in archives not allowed "
                        + "when replacing");                            //NOI18N
            }
        } else {
            chkArchives.setSelected(searchInArchives);
        }
    }

    /**
     * Enable/disable searching in generated sources.
     */
    public void setSearchInGenerated(boolean searchInGenerated) {
        if (chkGenerated == null) {
            if (searchInGenerated) {
                throw new IllegalArgumentException(
                        "Searching in generated sources not allowed "
                        + "when replacing");                            //NOI18N
            }
        } else {
            chkGenerated.setSelected(searchInGenerated);
        }
    }

    /**
     * Enable/disable using ignore list.
     */
    public void setUseIgnoreList(boolean useIgnoreList) {
        chkUseIgnoreList.setSelected(useIgnoreList);
    }

    /**
     * Enable/disable regular expression mode.
     *
     * @see #isFileNameRegExp()
     */
    public void setFileNameRegexp(boolean fileNameRegexp) {
        chkFileNameRegex.setSelected(fileNameRegexp);
    }

    /**
     * Get the panel containing controls related to file name pattern settings.
     * This is only applicable if the controller was created using
     * {@link ComponentUtils#adjustPanelsForOptions(JPanel, JPanel, boolean, FileNameController)}.
     *
     * @return Panel containing controls related to file name pattern settings,
     * or null if there is a single panel for all settings.
     * @since api.search/1.12
     */
    public @NullUnknown JPanel getFileNameComponent() {
        return fileNameComponent;
    }

    /**
     * Checkbox listener.
     */
    private final class CheckBoxListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }
    }
}
