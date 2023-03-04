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

package org.netbeans.modules.gradle.test.ui.nodes;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ReRunTestAction extends AbstractAction {

    private final ActionProvider actionProvider;
    private final Lookup context;
    private final String command;

    public ReRunTestAction(ActionProvider actionProvider, Lookup context, String command, String name) {
        super(name);
        this.actionProvider = actionProvider;
        this.context = context;
        this.command = command;
        
        putValue(Action.ACTION_COMMAND_KEY, command);
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        actionProvider.invokeAction(command, context);
    }
    
}
