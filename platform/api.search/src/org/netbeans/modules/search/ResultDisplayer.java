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

import javax.swing.JComponent;
import org.netbeans.modules.search.ui.BasicAbstractResultsPanel;
import org.netbeans.modules.search.ui.BasicReplaceResultsPanel;
import org.netbeans.modules.search.ui.BasicSearchResultsPanel;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
class ResultDisplayer extends SearchResultsDisplayer<MatchingObject.Def> {

    private final ResultModel resultModel;
    private final BasicSearchCriteria criteria;
    private final BasicComposition composition;
    private BasicAbstractResultsPanel resultPanel;
    private Node infoNode;

    public ResultDisplayer(BasicSearchCriteria criteria,
            BasicComposition composition) {
        this.criteria = criteria;
        this.resultModel = new ResultModel(criteria,
                criteria.getReplaceString(), composition);
        this.composition = composition;
    }

    @Override
    public synchronized JComponent getVisualComponent() {
        if (resultPanel != null) {
            return resultPanel;
        }
        if (criteria.isSearchAndReplace()) {
            resultPanel = new BasicReplaceResultsPanel(resultModel, composition,
                    infoNode);
        } else {
            resultPanel = new BasicSearchResultsPanel(resultModel, composition,
                    criteria.isFullText(), infoNode);
        }
        resultPanel.setToolTipText(composition.getScopeDisplayName()
                + ": " + getTitle());                                   //NOI18N
        return resultPanel;
    }

    @Override
    public void addMatchingObject(MatchingObject.Def object) {
        if (resultModel.objectFound(object.getFileObject(), object.getCharset(),
                object.getTextDetails())) {
            resultPanel.update();
            resultPanel.addMatchingObject(
                    resultModel.getMatchingObjects().get(
                    resultModel.size() - 1));
        }
        if (resultModel.wasLimitReached()) {
            composition.terminate();
        }
    }

    ResultModel getResultModel() {
        return resultModel;
    }

    @Override
    public String getTitle() {
        if (criteria.getTextPattern() == null) {
            if (criteria.getFileNamePattern() == null) {
                return NbBundle.getMessage(ResultView.class,
                        "TEXT_MSG_RESULTS_FOR_FILE_PATTERN"); //NOI18N
            } else {
                return criteria.getFileNamePatternExpr();
            }
        } else {
            return criteria.getTextPatternExpr();
        }
    }

    @Override
    public void searchStarted() {
        resultModel.setStartTime();
        resultPanel.searchStarted();
    }

    @Override
    public void searchFinished() {
        resultPanel.searchFinished();
    }

    @Override
    public void setInfoNode(Node infoNode) {
        this.infoNode = infoNode;
    }

    @Override
    public void closed() {
        resultPanel.closed();
    }
}
