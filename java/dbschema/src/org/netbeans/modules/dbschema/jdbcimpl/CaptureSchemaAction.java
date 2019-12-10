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
                                wizard.setTargetFolder(n[nId].getCookie(DataFolder.class));
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
