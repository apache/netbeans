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
package org.netbeans.modules.groovy.grailsproject.actions;

import static org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage.ACTIONS;
import static org.netbeans.modules.groovy.grailsproject.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@Messages("CTL_GenerateAllAction=Generate All")
@ActionID(id = "org.netbeans.modules.groovy.grailsproject.actions.GenerateAllAction", category = "Build")
@ActionRegistration(lazy = false, displayName = "#CTL_GenerateAllAction")
@ActionReference(path = ACTIONS, position = 20,separatorAfter=50)
public final class GenerateAllAction extends GenerateAction {

    public GenerateAllAction() {
        super("generate-all", CTL_GenerateAllAction());
    }

}

