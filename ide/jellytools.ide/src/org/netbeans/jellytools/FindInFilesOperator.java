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

import org.netbeans.jellytools.actions.FindInFilesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Provides access to the Find in Files/Projects dialog.
 */
public class FindInFilesOperator extends NbDialogOperator {

    private static final FindInFilesAction invokeAction = new FindInFilesAction();
    private JButtonOperator _btFind;

    /**
     * Waits for dialog displayed.
     */
    public FindInFilesOperator() {
        super(Bundle.getString("org.netbeans.modules.search.Bundle", "LBL_FindInProjects"));
    }

    /**
     * Invokes dialog by selecting a node and pushing menu.
     */
    public static FindInFilesOperator invoke(Node node) {
        invokeAction.perform(node);
        return new FindInFilesOperator();
    }

    /**
     * Returns operator for a Find button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btFind() {
        if(_btFind == null) {
            _btFind = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "TEXT_BUTTON_SEARCH"));
        }
        return _btFind;
    }

    /**
     * Returns operator for the "Whole Words" check box.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbWholeWords() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "BasicSearchForm.chkWholeWords.text");
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Case Sensitive" check box.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbCase() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "BasicSearchForm.chkCaseSensitive.text");
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Containing Text:" text field.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtText() {
        return new JTextFieldOperator(this);
    }
    
    /**
     * Returns operator for the "File Name Patterns:" text field.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtPatterns() {
        return new JTextFieldOperator(this, 1);
    }

    /**
     * Pushes Find button.
     * @return "Search Result" window.
     */
    public SearchResultsOperator find() {
        btFind().push();
        SearchResultsOperator results = new SearchResultsOperator();
        results.waitEndOfSearch();
        return results;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btFind();
        btClose();
        btHelp();
        cbWholeWords();
        cbCase();
        txtText();
        txtPatterns();
    }
}
