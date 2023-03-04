/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
