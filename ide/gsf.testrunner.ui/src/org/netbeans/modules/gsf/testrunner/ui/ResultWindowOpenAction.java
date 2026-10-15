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

package org.netbeans.modules.gsf.testrunner.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * TODO: remove
 * Action which opens the Test Results window.
 *
 * @see  ResultWindow
 * @author  Marian Petras, Erno Mononen
 */
@ActionID(
        category = "Window",
        id = "org.netbeans.modules.gsf.testrunner.ui.ResultWindowOpenAction"
)
@ActionRegistration(
        iconBase = "org/netbeans/modules/gsf/testrunner/ui/resources/testResults.png",
        displayName = "#ResultWindowOpenAction.MenuName"
)
@ActionReferences({
    @ActionReference(path = "Menu/Window/Tools", position = 250),
    @ActionReference(path = "Shortcuts", name = "AS-R")
})
@NbBundle.Messages({"ResultWindowOpenAction.MenuName=&Test Results"})
public final class ResultWindowOpenAction extends AbstractAction {
    
    /**
     * Opens and activates the JUnit Test Results window.
     *
     * @param  e  event that caused this action to be called
     */
    public void actionPerformed(ActionEvent e) {
        ResultWindow resultWindow = ResultWindow.getInstance();
        resultWindow.open();
        resultWindow.requestActive();
    }
    
}
