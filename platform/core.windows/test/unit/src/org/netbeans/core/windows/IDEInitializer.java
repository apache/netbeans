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

package org.netbeans.core.windows;

import java.net.URL;
import org.netbeans.core.startup.MainLookup;
import org.openide.filesystems.XMLFileSystem;


/**
 * Inspired by org.netbeans.api.project.TestUtil.
 *
 * @author Marek Slama
 */
public class IDEInitializer {
    
    private static XMLFileSystem systemFS;
    
    /**
     * Add layers to system filesystem.
     *
     * @param layers xml-layer URLs to be present in the system filesystem.
     *
     */
    public static void addLayers (String[] layers) {
        ClassLoader classLoader = IDEInitializer.class.getClassLoader ();
        URL[] urls = new URL [layers.length];
        int i, k = urls.length;
        for (i = 0; i < k; i++) {
            urls [i] = classLoader.getResource (layers [i]);
        }

        systemFS = new XMLFileSystem ();
        try {
            systemFS.setXmlUrls (urls);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        MainLookup.register(systemFS);
    }

    /**
     * Remove layers from system filesystem which were added using addLayers
     *
     */
    public static void removeLayers () {
        MainLookup.unregister(systemFS);
    }
        
}
