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

package org.netbeans.modules.j2ee.core.api.support.java;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Copied from java/source TestUtilities. To be removed when SourceUtils
 * is moved to java/source.
 *
 * @author Andrei Badea
 */
public class TestUtilities {

    private TestUtilities() {
    }

    public static final FileObject copyStringToFileObject(FileObject fo, String content) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            FileUtil.copy(is, os);
            return fo;
        } finally {
            os.close();
        }
    }

    public static final String copyFileObjectToString (FileObject fo) throws java.io.IOException {
        int s = (int)FileUtil.toFile(fo).length();
        byte[] data = new byte[s];
        InputStream stream = fo.getInputStream();
        try {
            int len = stream.read(data);
            if (len != s) {
                throw new EOFException("truncated file");
            }
            return new String (data);
        } finally {
            stream.close();
        }
    }
    
    /**
     * Creates a cache folder for the Java infrastructure.
     * 
     * @param folder the parent folder for the cache folder, 
     * typically the working dir.
     */ 
    public static void setCacheFolder(File folder){
        File cacheFolder = new File(folder,"cache");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
}
