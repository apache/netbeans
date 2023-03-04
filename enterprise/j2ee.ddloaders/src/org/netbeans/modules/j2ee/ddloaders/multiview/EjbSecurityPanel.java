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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import java.util.StringTokenizer;
import javax.swing.JRadioButton;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission;
import org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.SecurityForm;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ItemOptionHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * EjbSecurityPanel.java
 *
 * Panel for adding and creating security related deployment descriptor elements.
 * @author ptliu
 */
public class EjbSecurityPanel extends SecurityForm {
    private static String ALL_METHODS = "*";        //NOI18N
    
    private MethodPermission methodPermission;
    
    private EjbJar ejbJar;
    
    private AssemblyDescriptor assemblyDesc;
    
    /** Creates a new instance of EjbSecurityPanel */
    public EjbSecurityPanel(SectionNodeView sectionNodeView, final Ejb ejb) {
        super(sectionNodeView);
 
        EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();
        this.ejbJar = dataObject.getEjbJar();
        
        this.assemblyDesc = ejbJar.getSingleAssemblyDescriptor();
        
        final XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
        
        addRefreshable(new ItemOptionHelper(synchronizer, getSecurityIDButtonGroup()) {
            public String getItemValue() {
                SecurityIdentity securityIdentity = ejb.getSecurityIdentity();
                
                if (securityIdentity != null) {
                    if (securityIdentity.isUseCallerIdentity()) {
                        return USE_CALLER_ID;
                    } else if (securityIdentity.getRunAs() != null) {
                        return RUN_AS;
                    }
                }
                
                return NO_SECURITY_ID;
            }
            
            public void setItemValue(String value) {
                updateSecurityIdentity(ejb);
                
                updateVisualState();
            }
        });
        
        addRefreshable(new ItemEditorHelper(getRunAsRoleNameTF(),
                new TextItemEditorModel(synchronizer, true, true) {
            protected String getValue() {
                RunAs runAs = getRunAs(ejb);
                
                if (runAs != null) {
                    return runAs.getRoleName();
                } else {
                    return getRunAsRoleNameTF().getText();
                }
            }
            
            protected void setValue(String value) {
                RunAs runAs = getRunAs(ejb);
                
                if (runAs != null) {
                    updateRunAs(runAs);
                }
            }
        }));
        
        addRefreshable(new ItemEditorHelper(getRunAsDescriptionTF(),
                new TextItemEditorModel(synchronizer, true, true) {
            protected String getValue() {
                RunAs runAs = getRunAs(ejb);
                
                if (runAs != null) {
                    return runAs.getDefaultDescription();
                } else {
                    return getRunAsDescriptionTF().getText();
                }
            }
            
            protected void setValue(String value) {
                RunAs runAs = getRunAs(ejb);
                
                if (runAs != null) {
                    updateRunAs(runAs);
                }
            }
        }));
        
        addRefreshable(new ItemOptionHelper(synchronizer,
                getGlobalMethodPermissionButtonGroup()) {
            public String getItemValue() {
                MethodPermission permission = getGlobalMethodPermission(ejb);
  
                if (permission != null) {
                    try {
                        if (permission.isUnchecked()) {
                            return ALL_METHOD_PERMISSION;
                        } else {
                            return SET_ROLE_METHOD_PERMISSION;
                        }
                    } catch (Exception ex) {
                        return SET_ROLE_METHOD_PERMISSION;
                    }
                }
                
                return NO_METHOD_PERMISSION;
            }
            
            public void setItemValue(String value) {
                updateMethodPermission(assemblyDesc, ejb);
                
                updateVisualState();
            }
        });
        
        addRefreshable(new ItemEditorHelper(getSetRoleRoleNamesTF(),
                new TextItemEditorModel(synchronizer, true, true) {
            boolean endsWithComma = false;
            
            protected String getValue() {
                MethodPermission permission = getGlobalMethodPermission(ejb);
                
                try {
                    if (permission != null && !permission.isUnchecked()) {
                        String roleNames = getCommaSeparatedString(permission.getRoleName());
                        if (endsWithComma)
                            roleNames += ",";   //NOI18N
                        
                        return roleNames;
                    } else {
                        return getSetRoleRoleNamesTF().getText();
                    }
                } catch (VersionNotSupportedException ex) {
                    return "";  //NOI18N
                }
            }
            
            protected void setValue(String value) {
                if (value != null && value.trim().endsWith(",")) {   //NOI18N
                    endsWithComma = true;
                } else {
                    endsWithComma = false;
                }
                
                updateMethodPermission(assemblyDesc, ejb);
            }
        }));
        
        updateVisualState();
    }
    
    public void dataModelPropertyChange(Object source, String propertyName,
            Object oldValue, Object newValue) {
        scheduleRefreshView();
    }
    
    private void updateSecurityIdentity(Ejb ejb) {
        JRadioButton noSecurityIDRB = getNoSecurityIDRB();
        JRadioButton useCallerIDRB = getUseCallerIDRB();
        JRadioButton runAsRB = getRunAsRB();
        
        if (noSecurityIDRB.isSelected()) {
            removeSecurityIdentity(ejb);
        } else {
            SecurityIdentity securityID = ejb.getSecurityIdentity();
            if (securityID == null) {
                securityID = ejb.newSecurityIdentity();
                ejb.setSecurityIdentity(securityID);
            }
            
            if (runAsRB.isSelected()) {
                RunAs runAs = securityID.getRunAs();
                
                if (runAs == null) {
                    runAs = securityID.newRunAs();
                    securityID.setRunAs(runAs);
                    
                    updateRunAs(runAs);
                }
            } else {
                removeRunAs(securityID);
            }
            
            if (useCallerIDRB.isSelected()) {
                securityID.setUseCallerIdentity(true);
            } else {
                securityID.setUseCallerIdentity(false);
            }
        }
    }
    
    private void removeSecurityIdentity(Ejb ejb) {
        ejb.setSecurityIdentity(null);
    }
    
    private RunAs getRunAs(Ejb ejb) {
        SecurityIdentity securityIdentity = ejb.getSecurityIdentity();
        if (securityIdentity != null) {
            return securityIdentity.getRunAs();
        }
        
        return null;
    }
    
    private void updateRunAs(RunAs runAs) {
        String newRoleName = getRunAsRoleNameTF().getText();
        runAs.setRoleName(newRoleName);
        runAs.setDescription(this.getRunAsDescriptionTF().getText());
    }
    
    private void removeRunAs(SecurityIdentity securityIdentity) {
        RunAs runAs = securityIdentity.getRunAs();
        
        if (runAs != null) {
            securityIdentity.setRunAs(null);
        }
    }
    
    private MethodPermission getGlobalMethodPermission(Ejb ejb) {
        
        if (assemblyDesc == null) return null;
        
        MethodPermission methodPermission = null;
        
        MethodPermission[] permissions = assemblyDesc.getMethodPermission();
        String ejbName = ejb.getEjbName();
        
        for (int i = 0; i < permissions.length; i++) {
            MethodPermission permission = permissions[i];
            Method method = permission.getMethod(0);
            
            if (method != null) {
                String methodEjbName = method.getEjbName();
                String methodName = method.getMethodName();
                
                if (methodEjbName != null && methodEjbName.equals(ejbName) &&
                        methodName != null && methodName.equals(ALL_METHODS)) {
                    methodPermission = permission;
                    break;
                }
            }
        }
     
        return methodPermission;
    }
    
    private MethodPermission createGlobalMethodPermission(Ejb ejb) {
        if (assemblyDesc == null) {
            assemblyDesc = getAssemblyDesc();
        }
        
        methodPermission = assemblyDesc.newMethodPermission();
        Method method = methodPermission.newMethod();
        method.setEjbName(ejb.getEjbName());
        method.setMethodName(ALL_METHODS);      //NOI18N
        methodPermission.addMethod(method);
        assemblyDesc.addMethodPermission(methodPermission);
        
        return methodPermission;
    }
    
    private void removeGlobalMethodPermission() {
        if (methodPermission != null) {
            assemblyDesc.removeMethodPermission(methodPermission);
            methodPermission = null;
        }
    }
    
    private void updateMethodPermission(AssemblyDescriptor assemblyDesc, Ejb ejb) {
        if (this.getNoPermissionRB().isSelected()) {
            removeGlobalMethodPermission();
        } else {
            MethodPermission permission = getGlobalMethodPermission(ejb);
            if (permission == null) {
                permission = createGlobalMethodPermission(ejb);
            }
            
            if (this.getAllMethodPermissionRB().isSelected()) {
                permission.setRoleName(null);
                
                try {
                    permission.setUnchecked(true);
                } catch (VersionNotSupportedException ex) {
                    ex.printStackTrace();
                }
            } else if (this.getSetRolePermissionRB().isSelected()) {
                try {
                    permission.setUnchecked(false);
                } catch (VersionNotSupportedException ex) {
                    ex.printStackTrace();
                }
                
                String roleNames = getSetRoleRoleNamesTF().getText();
                StringTokenizer tokenizer = new StringTokenizer(roleNames, ","); //NOI18N
                permission.setRoleName(null);
                
                while (tokenizer.hasMoreTokens()) {
                    String roleName = tokenizer.nextToken().trim();
                    
                    if (roleName.length() > 0)
                        permission.addRoleName(roleName);
                }
            }
        }
    }
    
    private String getCommaSeparatedString(String[] values) {
        String result = "";         //NOI18N
        
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                result += ", ";     //NOI18N
            }
            
            result += values[i];
        }
        
        return result;
    }
    
    private AssemblyDescriptor getAssemblyDesc() {
        AssemblyDescriptor assemblyDesc = ejbJar.getSingleAssemblyDescriptor();
        
        if (assemblyDesc == null) {
            assemblyDesc = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDesc);
        }
        
        return assemblyDesc;
    }
}
