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
package org.netbeans.modules.java.hints;

import java.io.IOException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.hints.infrastructure.HintAction;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertAnonymousToInnerAction extends HintAction {

    public ConvertAnonymousToInnerAction() {
        putValue(NAME, NbBundle.getMessage(ConvertAnonymousToInnerAction.class, "CTL_ConvertAnonymousToInner"));
    }

    protected void perform(JavaSource js, JTextComponent pane, final int[] selection) {
        final Fix[] f = new Fix[1];
        String error = null;
        
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    
                    f[0] = ConvertAnonymousToInner.computeFix(parameter, selection[0], selection[1], false);
                }
            }, true);

            if (f[0] == null) {
                error = selection[0] == selection[1] ? "ERR_CaretNotInAnonymousInnerclass" : "ERR_SelectionNotSupported";
            }
        } catch (IOException e) {
            error = "ERR_SelectionNotSupported";
            Exceptions.printStackTrace(e);
        }
        
        if (f[0] != null) {
            try {
                f[0].implement();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            
            return ;
        }
        
        if (error != null) {
            String errorText = NbBundle.getMessage(ConvertAnonymousToInnerAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    @Override
    protected boolean requiresSelection() {
        return false;
    }

}
