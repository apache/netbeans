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

package org.netbeans.modules.java.j2seproject.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.spi.project.support.ant.EditableProperties;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Utility class for creating project run configuration files
 * 
 * @author Milan Kubec
 * @since 1.10
 */
public final class J2SEProjectConfigurations {
    
    J2SEProjectConfigurations() {}
    
    /**
     * Creates property files for run configuration and writes passed properties.
     * Shared properties are written to nbproject/configs folder and private properties
     * are written to nbproject/private/configs folder. The property file is not created
     * if null is passed for either shared or private properties.
     * 
     * @param prj project under which property files will be created
     * @param configName name of the config file, '.properties' is apended
     * @param sharedProps properties to be written to shared file; is allowed to
     *        contain special purpose properties starting with $ (e.g. $label)
     * @param privateProps properties to be written to private file
     * @since 1.29
     */
    public static void createConfigurationFiles(Project prj, String configName, 
            final EditableProperties sharedProps, final EditableProperties privateProps) throws IOException, IllegalArgumentException {
        
        if (prj == null || configName == null || "".equals(configName)) {
            throw new IllegalArgumentException();
        }
        
        final String configFileName = configName + ".properties"; // NOI18N
        final FileObject prjDir = prj.getProjectDirectory();
        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    prjDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            generateConfig(prjDir, "nbproject/configs/" + configFileName, sharedProps); // NOI18N
                            generateConfig(prjDir, "nbproject/private/configs/" + configFileName, privateProps); // NOI18N
                        }
                    });
                    return null;
                }
            });
        } catch (MutexException ex) {
            throw (IOException) ex.getException();
        }
        
    }

    /**
     * Creates property files for run configuration and writes passed properties.
     * Shared properties are written to nbproject/configs folder and private properties
     * are written to nbproject/private/configs folder. The property file is not created
     * if null is passed for either shared or private properties.
     *
     * @param prj project under which property files will be created
     * @param configName name of the config file, '.properties' is apended
     * @param sharedProps properties to be written to shared file; is allowed to
     *        contain special purpose properties starting with $ (e.g. $label)
     * @param privateProps properties to be written to private file
     */
    public static void createConfigurationFiles(Project prj, String configName,
            final Properties sharedProps, final Properties privateProps) throws IOException, IllegalArgumentException {
        createConfigurationFiles(prj, configName, props2EditableProps(sharedProps), props2EditableProps(privateProps));
    }

    private static void generateConfig(FileObject prjDir, String cfgFilePath, EditableProperties propsToWrite) throws IOException {
        
        if (propsToWrite == null) {
            // do not create anything if props is null
            return;
        }
        FileObject jwsConfigFO = FileUtil.createData(prjDir, cfgFilePath);
        Properties props = new Properties();
        InputStream is = jwsConfigFO.getInputStream();
        props.load(is);
        is.close();
        if (props.equals(propsToWrite)) {
            // file already exists and props are the same
            return;
        }
        OutputStream os = jwsConfigFO.getOutputStream();
        propsToWrite.store(os);
        os.close();
        
    }

    private static EditableProperties props2EditableProps(Properties props) {
        if (props == null) {
            return null;
        }
        EditableProperties edProps = new EditableProperties(true);
        for (Iterator<Entry<Object,Object>> iter = props.entrySet().iterator(); iter.hasNext(); ) {
            Entry entry = iter.next();
            edProps.put((String) entry.getKey(), (String) entry.getValue());
        }
        return edProps;
    }

}
