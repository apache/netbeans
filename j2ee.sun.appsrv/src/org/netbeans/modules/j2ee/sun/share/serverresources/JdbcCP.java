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
 * JdbcCP.java
 *
 * Created on September 12, 2003, 5:39 PM
 */
package org.netbeans.modules.j2ee.sun.share.serverresources;

/**
 *
 * @author  nityad
 */
public class JdbcCP extends BaseResource implements java.io.Serializable{

    private String dsClass;
    private String resType;
    private String steadyPoolSize;
    private String maxPoolSize;
    private String maxWaitTimeMilli;
    private String poolResizeQty;
    private String idleIimeoutSecond;
    private String tranxIsoLevel;
    private String isIsoLevGuaranteed;
    private String isConnValidReq;
    private String connValidMethod;
    private String validationTableName;
    private String failAllConns;
    private String nontranxconns;
    private String allowNonComponentCallers;
    
    /** Creates a new instance of JdbcCP */
    public JdbcCP() {
    }
            
    public String getDsClass() {
        return dsClass;
    }
    public void setDsClass(String value) {
        String oldValue = dsClass;
        this.dsClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("dsClass", oldValue, dsClass);//NOI18N
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
    
    public String getSteadyPoolSize() {
        return steadyPoolSize;
    }
    public void setSteadyPoolSize(String value) {
        String oldValue = steadyPoolSize;
        this.steadyPoolSize = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("steadyPoolSize", oldValue, steadyPoolSize);//NOI18N
    }
    
    public String getMaxPoolSize() {
        return maxPoolSize;
    }
    public void setMaxPoolSize(String value) {
        String oldValue = maxPoolSize;
        this.maxPoolSize = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("maxPoolSize", oldValue, maxPoolSize);//NOI18N
    }
    
    public String getMaxWaitTimeMilli() {
        return maxWaitTimeMilli;
    }
    public void setMaxWaitTimeMilli(String value) {
        String oldValue = maxWaitTimeMilli;
        this.maxWaitTimeMilli = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("maxWaitTimeMilli", oldValue, maxWaitTimeMilli);//NOI18N
    }
    
    public String getPoolResizeQty() {
        return poolResizeQty;
    }
    public void setPoolResizeQty(String value) {
        String oldValue = poolResizeQty;
        this.poolResizeQty = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("poolResizeQty", oldValue, poolResizeQty);//NOI18N
    }
    
    public String getIdleIimeoutSecond() {
        return idleIimeoutSecond;
    }
    public void setIdleIimeoutSecond(String value) {
        String oldValue = idleIimeoutSecond;
        this.idleIimeoutSecond = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("idleIimeoutSecond", oldValue, idleIimeoutSecond);//NOI18N
    }
    
    public String getTranxIsoLevel() {
        return tranxIsoLevel;
    }
    public void setTranxIsoLevel(String value) {
        String oldValue = tranxIsoLevel;
        this.tranxIsoLevel = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("tranxIsoLevel", oldValue, tranxIsoLevel);//NOI18N
    }
    
    public String getIsIsoLevGuaranteed() {
        return isIsoLevGuaranteed;
    }
    public void setIsIsoLevGuaranteed(String value) {
        String oldValue = isIsoLevGuaranteed;
        this.isIsoLevGuaranteed = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isIsoLevGuaranteed", oldValue, isIsoLevGuaranteed);//NOI18N
    }
    
    public String getIsConnValidReq() {
        return isConnValidReq;
    }
    public void setIsConnValidReq(String value) {
        String oldValue = isConnValidReq;
        this.isConnValidReq = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isConnValidReq", oldValue, isConnValidReq);//NOI18N
    }
    
    public String getConnValidMethod() {
        return connValidMethod;
    }
    public void setConnValidMethod(String value) {
        String oldValue = connValidMethod;
        this.connValidMethod = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("connValidMethod", oldValue, connValidMethod);//NOI18N
    }
    
    public String getValidationTableName() {
        return validationTableName;
    }
    public void setValidationTableName(String value) {
        String oldValue = validationTableName;
        this.validationTableName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("validationTableName", oldValue, validationTableName);//NOI18N
    }
    
    public String getFailAllConns() {
        return failAllConns;
    }
    public void setFailAllConns(String value) {
        String oldValue = failAllConns;
        this.failAllConns = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("failAllConns", oldValue, failAllConns);//NOI18N
    }

    public String getNontranxconns() {
        return nontranxconns;
    }

    public void setNontranxconns(String value) {
        String oldValue = nontranxconns;
        this.nontranxconns = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("nontranxconns", oldValue, nontranxconns); //NOI18N
    }

    public String getAllowNonComponentCallers() {
        return allowNonComponentCallers;
    }

    public void setAllowNonComponentCallers(String value) {
        String oldValue = allowNonComponentCallers;
        this.allowNonComponentCallers = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("allowNonComponentCallers", oldValue, allowNonComponentCallers); //NOI18N
    }
    
 }
