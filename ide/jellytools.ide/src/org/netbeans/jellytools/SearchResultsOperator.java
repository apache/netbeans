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
package org.netbeans.jellytools;

import java.awt.Container;
import javax.swing.JButton;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Provides access to the "Search Results" view. <p> Usage:<br>
 * <pre>
 *      SearchResultsOperator sro = new SearchResultsOperator();
 *      sro.openResult("MyClass|myMethod");
 * </pre><p> Timeouts used:<br> SearchResultsOperator.SearchTime - maximum
 * time for search to be performed.
 */
public class SearchResultsOperator extends TopComponentOperator {

    private static final long SEARCH_TIME = 600000;
    private static final String TITLE = Bundle.getString("org.netbeans.modules.search.Bundle",
            "TITLE_SEARCH_RESULTS");
    private JButtonOperator _btStop;
    private JButtonOperator _btShowDetails;
    private JButtonOperator _btModifySearch;
    private OutlineOperator _outlineResult;

    /**
     * Waits for view opened.
     */
    public SearchResultsOperator() {
        // "Search Results"
        super(TITLE);
    }

    static {
        Timeouts.initDefault("SearchResultsOperator.SearchTime", SEARCH_TIME);
    }

    //component access    
    /**
     * returns operator for "Stop Search" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btStopSearch() {
        if (_btStop == null) {
            _btStop = new JButtonOperator(
                    (JButton) JButtonOperator.waitJComponent((Container) this.getSource(),
                    Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_STOP"),
                    true,
                    true));
        }
        return _btStop;
    }

    /**
     * Returns operator for "Show Details" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btShowDetails() {
        if (_btShowDetails == null) {
            _btShowDetails = new JButtonOperator(
                    (JButton) JButtonOperator.waitJComponent((Container) this.getSource(),
                    Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_FILL"),
                    true,
                    true));
        }
        return _btShowDetails;
    }

    /**
     * Returns operator for "Modify Search" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btModifySearch() {
        if (_btModifySearch == null) {
            _btModifySearch = new JButtonOperator(
                    (JButton) JButtonOperator.waitJComponent((Container) this.getSource(),
                    Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_CUSTOMIZE"),
                    true,
                    true));
        }
        return _btModifySearch;
    }

    /**
     * Returns operator for search outline.
     *
     * @return OutlineOperator instance
     */
    public OutlineOperator outlineResult() {
        if (_outlineResult == null) {
            _outlineResult = new OutlineOperator(this);
        }
        return _outlineResult;
    }

    /**
     * Selects a path in the results tree
     *
     * @param path path to requested result (e.g. "MyClass|myMethod")
     */
    public void selectResult(String path) {
        new OutlineNode(outlineResult(), "Found|" + path).select();
    }

    /**
     * Double clicks on the specified path in the results tree. It opens file in
     * editor.
     *
     * @param path path to requested result (e.g. "MyClass|myMethod")
     */
    public void openResult(String path) {
        TreePath treePath = new OutlineNode(outlineResult(), "Found|" + path).getTreePath();
        outlineResult().clickOnCell(outlineResult().getRowForPath(treePath), 0, 2);
    }

    /**
     * Pushes "Stop Search" button.
     */
    public void stopSearch() {
        btStopSearch().push();
    }

    /**
     * Pushes "Show Details" button and returns {@link OutputTabOperator} from output window.
     * @return OutputTabOperator instance
     */
    public OutputTabOperator showDetails() {
        btShowDetails().push();
        return new OutputTabOperator(TITLE);
    }

    /**
     * Pushes "Modify Search" button. and return {@link FindInFilesOperator}
     * @return FindInFilesOperator instance
     */
    public FindInFilesOperator modifySearch() {
        btModifySearch().pushNoBlock();
        return new FindInFilesOperator();
    }

    /**
     * Waits until search is finished.
     */
    public void waitEndOfSearch() {
        // wait here because there is no other way how to detect start of searching
        new EventTool().waitNoEvent(300);
        // wait until Stop button is replaced by Modify Criteria button
        JButtonOperator.waitJComponent(
                (Container) SearchResultsOperator.this.getSource(),
                "Modify Criteria",
                true,
                true);
    }

    /** Performs verification by accessing all sub-components */
    public void verify() {
        btShowDetails();
        btModifySearch();
        outlineResult();
    }
}
