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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.docker.api.DockerEntity;

/**
 *
 * @author Petr Hejl
 */
public class CopyIdAction extends NodeAction {

    @NbBundle.Messages({
        "# {0} - copied ID",
        "MSG_StatusCopyToClipboard=Copy to Clipboard: {0}",
        "MSG_CouldNotCopy=Could not copy the ID"})
    @Override
    protected void performAction(Node[] activatedNodes) {
        assert activatedNodes.length == 1;
        DockerEntity idProvider = activatedNodes[0].getLookup().lookup(DockerEntity.class);
        if (idProvider != null) {
            Clipboard clipboard = getClipboard();
            if (clipboard != null) {
                try {
                    clipboard.setContents(new StringSelection(idProvider.getId()), null);
                    StatusDisplayer.getDefault().setStatusText(Bundle.MSG_StatusCopyToClipboard(idProvider.getId()));
                } catch (IllegalStateException ex) {
                    StatusDisplayer.getDefault().setStatusText(Bundle.MSG_CouldNotCopy());
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1
                && activatedNodes[0].getLookup().lookup(DockerEntity.class) != null;
    }

    @NbBundle.Messages("LBL_CopyIdAction=Copy ID")
    @Override
    public String getName() {
        return Bundle.LBL_CopyIdAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    private static Clipboard getClipboard() {
        Clipboard ret = Lookup.getDefault().lookup(Clipboard.class);
        if (ret == null) {
            ret = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return ret;
    }
}
