/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.actions;

import java.io.Writer;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.windows.InputOutput;

/**
 * Implements Make action
 */
public class MakeAction extends MakeBaseAction {

    @Override
    public String getName () {
        return getString("BTN_Execute"); // NOI18N
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i], ""); // NOI18N
        }
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param node A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static void execute(Node node) {
        (SystemAction.get(MakeAction.class)).performAction(node, ""); // NOI18N
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param node A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static void execute(Node node, String target) {
        (SystemAction.get(MakeAction.class)).performAction(node, target);
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param node A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static Future<Integer> execute(Node node, String target, ExecutionListener listener, Writer outputListener,
                               Project project, List<String> additionalEnvironment, InputOutput inputOutput) {
        return (SystemAction.get(MakeAction.class)).performAction(node, target, listener, outputListener, project, additionalEnvironment, inputOutput);
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/resources/MakeAction.gif"; // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MakeAction.class); // FIXUP ???
    }
}
