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
package org.netbeans.test.modules.search;

import java.awt.Component;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;

/**
 * This Operator operates Search Results Window
 * @author Max Sauer
 */
public class SearchResultsOperator extends TopComponentOperator {
    
//    private static Action invokeAction;// = new SearchResultsViewAction();
    
    /**
     * Creates a new instance of SearchResultsOperator
     */
    public SearchResultsOperator() {
        /* In IDE ResultWindow top component is singleton but in sense of
         * jellytools, it is not singleton. It can be closed/hidden and
         * again opened/shown, so it make sense to wait for OutputWindow
         * top component again.
         */
        super(waitTopComponent(null, null, 0, resultsSubchooser));
    }
    
    /**
     *
     * Opens JUnit Test Results from main menu Window|Search Results and
     * returns SearchResultsOperator.
     *
     * @return instance of ResultsWindowOperatorOperator
     */
    public static SearchResultsOperator invoke() {
//        invokeAction.perform();
        return new SearchResultsOperator();
    }
    
    /**
     * SubChooser to determine ResultsWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser resultsSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ResultView"); //NOI18N
        }
        public String getDescription() {
            return "component instanceof org.netbeans.modules.search.ResultView";// NOI18N
        }
    };
}
