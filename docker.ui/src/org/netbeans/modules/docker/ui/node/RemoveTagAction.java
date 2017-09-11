/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerTag;
import org.netbeans.modules.docker.api.DockerAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Hejl
 */
public class RemoveTagAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(RemoveTagAction.class.getName());

    private final RequestProcessor requestProcessor = new RequestProcessor(RemoveTagAction.class);

    @NbBundle.Messages({
        "# {0} - image id",
        "MSG_RemovingTag=Removing image {0}"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        for (final Node node : activatedNodes) {
            final DockerTag tag = node.getLookup().lookup(DockerTag.class);
            if (tag != null) {
                final ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_RemovingTag(tag.getShortId()));
                handle.start();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DockerInstance instance = tag.getImage().getInstance();
                            DockerAction facade = new DockerAction(instance);
                            facade.remove(tag);
                        } catch (Exception ex) {
                            // FIXME offer force remove ?
                            LOGGER.log(Level.INFO, null, ex);
                            String msg = ex.getLocalizedMessage();
                            NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        } finally {
                            handle.finish();
                        }
                    }
                };
                requestProcessor.post(task);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(DockerTag.class) == null) {
                return false;
            }
        }
        return true;
    }

    @NbBundle.Messages("LBL_RemoveTagAction=Remove")
    @Override
    public String getName() {
        return Bundle.LBL_RemoveTagAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
