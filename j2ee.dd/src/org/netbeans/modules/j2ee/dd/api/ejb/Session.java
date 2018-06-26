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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface Session extends EntityAndSession {

    public static final String SERVICE_ENDPOINT = "ServiceEndpoint";	// NOI18N
    public static final String SESSION_TYPE = "SessionType";	// NOI18N
    public static final String TRANSACTION_TYPE = "TransactionType";	// NOI18N
    public static final String SESSION_TYPE_STATEFUL = "Stateful"; // NOI18N
    public static final String SESSION_TYPE_STATELESS = "Stateless"; // NOI18N
    public static final String SESSION_TYPE_SINGLETON = "Singleton"; // NOI18N
    public static final String TRANSACTION_TYPE_BEAN = "Bean"; // NOI18N
    public static final String TRANSACTION_TYPE_CONTAINER = "Container"; // NOI18N
    public static final String BUSINESS_LOCAL = "BusinessLocal";	// NOI18N
    public static final String BUSINESS_REMOTE = "BusinessRemote";	// NOI18N
    
    public String getSessionType();
    
    public void setSessionType(String value);

    public String getTransactionType(); 
    
    public void setTransactionType(String value);

    //2.1
        
    public void setServiceEndpoint(String value) throws VersionNotSupportedException;
    
    public String getServiceEndpoint() throws VersionNotSupportedException;
    
    // EJB 3.0

    void setMappedName(String value) throws VersionNotSupportedException;
    String getMappedName() throws VersionNotSupportedException;
    void setBusinessLocal(int index, String value) throws VersionNotSupportedException;
    String getBusinessLocal(int index) throws VersionNotSupportedException;
    int sizeBusinessLocal() throws VersionNotSupportedException;
    void setBusinessLocal(String[] value) throws VersionNotSupportedException;
    String[] getBusinessLocal() throws VersionNotSupportedException;
    int addBusinessLocal(String value) throws VersionNotSupportedException;
    int removeBusinessLocal(String value) throws VersionNotSupportedException;
    void setBusinessRemote(int index, String value) throws VersionNotSupportedException;
    String getBusinessRemote(int index) throws VersionNotSupportedException;
    int sizeBusinessRemote() throws VersionNotSupportedException;
    void setBusinessRemote(String[] value) throws VersionNotSupportedException;
    String[] getBusinessRemote() throws VersionNotSupportedException;
    int addBusinessRemote(String value) throws VersionNotSupportedException;
    int removeBusinessRemote(String value) throws VersionNotSupportedException;
    void setTimeoutMethod(NamedMethod valueInterface) throws VersionNotSupportedException;
    NamedMethod getTimeoutMethod() throws VersionNotSupportedException;
    void setInitMethod(int index, InitMethod valueInterface) throws VersionNotSupportedException;
    InitMethod getInitMethod(int index) throws VersionNotSupportedException;
    int sizeInitMethod() throws VersionNotSupportedException;
    void setInitMethod(InitMethod[] value) throws VersionNotSupportedException;
    InitMethod[] getInitMethod() throws VersionNotSupportedException;
    int addInitMethod(InitMethod valueInterface) throws VersionNotSupportedException;
    int removeInitMethod(InitMethod valueInterface) throws VersionNotSupportedException;
    void setRemoveMethod(int index, RemoveMethod valueInterface) throws VersionNotSupportedException;
    RemoveMethod getRemoveMethod(int index) throws VersionNotSupportedException;
    int sizeRemoveMethod() throws VersionNotSupportedException;
    void setRemoveMethod(RemoveMethod[] value) throws VersionNotSupportedException;
    RemoveMethod[] getRemoveMethod() throws VersionNotSupportedException;
    int addRemoveMethod(RemoveMethod valueInterface) throws VersionNotSupportedException;
    int removeRemoveMethod(RemoveMethod valueInterface) throws VersionNotSupportedException;
    void setAroundInvoke(int index, AroundInvoke valueInterface) throws VersionNotSupportedException;
    AroundInvoke getAroundInvoke(int index) throws VersionNotSupportedException;
    int sizeAroundInvoke() throws VersionNotSupportedException;
    void setAroundInvoke(AroundInvoke[] value) throws VersionNotSupportedException;
    AroundInvoke[] getAroundInvoke() throws VersionNotSupportedException;
    int addAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException;
    int removeAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException;
    void setPersistenceContextRef(int index, PersistenceContextRef valueInterface) throws VersionNotSupportedException;
    PersistenceContextRef getPersistenceContextRef(int index) throws VersionNotSupportedException;
    int sizePersistenceContextRef() throws VersionNotSupportedException;
    void setPersistenceContextRef(PersistenceContextRef[] value) throws VersionNotSupportedException;
    PersistenceContextRef[] getPersistenceContextRef() throws VersionNotSupportedException;
    int addPersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException;
    int removePersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException;
    void setPersistenceUnitRef(int index, PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
    PersistenceUnitRef getPersistenceUnitRef(int index) throws VersionNotSupportedException;
    int sizePersistenceUnitRef() throws VersionNotSupportedException;
    void setPersistenceUnitRef(PersistenceUnitRef[] value) throws VersionNotSupportedException;
    PersistenceUnitRef[] getPersistenceUnitRef() throws VersionNotSupportedException;
    int addPersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
    int removePersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
    void setPostConstruct(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
    LifecycleCallback getPostConstruct(int index) throws VersionNotSupportedException;
    int sizePostConstruct() throws VersionNotSupportedException;
    void setPostConstruct(LifecycleCallback[] value) throws VersionNotSupportedException;
    LifecycleCallback[] getPostConstruct() throws VersionNotSupportedException;
    int addPostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    int removePostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    void setPreDestroy(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
    LifecycleCallback getPreDestroy(int index) throws VersionNotSupportedException;
    int sizePreDestroy() throws VersionNotSupportedException;
    void setPreDestroy(LifecycleCallback[] value) throws VersionNotSupportedException;
    LifecycleCallback[] getPreDestroy() throws VersionNotSupportedException;
    int addPreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    int removePreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    void setPostActivate(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
    LifecycleCallback getPostActivate(int index) throws VersionNotSupportedException;
    int sizePostActivate() throws VersionNotSupportedException;
    void setPostActivate(LifecycleCallback[] value) throws VersionNotSupportedException;
    LifecycleCallback[] getPostActivate() throws VersionNotSupportedException;
    int addPostActivate(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    int removePostActivate(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    void setPrePassivate(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
    LifecycleCallback getPrePassivate(int index) throws VersionNotSupportedException;
    int sizePrePassivate() throws VersionNotSupportedException;
    void setPrePassivate(LifecycleCallback[] value) throws VersionNotSupportedException;
    LifecycleCallback[] getPrePassivate() throws VersionNotSupportedException;
    int addPrePassivate(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    int removePrePassivate(LifecycleCallback valueInterface) throws VersionNotSupportedException;
    NamedMethod newNamedMethod() throws VersionNotSupportedException;
    InitMethod newInitMethod() throws VersionNotSupportedException;
    RemoveMethod newRemoveMethod() throws VersionNotSupportedException;
    AroundInvoke newAroundInvoke() throws VersionNotSupportedException;
    PersistenceContextRef newPersistenceContextRef() throws VersionNotSupportedException;
    PersistenceUnitRef newPersistenceUnitRef() throws VersionNotSupportedException;
    LifecycleCallback newLifecycleCallback() throws VersionNotSupportedException;

    //EJB 3.1
    public boolean isLocalBean();
}

