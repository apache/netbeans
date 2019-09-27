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
package org.netbeans.modules.docker.ui.output;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class TerminalOptionsAction extends AbstractAction {

    @NbBundle.Messages(value = "LBL_TerminalOptions=Terminal Options")
    public TerminalOptionsAction() {
        setEnabled(true);
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/docker/ui/resources/terminal_options.png", false)); // NOI18N
        putValue(Action.SHORT_DESCRIPTION, Bundle.LBL_TerminalOptions());
    } // NOI18N

    @Override
    public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open("Advanced/TermAdvancedOption"); //NOI18N
    }

}
