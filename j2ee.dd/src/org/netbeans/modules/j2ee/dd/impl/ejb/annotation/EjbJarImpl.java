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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import java.math.BigDecimal;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Interceptors;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Martin Adamek
 */
public class EjbJarImpl implements EjbJar {
    
    private final AnnotationModelHelper helper;
    private final EnterpriseBeans enterpriseBeans;

    public EjbJarImpl(AnnotationModelHelper helper) {
        this.helper = helper;
        this.enterpriseBeans = EnterpriseBeansImpl.create(helper);
    }
    
    public EnterpriseBeans getEnterpriseBeans() {
        return enterpriseBeans;
    }

    public BigDecimal getVersion() {
        return BigDecimal.valueOf(3.0);
    }

    public AssemblyDescriptor getSingleAssemblyDescriptor() {
        return new AssemblyDescriptorImpl(helper);
    }

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    
    public Object clone()  {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setVersion(BigDecimal version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SAXParseException getError() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnterpriseBeans(EnterpriseBeans value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnterpriseBeans newEnterpriseBeans() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRelationships(Relationships value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Relationships getSingleRelationships() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Relationships newRelationships() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAssemblyDescriptor(AssemblyDescriptor value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AssemblyDescriptor newAssemblyDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbClientJar(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSingleEjbClientJar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInterceptors(Interceptors valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Interceptors getInterceptors() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Interceptors newInterceptors() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(FileObject fo) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(RootInterface root, int mode) {
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
    
    // </editor-fold>

}
