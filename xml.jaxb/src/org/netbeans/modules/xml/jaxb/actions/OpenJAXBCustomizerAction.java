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

package org.netbeans.modules.xml.jaxb.actions;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.xml.jaxb.cfg.schema.Binding;
import org.netbeans.modules.xml.jaxb.cfg.schema.Bindings;
import org.netbeans.modules.xml.jaxb.cfg.schema.Catalog;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSource;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardIterator;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardSchemaNode;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * @author lgao
 * @author gmpatil
 */

public class OpenJAXBCustomizerAction extends NodeAction  {
    
    private void populateSchemaBindingValues(WizardDescriptor wiz,
                                             Project prj,
                                             Schema schema){
        String name = ProjectUtils.getInformation(prj).getName();
        wiz.putProperty(JAXBWizModuleConstants.SCHEMA_NAME, schema.getName());
        wiz.putProperty(JAXBWizModuleConstants.PROJECT_NAME, name);
        wiz.putProperty(JAXBWizModuleConstants.PROJECT_DIR, 
                FileUtil.toFile(prj.getProjectDirectory()));
        wiz.putProperty(JAXBWizModuleConstants.PACKAGE_NAME, schema.getPackage());
        wiz.putProperty(JAXBWizModuleConstants.SCHEMA_TYPE, schema.getType());
        
        XjcOptions opts = schema.getXjcOptions();
        if (opts != null){
            int i = opts.sizeXjcOption();
            if (i > 0){
                Map<String, Boolean> options = new HashMap<String, Boolean>();                
                String key = null;
                String value = null;
                Boolean boolVal = null;
                for (int j =0; j < i; j++){
                    XjcOption xo = opts.getXjcOption(j);
                    key = xo.getName();
                    value = xo.getValue();
                    boolVal = Boolean.FALSE;
                    if ((value != null) 
                            && ("true".equals(value.toLowerCase()))){ //NOI18N
                        boolVal = Boolean.TRUE;
                    }
                    options.put(key, boolVal);
                }                
                wiz.putProperty(JAXBWizModuleConstants.XJC_OPTIONS, options);                
            }
        }
        
        SchemaSources sss = schema.getSchemaSources();
        SchemaSource ss = null;
        if (sss != null){
            int sssSize = sss.sizeSchemaSource();
            String origSrcLocType = null;
            if (sssSize > 0){
                List<String> xsdFileList = new ArrayList<String>();                            
                for (int i=0; i < sssSize; i++){
                    ss = sss.getSchemaSource(i);
                    xsdFileList.add(ss.getOrigLocation());
                    origSrcLocType = ss.getOrigLocationType();
                }
                
                wiz.putProperty(JAXBWizModuleConstants.XSD_FILE_LIST,
                        xsdFileList);                
                wiz.putProperty(JAXBWizModuleConstants.SOURCE_LOCATION_TYPE, 
                        origSrcLocType); 
            }
        }
        
        Bindings bindings = schema.getBindings();
        if (bindings != null){
            int numBindings = bindings.sizeBinding();
            if (numBindings > 0){
                List<String> bs = new ArrayList<String>();
                Binding binding = null;
                for (int i=0; i < numBindings;i++){
                    binding = bindings.getBinding(i);
                    bs.add(binding.getOrigLocation());
                }
                wiz.putProperty(JAXBWizModuleConstants.JAXB_BINDING_FILES, bs);
            }
        }
        
        Catalog cat = schema.getCatalog();
        if (cat != null){
            if (cat.getOrigLocation() != null){
                wiz.putProperty(JAXBWizModuleConstants.CATALOG_FILE, 
                        cat.getOrigLocation());
            }
        }
    }
    
    protected void performAction(Node[] activatedNodes) {
        JAXBWizardSchemaNode schemaNode = null;
        Project project = null;
        Schema schema = null;
        
        if (activatedNodes.length == 1){
            final Node theNode = activatedNodes[0];            
            schemaNode = theNode.getLookup().lookup(
                    JAXBWizardSchemaNode.class );
            project = schemaNode.getProject();
            schema = schemaNode.getSchema();

            if ( project != null ) {
                JAXBWizardIterator wizardIter = new JAXBWizardIterator(project);
                WizardDescriptor wd = new WizardDescriptor(
                        wizardIter );
                wd.putProperty(JAXBWizModuleConstants.WIZ_STYLE_AUTO, 
                        Boolean.TRUE);                
                wd.putProperty(
                        JAXBWizModuleConstants.WIZ_CONTENT_DISPLAYED,
                        Boolean.TRUE);
                wd.putProperty(
                        JAXBWizModuleConstants.WIZ_CONTENT_NUMBERED, 
                        Boolean.TRUE);  
                
                List<String> schemaNames = ProjectHelper.getSchemaNames(project);                
                if (schemaNames != null){
                    schemaNames.remove(schema.getName());
                }
                wd.putProperty(
                        JAXBWizModuleConstants.EXISTING_SCHEMA_NAMES,
                        schemaNames);
                boolean displayDlg = true;
                wizardIter.initialize(wd);
                populateSchemaBindingValues(wd, project, schema);
                wd.setTitleFormat(new MessageFormat("{0}"));
                
                DialogDisplayer dd = DialogDisplayer.getDefault();
                Dialog dlg = dd.createDialog(wd);
                dlg.setTitle(getDialogTitle()); 
                dlg.getAccessibleContext().setAccessibleDescription(
                        getDialogTitle());
                
                while (displayDlg) {
                    dlg.setVisible(true);                    
                    // Redisplay only if errors
                    displayDlg = false;
                    if ( wd.getValue() == WizardDescriptor.FINISH_OPTION ) {
                        try {
                            Schema nSchema = ProjectHelper.importResources(project, 
                                    wd, schema);

                            schemaNode.setSchema(nSchema);                        
                            ProjectHelper.changeSchemaInModel(project, schema, 
                                    nSchema);                        
                            ProjectHelper.cleanCompileXSDs(project, true);
                        } catch (Throwable ex) {
                            displayDlg = true;
                            //Exceptions.printStackTrace(ioe);
                            wd = new WizardDescriptor(wizardIter);
                            String msg = NbBundle.getMessage(JAXBWizardIterator.class, 
                                    "MSG_ErrorReadingSchema");//NOI18N
                            wd.putProperty(JAXBWizModuleConstants.WIZ_ERROR_MSG, msg); 
                            
                            wd.putProperty(JAXBWizModuleConstants.WIZ_STYLE_AUTO, 
                                    Boolean.TRUE);                
                            wd.putProperty(
                                    JAXBWizModuleConstants.WIZ_CONTENT_DISPLAYED,
                                    Boolean.TRUE);
                            wd.putProperty(
                                    JAXBWizModuleConstants.WIZ_CONTENT_NUMBERED, 
                                    Boolean.TRUE);  
                            wd.putProperty(JAXBWizModuleConstants.EXISTING_SCHEMA_NAMES, 
                                    schemaNames);
                            populateSchemaBindingValues(wd, project, schema);
                            wd.setTitleFormat(new MessageFormat("{0}"));                            
                            wizardIter.initialize(wd);  
                            
                            wd.setValid(false);
                            wd.setMessage(msg);
                            
                            dlg = dd.createDialog(wd);
                            dlg.setTitle(getDialogTitle()); 
                            dlg.getAccessibleContext().setAccessibleDescription(
                                    getDialogTitle());                            
                        }                        
                    }
                }
            }
        }
    }
    
    
    public String getName() {
        return NbBundle.getMessage(
                this.getClass(), "LBL_CustomizeJAXBOptions");//NOI18N
    }

    protected String getDialogTitle(){
        return NbBundle.getMessage(
                this.getClass(), "LBL_DialogTitleChangeBindingOptions");//NOI18N  
    }   
            
    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource()
        // javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if ( activatedNodes.length != 1 )
            return false;
        
        DataObject dataobj = activatedNodes[0].getCookie(DataObject.class);
        if ( dataobj != null ) {
            FileObject fo = dataobj.getPrimaryFile();
        }
        
        return true;
    }    
}
