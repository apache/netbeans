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
 * MailSessionResource.java
 *
 * Created on September 17, 2003, 2:38 PM
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

/**
 *
 * @author  nityad
 */
public class MailSessionResource extends BaseResource implements java.io.Serializable{

    private String jndiName;
    private String storeProt;
    private String storeProtClass;
    private String transProt;
    private String transProtClass;
    private String hostName;
    private String userName;
    private String fromAddr;
    private String isDebug;
    private String isEnabled;
        
    /** Creates a new instance of MailSessionResource */
    public MailSessionResource() {
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
    
    public String getStoreProt() {
        return storeProt;
    }
    public void setStoreProt(String value) {
        String oldValue = storeProt;
        this.storeProt = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("storeProt", oldValue, storeProt);//NOI18N
    }
    
    public String getStoreProtClass() {
        return storeProtClass;
    }
    public void setStoreProtClass(String value) {
        String oldValue = storeProtClass;
        this.storeProtClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("storeProtClass", oldValue, storeProtClass);//NOI18N
    }
    
    public String getTransProt() {
        return transProt;
    }
    public void setTransProt(String value) {
        String oldValue = transProt;
        this.transProt = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("transProt", oldValue, transProt);//NOI18N
    }
    
    public String getTransProtClass() {
        return transProtClass;
    }
    public void setTransProtClass(String value) {
        String oldValue = transProtClass;
        this.transProtClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("transProtClass", oldValue, transProtClass);//NOI18N
    }
    
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String value) {
        String oldValue = hostName;
        this.hostName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("hostName", oldValue, hostName);//NOI18N
    }
    
    public String getUserName() {
        return userName;
    }
    public void setUserName(String value) {
        String oldValue = userName;
        this.userName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("userName", oldValue, userName);//NOI18N
    }
    public String getFromAddr() {
        return fromAddr;
    }
    public void setFromAddr(String value) {
        String oldValue = fromAddr;
        this.fromAddr = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("fromAddr", oldValue, fromAddr);//NOI18N
    }
    
    public String getIsDebug() {
        return isDebug;
    }
    public void setIsDebug(String value) {
        String oldValue = isDebug;
        this.isDebug = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isDebug", oldValue, isDebug);//NOI18N
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
            
}
