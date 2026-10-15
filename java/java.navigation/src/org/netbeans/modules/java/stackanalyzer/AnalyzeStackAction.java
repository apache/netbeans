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
package org.netbeans.modules.java.stackanalyzer;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Action which shows AnalyzeStack component.
 * @author Jan Becicka
 */
public class AnalyzeStackAction extends AbstractAction {

    public AnalyzeStackAction() {
        super(NbBundle.getMessage(AnalyzeStackAction.class, "CTL_AnalyzeStackAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(AnalyzeStackTopComponent.ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = AnalyzeStackTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}
