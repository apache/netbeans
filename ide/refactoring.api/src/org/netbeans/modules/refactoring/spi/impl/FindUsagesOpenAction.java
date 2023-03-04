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


package org.netbeans.modules.refactoring.spi.impl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 * Action which opens the Find Usages window.
 *
 * @author Jan Becicka
 */
public class FindUsagesOpenAction extends AbstractAction {

    /**
     * Creates an instance of this action.
     */
    public FindUsagesOpenAction() {
        String name = NbBundle.getMessage(
                FindUsagesOpenAction.class,
                "LBL_UsagesWindow");                          //NOI18N
        putValue(NAME, name);
        putValue("iconBase", "org/netbeans/modules/refactoring/api/resources/findusages.png"); // NOI18N
    }
    
    /**
     * Opens and activates the Find Usages window.
     *
     * @param  e  event that caused this action to be called
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        RefactoringPanelContainer resultView = RefactoringPanelContainer.getUsagesComponent();
        resultView.open();
        resultView.requestActive();
    }
}
