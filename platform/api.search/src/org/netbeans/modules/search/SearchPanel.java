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
package org.netbeans.modules.search;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
public class SearchPanel extends JPanel implements FocusListener,
        ActionListener {

    private static SearchPanel currentlyShown = null;
    private boolean replacing;
    private List<PresenterProxy> presenters;
    private DialogDescriptor dialogDescr;
    /**
     * OK button.
     */
    private JButton okButton;
    /**
     * Cancel button.
     */
    private JButton cancelButton;
    /**
     * Open in new Tab checkbox
     */
    private JCheckBox newTabCheckBox;
    /**
     * Tabbed pane if there are extra providers.
     */
    JTabbedPane tabbedPane = null;
    /**
     * Dialog in which this search panel is displayed.
     */
    private Dialog dialog;
    /**
     * Selected Search presenter
     */
    private Presenter selectedPresenter = null;

    private boolean preferScopeSelection = false;

    /**
     * Panel that can show form with settings for several search providers.
     */
    public SearchPanel(boolean replacing) {
        this(replacing, null);
    }

    /**
     * Create search panel, using an explicit presenter for one of providers.
     */
    public SearchPanel(boolean replacing, Presenter presenter) {
        this.replacing = replacing;
        init(presenter);
    }

    private void init(Presenter explicitPresenter) {

        presenters = makePresenters(explicitPresenter);
        setLayout(new GridLayout(1, 1));

        if (presenters.isEmpty()) {
            throw new IllegalStateException("No presenter found");      //NOI18N
        } else if (presenters.size() == 1) {
            selectedPresenter = presenters.get(0).getPresenter();
            add(selectedPresenter.getForm());
        } else {
            tabbedPane = new JTabbedPane();
            for (PresenterProxy pp : presenters) {
                Component tab = tabbedPane.add(pp.getForm());
                if (pp.isInitialized()) {
                    tabbedPane.setSelectedComponent(tab);
                    selectedPresenter = pp.getPresenter();
                }
            }
            tabbedPane.addChangeListener((ChangeEvent e) -> tabChanged());
            add(tabbedPane);
        }
        if (selectedPresenter == null) {
            chooseLastUsedPresenter();
        }
        newTabCheckBox = new JCheckBox(NbBundle.getMessage(SearchPanel.class,
                "TEXT_BUTTON_NEW_TAB"));                                //NOI18N
        newTabCheckBox.setMaximumSize(new Dimension(1000, 200));
        newTabCheckBox.setSelected(
                FindDialogMemory.getDefault().isOpenInNewTab());
        initLocalStrings();
        initAccessibility();
    }

    private void chooseLastUsedPresenter() {
        FindDialogMemory memory = FindDialogMemory.getDefault();
        String lastProv = memory.getProvider();
        if (lastProv != null) {
            for (PresenterProxy pp : presenters) {
                if (lastProv.equals(pp.getTitle())) {
                    selectedPresenter = pp.getPresenter();
                    tabbedPane.setSelectedComponent(pp.getForm());
                    return;
                }
            }
        }
        selectedPresenter = presenters.get(0).getPresenter();
    }

    private void initLocalStrings() throws MissingResourceException {
        setName(NbBundle.getMessage(SearchPanel.class,
                "TEXT_TITLE_CUSTOMIZE"));           //NOI18N

        String bundleKey = isSearchAndReplace()
                ? "TEXT_BUTTON_SEARCH_CONTINUE" : "TEXT_BUTTON_SEARCH"; //NOI18N
        Mnemonics.setLocalizedText(okButton = new JButton(),
                NbBundle.getMessage(
                org.netbeans.modules.search.SearchPanel.class, bundleKey));

        Mnemonics.setLocalizedText(cancelButton = new JButton(),
                NbBundle.getMessage(
                org.netbeans.modules.search.SearchPanel.class,
                "TEXT_BUTTON_CANCEL"));     //NOI18N
    }

    private void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescr = dialogDescriptor;
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACS_SearchPanel")); // NOI18N
        if (tabbedPane != null) {
            tabbedPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "ACSN_Tabs")); // NOI18N
            tabbedPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACSD_Tabs")); // NOI18N
        }
        String descSearchContinue = NbBundle.getMessage(SearchPanel.class,
                isSearchAndReplace() ? "ACS_TEXT_BUTTON_SEARCH_CONTINUE" : "ACS_TEXT_BUTTON_SEARCH"); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(descSearchContinue);
        okButton.setToolTipText(descSearchContinue);
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACS_TEXT_BUTTON_CANCEL")); // NOI18N
        newTabCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACS_TEXT_BUTTON_NEW_TAB")); //NOI18N
    }

    /**
     * Make list of presenters created for all available search providers.
     *
     * @param explicitPresenter One of providers can be assigned an explicit
     * presenter, that will be used instead of creating a new one.
     *
     */
    List<PresenterProxy> makePresenters(Presenter explicitPresenter) {

        List<PresenterProxy> presenterList = new LinkedList<>();
        SearchProvider explicitProvider = explicitPresenter == null
                ? null
                : explicitPresenter.getSearchProvider();
        for (SearchProvider p :
                Lookup.getDefault().lookupAll(SearchProvider.class)) {
            if ((!replacing || p.isReplaceSupported())
                    && (p == explicitProvider || p.isEnabled())) {
                if (explicitProvider == p) {
                    presenterList.add(new PresenterProxy(explicitProvider,
                            explicitPresenter));
                } else {
                    presenterList.add(new PresenterProxy(p));
                }
            }
        }
        return presenterList;
    }

    public void showDialog() {

        String titleMsgKey = replacing
                ? "LBL_ReplaceInProjects" //NOI18N
                : "LBL_FindInProjects"; //NOI18N

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(getClass(), titleMsgKey),
                false,
                new Object[]{okButton, cancelButton},
                okButton,
                DialogDescriptor.BOTTOM_ALIGN,
                new HelpCtx(getClass().getCanonicalName() + "." + replacing),
                this);

        dialogDescriptor.setTitle(NbBundle.getMessage(getClass(), titleMsgKey));
        dialogDescriptor.createNotificationLineSupport();
        dialogDescriptor.setAdditionalOptions(new Object[] {newTabCheckBox});

        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.addWindowListener(new DialogCloseListener());
        this.setDialogDescriptor(dialogDescriptor);

        dialog.pack();
        setCurrentlyShown(this);
        dialog.setVisible(
                true);
        dialog.requestFocus();
        this.requestFocusInWindow();
        updateHelp();
        updateUsability();
        if (selectedPresenter == null) {
            chooseLastUsedPresenter();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        // Tab changed
        tabChanged();
    }

    @Override
    public void focusLost(FocusEvent e) {
        // Tab changed
        tabChanged();
    }

    @Override
    public boolean requestFocusInWindow() {
        return selectedPresenter.getForm().requestFocusInWindow();
    }

    /**
     * Called when tab panel was changed.
     */
    private void tabChanged() {
        if (tabbedPane != null) {
            int i = tabbedPane.getSelectedIndex();
            PresenterProxy pp = presenters.get(i);
            selectedPresenter = pp.getPresenter();
            if (dialogDescr != null) {
                dialogDescr.getNotificationLineSupport().clearMessages();
                updateUsability();
                dialog.pack();
            }
            updateHelp();
            FindDialogMemory.getDefault().setProvider(
                    selectedPresenter.getSearchProvider().getTitle());
        }
    }

    private void updateHelp() {
        HelpCtx ctx = selectedPresenter.getHelpCtx();
        if (this.dialogDescr != null) {
            dialogDescr.setHelpCtx(ctx);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            search();
        } else if (e.getSource() == cancelButton) {
            cancel();
        }
    }

    private void search() {

        if (selectedPresenter != null) {
            SearchComposition<?> sc = selectedPresenter.composeSearch();
            if (sc != null) {
                SearchTask st = new SearchTask(sc, replacing);
                boolean openInNewTab = newTabCheckBox.isSelected();
                if (!openInNewTab) {
                    ResultView.getInstance().markCurrentTabAsReusable();
                }
                FindDialogMemory.getDefault().setOpenInNewTab(openInNewTab);
                Manager.getInstance().scheduleSearchTask(st);
                close();
            }
        }
    }

    private void cancel() {
        close();
        ResultView.getInstance().clearReusableTab();
    }

    /**
     * Is this panel in search-and-replace mode?
     */
    boolean isSearchAndReplace() {
        return replacing;
    }

    /**
     * Close this search panel - dispose its containig dialog.
     *
     * {@link DialogCloseListener#windowClosed(java.awt.event.WindowEvent)} will
     * be called afterwards.
     */
    public void close() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }

    /**
     * Focus containig dialog.
     */
    void focusDialog() {
        if (dialog != null) {
            dialog.requestFocus();
        }
        this.requestFocusInWindow();
    }

    /**
     * Get currently displayed search panel, or null if no panel is shown.
     */
    public static SearchPanel getCurrentlyShown() {
        synchronized (SearchPanel.class) {
            return currentlyShown;
        }
    }

    /**
     * Set currently shoen panel, can be null (no panel shown currently.)
     */
    static void setCurrentlyShown(SearchPanel searchPanel) {
        synchronized (SearchPanel.class) {
            SearchPanel.currentlyShown = searchPanel;
        }
    }

    /**
     * Add change listener to a presenter.
     */
    private void initChangeListener(final Presenter p) {
        p.addChangeListener((ChangeEvent e) -> okButton.setEnabled(
                p.isUsable(dialogDescr.getNotificationLineSupport())));
    }

    private void updateUsability() {
        okButton.setEnabled(selectedPresenter.isUsable(
                dialogDescr.getNotificationLineSupport()));
    }

    public boolean isPreferScopeSelection() {
        return preferScopeSelection;
    }

    public void setPreferScopeSelection(boolean preferScopeSelection) {
        this.preferScopeSelection = preferScopeSelection;
    }

    public static boolean isOpenedForSelection() {
        SearchPanel sp = getCurrentlyShown();
        if (sp == null) {
            return false;
        } else {
            return sp.isPreferScopeSelection();
        }
    }

    /**
     * Dialog-Close listener that clears reference to currently displayed panel
     * when its dialog is closed.
     */
    private class DialogCloseListener extends WindowAdapter {

        @Override
        public void windowClosed(WindowEvent e) {
            for (PresenterProxy presenter : presenters) {
                if (presenter.isInitialized()) {
                    presenter.getPresenter().clean();
                }
            }
            if (getCurrentlyShown() == SearchPanel.this) {
                setCurrentlyShown(null);
            }
        }
    }

    private class PresenterProxy {

        private SearchProvider searchProvider;
        private Presenter presenter;
        private JPanel panel;

        PresenterProxy(SearchProvider searchProvider) {
            this(searchProvider, null);
        }

        PresenterProxy(SearchProvider searchProvider,
                Presenter presenter) {
            this.searchProvider = searchProvider;
            this.presenter = presenter;
            this.panel = new JPanel();
            this.panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            this.panel.setName(getTitle());
            if (presenter != null) {
                initUI();
            }
        }

        final String getTitle() {
            return searchProvider.getTitle();
        }

        synchronized Presenter getPresenter() {
            if (presenter == null) {
                presenter = searchProvider.createPresenter(replacing);
                initUI();
            }
            return presenter;
        }

        synchronized boolean isInitialized() {
            return presenter != null;
        }

        synchronized JComponent getForm() {
            return panel;
        }

        private void initUI() {
            panel.add(presenter.getForm());
            initChangeListener(presenter);
            panel.validate();
        }

        @Override
        public String toString() {
            return "Proxy presenter for " + getTitle();
        }
    }
}
