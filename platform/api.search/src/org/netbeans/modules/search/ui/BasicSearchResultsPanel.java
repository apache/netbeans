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
package org.netbeans.modules.search.ui;

import java.beans.PropertyChangeEvent;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.ContextView;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.modules.search.ResultModel;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jhavlin
 */
public class BasicSearchResultsPanel extends BasicAbstractResultsPanel {
    private final RequestProcessor.Task SAVE_TASK = RequestProcessor.getDefault().create(new BasicSearchResultsPanel.SaveTask());
    private JSplitPane splitPane;

    public BasicSearchResultsPanel(ResultModel resultModel,
            BasicComposition composition, boolean details, Node infoNode) {
        super(resultModel, composition, details,
                new ResultsOutlineSupport(false, details, resultModel,
                composition, infoNode));
        init();
    }

    private void init() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        leftPanel.add(resultsOutlineSupport.getOutlineView());

        this.splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(new ContextView(resultModel,
                getExplorerManager()));
        initSplitDividerLocationHandling();
        getContentPanel().add(splitPane);
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
