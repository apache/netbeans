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
import javax.swing.JToggleButton;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.ContextView;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.TextDetail;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jhavlin
 */
public class BasicSearchResultsPanel extends BasicAbstractResultsPanel {

    @StaticResource
    private static final String PREVIEW_ICON =
            "org/netbeans/modules/search/res/preview.png";         //NOI18N

    private final RequestProcessor.Task dividerSaveTask;
    private final JComponent rightComponent;
    private final JSplitPane splitPane;
    private JToggleButton showPreviewButton;

    public BasicSearchResultsPanel(ResultModel resultModel, BasicComposition composition, boolean details, Node infoNode) {
        this(resultModel, composition, details, 
                new ResultsOutlineSupport(false, details, resultModel, composition, infoNode));
    }

    BasicSearchResultsPanel(ResultModel resultModel, BasicComposition composition, boolean details, ResultsOutlineSupport resultsOutlineSupport) {
        super(resultModel, composition, details, resultsOutlineSupport);

        rightComponent = createRightComponent();

        splitPane = new JSplitPane();
        splitPane.setLeftComponent(createLeftComponent());
        getContentPanel().add(splitPane);

        // divider persistance
        dividerSaveTask = RequestProcessor.getDefault().create(() -> {
            if(splitPane.getRightComponent() != null) {
                FindDialogMemory.getDefault().setReplaceResultsDivider(splitPane.getDividerLocation());
            }
        });

        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e -> {
            dividerSaveTask.schedule(1000);
        });

        updatePreview();
    }

    private void updatePreview() {
        int location = FindDialogMemory.getDefault().getReplaceResultsDivider();
        FindDialogMemory.getDefault().setShowPreview(getClass().getName(), showPreviewButton.isSelected());
        if (showPreviewButton.isSelected()) {
            splitPane.setRightComponent(rightComponent);
            splitPane.setDividerLocation(Math.max(location, 250));
        } else {
            splitPane.setRightComponent(null);
        }
    }

    JComponent createLeftComponent() {
        return resultsOutlineSupport.getOutlineView();
    }

    JComponent createRightComponent() {
        return new ContextView(resultModel, getExplorerManager());
    }

    @Override
    protected void onDetailShift(Node next) {
        TextDetail textDetail = next.getLookup().lookup(
                TextDetail.class);
        if (textDetail != null ) {
            textDetail.showDetail(TextDetail.DH_SHOW);
        }
    }

    @Override
    protected void initButtons() {
        super.initButtons();
        showPreviewButton = new JToggleButton();
        showPreviewButton.setEnabled(true);
        showPreviewButton.setIcon(ImageUtilities.loadImageIcon(PREVIEW_ICON, true));
        showPreviewButton.setToolTipText(UiUtils.getText("TEXT_BUTTON_SHOW_PREVIEW")); //NOI18N
        showPreviewButton.setSelected(FindDialogMemory.getDefault().isShowPreview(getClass().getName()));
        showPreviewButton.addActionListener(ev -> updatePreview());
        addButton(showPreviewButton);
    }

}
