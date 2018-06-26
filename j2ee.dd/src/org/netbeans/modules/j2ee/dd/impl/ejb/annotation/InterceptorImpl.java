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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke;
import org.netbeans.modules.j2ee.dd.api.ejb.Interceptor;
import org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef;

/**
 *
 * @author Martin Adamek
 */
public class InterceptorImpl implements Interceptor {

    public int addAroundInvoke(AroundInvoke value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addDescription(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEjbLocalRef(EjbLocalRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEjbRef(EjbRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEnvEntry(EnvEntry valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDestinationRef(MessageDestinationRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPersistenceContextRef(PersistenceContextRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPersistenceUnitRef(PersistenceUnitRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPostActivate(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPostConstruct(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPreDestroy(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPrePassivate(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceEnvRef(ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceRef(ResourceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addServiceRef(ServiceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AroundInvoke[] getAroundInvoke() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AroundInvoke getAroundInvoke(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef[] getEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef getEjbLocalRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef[] getEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef getEjbRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry[] getEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry getEnvEntry(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getInterceptorClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef[] getMessageDestinationRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef getMessageDestinationRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceContextRef[] getPersistenceContextRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceContextRef getPersistenceContextRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceUnitRef[] getPersistenceUnitRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceUnitRef getPersistenceUnitRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback[] getPostActivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback getPostActivate(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback[] getPostConstruct() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback getPostConstruct(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback[] getPreDestroy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback getPreDestroy(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback[] getPrePassivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback getPrePassivate(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef[] getResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef getResourceEnvRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef[] getResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef getResourceRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef[] getServiceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef getServiceRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AroundInvoke newAroundInvoke() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef newEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef newEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry newEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback newLifecycleCallback() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef newMessageDestinationRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceContextRef newPersistenceContextRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceUnitRef newPersistenceUnitRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef newResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef newResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef newServiceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeAroundInvoke(AroundInvoke value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeDescription(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEjbLocalRef(EjbLocalRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEjbRef(EjbRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEnvEntry(EnvEntry valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDestinationRef(MessageDestinationRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePersistenceContextRef(PersistenceContextRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePersistenceUnitRef(PersistenceUnitRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePostActivate(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePostConstruct(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePreDestroy(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePrePassivate(LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceEnvRef(ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceRef(ResourceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeServiceRef(ServiceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAroundInvoke(int index, AroundInvoke value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAroundInvoke(AroundInvoke[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(int index, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbLocalRef(int index, EjbLocalRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbLocalRef(EjbLocalRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(int index, EjbRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(EjbRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(int index, EnvEntry valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(EnvEntry[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInterceptorClass(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(int index,
                                         MessageDestinationRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(MessageDestinationRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceContextRef(int index, PersistenceContextRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceContextRef(PersistenceContextRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceUnitRef(int index, PersistenceUnitRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceUnitRef(PersistenceUnitRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostActivate(int index, LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostActivate(LifecycleCallback[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostConstruct(int index, LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostConstruct(LifecycleCallback[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPreDestroy(int index, LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPreDestroy(LifecycleCallback[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrePassivate(int index, LifecycleCallback value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrePassivate(LifecycleCallback[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(int index, ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(ResourceEnvRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(int index, ResourceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(ResourceRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(int index, ServiceRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(ServiceRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeAroundInvoke() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMessageDestinationRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePersistenceContextRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePersistenceUnitRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePostActivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePostConstruct() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePreDestroy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePrePassivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeServiceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
