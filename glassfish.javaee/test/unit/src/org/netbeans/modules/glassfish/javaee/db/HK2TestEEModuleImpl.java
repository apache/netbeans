/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.javaee.db;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Java EE module for GlassFish features tests.
 * <p/>
 * @author Tomas Kraus
 */
public class HK2TestEEModuleImpl implements J2eeModuleImplementation2 {
    
    private final FileObject appRoot;
    private final File srcDir;
    private final File configDir;
    private final J2eeModule.Type moduleType;
    private final String moduleVersion;

    /** Creates a new instance of TestJ2eeModule
     * @param appRoot Application root directory.
     * @param moduleType Java EE module type.
     * @param moduleVersion Java EE version.
     */
    public HK2TestEEModuleImpl(
            final FileObject appRoot, final J2eeModule.Type moduleType,
            final String moduleVersion
    ) {
        this.appRoot = appRoot;
        this.srcDir = new File(FileUtil.toFile(appRoot), "src");
        this.configDir = new File(srcDir, "conf");
        this.moduleType = moduleType;
        this.moduleVersion = moduleVersion;
    }

    @Override
    public FileObject getArchive() {
        return null;
    }
    
    @Override
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() {
        return Collections.<J2eeModule.RootedEntry>emptySet().iterator();
    }
    
    @Override
    public FileObject getContentDirectory() {
        return appRoot;
    }
    
    @Override
    public J2eeModule.Type getModuleType() {
        return moduleType;
    }
    
    @Override
    public String getModuleVersion() {
        return moduleVersion;
    }
    
    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public File getResourceDirectory() {
        return new File(FileUtil.toFile(appRoot), "setup");
    }

    @Override
    public File getDeploymentConfigurationFile(String name) {
        return new File(configDir, name);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
