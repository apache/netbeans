/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http:www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.myorg.systemproperties;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


import org.openide.util.datatransfer.NewType;


/**
 *
 * @author Administrator
 */
public class AllPropsNode extends AbstractNode {
    private static ResourceBundle bundle = NbBundle.getBundle(AllPropsNode.class);
    public AllPropsNode() {
        super(new AllPropsChildren());
        setIconBase("org/myorg/systemproperties/allPropsIcon");
        setName("AllPropsNode");
        setDisplayName(bundle.getString("LBL_AllPropsNode"));
        setShortDescription(bundle.getString("HINT_AllPropsNode"));
    }
    public Action[] getActions(boolean context) {
        Action[] result = new Action[] {
            SystemAction.get(RefreshPropsAction.class),
                    null,
                    SystemAction.get(OpenLocalExplorerAction.class),
                    null,
                    SystemAction.get(NewAction.class),
                    null,
                    SystemAction.get(ToolsAction.class),
                    SystemAction.get(PropertiesAction.class),
        };
        return result;
    }
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.myorg.systemproperties");
    }
    public Node cloneNode() {
        return new AllPropsNode();
    }
    public NewType[] getNewTypes() {
        return new NewType[] { new NewType() {
            public String getName() {
                return bundle.getString("LBL_NewProp");
            }
            public HelpCtx getHelpCtx() {
                return new HelpCtx("org.myorg.systemproperties");
            }
            public void create() throws IOException {
                String title = bundle.getString("LBL_NewProp_dialog");
                String msg = bundle.getString("MSG_NewProp_dialog_key");
                NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(msg, title);
                DialogDisplayer.getDefault().notify(desc);
                String key = desc.getInputText();
                if ("".equals(key)) return;
                msg = bundle.getString("MSG_NewProp_dialog_value");
                desc = new NotifyDescriptor.InputLine(msg, title);
                DialogDisplayer.getDefault().notify(desc);
                String value = desc.getInputText();
                System.setProperty(key, value);
                PropertiesNotifier.changed();
            }
        } };
    }
}
