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
package org.netbeans.modules.search.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.ContextView;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.modules.search.Manager;
import org.netbeans.modules.search.ReplaceTask;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.ResultView;
import org.openide.awt.Mnemonics;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jhavlin
 */
public class BasicReplaceResultsPanel extends BasicAbstractResultsPanel {

    private static final RequestProcessor RP =
            new RequestProcessor(BasicReplaceResultsPanel.class.getName());
    private final RequestProcessor.Task SAVE_TASK = RP.create(new SaveTask());
    private JButton replaceButton;
    private JSplitPane splitPane;

    public BasicReplaceResultsPanel(ResultModel resultModel,
            BasicComposition composition, Node infoNode) {
        super(resultModel, composition, true,
                new ResultsOutlineSupport(true, true, resultModel, composition,
                infoNode));
        init();
    }

    private void init() {
        JPanel leftPanel = new JPanel();
        replaceButton = new JButton();
        replaceButton.addActionListener((ActionEvent e) -> replace());
        updateReplaceButton();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 1));
        buttonPanel.add(replaceButton);
        replaceButton.setMaximumSize(replaceButton.getPreferredSize());
        buttonPanel.setMaximumSize(new Dimension( // #225246
                (int) buttonPanel.getMaximumSize().getWidth(),
                (int) buttonPanel.getPreferredSize().getHeight()));
        leftPanel.add(resultsOutlineSupport.getOutlineView());
        leftPanel.add(buttonPanel);

        this.splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(new ContextView(resultModel,
                getExplorerManager()));
        initSplitDividerLocationHandling();

        getContentPanel().add(splitPane);
        initResultModelListener();
        replaceButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResultView.class,
                "ACS_TEXT_BUTTON_REPLACE"));                            //NOI18N
    }

    private void replace() {
        ReplaceTask taskReplace =
                new ReplaceTask(resultModel.getMatchingObjects(), this);
        resultsOutlineSupport.clean();
        replaceButton.setEnabled(false);

        Manager.getInstance().scheduleReplaceTask(taskReplace);
    }

    private void initResultModelListener() {
        resultModel.addPropertyChangeListener(new ModelListener());
    }

    private void initSplitDividerLocationHandling() {
        int location = FindDialogMemory.getDefault().getReplaceResultsDivider();
        if (location > 0) {
            splitPane.setDividerLocation(location);
        }
        splitPane.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            String pn = evt.getPropertyName();
            if (pn.equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                SAVE_TASK.schedule(1000);
            }
        });
    }

    @Override
    public void searchFinished() {
        super.searchFinished(); 
        if (resultModel.isValid()) {
            updateReplaceButton();
        }
        if (replaceButton.isVisible() && replaceButton.isEnabled()){
            replaceButton.requestFocusInWindow();
        }
    }

    private class ModelListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String pn = evt.getPropertyName();
            if (ResultModel.PROP_VALID.equals(pn)
                    && Boolean.FALSE.equals(evt.getNewValue())) {
                replaceButton.setText(NbBundle.getMessage(ResultView.class,
                        "TEXT_BUTTON_REPLACE_INVALID"));                //NOI18N
                replaceButton.setEnabled(false);
            } else if (resultModel.isValid()) {
                if (ResultModel.PROP_VALID.equals(pn)
                        && Boolean.TRUE.equals(evt.getNewValue())) {
                    setFinalRootNodeText();
                }
                updateReplaceButton();
            }
        }
    }

    private void updateReplaceButton() {
        int matches = resultModel.getSelectedMatchesCount();
        Mnemonics.setLocalizedText(replaceButton, NbBundle.getMessage(ResultView.class, "TEXT_BUTTON_REPLACE", matches));//NOI18N
        replaceButton.setEnabled(matches > 0 && isFinished());
    }

    /**
     */
    public void displayIssuesToUser(final ReplaceTask task, final String title,
            final String[] problems, final boolean reqAtt) {

        Mutex.EVENT.writeAccess(() -> {
            IssuesPanel issuesPanel = new IssuesPanel(title, problems);
            if (isMacLaf) {
                issuesPanel.setBackground(macBackground);
            }
            displayIssues(issuesPanel);
            if (!ResultView.getInstance().isOpened()) {
                ResultView.getInstance().open();
            }
            if (reqAtt) {
                ResultView.getInstance().requestAttention(true);
            }
        });
    }

    void displayIssues(IssuesPanel issuesPanel) {
        if (issuesPanel != null) {
            showRefreshButton();
            removeButtons(btnNext, btnPrev, btnFlatView, btnTreeView,
                    btnExpand, showDetailsButton);
            Container p = getContentPanel();
            p.removeAll();
            p.add(issuesPanel);
            validate();
            repaint();
        }
    }

    private void removeButtons(AbstractButton... abstractButtons) {
        for (AbstractButton ab : abstractButtons) {
            if (ab != null) {
                Container c = ab.getParent();
                c.remove(ab);
            }
        }
    }

    public void rescan() {
        BasicComposition bc = new BasicComposition(composition.getSearchInfo(),
                composition.getMatcher(), composition.getBasicSearchCriteria(),
                composition.getScopeDisplayName());
        Manager.getInstance().scheduleSearchTask(bc, true);
    }

    public void showFinishedInfo() {
        final AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension(
                "org/netbeans/modules/search/res/info.png");            //NOI18N
        an.setDisplayName(NbBundle.getMessage(ResultView.class,
                "TEXT_INFO_REPLACE_FINISHED", //NOI18N
                resultModel.getSelectedMatchesCount()));
        Mutex.EVENT.writeAccess(() -> {
            getOutlineView().getOutline().setRootVisible(true);
            getExplorerManager().setRootContext(an);
            getOutlineView().validate();
            getOutlineView().repaint();
            btnNext.setEnabled(false);
            btnPrev.setEnabled(false);
            btnTreeView.setEnabled(false);
            btnFlatView.setEnabled(false);
            btnExpand.setEnabled(false);
        });
    }

    private class SaveTask implements Runnable {

        @Override
        public void run() {
            if (splitPane != null) {
                FindDialogMemory.getDefault().setReplaceResultsDivider(
                        splitPane.getDividerLocation());
            }
        }
    }
}
