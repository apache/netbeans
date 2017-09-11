/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
