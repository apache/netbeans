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
package org.netbeans.modules.xml.jaxb.ui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.openide.WizardValidationException;

import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * @author gmpatil
 */
public class JAXBWizBindingCfgPanel implements WizardDescriptor.Panel,
                                        WizardDescriptor.ValidatingPanel, 
                                        WizardDescriptor.FinishablePanel{
    public static final int MODE_WIZARD  = 0;
    public static final int MODE_EDITING = 1;
    private static final String WIZ_NEW_FILE_TITLE = "NewFileWizard_Title"; //NOI18N

    private WizardDescriptor wd = null;
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();  
    private JAXBBindingInfoPnl bindingInfoPnl = null;
    private List<String> existingSchemaNames = null;
    private File projDir = null;
    private Logger logger;
    
    public JAXBWizBindingCfgPanel() {
        logger = Logger.getLogger(this.getClass().getName());
        initUI();
    }
        
    private void initUI() {
        bindingInfoPnl = new JAXBBindingInfoPnl(this);
        bindingInfoPnl.setName(NbBundle.getMessage(this.getClass(), 
                "LBL_JAXBWizTitle")); //NOI18N        
    }
        
    public void removeChangeListener(ChangeListener cl) {
        this.listeners.remove( cl );
    }
    
    public void addChangeListener(ChangeListener cl) {
        this.listeners.add( cl );
    }

    public void validate() throws WizardValidationException {
        logger.log(Level.FINEST, "validate() called.");
    }

    private boolean isEmpty(String str){
        boolean ret = true;
        if ((str != null) && (!"".equals(str.trim()))){ //NOI18N
            ret = false;
        }
        return ret;
    }
    
    private void setError(String msg){
        this.wd.putProperty(JAXBWizModuleConstants.WIZ_ERROR_MSG, msg);  
    }
    
    private boolean isValid(StringBuffer sb){
        logger.log(Level.FINEST, "isValidate() called.");        
        boolean valid = true;
        
        if (isEmpty(this.bindingInfoPnl.getSchemaName())){
            sb.append(NbBundle.getMessage(this.getClass(),
                    "MSG_EnterSchemaName")); //NOI18N
            valid = false;
            setError(sb.toString());
            return valid;
        } else {
            String schemaName = this.bindingInfoPnl.getSchemaName();
            if ((this.existingSchemaNames != null) && 
                    (this.existingSchemaNames.contains(schemaName))){
                sb.append(NbBundle.getMessage(this.getClass(),
                        "MSG_SchemaNameExists")); //NOI18N
                valid = false;
                setError(sb.toString());
                return valid;                
            }
            
            // Do not allow characters (,.\\/;:)
            if ((schemaName.indexOf(",") > -1) ||           //NOI18N
                    (schemaName.indexOf(".") > -1)  ||      //NOI18N  
                    (schemaName.indexOf("\\") > -1) ||      //NOI18N
                    (schemaName.indexOf("/") > -1)  ||      //NOI18N
                    (schemaName.indexOf(";") > -1)  ||      //NOI18N
                    (schemaName.indexOf(":") > -1)  ){      //NOI18N  
                sb.append(NbBundle.getMessage(this.getClass(),
                        "MSG_InvalidCharInSchemaName")); //NOI18N
                valid = false;
                setError(sb.toString());
                return valid;                
            }
        }
        
        if ( isEmpty(this.bindingInfoPnl.getSchemaFile())
                && isEmpty(this.bindingInfoPnl.getSchemaURL())){
            sb.append(NbBundle.getMessage(this.getClass(), 
                    "MSG_EnterSchemaFileOrURL")); //NOI18N
            valid = false;
            setError(sb.toString());
            return valid;
        }else {
            // When from URL is selected check for empty "http://"
            if (this.bindingInfoPnl.isFromURLSelected()){
                String url = this.bindingInfoPnl.getSchemaURL();
                if (url != null){
                    url = url.trim();
                } else {
                    url = ""; //NOI18N
                }
                if (isEmpty(url) || "http://".equalsIgnoreCase(url)){ //NOI18N
                    sb.append(NbBundle.getMessage(this.getClass(), 
                                        "MSG_EnterSchemaFileOrURL")); //NOI18N
                    valid = false;
                    setError(sb.toString());
                    return valid;                    
                }
                //TODO check for valid URL
            }
            
            //check for valid file
            if (this.bindingInfoPnl.isFromFSSelected()){
                String schFl = this.bindingInfoPnl.getSchemaFile();
                if (schFl != null){
                    schFl = schFl.trim();
                } else {
                    schFl = ""; //NOI18N
                }
                
                if (isEmpty(schFl)){ 
                    sb.append(NbBundle.getMessage(this.getClass(), 
                                        "MSG_EnterSchemaFileOrURL")); //NOI18N
                    valid = false;
                    setError(sb.toString());
                    return valid;                    
                }
                               
                File file = null;
                if (schFl.startsWith("\\") || schFl.startsWith("/") || (schFl.indexOf(":") == 1)){ //NOI18N
                    file = new File(schFl); // abs path
                } else {
                    // starts with . or .. or any alpha numeric character 
                    file = new File(this.projDir, schFl); // relative path                    
                }
                
                if (schFl.startsWith(".") || schFl.startsWith("..")){ 

                } else {
                    
                }

                if (!file.exists()){
                    sb.append(NbBundle.getMessage(this.getClass(), 
                                    "MSG_enterValidSchemaFile")); //NOI18N
                    valid = false;
                    setError(sb.toString());
                    return valid;                    
                }

                if (!file.isFile()){
                    sb.append(NbBundle.getMessage(this.getClass(), 
                                    "MSG_enterValidSchemaFile")); //NOI18N
                    valid = false;
                    setError(sb.toString());
                    return valid;                    
                }                    
            }
            
        }
        
        if (!isEmpty(this.bindingInfoPnl.getPackageName())){
            String packageName = this.bindingInfoPnl.getPackageName();
            // Do not allow characters (,\\/;:)
            if ((packageName.indexOf(",") > -1) ||           //NOI18N
                    (packageName.indexOf("\\") > -1) ||      //NOI18N
                    (packageName.indexOf("/") > -1)  ||      //NOI18N
                    (packageName.indexOf(";") > -1)  ||      //NOI18N
                    (packageName.indexOf(":") > -1)  ){      //NOI18N  
                sb.append(NbBundle.getMessage(this.getClass(),
                        "MSG_InvalidCharInPackageName")); //NOI18N
                valid = false;
                setError(sb.toString());
                return valid;                
            }            
        }
        
        Map<String, Boolean> options = this.bindingInfoPnl.getOptions();
        if (Boolean.TRUE.equals(
                options.get(JAXBWizModuleConstants.JAXB_OPTION_QUIET)) && 
                Boolean.TRUE.equals(
                options.get(JAXBWizModuleConstants.JAXB_OPTION_VERBOSE))){ 
            valid = false;
            sb.append(NbBundle.getMessage(this.getClass(), 
                    "MSG_CanNotSelectQuietAndVerbose")); //NOI18N
            setError(sb.toString());
            return valid;
        }
        
        if (valid){
            setError(null);           
            return valid;
        } else{
            setError(sb.toString());
            return valid;
        }
    }
    
    public boolean isValid() {
        StringBuffer sb = new StringBuffer(); 
        return isValid(sb);
    }
    
    public boolean isFinishPanel() {
        return true;
    } 
    
    public HelpCtx getHelp() {
        return new HelpCtx(JAXBWizBindingCfgPanel.class);
    }

    public Component getComponent() {
        return bindingInfoPnl;
    }
        
    public void setMode(int mode) {
        switch ( mode ) {
            case MODE_EDITING:
                break;
            default:
            case MODE_WIZARD:
                break;
        }
    }
    
    public void fireChangeEvent() {
        ChangeEvent che = new ChangeEvent(this);
        try {
            for ( ChangeListener cl : listeners ) {
                cl.stateChanged( che );
            }
        } catch (Exception ex){
            this.logger.log(Level.WARNING, "fireChangeEvent()", ex);  
        }
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor wdNew = (WizardDescriptor) settings;              
        wdNew.putProperty(WIZ_NEW_FILE_TITLE, null);

        if (this.bindingInfoPnl.getSchemaName() != null) {
            wdNew.putProperty(JAXBWizModuleConstants.SCHEMA_NAME, 
                    this.bindingInfoPnl.getSchemaName());    
        }

        if (this.bindingInfoPnl.getPackageName() != null) {
            wdNew.putProperty(JAXBWizModuleConstants.PACKAGE_NAME, 
                    this.bindingInfoPnl.getPackageName());    
        }

        if (this.bindingInfoPnl.getSchemaType() != null) {
            wdNew.putProperty(JAXBWizModuleConstants.SCHEMA_TYPE, 
                    this.bindingInfoPnl.getSchemaType());    
        }
        
        Map<String, Boolean> options =  this.bindingInfoPnl.getOptions();
        wdNew.putProperty(JAXBWizModuleConstants.XJC_OPTIONS, options);
        
        List<String> xsdFileList = new ArrayList<String>();
        String schemaLoc = this.bindingInfoPnl.getSchemaFile();
        if (schemaLoc == null){
            xsdFileList.add(this.bindingInfoPnl.getSchemaURL());            
            wdNew.putProperty(JAXBWizModuleConstants.SOURCE_LOCATION_TYPE, 
                    JAXBWizModuleConstants.SRC_LOC_TYPE_URL);
        } else {
            xsdFileList.add(this.bindingInfoPnl.getSchemaFile());            
            wdNew.putProperty(JAXBWizModuleConstants.SOURCE_LOCATION_TYPE, 
                    JAXBWizModuleConstants.SRC_LOC_TYPE_FS);            
        }
        
        wdNew.putProperty(JAXBWizModuleConstants.XSD_FILE_LIST, xsdFileList);            
        
        // Binding files
        List<String> bindings = this.bindingInfoPnl.getBindingFiles();
        wdNew.putProperty(JAXBWizModuleConstants.JAXB_BINDING_FILES, bindings);
        
        // catalog file
        String catalogFile = this.bindingInfoPnl.getCatalogFile();
        wdNew.putProperty(JAXBWizModuleConstants.CATALOG_FILE, catalogFile);
    }
        
    public void readSettings(Object settings) {
        try {
            this.wd = (WizardDescriptor) settings;
            this.bindingInfoPnl.setInitializing(true);
            
            if (wd.getProperty(JAXBWizModuleConstants.SCHEMA_NAME) != null) {
                this.bindingInfoPnl.setSchemaName((String) 
                        wd.getProperty(JAXBWizModuleConstants.SCHEMA_NAME));
            }

            if (wd.getProperty(JAXBWizModuleConstants.PROJECT_NAME) != null) {
                this.bindingInfoPnl.setProjectName((String) 
                        wd.getProperty(JAXBWizModuleConstants.PROJECT_NAME));
            }

            if (wd.getProperty(JAXBWizModuleConstants.PROJECT_DIR) != null) {
                this.bindingInfoPnl.setProjectDir((File) 
                        wd.getProperty(JAXBWizModuleConstants.PROJECT_DIR));
                this.projDir = (File) wd.getProperty(JAXBWizModuleConstants.PROJECT_DIR);
            }

            if (wd.getProperty(JAXBWizModuleConstants.PACKAGE_NAME) != null) {
                this.bindingInfoPnl.setPackageName((String) 
                        wd.getProperty(JAXBWizModuleConstants.PACKAGE_NAME));
            }

            if (wd.getProperty(JAXBWizModuleConstants.SCHEMA_TYPE) != null) {
                this.bindingInfoPnl.setSchemaType((String) 
                        wd.getProperty(JAXBWizModuleConstants.SCHEMA_TYPE));
            }

            Map<String, Boolean> options = (Map<String, Boolean>) 
                    wd.getProperty(JAXBWizModuleConstants.XJC_OPTIONS);
            if (options != null) {
                this.bindingInfoPnl.setOptions(options);
            }

            String origSrcLocType = (String) 
                    wd.getProperty(JAXBWizModuleConstants.SOURCE_LOCATION_TYPE);
            List<String> xsdFileList = (List<String>) 
                    wd.getProperty(JAXBWizModuleConstants.XSD_FILE_LIST);

            if ((origSrcLocType != null) && (
                    JAXBWizModuleConstants.SRC_LOC_TYPE_URL.equals(
                        origSrcLocType))) {
                if ((xsdFileList != null) && (xsdFileList.size() > 0)) {
                    Iterator<String> itr = xsdFileList.iterator();
                    String file = itr.next();
                    this.bindingInfoPnl.setSchemaURL(file);
                }
            } else {
                if ((xsdFileList != null) && (xsdFileList.size() > 0)) {
                    Iterator<String> itr = xsdFileList.iterator();
                    String file = itr.next();
                    this.bindingInfoPnl.setSchemaFile(file);
                }
            }

            // Bindig files
            List<String> bindingFileList = (List<String>) 
                    wd.getProperty(JAXBWizModuleConstants.JAXB_BINDING_FILES);
            if (bindingFileList != null) {
                this.bindingInfoPnl.setBindingFiles(bindingFileList);
            }

            String catalog = (String) 
                    wd.getProperty(JAXBWizModuleConstants.CATALOG_FILE);
            if (catalog != null) {
                this.bindingInfoPnl.setCatalogFile(catalog);
            }

            this.existingSchemaNames = (List<String>) 
                    wd.getProperty(JAXBWizModuleConstants.EXISTING_SCHEMA_NAMES);

            // XXX hack, TemplateWizard in final setTemplateImpl() forces new 
            // wizard's title this name is used in NewFileWizard to modify the title
            if (wd instanceof TemplateWizard) {
                wd.putProperty(WIZ_NEW_FILE_TITLE, NbBundle.getMessage(
                        this.getClass(), "LBL_TemplateWizardTitle")); //NOI18N
            }
        } finally {
            this.bindingInfoPnl.setInitializing(false);
        }
    }    
}
