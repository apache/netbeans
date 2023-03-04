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

package org.netbeans.upgrade.systemoptions;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.*;

/**
 *
 * @author Radek Matous
 */
public class Importer {
    private static final String DEFINITION_OF_FILES =  "systemoptionsimport";//NOI18N
            
    public static void doImport() throws IOException  {
        Set<FileObject> files = getImportFiles(loadImportFilesDefinition());
        for (Iterator<DefaultResult> it = parse(files).iterator(); it.hasNext();) {
            saveResult(it.next());
        }
        for (Iterator it = files.iterator(); it.hasNext();) {
            FileObject fo = (FileObject) it.next();
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
        String[] propertyNames = result.getPropertyNames();
        for (int i = 0; i < propertyNames.length; i++) {
            String val = result.getProperty(propertyNames[i]);
            if (val != null) {
                props.put(propertyNames[i], val);
            }
        }
        if (props.size() > 0) {
            ps.save(props);
        }
    }
    
    private static Set<DefaultResult> parse(final Set<FileObject> files) {
        Set<DefaultResult> retval = new HashSet<DefaultResult>();
        for (FileObject f: files) {
            try {
                retval.add(SystemOptionsParser.parse(f, false));
            } catch (Exception ex) {
                boolean assertOn = false;
                assert assertOn = true;
                if (assertOn) {
                    Logger.getLogger("org.netbeans.upgrade.systemoptions.parse").log(Level.INFO, "importing: " + f.getPath(), ex); // NOI18N
                }
                continue;
            }
        }
        return retval;
    }
    

    static Properties loadImportFilesDefinition() throws IOException {
        Properties props = new Properties();
        InputStream is = Importer.class.getResourceAsStream(DEFINITION_OF_FILES);
        try {
            props.load(is);
        } finally {
            is.close();
        }
        return props;
    }

    private static Set<FileObject> getImportFiles(final Properties props) {
        Set<FileObject> fileobjects = new HashSet<FileObject>();        
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String path = (String) it.next();
            FileObject f = FileUtil.getConfigFile(path);
            if (f != null) {
                fileobjects.add(f);
            }
        }
        return fileobjects;
    }
    
    /** Creates a new instance of SettingsReadSupport */
    private Importer() {}    
}
