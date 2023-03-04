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
 * JavaWebStartAccess.java
 *
 * Created on February 10, 2006, 11:28 AM
 *
 */

package org.netbeans.modules.j2ee.sun.dd.api.client;

/**
 *
 * @author Nitya Doraisamy
 */
public interface JavaWebStartAccess extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
    public static final String ELIGIBLE = "Eligible";	// NOI18N
    public static final String VENDOR = "Vendor";	// NOI18N
    
    public void setContextRoot(String value);
    
    public String getContextRoot();
    
    public void setEligible(String value);
    
    public String getEligible();
    
    public void setVendor(String value);
    
    public String getVendor();

    public void setJnlpDoc(boolean value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

    public boolean isJnlpDoc()throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

    public void setJnlpDocHref(java.lang.String value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

    public String getJnlpDocHref() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

}
