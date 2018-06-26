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
 * EnterpriseBeans.java
 *
 * Created on November 17, 2004, 4:38 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface EnterpriseBeans extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String NAME = "Name";	// NOI18N
    public static final String UNIQUE_ID = "UniqueId";	// NOI18N
    public static final String EJB = "Ejb";	// NOI18N
    public static final String PM_DESCRIPTORS = "PmDescriptors";	// NOI18N
    public static final String CMP_RESOURCE = "CmpResource";	// NOI18N
    public static final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
    public static final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N
        
    public String getName();
    public void setName(String value);
    
    public String getUniqueId();
    public void setUniqueId(String value);
    
    public Ejb[] getEjb(); 
    public Ejb getEjb(int index);
    public void setEjb(Ejb[] value);
    public void setEjb(int index, Ejb value);
    public int addEjb(Ejb value);
    public int removeEjb(Ejb value);
    public int sizeEjb();
    public Ejb newEjb();
    
    public PmDescriptors getPmDescriptors();
    public void setPmDescriptors(PmDescriptors value);
    public PmDescriptors newPmDescriptors(); 
    
    public CmpResource getCmpResource();
    public void setCmpResource(CmpResource value); 
    public CmpResource newCmpResource();
    
    public MessageDestination[] getMessageDestination(); 
    public MessageDestination getMessageDestination(int index);
    public void setMessageDestination(MessageDestination[] value);
    public void setMessageDestination(int index, MessageDestination value);
    public int addMessageDestination(MessageDestination value);
    public int removeMessageDestination(MessageDestination value);
    public int sizeMessageDestination(); 
    public MessageDestination newMessageDestination();
    
    public WebserviceDescription[] getWebserviceDescription(); 
    public WebserviceDescription getWebserviceDescription(int index);
    public void setWebserviceDescription(WebserviceDescription[] value);
    public void setWebserviceDescription(int index, WebserviceDescription value);
    public int addWebserviceDescription(WebserviceDescription value);
    public int removeWebserviceDescription(WebserviceDescription value);
    public int sizeWebserviceDescription(); 
    public WebserviceDescription newWebserviceDescription();
    
    public void setPropertyElement(int index, PropertyElement value) throws VersionNotSupportedException;
    public PropertyElement getPropertyElement(int index) throws VersionNotSupportedException;
    public int sizePropertyElement() throws VersionNotSupportedException;
    public void setPropertyElement(PropertyElement[] value) throws VersionNotSupportedException;
    public PropertyElement[] getPropertyElement() throws VersionNotSupportedException;
    public int addPropertyElement(PropertyElement value) throws VersionNotSupportedException;
    public int removePropertyElement(PropertyElement value) throws VersionNotSupportedException;
    public PropertyElement newPropertyElement() throws VersionNotSupportedException;
    
}
