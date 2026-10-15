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

package org.netbeans.modules.web.jsf.editor.facelets;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.web.common.taginfo.LibraryMetadata;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 *
 * @deprecated use DefaultFaceletLibraries
 *
 * @todo remove this class along with the **.libdefs.* package content
 * since DefaultFaceletLibraries provides basically the same info
 */
@Deprecated
public class FaceletsLibraryMetadata {
    private static Map<String, LibraryMetadata> libMap = new TreeMap<>();

    static {
        loadLib("composite");  //NOI18N
        loadLib("core");  //NOI18N
        loadLib("functions");  //NOI18N
        loadLib("html");  //NOI18N
        loadLib("ui");  //NOI18N
    }

    public static LibraryMetadata get(String libraryURL){
        LibraryMetadata metadata = libMap.get(libraryURL);
        if (metadata == null) {
            String legacyNamespace = NamespaceUtils.NS_MAPPING.get(libraryURL);
            if (legacyNamespace != null) {
                metadata = libMap.get(legacyNamespace);
            }
        }
        return metadata;
    }

    private static void loadLib(String filePath){
        InputStream is = FaceletsLibraryMetadata.class.getResourceAsStream("libdefs/" + filePath + ".xml"); //NOI18N

        try {
            LibraryMetadata lib = LibraryMetadata.readFromXML(is);
            libMap.put(lib.getId(), lib);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
