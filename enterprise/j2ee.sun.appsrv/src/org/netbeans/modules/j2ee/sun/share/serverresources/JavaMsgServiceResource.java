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
 * JavaMsgServiceResource.java
 *
 * Created on November 13, 2003, 3:01 PM
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

/**
 *
 * @author  nityad
 */
public class JavaMsgServiceResource extends BaseResource implements java.io.Serializable{

    private String jndiName;
    private String resType;
    private String isEnabled;
    private String resAdapter = "jmsra";  //NOI18N
    private String poolName;  
    
    /** Creates a new instance of JavaMsgServiceResource */
    public JavaMsgServiceResource() {
    }

    public String getJndiName() {
        return jndiName;
    }
    public void setJndiName(String value) {
        String oldValue = jndiName;
        this.jndiName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("jndiName", oldValue, jndiName);//NOI18N
    }
    
    public String getResType() {
        return resType;
    }
    public void setResType(String value) {
        String oldValue = resType;
        this.resType = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("resType", oldValue, resType);//NOI18N
    }
    
    public String getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(String value) {
        String oldValue = isEnabled;
        this.isEnabled = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isEnabled", oldValue, isEnabled);//NOI18N
    }

    public String getResAdapter() {
        return resAdapter;
    }

    public void setResAdapter(String value) {
        String oldValue = resAdapter;
        this.resAdapter = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("resAdapter", oldValue, isEnabled);//NOI18N
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String value) {
        String oldValue = poolName;
        this.poolName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("poolName", oldValue, poolName);//NOI18N
    }
    
}
