/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.nashorn.execution.actions;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.nashorn.execution.JSExecutor;
import static org.netbeans.modules.nashorn.execution.actions.ExecJSAction.isEnabledAction;
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
@ActionID(id = "org.netbeans.modules.nashorn.execution.actions.DebugJSAction", category = "Tools")
@ActionRegistration(displayName = "#DebugJSActionName", lazy = false, asynchronous = true)
@ActionReferences ({
    @ActionReference (path = "Loaders/text/javascript/Actions", position = 660),
    @ActionReference (path = "Editors/text/javascript/Popup", position = 5060)
})
@NbBundle.Messages("DebugJSActionName=Debug File")
public class DebugJSAction extends ExecJSAction {
    
    public DebugJSAction() {
        super(Bundle.DebugJSActionName());
    }
    
    private DebugJSAction(FileObject fo) {
        super(Bundle.DebugJSActionName(), fo, ActionProvider.COMMAND_DEBUG_SINGLE);
    }
    
    @Override
    protected void exec(JavaPlatform javaPlatform, FileObject js) throws IOException, UnsupportedOperationException {
        JSExecutor.run(javaPlatform, js, true);
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
        return new DebugJSAction(fo);
    }
    
}
