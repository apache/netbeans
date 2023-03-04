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

package org.netbeans.modules.openide.filesystems;

import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default URLMapper for public lookup.
 */
@ServiceProvider(service=URLMapper.class)
public final class DefaultURLMapperProxy extends URLMapper {

    private static URLMapper DEFAULT;

    public static void setDefault(URLMapper m) {
        assert DEFAULT == null;
        assert m != null;
        DEFAULT = m;
    }

    /**
     * Default constructor for lookup.
     */
    public DefaultURLMapperProxy() {}

    public URL getURL(FileObject fo, int type) {
        assert DEFAULT != null;
        return DEFAULT.getURL(fo, type);
    }

    public FileObject[] getFileObjects(URL url) {
        assert DEFAULT != null;
        return DEFAULT.getFileObjects(url);
    }
    
}
