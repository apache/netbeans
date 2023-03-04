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

package org.netbeans.modules.nashorn.execution.actions;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.nashorn.execution.JSExecutor;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin
 */
@ActionID(id = "org.netbeans.modules.nashorn.execution.actions.RunJSAction", category = "Tools")
@ActionRegistration(displayName = "#RunJSActionName", lazy = false, asynchronous = true)
@ActionReferences ({
    @ActionReference (path = "Loaders/text/javascript/Actions", position = 650, separatorBefore = 630, separatorAfter = 670),
    @ActionReference (path = "Editors/text/javascript/Popup", position = 5050, separatorBefore = 5030, separatorAfter = 5070)
})
@NbBundle.Messages("RunJSActionName=Run File")
public class RunJSAction extends ExecJSAction {
    
    public RunJSAction() {
        super(Bundle.RunJSActionName());
    }
    
    private RunJSAction(FileObject fo) {
        super(Bundle.RunJSActionName(), fo, ActionProvider.COMMAND_RUN_SINGLE);
    }
    
    @Override
    protected void exec(JavaPlatform javaPlatform, FileObject js) throws IOException, UnsupportedOperationException {
        JSExecutor.run(javaPlatform, js, false);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (!isEnabled()) {
            return NO_ACTION;
        }
        FileObject fo = actionContext.lookup(FileObject.class);
        if (fo == null) {
            return NO_ACTION;
        }
        if (isEnabledAction(ActionProvider.COMMAND_RUN_SINGLE, fo, actionContext)) {
            // There's a project's run action already.
            return NO_ACTION;
        }
        return new RunJSAction(fo);
    }
    
}
