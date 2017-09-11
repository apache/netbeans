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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
