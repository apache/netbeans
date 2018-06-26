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

package org.netbeans.tests.j2eeserver.devmodule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;
import org.netbeans.modules.j2ee.metadata.model.api.SimpleMetadataModelImpl;

/**
 *
 * @author  sherold
 */
public class TestJ2eeModuleImpl implements J2eeModuleImplementation2 {
    
    private final FileObject webAppRoot;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final MetadataModel<WebAppMetadata> webAppMetadata;

    /** Creates a new instance of TestJ2eeModule */
    public TestJ2eeModuleImpl(FileObject webAppRoot) throws IOException, SAXException {
        this.webAppRoot = webAppRoot;
        webAppMetadata = MetadataModelFactory.createMetadataModel(new SimpleMetadataModelImpl<WebAppMetadata>());
    }

    public FileObject getArchive() {
        return null;
    }
    
    @Override public Iterator<J2eeModule.RootedEntry> getArchiveContents() {
        return Collections.<J2eeModule.RootedEntry>emptySet().iterator();
    }
    
    public FileObject getContentDirectory() {
        return webAppRoot;
    }
    
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.EJB;
    }
    
    public String getModuleVersion() {
        return J2eeModule.JAVA_EE_5;
    }
    
    public String getUrl() {
        return null;
    }
    
    public void setUrl(String url) {
        // noop
    }

    public File getResourceDirectory() {
        return new File(FileUtil.toFile(webAppRoot), "resources");
    }

    public File getDeploymentConfigurationFile(String name) {
        if (name.equals(J2eeModule.WEB_XML)) {
            return new File(FileUtil.toFile(webAppRoot), name);
        } else {
            return null;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == WebAppMetadata.class) {
            return (MetadataModel<T>) webAppMetadata;
        } else {
            return null;
        }
    }
}
