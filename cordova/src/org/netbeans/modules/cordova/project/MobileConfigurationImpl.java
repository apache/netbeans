/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cordova.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.spi.PropertyProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 *
 */
public class MobileConfigurationImpl implements ProjectConfiguration, PropertyProvider {

    final private Project project;
    //final private ClientProjectPlatformImpl platform;
    private final String name;
    private final String displayName;
    private final String type;
    private final EditableProperties props;
    private final FileObject file;

    private MobileConfigurationImpl(Project project, FileObject kid, String id, String displayName, String type, EditableProperties ep) {
        this.project = project;
        this.name = id;
        this.displayName = displayName;
        this.type = type;
        this.props = ep;
        this.file = kid;
    }
    
//    @Override
    public String getId() {
        return name;
    }

//    @Override
    public void save() {
        if (file == null) {
            return;
        }
        OutputStream os = null;
        try {
            os = file.getOutputStream();
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getType() {
        return type;
    }
    
    public Device getDevice() {
        return PlatformManager.getPlatform(type).getDevice(name, props);
    }

    @Override
    public String getProperty(String prop) {
        return props.getProperty(prop);
    }
    
    @Override
    public String putProperty(String prop, String value) {
        return props.put(prop, value);
    }

    public static MobileConfigurationImpl create(Project project, String id) {
        FileObject configFile = project.getProjectDirectory().getFileObject("nbproject/configs/" + id + ".properties"); // NOI18N
        assert configFile != null : "missing configuration file for id: " + id;
        return create(project, configFile); 
    }

    public static MobileConfigurationImpl create(Project proj, FileObject configFile) {
        try {
            InputStream is = configFile.getInputStream();
            try {
                EditableProperties p = new EditableProperties(true);
                p.load(is);
                String id = configFile.getName();
                String label = p.getProperty("display.name"); // NOI18N
                String type = p.getProperty("type"); //NOI18N
                return new MobileConfigurationImpl(proj, configFile, id, label != null ? label : id, type, p);
            } finally {
                is.close();
            }
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }


//    @Override
    public boolean canBeDeleted() {
        return true;
    }

//    @Override
    public void delete() {
        try {
            file.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
