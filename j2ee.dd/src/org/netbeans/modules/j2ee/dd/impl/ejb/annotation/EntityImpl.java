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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity;

public class EntityImpl implements Entity {

    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPersistenceType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPersistenceType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrimKeyClass(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrimKeyClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setReentrant(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isReentrant() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCmpVersion(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCmpVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAbstractSchemaName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAbstractSchemaName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCmpField(int index, CmpField value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CmpField getCmpField(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCmpField(CmpField[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CmpField[] getCmpField() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeCmpField() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addCmpField(CmpField value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeCmpField(CmpField value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CmpField newCmpField() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrimkeyField(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrimkeyField() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrimkeyFieldId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrimkeyFieldId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setQuery(int index, Query value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Query getQuery(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setQuery(Query[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Query[] getQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeQuery(Query value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addQuery(Query value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Query newQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getHome() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHome(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRemote() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRemote(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLocal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLocal(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLocalHome() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLocalHome(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRoleRef(int index, SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRoleRef getSecurityRoleRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRoleRef(SecurityRoleRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRoleRef[] getSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeSecurityRoleRef(SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addSecurityRoleRef(SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRoleRef newSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbJar getRoot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getEjbName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getEjbClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbClass(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(int index, EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry getEnvEntry(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(EnvEntry[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry[] getEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEnvEntry(EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEnvEntry(EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry newEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(int index, EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef getEjbRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(EjbRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef[] getEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEjbRef(EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEjbRef(EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef newEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbLocalRef(int index, EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef getEjbLocalRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbLocalRef(EjbLocalRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef[] getEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEjbLocalRef(EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEjbLocalRef(EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef newEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityIdentity getSecurityIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityIdentity(SecurityIdentity value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityIdentity newSecurityIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(int index, ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef getResourceRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(ResourceRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef[] getResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceRef(ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceRef(ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef newResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(int index, ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef getResourceEnvRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(ResourceEnvRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef[] getResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceEnvRef(ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceEnvRef(ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef newResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMessageDestinationRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(int index, ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef getServiceRef(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef[] getServiceRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeServiceRef(ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeServiceRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addServiceRef(ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef newServiceRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDefaultDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDisplayName(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDefaultDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean addBean(String beanName, String[] propertyNames,
                                Object[] propertyValues, String keyProperty) throws ClassNotFoundException,
                                                                                    NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean findBeanByName(String beanName, String propertyName,
                                       String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllIcons(String[] locales, String[] smallIcons,
                            String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon getDefaultIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

