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

package org.netbeans.modules.bugtracking.ui.selectors;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.ItemSelectable;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import static javax.swing.JComponent.LEFT_ALIGNMENT;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import javax.swing.ListCellRenderer;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.HORIZONTAL;
import static javax.swing.SwingConstants.NORTH;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.VERTICAL;
import static javax.swing.SwingConstants.WEST;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.openide.DialogDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Allows the user to select an existing connection to a bug-tracking repository
 * or to create a new connection.
 *
 * @author Marian Petras
 */
public final class RepositorySelectorBuilder implements ItemListener,
                                                        ItemSelectable {
                                                              

    private static final String EMPTY_PANEL = "empty panel";            //NOI18N
    private static final String NEW_REPO_PANEL = "new repo panel";      //NOI18N

    private RepositoryImpl[] existingRepositories;
    private DelegatingConnector[] bugtrackingConnectors;

    private JLabel label;
    private JComboBox combo;
    private final JComponent cardsPanel;

    private boolean emptyPanelInitialized;
    private RepositoryFormPanel repositoryFormsPanel;
    private ChangeListener repositoryFormPanelListener;

    private boolean isValidData;
    private boolean repositoryFormVisible;

    private List<ChangeListener> changeListeners;
    private ChangeEvent changeEvent;

    private List<ResizeListener> resizeListeners;

    private List<ItemListener> selectionListeners;

    public RepositorySelectorBuilder() {
        cardsPanel = new JPanel(new CardLayout());
    }

    private String labelText = null;
    private boolean labelAbove = false;
    private boolean labelVisible = true;
    private boolean comboVisible = true;
    private boolean displayFormForExistingRepo = true;
    private String bugtrackingConnectorDisplayFormat;
    private String initialErrorMessage;
    private RepositoryImpl repoToPreselect;

    /* SETTERS */

    public void setLabelVisible(boolean visible) {
        if (!visible && (label != null)) {
            throw new IllegalStateException(
                  "Cannot change visibility of an already created label."); //NOI18N
        }

        labelVisible = visible;
    }

    public void setLabelText(String text) {
        if (label == null) {
            labelText = text;
        } else {
            Mnemonics.setLocalizedText(label, text);
        }
    }

    public void setComboBoxVisible(boolean visible) {
        if (!visible && (combo != null)) {
            throw new IllegalStateException(
                  "Cannot change parameters of an already created combo-box."); //NOI18N
        }

        comboVisible = visible;
        if (!visible) {
            combo = null;
        }
    }

    public void setBugtrackingConnectorDisplayFormat(String format) {
        this.bugtrackingConnectorDisplayFormat = format;
    }

    public void setLabelAboveComboBox() {
        labelAbove = true;
    }

    public void setLabelNextToComboBox() {
        labelAbove = false;
    }

    public void setDisplayFormForExistingRepositories(boolean display) {
        displayFormForExistingRepo = display;
    }

    public void setExistingRepositories(RepositoryImpl[] repositories) {
        if (combo != null) {
            throw new IllegalStateException(
                  "Cannot change parameters of an already created combo-box."); //NOI18N
        }

        if ((repositories != null) && (repositories.length == 0)) {
            repositories = null;
        }

        List<RepositoryImpl> l = new LinkedList<RepositoryImpl>();
        for (RepositoryImpl r : repositories) {
            DelegatingConnector c = BugtrackingManager.getInstance().getConnector(r.getConnectorId());
            if(c.providesRepositoryManagement()) {
                l.add(r);
            }
        }
        
        this.existingRepositories = l.toArray(new RepositoryImpl[0]);
    }

    public void setBugtrackingConnectors(DelegatingConnector[] connectors) {
        if (combo != null) {
            throw new IllegalStateException(
                  "Cannot change parameters of an already created combo-box."); //NOI18N
        }

        if ((connectors != null) && (connectors.length == 0)) {
            connectors = null;
        }

        this.bugtrackingConnectors = connectors;
    }

    public void setPreselectedRepository(RepositoryImpl repository) {
        repoToPreselect = repository;
        if (combo != null) {
            combo.setSelectedItem(repository);
        }
    }

    public void setInitialErrorMessage(String errMsg) {
        this.initialErrorMessage = errMsg;
    }

    /* GETTERS */

    public JLabel getLabel() {
        if (labelVisible && (label == null)) {
            initializeLabel();
        }
        return label;
    }

    public JComboBox getComboBox() {
        if (comboVisible && (combo == null)) {
            initializeCombo();
        }
        return combo;
    }

    public JComponent getFormPanel() {
        if (!comboVisible
                && (repositoryFormsPanel == null)
                && (repoToPreselect != null)) {
            initializeCardsPanel();
        }
        return cardsPanel;
    }

    /* CREATE PANEL */

    private void initializeLabel() {
        label = new JLabel();
        Mnemonics.setLocalizedText(label,
                                   (labelText != null) ? labelText : getDefaultLabelText());
        if (combo != null) {
            bindLabelToCombo();
        }
    }

    private void initializeCombo() {
        boolean hasExistingRepositories = (existingRepositories != null)
                                          && (existingRepositories.length != 0);
        boolean hasBugtrackingConnectors = (bugtrackingConnectors != null)
                                           && (bugtrackingConnectors.length != 0);

        if (!hasExistingRepositories && !hasBugtrackingConnectors) {
            throw new IllegalStateException("No data for the combo-box."); //NOI18N
        }

        String newConnectionFormatString
                    = (bugtrackingConnectorDisplayFormat != null)
                      ? bugtrackingConnectorDisplayFormat
                      : NbBundle.getMessage(
                                ComboItemsRenderer.class,
                                "NewBugtrackingRepositoryConnection");  //NOI18N

        combo = new JComboBox(joinArrays(existingRepositories,
                                         createRepositoryInfos(bugtrackingConnectors)));
        combo.setRenderer(new ComboItemsRenderer(combo.getRenderer(),
                                                 newConnectionFormatString));
        //combo.setEditable(false);

        if (repoToPreselect != null) {
            combo.setSelectedItem(repoToPreselect);
        }
        itemSelected(combo.getSelectedItem());
        combo.addItemListener(this);
        combo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RepositorySelectorBuilder.class, "RepositorySelectorBuilder.combo.accessibleDescription")); // NOI18N

        if (label != null) {
            bindLabelToCombo();
        }
    }

    private void initializeCardsPanel() {
        if (!comboVisible
                && (repositoryFormsPanel == null)
                && (repoToPreselect != null)) {
            displayRepositoryForm(repoToPreselect, initialErrorMessage);
        }
    }

    private void bindLabelToCombo() {
        if ((label != null) && (combo != null)) {
            label.setLabelFor(combo);
        }
    }

    public JComponent createPanel() {
        label = getLabel();
        combo = getComboBox();
        initializeCardsPanel();

        if ((label == null) && (combo == null)) {
            addInsetsToPanel(cardsPanel);
            return cardsPanel;
        }

        JComponent upperPanel;
        if ((label != null) && (combo != null) && labelAbove) {
            upperPanel = new JPanel();
            upperPanel.setLayout(new BoxLayout(upperPanel, Y_AXIS));
            upperPanel.add(label);
            upperPanel.add(createVerticalStrut(upperPanel, label, combo, RELATED));
            upperPanel.add(combo);
            label.setAlignmentX(LEFT_ALIGNMENT);
            combo.setAlignmentX(LEFT_ALIGNMENT);
        } else if ((label != null) && (combo != null)) {
            upperPanel = new JPanel();
            upperPanel.setLayout(new BoxLayout(upperPanel, X_AXIS));
            upperPanel.add(label);
            upperPanel.add(createHorizontalStrut(upperPanel, label, combo, RELATED));
            upperPanel.add(combo);
            combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        } else if (label != null) {
            upperPanel = label;
        } else {
            assert (combo != null);
            upperPanel = combo;
        }

        AutoResizingPanel panel = new AutoResizingPanel();
        panel.setLayout(new BorderLayout(0, getSpace(panel,
                                                     upperPanel,
                                                     cardsPanel,
                                                     UNRELATED,
                                                     VERTICAL)));
        panel.add(upperPanel, BorderLayout.NORTH);
        panel.add(cardsPanel, BorderLayout.CENTER);
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RepositorySelectorBuilder.class, "RepositorySelector.accessibleDescription")); // NOI18N

        addInsetsToPanel(panel);

        addResizeListener(panel);

        return panel;
    }

    public DialogDescriptor createDialogDescriptor(String title) {
        return new ValidatingDialogDescriptor(title);
    }

    private final class ValidatingDialogDescriptor extends DialogDescriptor
                                                   implements ChangeListener,
                                                              ItemListener {
        ValidatingDialogDescriptor(String title) {
            super(createPanel(), title);

            RepositorySelectorBuilder.this.addChangeListener(this);
            updateStatus();

            RepositorySelectorBuilder.this.addItemListener(this);
            updateHelpId();
        }

        public void stateChanged(ChangeEvent e) {
            updateStatus();
        }
        private void updateStatus() {
            setValid(RepositorySelectorBuilder.this.isValidData());
        }

        public void itemStateChanged(ItemEvent e) {
            updateHelpId();
        }
        private void updateHelpId() {
            setHelpCtx(getHelpFor(RepositorySelectorBuilder.this.getSelectedRepository()));
        }
        private HelpCtx getHelpFor(RepositoryImpl repository) {
            return (repository != null)
                   ? repository.getController().getHelpCtx()
                   : null;
        }

    }

    private final class AutoResizingPanel extends JPanel
                                          implements ResizeListener {

        AutoResizingPanel() {
            super();
        }

        AutoResizingPanel(LayoutManager lm) {
            super(lm);
        }

        private Dimension requestedSize;

        public void resizeMayBeNeeded() {
            expandWindowToFitNewConnectorForm();
        }

        private void expandWindowToFitNewConnectorForm() {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window == null) {
                return;
            }

            Dimension currSize = getSize();
            Dimension prefSize = getPreferredSize();
            if ((currSize.width >= prefSize.width) && (currSize.height >= prefSize.height)) {
                /* the dialog is large enough to fit the form */
                return;
            }

            try {
                requestedSize = new Dimension(
                                        Math.max(currSize.width, prefSize.width),
                                        Math.max(currSize.height, prefSize.height));
                window.pack();
            } finally {
                requestedSize = null;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return (requestedSize != null) ? requestedSize
                                           : super.getPreferredSize();
        }

    }

    /* PRIVATE METHODS */

    private static NewRepositoryInfo[] createRepositoryInfos(
                                            DelegatingConnector[] connectors) {
        if (connectors == null) {
            return null;
        }

        List<NewRepositoryInfo> result = new ArrayList<NewRepositoryInfo>(connectors.length);
        for (int i = 0; i < connectors.length; i++) {
            if(connectors[i].providesRepositoryManagement()) {
                result.add(new NewRepositoryInfo(connectors[i]));
            }
        }
        return result.toArray(new NewRepositoryInfo[0]);
    }

    public RepositoryImpl getSelectedRepository() {
        if (combo != null) {
            Object selectedItem = combo.getSelectedItem();
            if (selectedItem instanceof RepositoryImpl) {
                return (RepositoryImpl) selectedItem;
            }
        }

        return repositoryFormsPanel.getSelectedRepository();
    }

    public boolean isValidData() {
        return isValidData;
    }

    private static String getDefaultLabelText() {
        return getText("LBL_SelectBugtrackingRepository");              //NOI18N
    }

    private static void addInsetsToPanel(JComponent comp) {
        LayoutStyle layoutStyle = LayoutStyle.getInstance();
        comp.setBorder(BorderFactory.createEmptyBorder(
                layoutStyle.getContainerGap(comp, NORTH, null),
                layoutStyle.getContainerGap(comp, WEST,  null),
                layoutStyle.getContainerGap(comp, SOUTH, null),
                layoutStyle.getContainerGap(comp, EAST,  null)));
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            itemSelected(e.getItem());
        }
    }

    private void itemSelected(Object selectedItem) {
        if (selectedItem instanceof NewRepositoryInfo) {
            NewRepositoryInfo newRepoInfo = (NewRepositoryInfo) selectedItem;
            displayRepositoryForm(newRepoInfo);
        } else {
            assert selectedItem instanceof RepositoryImpl;
            if (displayFormForExistingRepo) {
                RepositoryImpl repository = (RepositoryImpl) selectedItem;
                displayRepositoryForm(repository);
            } else {
                displayEmptyPanel();
            }
        }

        fireSelectionChanged();
    }

    private void setDataValid(boolean valid) {
        boolean wasValid = isValidData;
        isValidData = valid;

        if (isValidData != wasValid) {
            fireDataValidityChanged();
        }
    }

    private void displayEmptyPanel() {
        if (!emptyPanelInitialized) {
            cardsPanel.add(new JPanel(), EMPTY_PANEL);
            emptyPanelInitialized = true;
        }
        ((CardLayout) cardsPanel.getLayout()).show(cardsPanel, EMPTY_PANEL);

        if (repositoryFormVisible) {
            repositoryFormsPanel.removeChangeListener(repositoryFormPanelListener);
            repositoryFormVisible = false;
        }
        setDataValid(true);
    }

    private void displayRepositoryForm(NewRepositoryInfo newRepoInfo) {
        if (newRepoInfo.repository == null) {
            newRepoInfo.initializeRepository();
        }

        displayRepositoryForm(newRepoInfo.repository);
    }

    public void displayRepository(RepositoryImpl repository) {
        boolean selectedInCombo;
        if (combo != null) {
            combo.setSelectedItem(repository);
            selectedInCombo = (combo.getSelectedItem() == repository);
        } else {
            selectedInCombo = false;
        }

        if (!selectedInCombo) {
            displayRepositoryForm(repository);
        }
    }

    public void displayRepositoryForm(RepositoryImpl repository) {
        displayRepositoryForm(repository, null);
    }

    public void displayRepositoryForm(RepositoryImpl repository, String initialErrMsg) {
        makeSureRepositoryFormsPanelExists();

        boolean wasRepositoryFormVisible = repositoryFormVisible;

        boolean firstUsed = repositoryFormsPanel.displayForm(repository, initialErrMsg);
        ((CardLayout) cardsPanel.getLayout()).show(cardsPanel, NEW_REPO_PANEL);

        if (!wasRepositoryFormVisible) {
            repositoryFormsPanel.addChangeListener(repositoryFormPanelListener);
            setDataValid(repositoryFormsPanel.isValidData());
            repositoryFormVisible = true;
        }

        if (firstUsed) {
            notifyResizeListeners();
        }
    }

    private void makeSureRepositoryFormsPanelExists() {
        if (repositoryFormsPanel != null) {
            return;
        }

        repositoryFormsPanel = new RepositoryFormPanel();
        if (initialErrorMessage != null) {
            repositoryFormsPanel.displayErrorMessage(initialErrorMessage);
        }
        if (repositoryFormPanelListener == null) {
            repositoryFormPanelListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    setDataValid(repositoryFormsPanel.isValidData());
                }
            };
        }

        cardsPanel.add(repositoryFormsPanel, NEW_REPO_PANEL);
    }

    private void fireDataValidityChanged() {
        if ((changeListeners != null) && !changeListeners.isEmpty()) {
            if (changeEvent == null) {
                changeEvent = new ChangeEvent(this);
            }
            for (ChangeListener l : changeListeners) {
                l.stateChanged(changeEvent);
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        if (changeListeners == null) {
            changeListeners = new ArrayList<ChangeListener>(4);
        }
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (changeListeners == null) {
            return;
        }
        changeListeners.remove(listener);
    }

    public void addItemListener(ItemListener listener) {
        if (selectionListeners == null) {
            selectionListeners = new ArrayList<ItemListener>(2);
        }
        selectionListeners.add(listener);
    }

    public void removeItemListener(ItemListener listener) {
        if (selectionListeners == null) {
            return;
        }
        selectionListeners.remove(listener);
    }

    public Object[] getSelectedObjects() {
        RepositoryImpl selectedRepo = getSelectedRepository();
        return (selectedRepo != null) ? new RepositoryImpl[] {selectedRepo}
                                      : null;
    }

    private void fireSelectionChanged() {
        if ((selectionListeners == null) || selectionListeners.isEmpty()) {
            return;
        }

        final ItemEvent event = new ItemEvent(this,
                                              ItemEvent.ITEM_STATE_CHANGED,
                                              getSelectedRepository(),
                                              ItemEvent.SELECTED);
        for (ItemListener l : selectionListeners) {
            l.itemStateChanged(event);
        }
    }

    public interface ResizeListener {
        void resizeMayBeNeeded();
    }

    public void addResizeListener(ResizeListener l) {
        if (resizeListeners == null) {
            resizeListeners = new ArrayList<ResizeListener>(4);
        }
        resizeListeners.add(l);
    }

    public void removeResizeListener(ResizeListener l) {
        if (resizeListeners == null) {
            return;
        }
        resizeListeners.remove(l);
    }

    private void notifyResizeListeners() {
        if ((resizeListeners != null) && !resizeListeners.isEmpty()) {
            for (ResizeListener l : resizeListeners) {
                l.resizeMayBeNeeded();
            }
        }
    }

    private static String getText(String msgKey) {
        return NbBundle.getMessage(RepositorySelectorBuilder.class, msgKey);
    }

    private Component createHorizontalStrut(JComponent parent, JComponent compA, JComponent compB, LayoutStyle.ComponentPlacement related) {
        return Box.createHorizontalStrut(getSpace(parent, compA, compB, related, HORIZONTAL));
    }

    private Component createVerticalStrut(JComponent parent, JComponent compA, JComponent compB, LayoutStyle.ComponentPlacement related) {
        return Box.createVerticalStrut(getSpace(parent, compA, compB, related, VERTICAL));
    }

    private int getSpace(JComponent parent, JComponent compA, JComponent compB, LayoutStyle.ComponentPlacement related, int horizontal) {
        return LayoutStyle.getInstance()
               .getPreferredGap(compA,
                                compB,
                                related,
                                (horizontal == HORIZONTAL) ? EAST : SOUTH,
                                parent);
    }

    private static Object[] joinArrays(Object[] array1, Object[] array2) {
        if ((array1 == null) || (array1.length == 0)) {
            return array2;
        }
        if ((array2 == null) || (array2.length == 0)) {
            return array1;
        }

        Object[] result = new Object[array1.length + array2.length];
        System.arraycopy(array1, 0, result,             0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    private static final class NewRepositoryInfo {
        private final DelegatingConnector connector;
        private RepositoryImpl repository;
        private NewRepositoryInfo(DelegatingConnector connector) {
            this.connector = connector;
        }
        RepositoryImpl initializeRepository() {
            assert repository == null;
            Repository repo = connector.createRepository();
            repository = APIAccessor.IMPL.getImpl(repo);
            return repository;
        }
    }

    private static final class ComboItemsRenderer implements ListCellRenderer {

        /*
         * Not extending the DefaultListCellRenderer due to JDK bug.
         * Doing so would break rendering of a combo-box in GTK L&F.
         */

        private final MessageFormat newConnectionFormat;
        private final ListCellRenderer defaultRenderer;

        private ComboItemsRenderer(ListCellRenderer defaultRenderer,
                                   String newConnectionFormatString) {
            this.defaultRenderer = defaultRenderer;
            newConnectionFormat = new MessageFormat(newConnectionFormatString);
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) 
        {
            String text;
            Image icon = null;
            if (value == null) {
                text = null;
            } else if (value instanceof RepositoryImpl) {
                RepositoryImpl repo = (RepositoryImpl) value;
                text = repo.getDisplayName();
                icon = repo.getIcon();
            } else if (value instanceof NewRepositoryInfo) {
                String connectorName = ((NewRepositoryInfo) value).connector
                                       .getDisplayName();
                text = newConnectionFormat.format(new Object[] {connectorName});
                icon = ((NewRepositoryInfo) value).connector.getIcon();
            } else {
                assert false;
                text = "???";                                           //NOI18N
            }
            Component r = defaultRenderer.getListCellRendererComponent(list,
                                                                         text,
                                                                         index,
                                                                         isSelected,
                                                                         cellHasFocus);
            if (r instanceof JLabel) {
                JLabel label = (JLabel) r;
                label.setIcon(new ImageIcon(icon));
            }
            return r;
        }

    }

}
