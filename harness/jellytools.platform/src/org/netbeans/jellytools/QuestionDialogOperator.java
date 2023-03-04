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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

/** Class implementing all necessary methods for handling "Question" dialog.
 *
 * @author Jiri.Kovalsky@sun.com
 * @author Jiri.Skrivanek@sun.com
 */
public class QuestionDialogOperator extends NbDialogOperator {

    /** instance of JLabelOperator of question */
    private JLabelOperator _lblQuestion;


    /** Waits until dialog with "Question" title is found.
     * If dialog is not found, runtime exception is thrown.
     */
    public QuestionDialogOperator() {
        super(Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title"));
    }
    
    /** Waits until dialog with "Question" title and given text is found.
     * If dialog is not found, runtime exception is thrown.
     * @param questionLabelText text to be compared to text dialog
     */
    public QuestionDialogOperator(String questionLabelText) {
        this();
        _lblQuestion = new JLabelOperator(this, questionLabelText);
    }
    
    /** Returns operator of question's label.
     * @return JLabelOperator instance of question's label
     */
    public JLabelOperator lblQuestion() {
        if(_lblQuestion == null) {
            _lblQuestion = new JLabelOperator(this);
        }
        return _lblQuestion;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblQuestion();
    }
}
