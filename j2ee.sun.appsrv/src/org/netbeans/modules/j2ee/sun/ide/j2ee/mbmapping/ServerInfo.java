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
 * ServerInfo.java
 *
 * Created on February 25, 2004, 4:50 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

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
public class ServerInfo extends ModuleMBean implements Constants{
    
    String PORT = "Port"; //NOI18N
    String DOMAIN = "Domain"; //NOI18N
    
    String port = null;
    String username = null;
    String password = null;
    String domain = null;
    
    /** Creates a new instance of ServerInfo */
    public ServerInfo( MBeanServerConnection in_conn) {
        super(in_conn);
        this.runtimeObjName = createRuntimeObjectName();
    }
    
    private ObjectName createRuntimeObjectName(){
        ObjectName runtimeName = null;
        try{
            runtimeName = new ObjectName(OBJ_J2EE);
        }catch(Exception ex){  
            return null;
        }    
        return runtimeName;
    }
    
    public AttributeList getAttributes(String[] attributes) {
        AttributeList attList = null;
        try{
            attList = this.conn.getAttributes(this.runtimeObjName, attributes);
            AttributeList jsrList = createAddAttributes();
            attList.addAll(jsrList);
        }catch(Exception ex){
            //System.out.println("Error in getAttributes of ServerInfo " + ex.getMessage());
            return attList;
        }
        return attList;
    }
    
    public MBeanInfo getMBeanInfo() {
        MBeanInfo updatedInfo = null;
        try{
            MBeanInfo currentInfo = this.conn.getMBeanInfo(this.runtimeObjName);
            MBeanAttributeInfo[] currentAttrInfo = currentInfo.getAttributes();
            int additionalSize = ADDITIONAL_SERVER_INFO.length;
            int size = JSR_SERVER_INFO.length + additionalSize; 
            MBeanAttributeInfo[] updatedAttrInfo = new MBeanAttributeInfo[size];
            
            Set reqdAttrs = new HashSet(Arrays.asList(JSR_SERVER_INFO));
            int j=0;
            for ( int i=0; i<currentAttrInfo.length; i++ ) {
                if(reqdAttrs.contains(currentAttrInfo[i].getName())){
                    updatedAttrInfo[j] = currentAttrInfo[i];
                    j++;
                }
            }
            //Additional Attributes : {"port", "username", "password", "domain" }
            /*MBeanAttributeInfo(String name, String type, String description, boolean isReadable,
                          boolean isWritable, boolean isIs) throws IllegalArgumentException*/
            updatedAttrInfo[j]   = new MBeanAttributeInfo(this.PORT, "int", "Server's port number", true, false, false); //NOI18N
            updatedAttrInfo[j+1] = new MBeanAttributeInfo(this.DOMAIN, "char", "Server's domain", true, false, false); //NOI18N
            
            updatedInfo = new MBeanInfo(currentInfo.getClassName(), currentInfo.getDescription(), updatedAttrInfo, currentInfo.getConstructors(),
                null, currentInfo.getNotifications());
        }catch(Exception ex){
            //System.out.println("Error in getMBeanInfo of ServerInfo " + ex.getMessage());
        }
        return updatedInfo;
    }
    
    public void setAttribute(Attribute attribute) throws RemoteException, InstanceNotFoundException, AttributeNotFoundException,
    InvalidAttributeValueException, MBeanException, ReflectionException, java.io.IOException {
        String attrName = attribute.getName();
        Set appList = new HashSet(Arrays.asList(JSR_SERVER_INFO));
        if(appList.contains(attrName)){
            this.conn.setAttribute(this.runtimeObjName, attribute);
        }
    }
    
    private AttributeList createAddAttributes(){
        AttributeList atList = new AttributeList();
        atList.add(new Attribute(this.PORT, this.getPort()));
        atList.add(new Attribute(this.DOMAIN, this.getDomain()));
        return atList;
    }
    
    
    public void setAttributes(String in_port, String in_userName, String in_password, String in_domain){
        this.port = in_port;
        this.username = in_userName;
        this.password = in_password;
        this.domain = in_domain;
    }
    
    public String getPort(){
        return this.port;
    }
    
    public String getUserName(){
        return this.username;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public String getHiddenPassword(){
        String modPassword = ""; //NOI18N
        for (int i = 0; i < password.length(); i++) {
            char c = '*' ; 
            modPassword += c;
        }
        return modPassword;
    }
    
    public String getDomain(){
        return this.domain;
    }

    //Fix for bug#5017963 - add api to identify if server requires restart
    public boolean isRestartRequired(){
        boolean restartReq = false;
        String val = (String)getRuntimeAttributeValue("restartRequired"); //NOI18N
        if(val != null){
            restartReq = Boolean.valueOf(val).booleanValue();
        }
        return restartReq;
    }
    
    public String getRuntimeAttributeValue(String attributeName){
        return super.getAttribute(this.runtimeObjName, attributeName);
    }
}
