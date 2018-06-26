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
/*
 * CommonAttributePanel.java
 *
 * Created on October 10, 2002
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import javax.swing.JTextField;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.openide.filesystems.FileObject;

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  shirleyc
 */
public class CommonAttributePanel extends ResourceWizardPanel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private Component component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private String[] groupNames;
    private boolean setupValid = true;
    private String panelType;
    private boolean isConnPool = false;
    
    /** Create the wizard panel descriptor. */
    public CommonAttributePanel(ResourceConfigHelper helper, Wizard wizardInfo, String[] groupNames) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
        this.groupNames = groupNames;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
               FieldGroup[] groups = new FieldGroup[groupNames.length];
                for (int i = 0; i < this.groupNames.length; i++) {
                    groups[i] = FieldGroupHelper.getFieldGroup(wizardInfo, this.groupNames[i]);  //NOI18N
                }
                if (wizardInfo.getName().equals(__JdbcConnectionPool)) {
                    panelType = CommonAttributeVisualPanel.TYPE_CP_POOL_SETTING;
                    component = new ConnectionPoolOptionalVisualPanel(this, this.helper); 
                }else if (wizardInfo.getName().equals(__JdbcResource)) {
                    panelType = CommonAttributeVisualPanel.TYPE_JDBC_RESOURCE;
                    component = new CommonAttributeVisualPanel(this, groups, panelType, this.helper);
                }else if (wizardInfo.getName().equals(__PersistenceManagerFactoryResource)) {
                    panelType = CommonAttributeVisualPanel.TYPE_PERSISTENCE_MANAGER;
                    component = new CommonAttributeVisualPanel(this, groups, panelType, this.helper);
                }
                setIsConnPool();
        }
        return component;
    }
    
    public boolean isNewResourceSelected() {
        if (component == null)
            return false;
        else {
            if(! getIsConnPool()) {
                return ((CommonAttributeVisualPanel)component).isNewResourceSelected(); 
            } else {
                return false;
            }
        }    
    }
    
    public void setInitialFocus(){
        if(component != null) {
            if(! getIsConnPool()) {
                ((CommonAttributeVisualPanel)component).setInitialFocus(); 
            }
        }
    }
    
    public void setPropInitialFocus(){
        if(component != null) {
            if(! getIsConnPool()) {
                ((CommonAttributeVisualPanel)component).setPropInitialFocus(); 
            }
        }
    }
    
    public String getResourceName() {
        return this.wizardInfo.getName();
    }
    
    public HelpCtx getHelp() {
        if (wizardInfo.getName().equals(__JdbcConnectionPool)) {
            return new HelpCtx("AS_Wiz_ConnPool_poolSettings"); //NOI18N
        }else if (wizardInfo.getName().equals(__JdbcResource)) {
            return new HelpCtx("AS_Wiz_DataSource_general"); //NOI18N
        }else if (wizardInfo.getName().equals(__PersistenceManagerFactoryResource)) {
            return new HelpCtx("AS_Wiz_PMF_general"); //NOI18N
        }else {
            return HelpCtx.DEFAULT_HELP;
        }
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public Wizard getWizard() {
        return wizardInfo;
    }
    
     /**
     * Checks if the JNDI Name in the wizard is duplicate name in the
     * Unregistered resource list for JDBC Data Sources, Persistenc Managers, 
     * and Java Mail Sessions.
     *
     * @return boolean true if there is a duplicate name.
     * false if not.
     */
      public boolean isValid () {
          // If it is always OK to press Next or Finish, then:
          if(! setupValid){
              setErrorMsg (bundle.getString ("Err_InvalidSetup"));
              return false;
          }
          setErrorMsg (bundle.getString ("Empty_String"));
          if(! getIsConnPool()) {
              CommonAttributeVisualPanel visComponent = (CommonAttributeVisualPanel) component;
              if (visComponent != null && visComponent.jLabels != null && visComponent.jFields != null) {
                  for (int i = 0; i < visComponent.jLabels.length; i++) {
                      String jLabel = (String) visComponent.jLabels[i].getText ();
                      if (jLabel.equals(Util.getCorrectedLabel(bundle, __JndiName))) { // NO18N
                          String jndiName = (String) ((JTextField)visComponent.jFields[i]).getText ();
                          if (jndiName == null || jndiName.length() == 0) {
                              setErrorMsg(bundle.getString("Err_InvalidJndiName"));
                              return false;
                          } else if (!ResourceUtils.isLegalResourceName(jndiName)) {
                              setErrorMsg(bundle.getString("Err_InvalidJndiName"));
                              return false;
                          } else {
                              FileObject resFolder = this.helper.getData().getTargetFileObject();
                              if (resFolder != null) {
                                  if (wizardInfo.getName().equals(__JdbcResource)) {
                                      if (!ResourceUtils.isUniqueFileName(jndiName, resFolder, __JDBCResource)) {
                                          setErrorMsg(bundle.getString("Err_DuplFileJndiName"));
                                          return false;
                                      }
                                  } else if (wizardInfo.getName().equals(__PersistenceManagerFactoryResource)) {
                                      if (!ResourceUtils.isUniqueFileName(jndiName, resFolder, __PersistenceResource)) {
                                          setErrorMsg(bundle.getString("Err_DuplFileJndiName"));
                                          return false;
                                      }
                                  }
                              }
                          }
                      } //if
                  } //for
              }
              if (!isNewResourceSelected()) {
                  //Need to check the poolname for jdbc
                  if ((this.helper.getData().getResourceName()).equals(__JdbcResource)) {
                      String cpname = this.helper.getData().getString(__PoolName);
                      if (cpname == null || cpname.trim().equals("")) { //NOI18N
                          setErrorMsg(bundle.getString("Err_ChooseOrCreatePool"));
                          return false;
                      }
                  }
                  //Need to get jdbc data if pmf and make sure it has a poolname
                  if ((this.helper.getData().getResourceName()).equals(__PersistenceManagerFactoryResource)) {
                      if (this.helper.getData().getHolder().hasDSHelper()) {
                          String cpname = this.helper.getData().getHolder().getDSHelper().getData().getString(__PoolName);
                          if (cpname == null || cpname.trim().equals("")) { //NOI18N
                              setErrorMsg(bundle.getString("Err_ChooseOrCreatePool"));
                              return false;
                          }
                      } else {
                          String dsname = this.helper.getData().getString(__JdbcResourceJndiName);
                          if (dsname == null || dsname.trim().equals("")) { //NOI18N
                              setErrorMsg(bundle.getString("Err_ChooseOrCreateDS"));
                              return false;
                          }
                      }
                  }
              }
          } else {
              ConnectionPoolOptionalVisualPanel visComponent = (ConnectionPoolOptionalVisualPanel) component;
              return visComponent.hasValidData();
          }
          return true;
      }
    
    public void initData() {
        if(! getIsConnPool()) {
            CommonAttributeVisualPanel visComponent = (CommonAttributeVisualPanel) component;
            visComponent.initData();
        }      
    }
    
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
        TemplateWizard wizard = (TemplateWizard)settings;
        String targetName = wizard.getTargetName();
        FileObject resFolder = ResourceUtils.getResourceDirectory(this.helper.getData().getTargetFileObject());
        this.helper.getData().setTargetFileObject (resFolder);
        if(component == null)
            getComponent();
        if(resFolder != null){
            String resourceName = this.helper.getData().getString("jndi-name");
            if((resourceName != null) && (! resourceName.equals(""))) {
                this.helper.getData().setTargetFile(resourceName);
            } else {
                if (wizardInfo.getName().equals(__JdbcResource)) {
                    if (this.helper.getData().getString(__DynamicWizPanel).equals("true")) { //NOI18N
                        targetName = null;
                    }
                    targetName = ResourceUtils.createUniqueFileName(targetName, resFolder, __JDBCResource);
                    this.helper.getData().setTargetFile(targetName);
                } else if (wizardInfo.getName().equals(__PersistenceManagerFactoryResource)) {
                    targetName = ResourceUtils.createUniqueFileName(targetName, resFolder, __PersistenceResource);
                    this.helper.getData().setTargetFile(targetName);
                }
            }
            if(! getIsConnPool()) {
              CommonAttributeVisualPanel visComponent = (CommonAttributeVisualPanel) component;
              visComponent.setHelper (this.helper);
            }
        }else
            setupValid = false;
    }
    
    public boolean isFinishPanel() {
       if(isNewResourceSelected())
            return false;
       else
           return isValid();
    }
    
    private boolean setupValid(){
        return setupValid;
    }
    
    private void setIsConnPool(){
        if (panelType.equals(CommonAttributeVisualPanel.TYPE_JDBC_RESOURCE) || (panelType.equals(CommonAttributeVisualPanel.TYPE_PERSISTENCE_MANAGER))){
            isConnPool = false;
        }else{
            isConnPool = true;
        }
    }
    
    private boolean getIsConnPool(){
        return isConnPool;
    }

//    protected final void fireChangeEvent (Object source) {
//       super.fireChange(this);
//    }
}

