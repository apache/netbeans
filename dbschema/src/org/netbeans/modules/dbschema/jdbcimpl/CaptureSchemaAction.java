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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.util.ResourceBundle;

import org.openide.WizardDescriptor;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class CaptureSchemaAction extends CallableSystemAction {

    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    /** Create. new ObjectViewAction. */
    public CaptureSchemaAction() {
    }

    /** Name of the action. */
    public String getName () {
        return bundle.getString("ActionName"); //NOI18N
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }

    protected String iconResource () {
        return "org/netbeans/modules/dbschema/jdbcimpl/DBschemaDataIcon.gif"; //NOI18N
    }

    public  void performAction() {
        try {
            TemplateWizard wizard = new TemplateWizard();
            
            DataObject templateDirs[] = wizard.getTemplatesFolder().getChildren();
            for (int i = 0; i < templateDirs.length; i++)
                if (templateDirs[i].getName().equals("Databases")) { //NOI18N
                    DataObject templates[] = ((DataFolder) templateDirs[i]).getChildren();
                    for (int j = 0; j < templates.length; j++)
                        if (templates[j].getName().equals("Database Schema")) { //NOI18N
                            Node n[] = WindowManager.getDefault().getRegistry().getActivatedNodes();
                            int nId = -1;
                            for (int k = 0; k < n.length; k++)
                                if (n[k].getCookie(DataFolder.class) instanceof DataFolder) {
                                    nId = k;
                                    break;
                                }
                            
                            wizard.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); //NOI18N
                            wizard.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); //NOI18N
                            wizard.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); //NOI18N
                            String[] prop = (String[]) wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
                            String[] stepsNames = new String[] {
                                wizard.targetChooser().getClass().toString().trim().equalsIgnoreCase("class org.openide.loaders.TemplateWizard2") ? bundle.getString("TargetLocation") :
                                    prop[0],
                                    bundle.getString("TargetLocation"),
                                    bundle.getString("ConnectionChooser"),
                                    bundle.getString("TablesChooser")
                            };
                            wizard.putProperty(WizardDescriptor.PROP_CONTENT_DATA, stepsNames); //NOI18N
                            wizard.setTitle(bundle.getString("WizardTitleName"));
                            
                            if(nId >= 0) {
                                wizard.setTargetFolder((DataFolder) n[nId].getCookie(DataFolder.class));
                                wizard.instantiate(templates[j]);
                            } else
                                wizard.instantiate(templates[j]);
                            
                            break;
                        }
                    break;
                }
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }
}
