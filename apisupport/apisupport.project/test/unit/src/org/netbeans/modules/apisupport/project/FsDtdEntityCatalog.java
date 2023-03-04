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

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Register filesystem XML layer DTDs.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.xml.EntityCatalog.class)
public final class FsDtdEntityCatalog extends EntityCatalog {

    private final Map<String,String> DTD_MAP = new HashMap<String,String>();

    /** Default constructor for lookup. */
    public FsDtdEntityCatalog() {
        DTD_MAP.put("-//NetBeans//DTD Filesystem 1.0//EN", "org/openide/filesystems/filesystem.dtd");
        DTD_MAP.put("-//NetBeans//DTD Filesystem 1.1//EN", "org/openide/filesystems/filesystem1_1.dtd");
    }
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String resourcePath = DTD_MAP.get(publicId);
        if (resourcePath == null) {
            return null;
        }
        URL location = FsDtdEntityCatalog.class.getClassLoader().getResource(resourcePath);
        assert location != null : resourcePath;
        return new InputSource(location.toExternalForm());
    }
    
}
