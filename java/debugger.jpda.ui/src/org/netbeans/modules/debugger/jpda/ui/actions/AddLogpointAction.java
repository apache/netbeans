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
package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Debug",
        id = "org.netbeans.modules.debugger.jpda.ui.actions.AddLogpointAction"
)
@ActionRegistration(
        displayName = "#CTL_AddLogpointAction",
        enabledOn = @ActionState( property = "hasJavaSource", type = Action.class )
)
@ActionReferences({
    @ActionReference(path = "Editors/text/x-java/Popup", position = 1900),
    @ActionReference(path = "Editors/AnnotationTypes/BreakpointActionsWithToggleNew", position = 400)
})
@Messages({
        "CTL_AddLogpointAction=Add &Logpoint",
        "# {0} - clazz name",
        "# {1} - method name",
        "# {2} - line number",
        "# {3} - suffix",
        "MSG_LogpointAt=Logpoint at {0}.{1}:{2} {3}"
})
public final class AddLogpointAction extends AbstractAction {
    private final DataObject context;

    public AddLogpointAction(DataObject context) {
        this.context = context;
    }

    @Override
    public Object getValue(String key) {
        if ("hasJavaSource".equals(key)) {
            String mime = context.getPrimaryFile().getMIMEType("text/x-java");
            return "text/x-java".equals(mime);
        }
        return super.getValue(key);
    }


    @Override
    public void actionPerformed(ActionEvent ev) {
        String where = EditorContextBridge.getContext().getCurrentURL();
        int at = EditorContextBridge.getContext().getCurrentLineNumber();

        LineBreakpoint lb = LineBreakpoint.create(where, at);
        lb.setSuspend(JPDABreakpoint.SUSPEND_NONE);
        lb.setPrintText(Bundle.MSG_LogpointAt("{className}", "{methodName}", "{lineNumber}", "{locals}"));

        DebuggerManager.getDebuggerManager().addBreakpoint(lb);
    }
}
