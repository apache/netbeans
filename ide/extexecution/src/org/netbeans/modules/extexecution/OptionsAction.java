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

package org.netbeans.modules.extexecution;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.spi.extexecution.open.OptionOpenHandler;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class OptionsAction extends AbstractAction {

    private final OptionOpenHandler handler;

    private final String optionsPath;

    public OptionsAction(OptionOpenHandler handler, String optionsPath) {
        setEnabled(true); // just to be sure
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/extexecution/resources/options.png", false)); // NOI18N
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(OptionsAction.class, "Options"));

        assert handler != null : "Null handler";
        this.handler = handler;
        this.optionsPath = optionsPath;
    }

    public void actionPerformed(ActionEvent e) {
        handler.open(optionsPath);
    }

    public String getOptionsPath() {
        return optionsPath;
    }

}
