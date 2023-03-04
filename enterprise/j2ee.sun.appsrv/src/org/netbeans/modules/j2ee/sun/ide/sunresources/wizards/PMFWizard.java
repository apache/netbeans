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
/*
 * PMFWizard.java
 *
 * Created on October 10, 2003, 6:57 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;

import java.util.Set;
import java.awt.Component;
import java.io.InputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.ErrorManager;

/**
 *
 * @author nityad
 */
public final class PMFWizard implements WizardDescriptor.InstantiatingIterator, ChangeListener, WizardConstants{
        
    private static Project project;
    
    /** An array of all wizard panels */
    private WizardDescriptor.Panel panels[];
    private transient WizardDescriptor.Panel[] morePanels = null; //datasource panels
    private transient WizardDescriptor.Panel[] moreCPPanels = null;
    private transient WizardDescriptor.Panel[] pmPanels = null;
    private transient int index;
    
//    private transient WizardDescriptor wiz;
    
    private static final String DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/PMFWizard.xml";  //NOI18N
    private static final String DS_DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/DSWizard.xml";  //NOI18N
    private static final String CP_DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/CPWizard.xml";  //NOI18N
    
    private static final int IN_PM = 1;
    private static final int IN_DS = 2;
    private static final int IN_CP = 3;
    private int stage = IN_PM;

    private ResourceConfigHelper helper;
    private ResourceConfigHelper dshelper;
    private ResourceConfigHelper cphelper;
    private ResourceConfigHelperHolder holder;
  
    private Wizard wizardInfo;
    private Wizard dsWizardInfo;
    private Wizard cpWizardInfo;
  
    private boolean addSteps = false;
//    private boolean addStepsCP = false;
    
    private transient String[] steps = null;
    private transient String[] moreSteps = null;
    private transient String[] moreCPSteps = null;
    private transient String[] pmSteps = null;
        
    /** Creates a new instance of PMFWizard */
    public static PMFWizard create () {
        return new PMFWizard ();
    }

    private WizardDescriptor.Panel[] createPanels() {
        WizardDescriptor.Panel[] tempPanels = null;
        WizardDescriptor.Panel panel = new CommonAttributePanel(helper, wizardInfo, new String[] {"general"});   //NOI18N
        panel.addChangeListener(this);
        if(stage == IN_PM){
            morePanels = null;
            tempPanels = new WizardDescriptor.Panel[] {
                panel,
                new CommonPropertyPanel(this.helper, this.wizardInfo),
            };
        }else if(stage == IN_DS){
            moreCPPanels = null;
            CommonAttributePanel dspan = new CommonAttributePanel(this.dshelper, this.dsWizardInfo, new String[] {"general"});   //NOI18N
            dspan.addChangeListener(this);
            tempPanels = new WizardDescriptor.Panel[] {
                panel,
                new CommonPropertyPanel(this.helper, this.wizardInfo),
                dspan,
                new CommonPropertyPanel(this.dshelper, this.wizardInfo),
            };
        }
        return tempPanels;
    }
    
    private static String CONST_ADD_PROPERTY = "LBL_AddProperty"; // NOI18N
    
    private String[] createSteps() {
        String[] tempSteps = null;
        if(stage == IN_PM){
            moreSteps = null;
            tempSteps = new String[] {
                NbBundle.getMessage(PMFWizard.class,__FirstStepChoose),
                NbBundle.getMessage(PMFWizard.class, "LBL_GeneralAttributes_PM"), // NOI18N
                NbBundle.getMessage(PMFWizard.class, CONST_ADD_PROPERTY),
            };
        }else if(stage == IN_DS){
            moreCPSteps = null;
            tempSteps = new String[] {
                NbBundle.getMessage(PMFWizard.class,__FirstStepChoose),
                NbBundle.getMessage(PMFWizard.class, "LBL_GeneralAttributes_PM"), // NOI18N
                NbBundle.getMessage(PMFWizard.class, CONST_ADD_PROPERTY),
                NbBundle.getMessage(PMFWizard.class, "LBL_GeneralAttributes_DS"), // NOI18N
                NbBundle.getMessage(PMFWizard.class, CONST_ADD_PROPERTY),
            };
        }
        return tempSteps;
    }
    
    public Set instantiate(){
        try{
            String baseName;
            if(this.holder.hasDSHelper()){
                FileObject fo = this.helper.getData().getTargetFileObject();
                String jdbcName = this.dshelper.getData().getString(__JndiName);
                this.helper.getData().setString(__JdbcResourceJndiName, jdbcName);
                this.dshelper.getData().setTargetFile(jdbcName);
                this.dshelper.getData().setTargetFileObject(fo);
                
                if(this.holder.hasCPHelper()){
                    String poolName = this.cphelper.getData().getString(__Name);
                    this.dshelper.getData().setString(__PoolName, poolName);
                    this.cphelper.getData().setTargetFile(poolName);
                    this.cphelper.getData().setTargetFileObject(fo);
                    ResourceUtils.savePMFResourceDatatoXml(this.helper.getData(), this.dshelper.getData(), this.cphelper.getData(),
                            Util.getBaseName(project));
                }else{
                    //FIXME
                    //need to check to make sure that dsData has a value for pool name
                    ResourceUtils.savePMFResourceDatatoXml(this.helper.getData(), this.dshelper.getData(), null,Util.getBaseName(project));
                }
            }else{
                ResourceUtils.savePMFResourceDatatoXml(this.helper.getData(), null, null,Util.getBaseName(project));
            }
        }catch (Exception ex){
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        ex);
        }
        return java.util.Collections.EMPTY_SET;
    }
        
    public void initialize(WizardDescriptor wiz){
        this.wizardInfo = getWizardInfo(DATAFILE);
        this.holder = new ResourceConfigHelperHolder();
        this.helper = holder.getPMFHelper();
        
 //       this.wiz = wiz;
        wiz.putProperty("NewFileWizard_Title", NbBundle.getMessage(PMFWizard.class, "Templates/SunResources/Persistence_Resource")); //NOI18N
        index = 0;
        panels = createPanels();
        
        // Make sure list of steps is accurate.
        steps = createSteps();
        
        project = Templates.getProject(wiz);
        try{
            FileObject pkgLocation = project.getProjectDirectory();
            if (pkgLocation != null) {
                this.helper.getData().setTargetFileObject(pkgLocation);
            }
        }catch (Exception ex){
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        ex);
        }
        this.helper.getData().setHolder(this.holder);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz){
 //       this.wiz = null;
        panels = null;
    }
    
    public Wizard getWizardInfo(String filePath){
        Wizard wizInfo = null;
        try{
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(filePath);
            wizInfo = Wizard.createGraph(in);
        }catch(Exception ex){
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        ex);
        }
        return wizInfo;
    }
    
    public String name(){
        return NbBundle.getMessage(PMFWizard.class, "Templates/SunResources/Persistence_Resource"); // NOI18N
    }
    
    public boolean hasNext(){
        boolean retVal = false;
        if (stage == IN_PM && index == 0 && addSteps)
            retVal = true;
        else
            retVal = index < panels.length - 1;
        return retVal;
    }
    
    public boolean hasPrevious(){
        return index > 0;
    }

    public synchronized void nextPanel(){
        if (index + 1 == panels.length) {
            throw new java.util.NoSuchElementException();
        }
        if (index == 0) {
            ((CommonPropertyPanel) panels[1]).setInitialFocus();
        }else if(index == 1){
            ((CommonAttributePanel) panels[2]).setInitialFocus();
        }else if (index == 2) {
            ((CommonPropertyPanel) panels[3]).setInitialFocus();
        }else if (index == 3) {
            ((CPVendor) panels[4]).setInitialFocus();
        }else if (index == 4){
            ((CPPropertiesPanelPanel) panels[5]).refreshFields();
        }else if (index == 5){
            ((CommonAttributePanel) panels[6]).setPropInitialFocus();
        }
        
        index ++;
        
        if (stage == IN_PM && index == 2) {
            stage = IN_DS;
        }else if(stage == IN_DS && index == 4){
            stage = IN_CP;
        }
    }
    
    public synchronized void previousPanel(){
        if (index == 0) {
            throw new java.util.NoSuchElementException();
        }
        
        index--;
        //Required to refresh number of panels that need to be displayed
        if( (stage == IN_DS) && (index < 2)){
            stage = IN_PM;
        }else{
            if((stage == IN_CP) && (index < 4)) {
                stage = IN_DS;
            }
        }
    }
    
    public WizardDescriptor.Panel current(){
        return (WizardDescriptor.Panel)panels[index];
    }
    
    public final void addChangeListener(ChangeListener l) {
    }
    public final void removeChangeListener(ChangeListener l) {
    }
    
    public void setResourceConfigHelper(ResourceConfigHelper helper){
        this.helper = helper;
    }
    
    public ResourceConfigHelper getResourceConfigHelper(){
        return this.helper;
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        if( (e.getSource().getClass() == CommonAttributePanel.class) || (e.getSource().getClass() == CommonAttributeVisualPanel.class) ) {
            CommonAttributePanel commonPane = (CommonAttributePanel)this.current();
            CommonAttributeVisualPanel visPane = (CommonAttributeVisualPanel)commonPane.getComponent();
//            boolean oldVal = addSteps;
            this.addSteps = visPane.isNewResourceSelected();
            
            WizardDescriptor.Panel[] tempPanels = null;
            if(this.addSteps){
                if (stage == IN_PM){
                    this.holder.setHasDSHelper(this.addSteps);
                    tempPanels = morePanels;
                }else if(stage == IN_DS){
                    this.holder.setHasCPHelper(this.addSteps);
                    tempPanels = moreCPPanels;
                }
            }else{
                if (stage == IN_PM){
                    this.holder.setHasDSHelper(this.addSteps);
                    tempPanels = morePanels;
                }else if(stage == IN_DS){
                    this.holder.setHasCPHelper(this.addSteps);
                    tempPanels = moreCPPanels;
                }
            }
            
            if (addSteps && tempPanels == null) {
                addPanels();
                addSteps();
                for (int i = 0; i < panels.length; i++) {
                    Component c = panels[i].getComponent();
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent)c;
                        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    }
                }
            }else if((!addSteps) && (tempPanels != null) ){
                if((stage == IN_PM) && (pmPanels != null)){
                    morePanels = null;
                    panels = pmPanels;
                }else if((stage == IN_DS) && (morePanels != null)){
                    moreCPPanels = null;
                    panels = morePanels;
                }else
                    panels = createPanels();
                
                if((stage == IN_PM) && (pmSteps != null)){
                    moreSteps = null;
                    steps = pmSteps;
                }else if((stage == IN_DS) && (moreSteps != null)){
                    moreCPSteps = null;
                    steps = moreSteps;
                }else
                    steps = createSteps();
                
                for (int i = 0; i < panels.length; i++) {
                    Component c = panels[i].getComponent();
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent)c;
                        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    }
                }
                
                if(stage == IN_PM){
                    ((CommonAttributePanel)panels[0]).setInitialFocus();
                }else if(stage == IN_DS){
                    ((CommonAttributePanel)panels[2]).setInitialFocus();
                }
            }
        } //Is CommonAttributePanel
        
    }
    
    protected void addPanels() {
        if (stage == IN_PM){
            if (panels != null && morePanels == null) {
                this.dshelper = this.holder.addAssociatedHelper();
                this.dshelper.getData().setResourceName(__JdbcResource);
                this.holder.setHasDSHelper(true);
                this.dsWizardInfo = getWizardInfo(DS_DATAFILE);
                this.dshelper.getData().setTargetFileObject(this.helper.getData().getTargetFileObject());
                this.dshelper.getData().setString(__DynamicWizPanel, "true"); //NOI18N
                
                CommonAttributePanel dspan = new CommonAttributePanel(this.dshelper, this.dsWizardInfo, new String[] {"general"});   //NOI18N
                dspan.addChangeListener(this);
                morePanels = new WizardDescriptor.Panel[] {
                    panels[0],
                    panels[1],
                    dspan,
                    new CommonPropertyPanel(this.dshelper, this.wizardInfo),
                };
            }
            pmPanels = panels;
            panels = morePanels;
        }else if(stage == IN_DS){
            if (panels != null && moreCPPanels == null) {
                this.cphelper = this.holder.addAssociatedHelper();
                this.cphelper.getData().setResourceName(__JdbcConnectionPool);
                this.holder.setHasCPHelper(true);
                this.cpWizardInfo = getWizardInfo(CP_DATAFILE);
                this.cphelper.getData().setTargetFileObject(this.helper.getData().getTargetFileObject());
                this.cphelper.getData().setString(__DynamicWizPanel, "true"); //NOI18N
                
                moreCPPanels = new WizardDescriptor.Panel[] {
                    panels[0],
                    panels[1],
                    panels[2],
                    panels[3],
                    new CPVendor(this.cphelper, this.cpWizardInfo),
                    new CPPropertiesPanelPanel(this.cphelper, this.cpWizardInfo),
                    new CommonAttributePanel(this.cphelper, this.cpWizardInfo,  new String[] {"pool-setting", "pool-setting-2", "pool-setting-3"}), //NOI18N
                };
            }
            //morePanels = panels; //--??
            panels = moreCPPanels;
        }
    }
    
    protected void addSteps() {
        if (stage == IN_PM){
            if (steps != null && moreSteps == null) {
                moreSteps = new String[] {
                    steps[0],
                    steps[1],
                    steps[2],
                    NbBundle.getMessage(PMFWizard.class, "LBL_GeneralAttributes_DS"), // NOI18N
                    NbBundle.getMessage(PMFWizard.class, CONST_ADD_PROPERTY),
                };
            }
            pmSteps = steps;
            steps = moreSteps;
        }else if(stage == IN_DS){
            if (steps != null && moreCPSteps == null) {
                moreCPSteps = new String[] {
                    steps[0],
                    steps[1],
                    steps[2],
                    steps[3],
                    steps[4],
                    NbBundle.getMessage(PMFWizard.class, "TITLE_ConnPoolWizardPanel_dbConn"), // NOI18N
                    NbBundle.getMessage(PMFWizard.class, "TITLE_ConnPoolWizardPanel_properties"), // NOI18N
                    NbBundle.getMessage(PMFWizard.class, "TITLE_ConnPoolWizardPanel_optionalProps") // NOI18N
                };
            }
            //moreSteps = steps; //--??
            steps = moreCPSteps;
        }
    }

}
