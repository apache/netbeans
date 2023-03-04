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

package org.netbeans.modules.options;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class Utils {


    private static Map<String, FileObject> filesCache = new HashMap<String, FileObject> ();
    public static final int ScrollBarUnitIncrement = 16;
    
    public static FileObject getFileObject (String name, String ext, boolean create)
    throws IOException {
        FileObject r = (FileObject) filesCache.get (name + '.' + ext);
        if (r != null) return r;
        FileObject optionsFolder = FileUtil.getConfigFile("Options");
        if (optionsFolder == null) {
            if (create) 
                optionsFolder = FileUtil.getConfigRoot().createFolder ("Options");
            else 
                return null;
        }
        FileObject fileObject = optionsFolder.getFileObject (name, ext);
        if (fileObject == null) {
            if (create)
                fileObject = optionsFolder.createData (name, ext);
            else
                return null;
        }
        filesCache.put (name + '.' + ext, fileObject);
        return fileObject;
    }
    
    public static Enumeration getInputStreams (String name, String ext)
    throws IOException {
        ClassLoader classLoader = (ClassLoader) Lookup.getDefault ().
                lookup (ClassLoader.class);
        return classLoader.getResources ("META-INF/options/" + name + "." + ext);
    }
}
