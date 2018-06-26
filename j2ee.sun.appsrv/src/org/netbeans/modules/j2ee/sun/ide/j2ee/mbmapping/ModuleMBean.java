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
 * ModuleMBean.java
 *
 * Created on February 19, 2004, 2:55 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping;

import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;

import java.rmi.RemoteException;




/**
 *
 * @author  nityad
 */
public abstract class ModuleMBean implements Constants{
    protected ObjectName runtimeObjName = null;
    protected MBeanServerConnection conn = null;
    
    public ObjectName configApplicationsObjName = null;
    
    public ModuleMBean(MBeanServerConnection in_conn) {
        this.conn = in_conn;
        this.configApplicationsObjName = setApplicationsObjectName();
    }
    
    public ModuleMBean(ObjectName objName) {
        this.runtimeObjName = objName;
        this.configApplicationsObjName = setApplicationsObjectName();
    }
     
    public ModuleMBean(ObjectName objName, MBeanServerConnection in_conn) {
        this.runtimeObjName = objName;
        this.conn = in_conn;
        this.configApplicationsObjName = setApplicationsObjectName();
    }
     
    public void setConnection(MBeanServerConnection in_conn){
         this.conn = in_conn;
         this.configApplicationsObjName = setApplicationsObjectName();
    }
    
    public abstract MBeanInfo getMBeanInfo();
        
    public abstract AttributeList getAttributes(String[] attributes);
   
    public abstract void setAttribute(Attribute attribute) throws RemoteException, InstanceNotFoundException, AttributeNotFoundException,
        InvalidAttributeValueException, MBeanException, ReflectionException, java.io.IOException;
    
    public Object start(){
        Object retVal = null;
        try{
            retVal = this.conn.invoke(this.runtimeObjName, "start", null, null); //NOI18N
        }catch(Exception ex){
            return null;
        }   
        return retVal;
    }
    
    public void stop(){
        try{
            this.conn.invoke(this.runtimeObjName, "stop", null, null); //NOI18N
        }catch(Exception ex){ }  
    }
    
    public void restart(){
        try{
            this.conn.invoke(this.runtimeObjName, "start", null, null); //NOI18N
            this.conn.invoke(this.runtimeObjName, "stop", null, null); //NOI18N
        }catch(Exception ex){ }  
    }
    
    public ObjectName setApplicationsObjectName(){
        ObjectName objName = null;
        try{
            objName = new ObjectName(MAP_J2EEAPP_STANDALONE);
        }catch(Exception ex){
            
        }
        return objName;
    }
    
    public String getResourceName(String key){
        String keyName = null;
        Object keyNameAttr = getAttribute(this.runtimeObjName, key); 
        if(keyNameAttr != null){
            keyName = keyNameAttr.toString();
        }
        return keyName;
    }
    
    public String getAttribute(ObjectName objName, String attributeName){
        String attrValue = null;
        try{
            Object attValue = this.conn.getAttribute(objName, attributeName);
            if(attValue != null){
                attrValue = attValue.toString();
            }
        }catch(Exception ex){
            //suppress Exception. Callers to any of the getters should handle null condition
        }
        return attrValue;
    }
    
    public ObjectName getConfigObjectName(String query, String resourceName){
        ObjectName configObjectName = null;
        try{
            configObjectName = (ObjectName)this.conn.invoke(this.configApplicationsObjName, query, getStringParam(resourceName), getStringSignature());
        }catch(Exception ex){
            
        }
        return configObjectName;
    }
    
    public Object invokeOperation(String operationName, Object[] params, String[] signature){
        Object retValue = null;
        try{
            retValue = this.conn.invoke(this.runtimeObjName, operationName, params, signature);
        }catch(Exception ex){
        }    
        return retValue;
    }
    
    /*********************Util classes ****************************************************/
    private String[] getStringSignature(){
        return Utils.getStringSignature();
    }
    
    private Object[] getStringParam(String paramValue){
        return Utils.getStringParam(paramValue);
    }
    
    public boolean isUserResource(ObjectName currentComp){
        boolean userRes = Utils.isUserResource(currentComp, this.conn);
        return userRes;
    }
    
    public MBeanAttributeInfo[] setSystemResourceNonEditable(MBeanAttributeInfo[] currentAttr){
        MBeanAttributeInfo[] updatedAttr = null;
        try{
            updatedAttr = new MBeanAttributeInfo[currentAttr.length];
            for ( int i=0; i<currentAttr.length; i++ ) {
                MBeanAttributeInfo currentAttrInfo = currentAttr[i];
                updatedAttr[i] = new MBeanAttributeInfo(currentAttrInfo.getName(), currentAttrInfo.getType(), currentAttrInfo.getDescription(), true, false, false);
            }    
        }catch(Exception ex){
            //System.out.println("Error in setSystemResourceNonEditable " + ex.getLocalizedMessage());
        }
        return updatedAttr;
    }
    
    public ObjectName getRequiredObjectName(ObjectName origName, ObjectName confName, Attribute attribute){
        ObjectName oName = Utils.getRequiredObjectName(origName, confName, attribute, this.conn);
        return oName;
    }
        
    public ObjectName getRequiredObjectName(ObjectName origName, ObjectName confName, String operationName){
        ObjectName oName = Utils.getRequiredObjectName(origName, confName, operationName, this.conn);
        return oName;
    }
    
    /*********************Util classes ****************************************************/
}
