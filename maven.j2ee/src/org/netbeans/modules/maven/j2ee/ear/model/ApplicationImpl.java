/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ear.model;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXParseException;

/**
 * Default implementation of {@link Application} for EAR project which <b>caches</b> application modules.
 * It should be used in {@link org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)} only.
 * <p>
 * <b>This class is not thread safe so it is necessary to ensure that
 * is controlled by {@link org.netbeans.api.project.ProjectManager#mutex() mutex}.</b>
 * @author Tomas Mysik
 * @see #enterRunReadAction()
 * @see #leaveRunReadAction()
 */
public class ApplicationImpl implements Application {
    
    private final Project earProject;
    private volatile boolean runReadActionRunning = false;
    
    
    /**
     * Create application for given EAR project.
     * @param earProject EAR project instance for which corresponding application is created.
     */
    public ApplicationImpl(Project earProject) {
        this.earProject = earProject;
    }

    protected void enterRunReadAction() {
        runReadActionRunning = true;
    }

    protected void leaveRunReadAction() {
        runReadActionRunning = false;
    }

    /**
     * @see EarProjectProperties#addItemToAppDD(Application, VisualClassPathItem)
     */
    private List<Module> getModules() {
        if (!ProjectManager.mutex().isWriteAccess()
                || !runReadActionRunning) {
             throw new IllegalStateException("Cannot read modules outside runReadAction()");
        }
        return new ArrayList<Module>();
    }
    
    @Override
    public String getDefaultDisplayName() {
        return ProjectUtils.getInformation(earProject).getDisplayName();
    }
    
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public BigDecimal getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public SAXParseException getError() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setModule(int index, Module value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Module getModule(int index) {
        if (index < 0 || index >= getModules().size()) {
            return null;
        }
        return getModules().get(index);
    }
    
    @Override
    public int sizeModule() {
        return getModules().size();
    }
    
    @Override
    public void setModule(Module[] value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Module[] getModule() {
        return getModules().toArray(new Module[getModules().size()]);
    }
    
    @Override
    public int addModule(Module value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int removeModule(Module value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Module newModule() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setSecurityRole(int index, SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public SecurityRole getSecurityRole(int index) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int sizeSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setSecurityRole(SecurityRole[] value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public SecurityRole[] getSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int addSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int removeSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public SecurityRole newSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setIcon(int index, Icon value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Icon getIcon(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int sizeIcon() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setIcon(Icon[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int addIcon(Icon value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public int removeIcon(Icon value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Icon newIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void write(FileObject fo) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void merge(RootInterface root, int mode) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getDefaultDescription() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getDisplayName(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Map getAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeDisplayName() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues,
            String keyProperty) throws ClassNotFoundException,
            NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String getLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Icon getDefaultIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public Map getAllIcons() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public void removeAllIcons() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        sb.append(this.getClass().getName() + " Object {");
        sb.append(newLine);
        
        sb.append(" Name: ");
        sb.append(getDefaultDisplayName());
        sb.append(newLine);

        sb.append(" Number of modules: ");
        sb.append(getModules().size());
        sb.append(newLine);

        sb.append(" Modules: ");
        sb.append(getModules());
        sb.append(newLine);

        sb.append("}");
        return sb.toString();
    }
}
