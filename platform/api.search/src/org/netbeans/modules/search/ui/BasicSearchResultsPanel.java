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

import javax.swing.JComponent;
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

    public BasicSearchResultsPanel(ResultModel resultModel, BasicComposition composition, boolean details, Node infoNode) {
        this(resultModel, composition, details, 
                new ResultsOutlineSupport(false, details, resultModel, composition, infoNode));
    }

    BasicSearchResultsPanel(ResultModel resultModel, BasicComposition composition, boolean details, ResultsOutlineSupport resultsOutlineSupport) {
        super(resultModel, composition, details, resultsOutlineSupport);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(createLeftComponent());
        splitPane.setRightComponent(createRightComponent());
        getContentPanel().add(splitPane);

        // divider persistance
        RequestProcessor.Task dividerSaveTask = RequestProcessor.getDefault().create(() ->
            FindDialogMemory.getDefault().setReplaceResultsDivider(splitPane.getDividerLocation())
        );
        int location = FindDialogMemory.getDefault().getReplaceResultsDivider();
        splitPane.setDividerLocation(Math.max(location, 250));
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e ->
            dividerSaveTask.schedule(1000)
        );
    }

    protected JComponent createLeftComponent() {
        return resultsOutlineSupport.getOutlineView();
    }

    protected JComponent createRightComponent() {
        return new ContextView(resultModel, getExplorerManager());
    }

}
