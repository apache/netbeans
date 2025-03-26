/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.upgrade.systemoptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class Importer {
    private static final String DEFINITION_OF_FILES =  "systemoptionsimport";//NOI18N

    public static void doImport() throws IOException  {
        Set<FileObject> files = getImportFiles(loadImportFilesDefinition());
        for (DefaultResult result : parse(files)) {
            saveResult(result);
        }
        for (FileObject fo : files) {
            FileLock fLock = fo.lock();
            try {
                fo.rename(fLock, fo.getName(), "imported");//NOI18N
            } finally {
                fLock.releaseLock();
            }
        }
    }
    
    private static void saveResult(final DefaultResult result) throws IOException {
        String absolutePath = "/"+result.getModuleName();
        PropertiesStorage ps = PropertiesStorage.instance(absolutePath);
        Properties props = ps.load();
        for (String name : result.getPropertyNames()) {
            String val = result.getProperty(name);
            if (val != null) {
                props.setProperty(name, val);
            }
        }
        if (!props.isEmpty()) {
            ps.save(props);
        }
    }
    
    private static Set<DefaultResult> parse(final Set<FileObject> files) {
        Set<DefaultResult> retval = new HashSet<>();
        for (FileObject f: files) {
            try {
                retval.add(SystemOptionsParser.parse(f, false));
            } catch (Exception ex) {
                boolean assertOn = false;
                assert assertOn = true;
                if (assertOn) {
                    Logger.getLogger("org.netbeans.upgrade.systemoptions.parse").log(Level.INFO, "importing: " + f.getPath(), ex); // NOI18N
                }
            }
        }
        return retval;
    }
    

    static Properties loadImportFilesDefinition() throws IOException {
        Properties props = new Properties();
        try (InputStream is = Importer.class.getResourceAsStream(DEFINITION_OF_FILES)) {
            props.load(is);
        }
        return props;
    }

    private static Set<FileObject> getImportFiles(final Properties props) {
        Set<FileObject> fileobjects = new HashSet<>();        
        for (Object path : props.keySet()) {
            FileObject f = FileUtil.getConfigFile((String) path);
            if (f != null) {
                fileobjects.add(f);
            }
        }
        return fileobjects;
    }
    
    /** Creates a new instance of SettingsReadSupport */
    private Importer() {}    
}
